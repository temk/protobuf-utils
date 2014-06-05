/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto.grep.expr;

/**
 * @author boris@temk.org
**/
public class IndexReference extends LongExpression implements Comparable<IndexReference> {
    private final String name;
    private FieldArrayReferenceExpression parent;
    private int depth = 0;
    private long value;

    public IndexReference(String name, FieldArrayReferenceExpression parent) {
        this.name = name;
        this.parent = parent;
    }

    public void setValue(long value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }
    
    @Override
    public final long eval() {
        return value;
    }
 
    public final long range() {
        return parent.eval();
    }

    private int depth() {
        return 0;
    }
    
    @Override
    public int compareTo(IndexReference t) {
        return Integer.compare(depth(), t.depth());
    }

    void upgrade(FieldArrayReferenceExpression far) {
        if (parent == null || depth > far.depth()) {
            depth = far.depth();
            parent = far;
        }
    }
}
