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
package com.gilecode.yagson;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import junit.framework.TestCase;

/**
 * Tests some of bugfixes made in the original Gson code.
 *
 * @author Andrey Mogilev
 */
public class TestGsonBugFixes extends TestCase {
    private static class Foo1<A> {
        Foo2<? extends A> foo2;
    }

    private static class Foo2<B> {
        Foo1<? super B> foo1;
    }

    /**
     * On original Gson 2.4, throws StackOverflowError, as infinite recursion occurs on
     * getAdapter for types like 'Foo2&lt;? extends ? super ? extends ... ? extends A&gt;'
     * <p/>
     * Fixed by modifing $Gson$Types methods subtypeOf & supertypeOf
     */
    public void testInfiniteRecursionOfGetAdapter() {
        new Gson().getAdapter(Foo1.class);
    }

    public void testDoubleSupertype() {
        assertEquals($Gson$Types.supertypeOf(Number.class), $Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class)));
    }

    public void testDoubleSubtype() {
        assertEquals($Gson$Types.subtypeOf(Number.class), $Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class)));
    }

    public void testSuperSubtype() {
        assertEquals($Gson$Types.subtypeOf(Object.class), $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Number.class)));
    }

    public void testSubSupertype() {
        assertEquals($Gson$Types.subtypeOf(Object.class), $Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class)));
    }
}
