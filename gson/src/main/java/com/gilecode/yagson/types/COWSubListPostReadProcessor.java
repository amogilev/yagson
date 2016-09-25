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

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A {@link PostReadProcessor} for sublists of {@link CopyOnWriteArrayList}. Sets the 'expectedArray'
 * field to match the correpsonding 'array' list in the backing full list.
 *
 * @author Andrey Mogilev
 */
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
