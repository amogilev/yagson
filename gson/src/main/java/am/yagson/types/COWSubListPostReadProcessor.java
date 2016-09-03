package am.yagson.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;


public class COWSubListPostReadProcessor implements PostReadProcessor {

    public void apply(Object instance) {
        Class c = instance.getClass();
        try {
            // synchronize 'array' (used for CoModification checks) with the current state of the backing list

            Field fBackingList = TypeUtils.findOneFieldByType(c, CopyOnWriteArrayList.class);
            Field fExpectedArray = TypeUtils.getDeclaredField(c, "expectedArray");
            Field fArray = TypeUtils.getDeclaredField(CopyOnWriteArrayList.class, "array");

            CopyOnWriteArrayList backingList = (CopyOnWriteArrayList) fBackingList.get(instance);
            Object[] array = (Object[]) fArray.get(backingList);
            fExpectedArray.set(instance, array);
        } catch (Exception e) {
            throw new IllegalStateException("COWSubListPostReadProcessor failed", e);
        }
    }

    public Iterable<String> getNamesOfProcessedClasses() {
        return Collections.singleton("java.util.concurrent.CopyOnWriteArrayList$COWSubList");
    }
}
