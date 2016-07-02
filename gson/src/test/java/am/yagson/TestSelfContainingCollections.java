package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import java.util.*;

import static am.yagson.TestingUtils.jsonStr;

public class TestSelfContainingCollections extends TestCase {

    public static class SelfContainingClass {
        public SelfContainingClass selfRef;
        public SelfContainingClass() {
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
        TestingUtils.testFullyByToString(obj, jsonStr("{'selfRef':'@root'}"));
    }

    public void testSelfContainingList() {
        List<Object> l = new ArrayList<Object>();
        l.add(1L);
        l.add(l);
        l.add(2L);
        l.add(l);

        TestingUtils.testFullyByToString(l, jsonStr("[1,'@root',2,'@root']"));
    }

    public void testSelfContainingMap1() {
        Map<Object, Object> m = new LinkedHashMap<Object, Object>();
        m.put("1", m);

        TestingUtils.testFullyByToString(m, jsonStr("{'1':'@root'}"));
    }

    public void testSelfContainingMap2() {
        Map<Object, Object> m = new IdentityHashMap<Object, Object>();
        m.put(m, m);

        TestingUtils.testFullyByToString(m, jsonStr("{'@root':'@root'}"));
    }

    public void testSelfContainingArray1() {
        Object[] arr = new Object[4];
        arr[0] = 1L;
        arr[1] = arr;
        arr[2] = 2L;
        arr[3] = arr;

        TestingUtils.testFullyByToString(arr, jsonStr("[1,'@root',2,'@root']"));
    }

    public void testSelfContainingArray2() {
        Object[] arr1 = new Object[1];
        Object[] arr2 = new Object[1];
        arr1[0] = arr2;
        arr2[0] = arr1;

        TestingUtils.testFullyByToString(arr1, jsonStr("[{'@type':'[Ljava.lang.Object;','@val':['@root']}]"));
    }

    public void testSelfContainingArray3() {
        Object[][] arr1 = new Object[1][];
        Object[] arr2 = new Object[1];
        arr1[0] = arr2;
        arr2[0] = arr1;

        TestingUtils.testFullyByToString(arr1, TypeInfoPolicy.DISABLED, jsonStr("[['@root']]"));
    }

    public void testSelfContainingArray4() {
        Object[][] arr = new Object[1][];
        arr[0] = arr;
        ClassWithObject obj = new ClassWithObject(arr);
        TestingUtils.testFullyByToString(obj, jsonStr(
                "{'obj':{'@type':'[[Ljava.lang.Object;','@val':['@root.obj']}}"));
    }

    public void testSelfContainingArrayThroughObj() {
        Object[] arr = new Object[1];
        ClassWithObject obj = new ClassWithObject(arr);
        arr[0] = obj;

        TestingUtils.testFully(arr, jsonStr(
                "[{'@type':'am.yagson.ClassWithObject','@val':{'obj':'@root'}}]"));
    }

    public void testSelfContainingArrayThroughList() {
        Object[] arr = new Object[1];
        List<Object[]> l = new ArrayList<Object[]>();
        l.add(arr);

        // ClassWithObject is used to redirect equals() to TestingUtils.objectEquals()
        ClassWithObject obj = new ClassWithObject(l);
        arr[0] = obj;

        TestingUtils.testFully(arr, jsonStr(
                "[{'@type':'am.yagson.ClassWithObject','@val':{'obj':['@root']}}]"));
    }

    public void testSelfContainingArrayThroughMap1() {
        Object[] arr = new Object[1];
        Map<Object, Object> m = new IdentityHashMap<Object, Object>();
        m.put(arr, arr);

        // ClassWithObject is used to redirect equals() to TestingUtils.objectEquals()
        ClassWithObject obj = new ClassWithObject(m);
        arr[0] = obj;

        TestingUtils.testFully(arr, jsonStr(
                "[{'@type':'am.yagson.ClassWithObject','@val':{'obj':" +
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

        TestingUtils.testFully(arr, jsonStr(
                "[{'@type':'am.yagson.ClassWithObject','@val':{'obj':{'@type':'java.util.LinkedHashMap','@val':" +
                        "[[{'@type':'java.lang.Integer','@val':1},'@root']," +
                        "['@root',{'@type':'java.lang.Integer','@val':2}]]}}}]"));
    }
}
