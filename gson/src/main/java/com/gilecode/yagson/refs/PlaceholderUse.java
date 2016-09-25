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

/**
 * Specifies the use site of the reference placeholder, such as an object field, an array's element, or map's key
 * or value. When the actual object is instantiated, it is applied to all registered use sites.
 *
 * @author Andrey Mogilev
 */
public interface PlaceholderUse<T> {

    /**
     * Sets the actual object to this use site, e.g. assigns it to a field, to an array's element etc.
     *
     * @param actualObject the actual object created as the replacement of the placeholder
     */
    void applyActualObject(T actualObject) throws IOException;
}
