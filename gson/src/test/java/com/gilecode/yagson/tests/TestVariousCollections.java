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

import com.gilecode.yagson.tests.util.BindingTestCase;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.*;

/**
 * Tests serialization of various special {@link Collection}s from the Java Collections Framework,
 * not including Sets, Lists and Deques (which are tested in separate test classes).
 *
 * @author Andrey Mogilev
 */
public class TestVariousCollections extends BindingTestCase {

    //
    // Tests for the special collections from java.util.Collections
    //

    public void testUnmodifiableCollection() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.unmodifiableCollection(l);

        test(obj, jsonStr("{'c':[1]}"));
        testAsCollection(obj);
    }

    public void testCheckedCollection() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.checkedCollection(l, Long.class);
        test(obj, jsonStr("{'c':[1],'type':'java.lang.Long'}"));
        testAsCollection(obj);
    }

    public void testSynchronizedCollection() {
        Collection c;
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.synchronizedCollection(l);
        test(obj, jsonStr("{'c':[1],'mutex':'@root'}"));
        testAsCollection(obj);
    }

    //
    // Tests for values() of some Maps. More tests are performed automatically from within testAsMap()
    //

    public void testEnumMapValues() {
        Map<TimeUnit, String> map = new EnumMap<TimeUnit, String>(TimeUnit.class);
        map.put(TimeUnit.DAYS, "days");

        test(map.values(), Object.class, jsonStr(
                "{'@type':'java.util.EnumMap$Values','@val':{'this$0':" +
                        "{'@type':'java.util.EnumMap<java.util.concurrent.TimeUnit,?>','@val':{'DAYS':'days'}}}}"));
    }

    public void testProcessEnvironmentValues() throws PropertyVetoException {
        Collection<String> obj = System.getenv().values();
        testAsCollection(obj);
    }

    //
    // Tests for other non-Set non-Queue non-List Collections
    //
    public void testBeanContextSupport() throws PropertyVetoException {
        BeanContextSupport context = new BeanContextSupport();
        context.setLocale(Locale.CHINESE);

        BeanContextChildSupport bean = new BeanContextChildSupport();
        context.add(bean);

        context = test(context);
        assertEquals(1, context.size());
        assertEquals(Locale.CHINESE, context.getLocale());
    }
}
