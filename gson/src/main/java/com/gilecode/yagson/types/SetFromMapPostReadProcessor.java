/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.types;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;


/**
 * A {@link PostReadProcessor} for sets created from maps by {@link Collections#newSetFromMap(Map)}.
 * Performs the same actions as the general Java serialization does.
 *
 * @author Andrey Mogilev
 */
public class SetFromMapPostReadProcessor implements PostReadProcessor {

    @SuppressWarnings("unchecked")
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
        VoidObjectInputStream() throws IOException, SecurityException {
        }

        @Override
        public void defaultReadObject() throws IOException, ClassNotFoundException {
        }
    }
}
