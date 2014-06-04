package net.katros.services.proto.grep.expr;

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MessageOrBuilder;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_ARRAY_REFERENCE;
import org.antlr.v4.runtime.Token;

/**
 * @author boris@temk.org
**/
public class FieldArrayReferenceExpression implements Expression {
    final private ObjectExpression parent;
    final private FieldDescriptor fd;

    public FieldArrayReferenceExpression(ObjectExpression parent, FieldDescriptor fd) {
        this.parent = parent;
        this.fd = fd;
    }
            
    @Override
    public Type getType() {
        return FIELD_ARRAY_REFERENCE;
    }

    public long eval() {
        return parent.eval().getRepeatedFieldCount(fd);
    }
     
    public Expression dereference(Token token, final LongExpression indexExpr) {
        final Expression expr;
        
        if (indexExpr instanceof IndexReference) {
            IndexReference.class.cast(indexExpr).upgrade(this);
        }
        
        switch (fd.getJavaType()) {
            case MESSAGE:
                expr = new ObjectExpression(fd.getMessageType()) {
                    @Override
                    public MessageOrBuilder eval() {
                        return MessageOrBuilder.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                    }

            @Override
            public int depth() {
                return parent.depth() + 1;
            }
                };
                break;

            case ENUM:
            case STRING:
            case BYTE_STRING:
                expr = new StringExpression() {
                    @Override
                    public String eval() {
                        return parent.eval().getRepeatedField(fd, (int )indexExpr.eval()).toString();
                    }
                };
                break;

            case BOOLEAN:
                expr = new BooleanExpression() {

                    @Override
                    public boolean eval() {
                        return Boolean.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                    }
                };
                break;

            case FLOAT:
                expr = new DoubleExpression() {

                    @Override
                    public double eval() {
                        return Float.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                    }
                };
                break;
                
            case DOUBLE:
                expr = new DoubleExpression() {

                    @Override
                    public double eval() {
                        return Double.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                    }
                };
                break;

            case INT:
                    expr = new LongExpression() {

                        @Override
                        public long eval() {
                            return Integer.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                        }
                    };
                break;
                
            case LONG:
                expr = new LongExpression() {

                    @Override
                    public long eval() {
                        return Long.class.cast(parent.eval().getRepeatedField(fd, (int )indexExpr.eval()));
                    }
                };
                break;
                
            default:
                throw new SemanticException(token, "Unexpected fields java type: " + fd.getJavaType());
        }
        return expr;
    }

    int depth() {
        return parent.depth() + 1;
    }
    
}
