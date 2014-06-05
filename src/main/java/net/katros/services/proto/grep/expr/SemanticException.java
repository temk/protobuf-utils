/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

import org.antlr.v4.runtime.Token;

/**
 * @author boris@temk.org
**/
public class SemanticException extends RuntimeException {
    private Token token;

    public Token getToken() {
        return token;
    }

    public SemanticException(Token token, String string) {
        super(string);
        this.token = token;
    }
    
}
