/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep;

import com.google.common.base.Joiner;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.ExtensionRegistry.ExtensionInfo;
import net.katros.services.proto.GrepBaseListener;
import net.katros.services.proto.GrepParser;
import net.katros.services.proto.GrepParser.*;
import static net.katros.services.proto.GrepParser.CAST_BOOL;
import static net.katros.services.proto.GrepParser.CAST_DOUBLE;
import static net.katros.services.proto.GrepParser.CAST_LONG;
import static net.katros.services.proto.GrepParser.CAST_STRING;
import static net.katros.services.proto.GrepParser.COUNT;
import static net.katros.services.proto.GrepParser.ID;
import net.katros.services.proto.grep.expr.BooleanExpression;
import net.katros.services.proto.grep.expr.Expression;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_ARRAY_REFERENCE;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_REFERENCE;
import static net.katros.services.proto.grep.expr.Expression.Type.LONG;
import static net.katros.services.proto.grep.expr.Expression.Type.OBJECT_REFERENCE;
import net.katros.services.proto.grep.expr.ExpressionFactory;
import net.katros.services.proto.grep.expr.FieldArrayReferenceExpression;
import net.katros.services.proto.grep.expr.FieldReferenceExpression;
import net.katros.services.proto.grep.expr.LongExpression;
import net.katros.services.proto.grep.expr.ObjectExpression;
import net.katros.services.proto.grep.expr.SemanticException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * @author boris@temk.org
**/
public class GrepExpressionListener extends GrepBaseListener {
    private Stack<Expression> stack = new Stack<>();
    private Stack<Expression> scope = new Stack<>();
    
    private RootPool rootPool;
    private IndexPool indexPool;
    private Descriptor rootDescr;
    private ExtensionRegistry registry;
    
    public GrepExpressionListener(ExtensionRegistry registry, Descriptor descr, RootPool rootPool,  IndexPool indexPool) {
        this.rootDescr = descr;
        this.rootPool = rootPool;
        this.indexPool = indexPool;
        this.registry = registry;
        scope.push(rootPool.getRoot(0));
        
    }

    public BooleanExpression getQueryExpression() {        
        return BooleanExpression.class.cast(dereference(null, stack.pop(), null));
    }
    
    public List<Expression> getPrintExpressions(List<Expression> result) {        
        for (Expression expr: stack) {
            result.add(dereference(null, expr, null));
        }
        stack.clear();
        return result;
    }

    @Override
    public void exitScoped_print(Scoped_printContext ctx) {
        scope.pop();
    }
    
//    @Override
//    public void exitPrint_part(Print_partContext ctx) {
//        scope.push(rootPool.getRoot(0));
//    }
    
    
    
    
    private Expression dereference(Token token, Expression expr, Expression idx) {
        if (expr.getType() == FIELD_REFERENCE) {
            if (idx != null) {
                throw new SemanticException(token, "Using index with single field.");
            }
            return FieldReferenceExpression.class.cast(expr).dereference(token);
        } else if (expr.getType() == FIELD_ARRAY_REFERENCE) {
            if (idx == null) {
                FieldArrayReferenceExpression far = FieldArrayReferenceExpression.class.cast(expr);
                idx = indexPool.createAnnonimous(far);
            }
            
            if (idx.getType() != LONG) {
                throw new SemanticException(token, "Expecting expression of type long. Found " + idx.getType());
            }
            return FieldArrayReferenceExpression.class.cast(expr).dereference(token, LongExpression.class.cast(idx));
        } else {
            return expr;
        }
    }
    
    @Override
    public void exitField(FieldContext ctx) {
        if (ctx.ID() != null && indexPool.contains(ctx.ID().getText())) {
            // if we see index but not real field, we have pop ROOT from stack and push index expression instead
            stack.pop();
            stack.push(indexPool.getIndex(ctx.ID().getText()));
            return;
        }
        
        // otherwise, it is a field.        
        Expression indexExpr = null;

        if (ctx.index() != null) {
            indexExpr = dereference(ctx.getStart(), stack.pop(), null);
        } 

        final ObjectExpression objExpr;
        final FieldDescriptor fd; 
        
        Expression scopeExpr = dereference(ctx.getStart(), stack.pop(), null);

        if (scopeExpr.getType() == OBJECT_REFERENCE) {
            objExpr = ObjectExpression.class.cast(scopeExpr);
        } else {
            throw new SemanticException(ctx.getStart(), "Cannot get field from expression of type " + scopeExpr.getType());
        }
         
        if (ctx.ext() != null) {
            ExtensionInfo extInfo = registry.findExtensionByName(ctx.ext().extName);
            if (extInfo == null) {
                throw new SemanticException(ctx.getStart(), "No such extension: " + ctx.ext().extName);
            }
            fd = extInfo.descriptor;
        } else {
            String name = ctx.ID().getText();

            if (scopeExpr.getType() != OBJECT_REFERENCE) {
                throw new SemanticException(ctx.getStart(), "Cannot take field " + name + " from " + scopeExpr.getType());
            }
            
            Descriptor descr = ObjectExpression.class.cast(scopeExpr).getDescriptor();
            fd = descr.findFieldByName(name);
            if (fd == null) {
                throw new SemanticException(ctx.getStart(), "Message " + descr.getFullName() + " does not have  field " + name);
            }
        }

        if (fd.isRepeated()) {
            if (indexExpr != null) {
                stack.push(dereference(ctx.getStart(), new FieldArrayReferenceExpression(objExpr, fd), indexExpr));                
            } else {
                stack.push(new FieldArrayReferenceExpression(objExpr, fd));                
            }
        } else {
            stack.push(new FieldReferenceExpression(objExpr, fd));
        }
    }

