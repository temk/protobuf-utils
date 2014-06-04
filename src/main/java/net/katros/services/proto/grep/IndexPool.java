package net.katros.services.proto.grep;

import net.katros.services.proto.grep.expr.Expression;
import net.katros.services.proto.grep.expr.FieldArrayReferenceExpression;
import net.katros.services.proto.grep.expr.IndexReference;
import net.katros.services.proto.grep.expr.LongExpression;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author boris@temk.org
**/
public class IndexPool {
    private int counter = 0;
    private IndexReference [] array;
    
    private final Set<String> names;
    private final Map<String, IndexReference> pool = new HashMap<>();
    private final Map<FieldArrayReferenceExpression, String> anonyms = new HashMap<>();

    public IndexPool(Set<String> names) {
        this.names = names;
    }
    
    
    void build() {
        List<IndexReference> list = new ArrayList<>(pool.values());
        Collections.sort(list);
        
        array = new IndexReference[list.size()];        
        array = list.toArray(array);
    }
    boolean contains(String name) {
        return pool.containsKey(name);
    }

    Expression getIndex(String name) {
        return pool.get(name);
    }

    LongExpression createNamed(String name, FieldArrayReferenceExpression far) {
        IndexReference index =  pool.get(name);
        if (index == null) {
            index = new IndexReference(name, far);
            pool.put(index.getName(), index);
        }
        return index;
    }
    
    LongExpression createAnnonimous(FieldArrayReferenceExpression far) {
        String name = anonyms.get(far);
        if (name == null) {
            name = "*" + (++ counter) + "*";
            anonyms.put(far, name); 
        }
        return createNamed(name, far);
    }
 
    IndexReference [] getIndexes() {
        return array;
    }
}
