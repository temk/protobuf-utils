/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import static net.katros.services.proto.grep.expr.Expression.Type.DOUBLE;

/**
 * @author boris@temk.org
**/
public abstract class DoubleExpression implements Expression {

    @Override
    public Type getType() {
        return DOUBLE;
    }
    
    public abstract double eval();
}
