package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;

import java.util.*;

/**
 * Tests various objects, collections, maps and arrays which contain itself as an element.
 *
 * @author Andrey Mogilev
 */
public class TestSelfContainingCollections extends BindingTestCase {

    private static class SelfContainingClass {
        SelfContainingClass selfRef;
        SelfContainingClass() {
            selfRef = this;
        }

        @Override
        public String toString() {
            return "SelfContainingClass{" +
                    "selfRef=" + (selfRef == this ? "this" : selfRef) +
                    '}';
        }
    }

    public void testSelfContainingObject() {
        SelfContainingClass obj = new SelfContainingClass();
        test(obj, jsonStr("{'selfRef':'@root'}"));
    }

    public void testSelfContainingList() {
        List<Object> l = new ArrayList<Object>();
        l.add(1L);
        l.add(l);
        l.add(2L);
        l.add(l);

        test(l, jsonStr("[1,'@root',2,'@root']"));
    }

    public void testSelfContainingMap1() {
        Map<Object, Object> m = new LinkedHashMap<Object, Object>();
        m.put("1", m);

        test(m, jsonStr("{'1':'@root'}"));
    }

    public void testSelfContainingMap2() {
        Map<Object, Object> m = new IdentityHashMap<Object, Object>();
        m.put(m, m);

        test(m, jsonStr("{'@root':'@root'}"));
    }

    public void testSelfContainingArray1() {
        Object[] arr = new Object[4];
        arr[0] = 1L;
        arr[1] = arr;
        arr[2] = 2L;
        arr[3] = arr;

        test(arr, jsonStr("[1,'@root',2,'@root']"));
    }

    public void testSelfContainingArray2() {
        Object[] arr1 = new Object[1];
        Object[] arr2 = new Object[1];
        arr1[0] = arr2;
        arr2[0] = arr1;

        test(arr1, jsonStr("[{'@type':'[Ljava.lang.Object;','@val':['@root']}]"));
    }

    public void testSelfContainingArray3() {
        Object[][] arr1 = new Object[1][];
        Object[] arr2 = new Object[1];
        arr1[0] = arr2;
        arr2[0] = arr1;

        test(gsonWithNoTypeInfo, arr1, jsonStr("[['@root']]"));
    }

    public void testSelfContainingArray4() {
        Object[][] arr = new Object[1][];
        arr[0] = arr;
        ClassWithObject obj = new ClassWithObject(arr);
        test(obj, jsonStr(
                "{'obj':{'@type':'[[Ljava.lang.Object;','@val':['@root.obj']}}"));
    }

    public void testSelfContainingArray5() {
        Object[] container = new Object[2];
        Object[] arr = new Object[1];
        arr[0] = arr;
        container[0] = container[1] = arr;

        test(container, jsonStr(
                "[{'@type':'[Ljava.lang.Object;','@val':['@root.0']},'@.0']"));
    }

    public void testSelfContainingArray6() {
        Object[] container = new Object[2];
        Object[] arr = new Object[1];
        arr[0] = container;
        container[0] = container[1] = arr;

        test(container, jsonStr(
                "[{'@type':'[Ljava.lang.Object;','@val':['@root']},'@.0']"));
    }

    public void testSelfContainingArrayThroughObj() {
        Object[] arr = new Object[1];
        ClassWithObject obj = new ClassWithObject(arr);
        arr[0] = obj;

        test(arr, jsonStr(
                "[{'@type':'com.gilecode.yagson.tests.data.ClassWithObject','@val':{'obj':'@root'}}]"));
    }

    public void testSelfContainingArrayThroughList() {
        Object[] arr = new Object[1];
        List<Object[]> l = new ArrayList<Object[]>();
        l.add(arr);

        // ClassWithObject is used to redirect equals() to TestingUtils.objectEquals()
        ClassWithObject obj = new ClassWithObject(l);
        arr[0] = obj;

        test(arr, jsonStr(
                "[{'@type':'com.gilecode.yagson.tests.data.ClassWithObject','@val':{'obj':['@root']}}]"));
    }

    public void testSelfContainingArrayThroughMap1() {
        Object[] arr = new Object[1];
        Map<Object, Object> m = new IdentityHashMap<Object, Object>();
        m.put(arr, arr);

        // ClassWithObject is used to redirect equals() to TestingUtils.objectEquals()
        ClassWithObject obj = new ClassWithObject(m);
        arr[0] = obj;

        test(arr, jsonStr(
                "[{'@type':'com.gilecode.yagson.tests.data.ClassWithObject','@val':{'obj':" +
                        "{'@type':'java.util.IdentityHashMap','@val':{'@root':'@root'}}}}]"));
    }

    public void testSelfContainingArrayThroughMap2() {
        Object[] arr = new Object[1];
        Map<Object, Object> m = new LinkedHashMap<Object, Object>();
        m.put(1, arr);
        m.put(arr, 2);

        // ClassWithObject is used to redirect equals() to TestingUtils.objectEquals()
        ClassWithObject obj = new ClassWithObject(m);
        arr[0] = obj;

        test(arr, jsonStr(
                "[{'@type':'com.gilecode.yagson.tests.data.ClassWithObject','@val':{'obj':{'@type':'java.util.LinkedHashMap','@val':" +
                        "[[{'@type':'java.lang.Integer','@val':1},'@root']," +
                        "['@root',{'@type':'java.lang.Integer','@val':2}]]}}}]"));
    }
}
