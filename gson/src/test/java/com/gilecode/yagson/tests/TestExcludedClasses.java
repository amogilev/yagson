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
package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;

/**
 * Tests for serialization of excluded classes, i.e. special classes which are serialized to {@code null}s.
 *
 * @author Andrey Mogilev
 */
public class TestExcludedClasses extends BindingTestCase {

    public void testRootClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();

        test(cl, cl.getClass(), "null", EqualityCheckMode.EXPECT_NULL);
        test(cl, (Class)null, "null", EqualityCheckMode.EXPECT_NULL);

        assertNull(new YaGson().fromJson(jsonStr("{'@type':'sun.misc.Launcher$AppClassLoader'}"), Object.class));
    }

    public void testFieldClassLoader() {
        ClassLoader cl = this.getClass().getClassLoader();
        ClassWithObject obj = new ClassWithObject(cl);

        obj = test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
        assertNull(obj.obj);
    }

    public void testClassLoaderClass() {
        Object cl = ClassLoader.class;

        test(cl, jsonStr("'java.lang.ClassLoader'"));
    }
}