    @Override
    public void exitIndex(IndexContext ctx) {
        if (ctx.ID() != null) {
            stack.push(indexPool.createNamed(ctx.ID().getText(), null));
        }
    }
    
    
    
    // =========================================================================
    private void exitBinary(ParserRuleContext ctx) {
        if (ctx.getChildCount() == 1) {
            return;
        }

        Token token = Token.class.cast(ctx.getChild(1).getPayload());
        int num = ctx.getChildCount();
        
        
        for (int k = 1; k < num; k += 2) {
            Expression rarg = dereference(token, stack.pop(), null);
            Expression larg = dereference(token, stack.pop(), null);
            
            stack.push(ExpressionFactory.createOpExpr(token, larg, rarg));
        }
        
    }
    
    @Override
    public void exitUnary(UnaryContext ctx) {
        if (ctx.getChildCount() == 1) {
            return;
        }

        final Token token;

        if (ctx.cast() != null) {
            CommonToken ct = new CommonToken(ctx.cast().type);
            switch (ct.getText()) {
                case "long":
                    ct.setType(CAST_LONG);
                    break;
                case "bool":
                    ct.setType(CAST_BOOL);
                    break;
                case "string":
                    ct.setType(CAST_STRING);
                    break;
                case "double":
                    ct.setType(CAST_DOUBLE);
                    break;
            }
            token = ct;
        }  else {
            token = ctx.getStart();
        }
        
        Expression arg = stack.pop();
        if (token.getType() != COUNT) {
            arg = dereference(token, arg, null);
        }
        stack.push(ExpressionFactory.createOpExpr(token, arg));
    }

    
    @Override
    public void exitConstant(ConstantContext ctx) {
        Token token = ctx.getStart();
        final Expression expr;
        
        switch(token.getType()) {
            case GrepParser.INTEGER:
                expr = ExpressionFactory.createConstExpr(Long.parseLong(token.getText()));
                break;
                
            case GrepParser.STRING:
                expr = ExpressionFactory.createConstExpr(token.getText().substring(1, token.getText().length() - 1));
                break;
                
            case GrepParser.FLOAT:
                expr = ExpressionFactory.createConstExpr(Double.parseDouble(token.getText()));
                break;
                
            case GrepParser.BOOL:
                expr = ExpressionFactory.createConstExpr(Boolean.parseBoolean(token.getText()));
                break; 
                
            default:
                throw new SemanticException(token, "Unexpected token type: " + token.getType());
        }
        
        stack.push(expr);
    }
    
    // =========================================================================
    @Override
    public void exitBit_xor(Bit_xorContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitCond_and(Cond_andContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitCond_eq(Cond_eqContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitAdd_op(Add_opContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitBit_shift(Bit_shiftContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitBit_or(Bit_orContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitBit_and(Bit_andContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitProd_op(Prod_opContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitCond_cmp(Cond_cmpContext ctx) {
        exitBinary(ctx);
    }

    @Override
    public void exitCond_or(Cond_orContext ctx) {
        exitBinary(ctx);
    }

    // ===============================================================
    @Override
    public void enterRoot(RootContext ctx) {
        if (ctx.ROOT() == null) {
            stack.push(scope.peek());
        } else {
            stack.push(rootPool.getRoot(ctx.ROOT().getText()));            
        }
    }
    

    @Override
    public void exitExt(ExtContext ctx) {
        List<String> list = new ArrayList<>();
        for (TerminalNode t: ctx.getTokens(ID)) { 
            list.add(t.getText());
        }
        // move first id to last position
        // since sintax is id@scope, but we need scope . id
        list.add(list.remove(0)); 
        
        ctx.extName = Joiner.on(".").join(list);
    }

    @Override
    public void exitScoped_expr(Scoped_exprContext ctx) {
        scope.pop();
    }

    @Override
    public void exitScope(ScopeContext ctx) {
        scope.push(stack.pop());
    }
    
}
