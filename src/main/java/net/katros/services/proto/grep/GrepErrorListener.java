/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep;

import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * @author boris@temk.org
**/
public class GrepErrorListener extends ConsoleErrorListener {
    private String input;

    public GrepErrorListener(String input) {
        this.input = input;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int c, String msg, RecognitionException e) {
        System.err.println("Syntax Error: at symbol " + c + ": " + msg);
        System.err.println();
        System.err.println("Input:" + input);
        System.err.print("Error:");
        for (int k = 0; k < c; ++k) {
            System.err.print("-");
        }
        System.err.println("^");
        throw new RuntimeException();
    }
    
}
