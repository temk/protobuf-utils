/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto;

import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author boris@temk.org
**/
public class Converter {
    private static final Logger logger = LogManager.getLogger(Converter.class);
    
    private static Object toMap(FieldDescriptor descr, Object value) {
        if (value == null) {
            return null;
        }
        
        final Object result;
        switch(descr.getJavaType()) {
            case MESSAGE:
                result = toMap((Message )value);
                break;

            case ENUM:
                result = ((EnumValueDescriptor )value).getName();
                break;

            default:
                result = value;
                break;                        
        }
        
        return result;
    }
    
    public static final Map toMap(Message msg) {
        if (msg == null) {
            return null;
        }
        Map map = new LinkedHashMap();
        for (Map.Entry<FieldDescriptor, Object> e :msg.getAllFields().entrySet()) {
            String key = e.getKey().getName();
            Object val = e.getValue();          
            if (e.getKey().isRepeated()) {
                List list = new ArrayList<>();
                for (Object element: (Collection )e.getValue()) {
                    list.add(toMap(e.getKey(), element));                    
                }
                map.put(key, list);
            } else {
                map.put(key, toMap(e.getKey(), e.getValue()));                
            }
        }
        return map;
    }
}
