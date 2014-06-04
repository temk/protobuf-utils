package net.katros.services.proto.grep.expr;

import static net.katros.services.proto.GrepLexer.COND_EQ;
import static net.katros.services.proto.GrepParser.ADD_OP;
import static net.katros.services.proto.GrepParser.BIT_AND;
import static net.katros.services.proto.GrepParser.BIT_NOT;
import static net.katros.services.proto.GrepParser.BIT_OR;
import static net.katros.services.proto.GrepParser.BIT_SHIFT_LEFT;
import static net.katros.services.proto.GrepParser.BIT_SHIFT_RIGHT;
import static net.katros.services.proto.GrepParser.BIT_XOR;
import static net.katros.services.proto.GrepParser.CAST_BOOL;
import static net.katros.services.proto.GrepParser.CAST_DOUBLE;
import static net.katros.services.proto.GrepParser.CAST_LONG;
import static net.katros.services.proto.GrepParser.CAST_STRING;
import static net.katros.services.proto.GrepParser.COND_AND;
import static net.katros.services.proto.GrepParser.COND_GE;
import static net.katros.services.proto.GrepParser.COND_GEQ;
import static net.katros.services.proto.GrepParser.COND_LE;
import static net.katros.services.proto.GrepParser.COND_LEQ;
import static net.katros.services.proto.GrepParser.COND_NEQ;
import static net.katros.services.proto.GrepParser.COND_OR;
import static net.katros.services.proto.GrepParser.COUNT;
import static net.katros.services.proto.GrepParser.DIV_OP;
import static net.katros.services.proto.GrepParser.MUL_OP;
import static net.katros.services.proto.GrepParser.NOT;
import static net.katros.services.proto.GrepParser.REGEXP;
import static net.katros.services.proto.GrepParser.REM_OP;
import static net.katros.services.proto.GrepParser.SUB_OP;
import net.katros.services.proto.grep.expr.Expression.Type;
import static net.katros.services.proto.grep.expr.Expression.Type.BOOL;
import static net.katros.services.proto.grep.expr.Expression.Type.DOUBLE;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_ARRAY_REFERENCE;
import static net.katros.services.proto.grep.expr.Expression.Type.FIELD_REFERENCE;
import static net.katros.services.proto.grep.expr.Expression.Type.LONG;
import org.antlr.v4.runtime.Token;

/**
 * @author boris@temk.org
**/
public class ExpressionFactory {

    // =============================================================================================
    public static final Expression createOpExpr(final Token token, final Expression larg, final Expression rarg) {
        Type largType = larg.getType();
        Type rargType = rarg.getType();

        final Expression expr;
        if (largType == rargType) {
            switch (largType) {
                case BOOL:
                    expr = createOpExpr(token, BooleanExpression.class.cast(larg), BooleanExpression.class.cast(rarg));
                    break;

                case LONG:
                    expr = createOpExpr(token, LongExpression.class.cast(larg), LongExpression.class.cast(rarg));
                    break;

                case DOUBLE:
                    expr = createOpExpr(token, DoubleExpression.class.cast(larg), DoubleExpression.class.cast(rarg));
                    break;

                case STRING:
                    expr = createOpExpr(token, StringExpression.class.cast(larg), StringExpression.class.cast(rarg));
                    break;

                case OBJECT_REFERENCE:
                    expr = createOpExpr(token, ObjectExpression.class.cast(larg), ObjectExpression.class.cast(rarg));
                    break;

                default:
                    throw new SemanticException(token, "Unexpected type " + largType);
            }
        } else if (largType.isNumeric() && rargType.isNumeric()) {
            expr = createOpExpr(token, castToDouble(token, larg), castToDouble(token, rarg));
        } else {
            throw new SemanticException(token, "Expression " + largType + " " + token.getText() + rargType + " is not valid");
        }

        return expr;
    }

