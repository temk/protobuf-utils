/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.MessageOrBuilder;
import net.katros.services.proto.grep.expr.ObjectExpression;

/**
 * @author boris@temk.org
**/
public class RootPool {

    private final int depth;
    private final MessageOrBuilder[] queue;
    private final Descriptor descriptor;

    private int index = 0;
    private int total = 0;

    private final ObjectExpression[] roots;

    public RootPool(Descriptor descriptor, final int depth) {
        this.depth = depth;
        this.descriptor = descriptor;

        queue = new MessageOrBuilder[depth];
        roots = new ObjectExpression[depth];
        
        for (int k = 0; k < depth; ++k) {
            final int offset = k;
            roots[k] = new ObjectExpression(descriptor) {
                @Override
                public final MessageOrBuilder eval() {
                    return  queue[(index + depth - offset - 1) % depth];
                }

                @Override
                public int depth() { 
                    return 0;
                }
                
                
            };

        }
    }

    public void push(MessageOrBuilder msg) {
        queue[index] = msg;
        index = ++total % depth;

    }

    public boolean ready() {
        return total >= depth;
    }

    public final ObjectExpression getRoot(String name) {
        return getRoot(name.length() - 1);
    }

    public final ObjectExpression getRoot(final int offset) {
        return roots[offset];
    }
}
