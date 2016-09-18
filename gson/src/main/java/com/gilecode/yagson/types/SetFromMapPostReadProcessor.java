package com.gilecode.yagson.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Collections;


public class SetFromMapPostReadProcessor implements PostReadProcessor {

    public void apply(Object instance) {
        Class c = instance.getClass();
        try {
            // sets internal fields as if were deserialized from Java ObjectStream
            Method mReadObject = c.getDeclaredMethod("readObject", ObjectInputStream.class);
            mReadObject.setAccessible(true);
            mReadObject.invoke(instance, new VoidObjectInputStream());

        } catch (Exception e) {
            throw new IllegalStateException("SetFromMapPostReadProcessor failed", e);
        }
    }

    public Iterable<String> getNamesOfProcessedClasses() {
        return Collections.singleton("java.util.Collections$SetFromMap");
    }

    private static class VoidObjectInputStream extends ObjectInputStream {
        protected VoidObjectInputStream() throws IOException, SecurityException {
        }

        @Override
        public void defaultReadObject() throws IOException, ClassNotFoundException {
        }
    }
}
