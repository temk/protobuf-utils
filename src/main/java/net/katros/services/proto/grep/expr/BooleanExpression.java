/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import static net.katros.services.proto.grep.expr.Expression.Type.BOOL;

/**
 * @author boris@temk.org
**/
public abstract class  BooleanExpression implements Expression {

    @Override
    public Type getType() {
        return BOOL;
    }
    
    public abstract boolean eval();
}
