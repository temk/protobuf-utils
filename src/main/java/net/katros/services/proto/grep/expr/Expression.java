package net.katros.services.proto.grep.expr;

/**
 * @author boris@temk.org
**/
public interface Expression {
    public static enum Type {
        BOOL,
        LONG,
        DOUBLE,
        STRING,
        FIELD_REFERENCE,
        FIELD_ARRAY_REFERENCE,
        OBJECT_REFERENCE;

        public boolean isNumeric() {
            return this == LONG || this == DOUBLE;
        }
    }
    
    public Type getType();
}
 