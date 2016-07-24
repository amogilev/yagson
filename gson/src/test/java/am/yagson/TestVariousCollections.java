package am.yagson;

import am.yagson.refs.ReferencesPolicy;
import junit.framework.TestCase;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.*;

import static am.yagson.TestingUtils.jsonStr;

public class TestVariousCollections extends TestCase {

    private static YaGson gsonAllDuplicatesMode = new YaGsonBuilder()
            .setReferencesPolicy(ReferencesPolicy.DUPLICATE_OBJECTS)
            .create();
    //
    // Tests for the special collections from java.util.Collections
    //

    public void testUnmodifiableCollection() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.unmodifiableCollection(l);
        TestingUtils.testFully(obj, jsonStr("{'c':[1]}"));
    }

    public void testCheckedCollection() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.checkedCollection(l, Long.class);
        TestingUtils.testFully(obj, jsonStr("{'c':[1],'type':'java.lang.Long'}"));
    }

    public void testSynchronizedCollection() {
        Collection c;
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.synchronizedCollection(l);
        TestingUtils.testFully(obj, jsonStr("{'c':[1],'mutex':'@root'}"));
    }

    //
    // Tests for values() of different Maps
    //

    public void testHashMapValues() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testTreeMapValues() {
        Map<String, String> map = new TreeMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testEmptyMapValues() {
        Map<String, String> map = Collections.emptyMap();

        TestingUtils.testFully(map.values(), jsonStr(
                "[]"));
    }

    public void testSingletonMapValues() {
        Map<String, String> map = Collections.singletonMap("foo", "bar");

        TestingUtils.testFully(map.values(), jsonStr(
                "{'element':'bar'}"));
    }

    public void testEnumMapValues() {
        Map<TimeUnit, String> map = new EnumMap<TimeUnit, String>(TimeUnit.class);
        map.put(TimeUnit.DAYS, "days");

        TestingUtils.testFully(map.values(), Object.class, jsonStr(
                "{'@type':'java.util.EnumMap$Values','@val':{'this$0':" +
                        "{'@type':'java.util.EnumMap<java.util.concurrent.TimeUnit,?>','@val':{'DAYS':'days'}}}}"));
    }

    public void testConcurrentHashMapValues() {
        Map<String, String> map = new ConcurrentHashMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testIdentityHashMapValues() {
        Map<String, String> map = new IdentityHashMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testHashtableValues() {
        Map<String, String> map = new Hashtable<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        // NOTE: duplication in refs modes other than 'all duplicates'
        TestingUtils.testFully(obj, jsonStr(
                "{'c':{'@type':'java.util.Hashtable$ValueCollection','@val':{'this$0':{'foo1':'foo2'}}}," +
                        "'mutex':{'@type':'java.util.Hashtable','@val':{'foo1':'foo2'}}}"));

        TestingUtils.testFully(gsonAllDuplicatesMode, obj, jsonStr(
                "{'c':{'@type':'java.util.Hashtable$ValueCollection','@val':{'this$0':{'foo1':'foo2'}}}," +
                        "'mutex':'@root.c.this$0'}"));
    }

    public void testWeakHashMapValues() {
        Map<String, String> map = new WeakHashMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'this$0':{'foo1':'foo2'}}"));
    }

    public void testConcurrentSkipListMap() {
        Map<String, String> map = new ConcurrentSkipListMap<String, String>();
        map.put("foo1", "foo2");

        Collection<String> obj = map.values();

        TestingUtils.testFully(obj, jsonStr(
                "{'m':{'@type':'java.util.concurrent.ConcurrentSkipListMap','@val':{'foo1':'foo2'}}}"));
    }

    public void testProcessEnvironmentValues() throws PropertyVetoException {
        Collection<String> obj = System.getenv().values();
        Collection<String> result = TestingUtils.testUnsorted(obj);
        assertEquals(obj.size(), result.size()); // order in non-sorted Maps is not always preserved
    }

    //
    // Tests for other non-Set non-Queue non-List Collections
    //
    public void testBeanContextSupport() throws PropertyVetoException {
        BeanContextSupport context = new BeanContextSupport();
        context.setLocale(Locale.CHINESE);

        BeanContextChildSupport bean = new BeanContextChildSupport();
        context.add(bean);

        // FIXME: requires advanced transient policies to work
//        context = TestingUtils.testFully(context);
//        assertEquals(1, context.size());
//        assertEquals(Locale.CHINESE, context.getLocale());
    }
}
