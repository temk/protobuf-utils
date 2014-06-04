package net.katros.services.proto.grep.expr;

import static net.katros.services.proto.grep.expr.Expression.Type.STRING;

/**
 * @author boris@temk.org
**/
public abstract class StringExpression implements Expression {

    @Override
    public Type getType() {
        return STRING;
    }
    
    public abstract String eval();
}