    private static Expression createOpExpr(final Token token, final BooleanExpression larg, final BooleanExpression rarg) {
        final Expression expr;
        switch (token.getType()) {
            case COND_OR:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        if (larg.eval()) {
                            return true;
                        }
                        return rarg.eval();
                    }
                };
                break;

            case COND_AND:
                expr = new BooleanExpression() {

                    @Override
                    public final boolean eval() {
                        if (!larg.eval()) {
                            return false;
                        }
                        return rarg.eval();
                    }
                };
                break;

            case BIT_SHIFT_LEFT:
            case BIT_SHIFT_RIGHT:
            case BIT_OR:
            case BIT_XOR:
            case BIT_AND:
            case COND_GE:
            case COND_LE:
            case COND_GEQ:
            case COND_LEQ:
            case COND_EQ:
            case COND_NEQ:
            case REGEXP:
            case ADD_OP:
            case SUB_OP:
            case MUL_OP:
            case DIV_OP:
            case REM_OP:
            case COUNT:
            default:
                throw new SemanticException(token, "Operator '" + token.getText() + "' not applicable to type boolean");
        }

        return expr;
    }

    private static Expression createOpExpr(final Token token, final LongExpression larg, final LongExpression rarg) {
        final Expression expr;
        switch (token.getType()) {
            case BIT_SHIFT_LEFT:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() << rarg.eval();
                    }
                };
                break;

            case BIT_SHIFT_RIGHT:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() >> rarg.eval();
                    }
                };
                break;

            case BIT_OR:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() | rarg.eval();
                    }
                };
                break;

            case BIT_XOR:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() ^ rarg.eval();
                    }
                };
                break;

            case BIT_AND:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() & rarg.eval();
                    }
                };
                break;

            case COND_GE:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() > rarg.eval();
                    }
                };
                break;

            case COND_LE:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() < rarg.eval();
                    }
                };
                break;

            case COND_GEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() >= rarg.eval();
                    }
                };
                break;

            case COND_LEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() <= rarg.eval();
                    }
                };
                break;

            case COND_EQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() == rarg.eval();
                    }
                };
                break;

            case COND_NEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() != rarg.eval();
                    }
                };
                break;

            case ADD_OP:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() + rarg.eval();
                    }
                };
                break;

            case SUB_OP:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() - rarg.eval();
                    }
                };
                break;

            case MUL_OP:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() * rarg.eval();
                    }
                };
                break;

            case DIV_OP:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() / rarg.eval();
                    }
                };
                break;

            case REM_OP:
                expr = new LongExpression() {
                    @Override
                    public final long eval() {
                        return larg.eval() % rarg.eval();
                    }
                };
                break;

            // not applocable 
            case REGEXP:
            case COND_OR:
            case COND_AND:
            case COUNT:
            default:
                throw new SemanticException(token, "Operator '" + token.getText() + "' not applicable to type long");
        }

        return expr;
    }

    private static Expression createOpExpr(final Token token, final DoubleExpression larg, final DoubleExpression rarg) {
        final Expression expr;
        switch (token.getType()) {
            case COND_GE:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() > rarg.eval();
                    }
                };
                break;

            case COND_LE:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() < rarg.eval();
                    }
                };
                break;

            case COND_GEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() >= rarg.eval();
                    }
                };
                break;

            case COND_LEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() <= rarg.eval();
                    }
                };
                break;

            case COND_EQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() == rarg.eval();
                    }
                };
                break;

            case COND_NEQ:
                expr = new BooleanExpression() {
                    @Override
                    public final boolean eval() {
                        return larg.eval() != rarg.eval();
                    }
                };
                break;

            case ADD_OP:
                expr = new DoubleExpression() {
                    @Override
                    public final double eval() {
                        return larg.eval() + rarg.eval();
                    }
                };
                break;

            case SUB_OP:
                expr = new DoubleExpression() {
                    @Override
                    public final double eval() {
                        return larg.eval() - rarg.eval();
                    }
                };
                break;

            case MUL_OP:
                expr = new DoubleExpression() {
                    @Override
                    public final double eval() {
                        return larg.eval() * rarg.eval();
                    }
                };
                break;

            case DIV_OP:
                expr = new DoubleExpression() {
                    @Override
                    public final double eval() {
                        return larg.eval() / rarg.eval();
                    }
                };
                break;

            case REM_OP:
                expr = new DoubleExpression() {
                    @Override
                    public final double eval() {
                        return larg.eval() % rarg.eval();
                    }
                };
                break;

            // not applocable 
            case BIT_SHIFT_LEFT:
            case BIT_SHIFT_RIGHT:
            case BIT_OR:
            case BIT_XOR:
            case BIT_AND:
            case REGEXP:
            case COND_OR:
            case COND_AND:
            case COUNT:
            default:
                throw new SemanticException(token, "Operator '" + token.getText() + "' not applicable to type double");
        }

        return expr;
    }

    private static Expression createOpExpr(final Token token, final StringExpression larg, final StringExpression rarg) {
        final Expression expr;
        switch (token.getType()) {
            case COND_GE:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().compareTo(rarg.eval()) > 0;
                    }
                };
                break;

            case COND_LE:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().compareTo(rarg.eval()) < 0;
                    }
                };
                break;

            case COND_GEQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().compareTo(rarg.eval()) >= 0;
                    }
                };
                break;

            case COND_LEQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().compareTo(rarg.eval()) <= 0;
                    }
                };
                break;

            case COND_EQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().equals(rarg.eval());
                    }
                };
                break;

            case COND_NEQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return !larg.eval().equals(rarg.eval());
                    }
                };
                break;

            case ADD_OP:
                expr = new StringExpression() {
                    @Override
                    public String eval() {
                        return larg.eval() + rarg.eval();
                    }
                };
                break;

            case REGEXP:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().matches(rarg.eval());
                    }
                };
                break;

            // not applocable 
            case SUB_OP:
            case MUL_OP:
            case DIV_OP:
            case REM_OP:
            case BIT_SHIFT_LEFT:
            case BIT_SHIFT_RIGHT:
            case BIT_OR:
            case BIT_XOR:
            case BIT_AND:
            case COND_OR:
            case COND_AND:
            case COUNT:
            default:
                throw new SemanticException(token, "Operator '" + token.getText() + "' not applicable to type long");
        }

        return expr;
    }


    private static Expression createOpExpr(final Token token, final ObjectExpression larg, final ObjectExpression rarg) {
        final Expression expr;
        switch (token.getType()) {
            case COND_EQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return larg.eval().equals(rarg.eval());
                    }
                };
                break;

            case COND_NEQ:
                expr = new BooleanExpression() {
                    @Override
                    public boolean eval() {
                        return !larg.eval().equals(rarg.eval());
                    }
                };
                break;

            // not applocable 
            case ADD_OP:
            case REGEXP:
            case COND_GE:
            case COND_LE:
            case COND_GEQ:
            case COND_LEQ:
            case SUB_OP:
            case MUL_OP:
            case DIV_OP:
            case REM_OP:
            case BIT_SHIFT_LEFT:
            case BIT_SHIFT_RIGHT:
            case BIT_OR:
            case BIT_XOR:
            case BIT_AND:
            case COND_OR:
            case COND_AND:
            case COUNT:
            default:
                throw new SemanticException(token, "Operator '" + token.getText() + "' not applicable to type long");
        }

        return expr;
    }

    public static final Expression createOpExpr(final Token token, final Expression arg) {
        Type argType = arg.getType();
        final Expression expr;

        switch (token.getType()) {
            case CAST_BOOL:
                expr = castToBool(token, arg);
                break;

            case CAST_LONG:
                expr = castToLong(token, arg);
                break;

            case CAST_DOUBLE:
                expr = castToDouble(token, arg);
                break;

            case CAST_STRING:
                expr = castToString(token, arg);
                break;

            case ADD_OP:
                if (argType.isNumeric()) {
                    throw new SemanticException(token, "Operator unary '+' not applicable to type " + argType);
                }
                expr = arg;
                break;

            case SUB_OP:
                if (argType == LONG) {
                    final LongExpression longExpr = LongExpression.class.cast(arg);
                    expr = new LongExpression() {

                        @Override
                        public long eval() {
                            return -longExpr.eval();
                        }
                    };
                } else if (argType == DOUBLE) {
                    final LongExpression doubleExpr = LongExpression.class.cast(arg);
                    expr = new DoubleExpression() {

                        @Override
                        public double eval() {
                            return -doubleExpr.eval();
                        }
                    };
                } else {
                    throw new SemanticException(token, "Operator unary '-' not applicable to type " + argType);
                }
                break;

            case NOT:
                if (argType == BOOL) {
                    final BooleanExpression boolExpr = BooleanExpression.class.cast(arg);
                    return new BooleanExpression() {

                        @Override
                        public boolean eval() {
                            return !boolExpr.eval();
                        }
                    };
                } else {
                    throw new SemanticException(token, "Operator unary '!' not applicable to type " + argType);
                }

            case BIT_NOT:
                if (argType == LONG) {
                    final LongExpression longExpr = LongExpression.class.cast(arg);
                    expr = new LongExpression() {

                        @Override
                        public long eval() {
                            return ~longExpr.eval();
                        }
                    };
                } else {
                    throw new SemanticException(token, "Operator unary '~' not applicable to type " + argType);
                }
                break;

            case COUNT:
                if (argType == FIELD_REFERENCE) {
                    final FieldReferenceExpression fieldExpr = FieldReferenceExpression.class.cast(arg);
                    expr = new LongExpression() {

                        @Override
                        public long eval() {
                            return fieldExpr.eval();
                        }
                    };
                } else if (argType == FIELD_ARRAY_REFERENCE) {
                    final FieldArrayReferenceExpression fieldExpr = FieldArrayReferenceExpression.class.cast(arg);
                    expr = new LongExpression() {

                        @Override
                        public long eval() {
                            return fieldExpr.eval();
                        }
                    };
                } else {
                    throw new SemanticException(token, "operator # not applicable to expression of type " + argType); 
                }
                break;
                
            default:
                throw new SemanticException(token, "unexpected case");
        }

        return expr;
    }

    // =============== casts ========================
    public static final BooleanExpression castToBool(Token token, final Expression arg) {
        final BooleanExpression expr;

        switch (arg.getType()) {
            case BOOL:
                expr = BooleanExpression.class.cast(arg);
                break;

            case LONG: {
                final LongExpression lexpr = LongExpression.class.cast(arg);
                expr = new BooleanExpression() {

                    @Override
                    public final boolean eval() {
                        return lexpr.eval() != 0;
                    }
                };
            }
            break;

            case DOUBLE: {
                final DoubleExpression lexpr = DoubleExpression.class.cast(arg);
                expr = new BooleanExpression() {

                    @Override
                    public final boolean eval() {
                        return lexpr.eval() != 0;
                    }
                };
            }
            break;

            case STRING: {
                final StringExpression lexpr = StringExpression.class.cast(arg);
                expr = new BooleanExpression() {

                    @Override
                    public final boolean eval() {
                        return Boolean.parseBoolean(lexpr.eval());
                    }
                };
            }
            break;

            default:
                throw new SemanticException(token, "unexpected case");

        }

        return expr;
    }

    public static final LongExpression castToLong(Token token, final Expression arg) {
        final LongExpression expr;

        switch (arg.getType()) {
            case BOOL: {
                final BooleanExpression lexpr = BooleanExpression.class.cast(arg);
                expr = new LongExpression() {

                    @Override
                    public final long eval() {
                        return lexpr.eval() ? 1 : 0;
                    }
                };
            }
            break;

            case LONG:
                expr = LongExpression.class.cast(arg);
                break;

            case DOUBLE: {
                final DoubleExpression lexpr = DoubleExpression.class.cast(arg);
                expr = new LongExpression() {

                    @Override
                    public final long eval() {
                        return (long) lexpr.eval();
                    }
                };
            }
            break;

            case STRING: {
                final StringExpression lexpr = StringExpression.class.cast(arg);
                expr = new LongExpression() {

                    @Override
                    public final long eval() {
                        return Long.parseLong(lexpr.eval());
                    }
                };
            }
            break;

            default:
                throw new SemanticException(token, "unexpected case");

        }

        return expr;
    }

    public static final DoubleExpression castToDouble(Token token, final Expression arg) {
        final DoubleExpression expr;

        switch (arg.getType()) {
            case BOOL: {
                final BooleanExpression lexpr = BooleanExpression.class.cast(arg);
                expr = new DoubleExpression() {

                    @Override
                    public final double eval() {
                        return lexpr.eval() ? 1 : 0;
                    }
                };
            }
            break;

            case LONG: {
                final LongExpression lexpr = LongExpression.class.cast(arg);
                expr = new DoubleExpression() {

                    @Override
                    public final double eval() {
                        return lexpr.eval();
                    }
                };
            }
            break;

            case DOUBLE:
                expr = DoubleExpression.class.cast(arg);
                break;

            case STRING: {
                final StringExpression lexpr = StringExpression.class.cast(arg);
                expr = new DoubleExpression() {

                    @Override
                    public final double eval() {
                        return Double.parseDouble(lexpr.eval());
                    }
                };
            }
            break;

            default:
                throw new SemanticException(token, "unexpected case");

        }

        return expr;
    }

    public static final StringExpression castToString(Token token, final Expression arg) {
        final StringExpression expr;

        switch (arg.getType()) {
            case BOOL: {
                final BooleanExpression lexpr = BooleanExpression.class.cast(arg);
                expr = new StringExpression() {

                    @Override
                    public final String eval() {
                        return Boolean.toString(lexpr.eval());
                    }
                };
            }
            break;

            case LONG: {
                final LongExpression lexpr = LongExpression.class.cast(arg);
                expr = new StringExpression() {

                    @Override
                    public final String eval() {
                        return Long.toString(lexpr.eval());
                    }
                };
            }
            break;

            case DOUBLE: {
                final DoubleExpression lexpr = DoubleExpression.class.cast(arg);
                expr = new StringExpression() {

                    @Override
                    public final String eval() {
                        return Double.toString(lexpr.eval());
                    }
                };
            }
            break;

            case OBJECT_REFERENCE: {
                final ObjectExpression lexpr = ObjectExpression.class.cast(arg);
                expr = new StringExpression() {

                    @Override
                    public final String eval() {
                        return lexpr.eval().toString();
                    }
                };
            }
            break;
                
            case STRING:
                expr = StringExpression.class.cast(arg);
                break;

            default:
                throw new SemanticException(token, "unexpected case: " + arg.getType());

        }

        return expr;
    }

    // =============== constants ========================
    public static final BooleanExpression createConstExpr(final boolean value) {
        return new BooleanExpression() {

            @Override
            public final boolean eval() {
                return value;
            }
        };
    }

    public static final LongExpression createConstExpr(final long value) {
        return new LongExpression() {

            @Override
            public final long eval() {
                return value;
            }
        };
    }

    public static final DoubleExpression createConstExpr(final double value) {
        return new DoubleExpression() {

            @Override
            public final double eval() {
                return value;
            }
        };
    }

    public static final StringExpression createConstExpr(final String value) {
        return new StringExpression() {

            @Override
            public final String eval() {
                return value;
            }
        };
    }
}
