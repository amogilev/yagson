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
package com.gilecode.yagson.tests.checkers;

import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.tests.util.TestUtils;

import java.util.*;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * A factory for equality checkers by mode.
 *
 * @author Andrey Mogilev
 */
public class EqualityCheckersFactory {
    public Collection<? extends EqualityChecker> getEqualityCheckersFor(EqualityCheckMode mode,
                                                                        Object obj1, Object obj2) {
        switch (mode) {
            case EQUALS:
            case REFLECTIVE:
                return singleton(new SafeEqualsChecker(mode));
            case NONE:
                return emptyList();
            case TO_STRING:
                return singleton(ToStringEqualityChecker.getInstance());
            case EXPECT_NULL:
                return singleton(NullChecker.getInstance());
            case AUTO:
                if (obj1 == null) {
                    return singleton(NullChecker.getInstance());
                }
                Class<?> objClass = obj1.getClass();
                List<EqualityChecker> result = new ArrayList<EqualityChecker>();
                if (TestUtils.hasMethod(objClass, "toString")
                        && !Collection.class.isAssignableFrom(objClass)
                        && !Map.class.isAssignableFrom(objClass)
                        && !Iterator.class.isAssignableFrom(objClass)) {
                    result.add(ToStringEqualityChecker.getInstance());
                }
                result.add(new SafeEqualsChecker(mode));
                return result;

        }
        throw new IllegalStateException("Unexpected mode: " + mode);
    }
}
