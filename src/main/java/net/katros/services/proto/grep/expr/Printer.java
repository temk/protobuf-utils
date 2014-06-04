package net.katros.services.proto.grep.expr;

import com.google.protobuf.GeneratedMessage;
import net.katros.services.proto.MessageOutputStream;
import static net.katros.services.proto.grep.expr.Expression.Type.OBJECT_REFERENCE;
import java.io.PrintStream;
import java.util.List;

/**
 * @author boris@temk.org
**/
public abstract class Printer {

    public abstract void print();

    public static Printer createPrinter(final MessageOutputStream mos, Expression expr) {
        if (expr.getType() != OBJECT_REFERENCE) {
            throw new IllegalStateException("Expression of type " + expr.getType() + " cannot be printed to MessageOutputStream");
        }

        final ObjectExpression objExpr = ObjectExpression.class.cast(expr);
        return new Printer() {

            @Override
            public void print() {
                try {
                    mos.write(GeneratedMessage.class.cast(objExpr.eval()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Printer createPrinter(final PrintStream ps, final Expression expr, final String delim) {
        final StringExpression strExpr = ExpressionFactory.castToString(null, expr);
        return new Printer() {

            @Override
            public void print() {
                try {
                    ps.println(strExpr.eval());
                    ps.println(delim);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
    }

    public static Printer createPrinter(final List list, final Expression expr) {
        final Printer printer;
        switch (expr.getType()) {
            case BOOL: {
                final BooleanExpression e = BooleanExpression.class.cast(expr);
                printer = new Printer() {

                    @Override
                    public void print() {
                        list.add(Boolean.valueOf(e.eval()));
                    }
                };
            }
            break;

            case LONG: {
                final LongExpression e = LongExpression.class.cast(expr);
                printer = new Printer() {

                    @Override
                    public void print() {
                        list.add(Long.valueOf(e.eval()));
                    }
                };
            }
            break;

            case DOUBLE: {
                final DoubleExpression e = DoubleExpression.class.cast(expr);
                printer = new Printer() {

                    @Override
                    public void print() {
                        list.add(Double.valueOf(e.eval()));
                    }
                };
            }
            break;

            case STRING: {
                final StringExpression e = StringExpression.class.cast(expr);
                printer = new Printer() {

                    @Override
                    public void print() {
                        list.add(e.eval());
                    }
                };
            }
            break;

            case OBJECT_REFERENCE: {
                final ObjectExpression e = ObjectExpression.class.cast(expr);
                printer = new Printer() {

                    @Override
                    public void print() {
                        list.add(e.eval());
                    }
                };
            }
            break;

            default:
                throw new IllegalStateException(
                        "Unexpected expression type: " + expr.getType());
        }

        return printer;
    }

}
