/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import static net.katros.services.proto.grep.expr.Expression.Type.LONG;

/**
 * @author boris@temk.org
**/
public abstract class LongExpression implements Expression {

    @Override
    public Type getType() {
        return LONG;
    }
    
    public abstract long eval();
}
