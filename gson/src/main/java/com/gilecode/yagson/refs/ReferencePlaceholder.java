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
package com.gilecode.yagson.refs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder to a referenced object used when the final object is not known at the moment of
 * the reference registration.
 * <p/>
 * This is a typical case for a self-referenced arrays, as an array instance cannot be created until we
 * read all its elements to count the array length.
 *
 * @param <T> the expected type of the actual object, to which the placeholder if finally replaced
 *
 * @author Andrey Mogilev
 */
public class ReferencePlaceholder<T> {

    private T actualObject;

    private List<PlaceholderUse<? extends T>> registeredUses;

    public ReferencePlaceholder() {
    }

    public void registerUse(PlaceholderUse<? extends T> pu) {
        if (registeredUses == null) {
            registeredUses = new ArrayList<PlaceholderUse<? extends T>>();
        }
        registeredUses.add(pu);
    }

    public T getActualObject() {
        return actualObject;
    }

    @SuppressWarnings("unchecked")
    public void applyActualObject(T actualObject) throws IOException {
        this.actualObject = actualObject;
        if (registeredUses != null) {
            for (PlaceholderUse pu : registeredUses) {
                pu.applyActualObject(actualObject);
            }
        }
    }

    @Override
    public String toString() {
        return "ReferencePlaceholder{}";
    }
}
