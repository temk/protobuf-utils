/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MessageOrBuilder;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_REFERENCE;
import org.antlr.v4.runtime.Token;


/**
 * @author boris@temk.org
**/
public class FieldReferenceExpression implements Expression {
    private final ObjectExpression parent;
    private final FieldDescriptor fd;

    public FieldReferenceExpression(ObjectExpression owner, FieldDescriptor fd) {
        this.parent = owner;
        this.fd = fd;
    }
    
    @Override
    public Type getType() {
        return FIELD_REFERENCE;
    }

    public long eval() {
        return parent.eval().hasField(fd) ? 1 : 0;
    }
    
    public Expression dereference(Token token) {
        final Expression expr;
        
        switch (fd.getJavaType()) {
            case MESSAGE:
                expr = new ObjectExpression(fd.getMessageType()) {
                    @Override
                    public MessageOrBuilder eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return MessageOrBuilder.class.cast(b.getField(fd));
                    }

            @Override
            public int depth() {
                return parent.depth() + 1;
            }
                };
                break;

            case ENUM:
                expr = new StringExpression() {
                    @Override
                    public String eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        EnumValueDescriptor eval = EnumValueDescriptor.class.cast(b.getField(fd));
                        return eval.getName();
                    }
                };
                break;
                
            case STRING:
            case BYTE_STRING:
                expr = new StringExpression() {
                    @Override
                    public String eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return b.getField(fd).toString();
                    }
                };
                break;

            case BOOLEAN:
                expr = new BooleanExpression() {

                    @Override
                    public boolean eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return Boolean.class.cast(b.getField(fd));
                    }
                };
                break;

            case FLOAT:
                expr = new DoubleExpression() {

                    @Override
                    public double eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return Float.class.cast(b.getField(fd));
                    }
                };
                break;
                
            case DOUBLE:
                expr = new DoubleExpression() {

                    @Override
                    public double eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return Double.class.cast(b.getField(fd));
                    }
                };
                break;

            case INT:
                expr = new LongExpression() {

                    @Override
                    public long eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return Integer.class.cast(b.getField(fd));
                    }
                };
                break;
                
            case LONG:
                expr = new LongExpression() {

                    @Override
                    public long eval() {
                        MessageOrBuilder b = parent.eval();
                        if (!b.hasField(fd)) {
                            throw new NoFieldException(fd);
                        }
                        return Long.class.cast(b.getField(fd));
                    }
                };
                break;
                
            default:
                throw new SemanticException(token, "Unexpected fields java type: " + fd.getJavaType());
        }
        return expr;
    }
}
