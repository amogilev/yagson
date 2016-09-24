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

import static junit.framework.Assert.assertNull;

/**
 * Checker that verifies that the de-serialized object is null.
 */
public class NullChecker implements EqualityChecker {

    private static NullChecker instance = new NullChecker();

    public static NullChecker getInstance() {
        return instance;
    }

    private NullChecker() {
    }

    @Override
    public void assertEquality(Object o1, Object o2) {
        assertNull(o2);
    }

    @Override
    public String toString() {
        return "NullChecker";
    }
}
