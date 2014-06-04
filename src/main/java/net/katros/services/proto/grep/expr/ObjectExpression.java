package net.katros.services.proto.grep.expr;

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.MessageOrBuilder;
import static net.katros.services.proto.grep.expr.Expression.Type.OBJECT_REFERENCE;

/**
 * @author boris@temk.org
**/
public abstract class ObjectExpression implements Expression {
    private final Descriptor descriptor;

    public ObjectExpression(Descriptor descriptor) {
        this.descriptor = descriptor;
    }

    public Descriptor getDescriptor() {
        return descriptor;
    }
    
    @Override
    public Type getType() {
        return OBJECT_REFERENCE;
    }
    
    public abstract MessageOrBuilder eval();

    public abstract int depth();
}
