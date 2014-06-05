/**
   Copyright Katros LTD (2014)
**/
package net.katros.services.proto;

import com.google.common.reflect.ClassPath;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.GeneratedMessage;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author boris@temk.org
**/
public class Registry {

    private static final Logger logger = LogManager.getLogger(Registry.class);
    private static final Registry instance = new Registry();

    private final ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
    private final Map<String, GeneratedMessage> messages = new HashMap<>();

    private Registry() {
        populate();
    }

    private static List<Class<?>> getListOfClasses(Class<?>... clazzes) {
        List<Class<?>> list = new ArrayList<>();
        for (Class clazz : clazzes) {
            if (!clazz.isLocalClass() || GeneratedMessage.class.isAssignableFrom(clazz)) {
                list.add(clazz);
            }
            list.addAll(getListOfClasses(clazz.getDeclaredClasses()));
        }

        return list;
    }

    private void addMessage(Class clazz) {
        try {
            Method method = clazz.getMethod("getDefaultInstance");
            GeneratedMessage msg = GeneratedMessage.class.cast(method.invoke(null));
            String name = msg.getDescriptorForType().getFullName();
            messages.put(name, msg);
        } catch (NullPointerException| IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex) {
            logger.error("Exception during initialization of " + clazz.getCanonicalName(), ex);
        }
    }

    private void addExtension(Field field) {
        try {
            extensionRegistry.add(GeneratedMessage.GeneratedExtension.class.cast(field.get(null)));
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.error("Exception during initialization of " + field.getType().getCanonicalName() + "." + field.getName(), ex);
        }
    }

    private void populate() {
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        List<Class<?>> list = new ArrayList<>();
        String prop = System.getProperty("reg.scan");
        if (prop == null) {
            try {
                for (ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                    try {
                        if (info.getPackageName().startsWith("com.google.protobuf") || info.getPackageName().startsWith("com.googlecode.protobuf")) {
                            continue;
                        }
                        list.addAll(getListOfClasses(info.load()));
                    } catch (Throwable e1) {
                        // some classes are in the class path but cannot be loaded. just ignore them.
                    }
                }
            } catch (Throwable ex) {
                logger.error("Exception during initialization:", ex);
            }
        } else {
            for (String pkg: prop.split(":")) {
                try {
                    //System.err.println("scan " + pkg);
                    for (ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClassesRecursive(pkg)) {
                        list.addAll(getListOfClasses(info.load()));
                    }
                } catch (Throwable ex) {
                    logger.error("Exception during initialization:", ex);
                }
            }
        }
        for (Class clazz : list) {
            try {
                if (GeneratedMessage.class.isAssignableFrom(clazz)) {
                        addMessage(clazz);
                }

                for (Field field : clazz.getDeclaredFields()) {
                    int m = field.getModifiers();
                    if (Modifier.isPublic(m) && Modifier.isFinal(m) && Modifier.isStatic(m) && GeneratedMessage.GeneratedExtension.class.isAssignableFrom(field.getType())) {
                        addExtension(field);
                    }
                }
            } catch (Throwable t) {
            }
        }
    }

    public GeneratedMessage getInstanceForType(String type) {
        return messages.get(type);
    }

    public ExtensionRegistry getExtensionRegistry() {
        return extensionRegistry;
    }

    public static Registry getInstance() {
        return instance;
    }
}
