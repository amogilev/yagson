package com.gilecode.yagson.tests.util;

import com.gilecode.yagson.tests.data.Person;
import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.YaGsonBuilder;
import com.gilecode.yagson.refs.ReferencesPolicy;
import com.gilecode.yagson.tests.checkers.*;
import com.gilecode.yagson.types.TypeInfoPolicy;
import com.google.gson.Gson;
import junit.framework.TestCase;

import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.*;

/**
 * A collection of various testing methods.
 *
 * @author Andrey Mogilev
 */
public abstract class BindingTestCase extends TestCase {

    private static final boolean printLogs = true;

    // instances of YaGson with custom references policies

    protected static YaGson gsonCircularOnlyMode = new YaGsonBuilder()
            .setReferencesPolicy(ReferencesPolicy.CIRCULAR_ONLY)
            .create();

    protected static YaGson gsonCircularAndSiblingsMode = new YaGsonBuilder()
            .setReferencesPolicy(ReferencesPolicy.CIRCULAR_AND_SIBLINGS)
            .create();

    protected static YaGson gsonAllDuplicatesMode = new YaGsonBuilder()
            .setReferencesPolicy(ReferencesPolicy.DUPLICATE_OBJECTS)
            .create();

    protected static YaGson gsonWithNoTypeInfo = new YaGsonBuilder()
            .setTypeInfoPolicy(TypeInfoPolicy.DISABLED)
            .create();

    protected static final Comparator<String> MY_STRING_CMP = new Comparator<String>() {
        public int compare(String s1, String s2) {
            int cmp = s1.length() - s2.length();
            if (cmp == 0) {
                cmp = s1.compareTo(s2);
            }
            return cmp;
        }
    };

    protected static final Comparator<Person> MY_PERSON_CMP = new Comparator<Person>() {
        public int compare(Person o1, Person o2) {
            int cmp = MY_STRING_CMP.compare(o1.family, o2.family);
            if (cmp == 0) {
                cmp = MY_STRING_CMP.compare(o1.name, o2.name);
            }
            return cmp;
        }
    };



    /**
     * Replaces all single quotations marks characters (') to double (") and returns the resulting string.
     * <p/>
     * Used to make the expected JSON strings more readable in Java sources.
     */
    public static String jsonStr(String s) {
        return s.replace('\'', '"');
    }

    //
    // Default test elements to use
    //
    protected static List<String> collectionsTestsElements = Arrays.asList("1foo", "2", "3333", "4bar");

    protected static final Map<String, String> mapsTestsElements = new LinkedHashMap<String, String>();
    static {
        mapsTestsElements.put("1foo", "1");
        mapsTestsElements.put("2", "2");
        mapsTestsElements.put("3333", "3");
        mapsTestsElements.put("4bar", "4");
    }

    //
    // General test methods, applicable to all binding test cases.
    // NOTE: for collections and maps, it is recommended to additionally use
    // specialized test methods 'testAsCollection()' and 'testAsMap()'.
    //

    /**
     * The general binding test, which serializes an object to JSON string, deserialize it back and
     * compares the initial and resulting objects according to the specified check mode.
     * <p/>
     * All parameters except 'obj' may be {@code null}, which is used to specify the default parameter
     * value.
     *
     * @param gson a Gson/YaGson instance to use
     * @param obj a source object to serialize
     * @param deserializationType a de-serialization type to be used both for the serialization and
     *                            the de-serialization. If {@code null}, the default is the obj's class
     * @param expectedJson the expected JSON string. Not checked if {@code null}
     * @param mode the equality check mode. The default is {@link EqualityCheckMode#AUTO}.
     *
     * @param <T> the formal object type
     *
     * @return the de-serialized object
     */
    protected <T> T test(Gson gson, T obj, Type deserializationType, String expectedJson, EqualityCheckMode mode) {
        return doTest(gson, obj, deserializationType, expectedJson, mode);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(Gson gson, T obj, Type deserializationType, String expected) {
        return doTest(gson, obj, deserializationType, expected, null);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(Gson gson, T obj, String expected) {
        return doTest(gson, obj, null, expected, null);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(T obj, Type deserializationType, String expectedJson, EqualityCheckMode mode) {
        return doTest(null, obj, deserializationType, expectedJson, mode);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(T obj, Type deserializationType, String expected) {
        return doTest(null, obj, deserializationType, expected, null);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(T obj, String expectedJson, EqualityCheckMode mode) {
        return doTest(null, obj, null, expectedJson, mode);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(T obj, String expected) {
        return doTest(null, obj, null, expected, null);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(Gson gson, T obj) {
        return doTest(gson, obj, null, null, null);
    }

    /**
     * A version of {@link #test(Gson, Object, Type, String, EqualityCheckMode)} with less parameters.
     * Missing parameters are set to the default values.
     */
    protected <T> T test(T obj) {
        return doTest(null, obj, null, null, null);
    }

    //
    // Specific collection test methods. Use as additional to general 'test' methods.
    // Tests include checking of a general collections functionality on a de-serialized instance
    // (iterators, clear, addAll, add etc.), and serialization tests for sublists and iterators.
    // If possible, a collection is tested in both 'empty' and 'filled' states
    //

    /**
     * A collection-specific binding test, which tests general collection functionality on the default collection
     * instance (created by a public no-arg constructor) and a set of pre-defined test elements.
     *
     * @param collClass the collection class to test
     */
    @SuppressWarnings("unchecked")
    protected void testAsCollection(Class<? extends Collection> collClass) {
        Collection<String> collInstance = createInstance(collClass);
        collInstance.addAll(collectionsTestsElements);

        doTestAsCollection(collInstance);
    }

    /**
     * A collection-specific binding test, which tests general collection functionality on the specified collection
     * instance, using the original collection's elements as the test elements.
     *
     * @param collInstance the collection instance to test, filled with the test elements
     */
    protected <E> void testAsCollection(Collection<E> collInstance) {
        doTestAsCollection(collInstance);
    }

    //
    // Specific Maps test methods. Use as additional to general 'test' methods.
    // Tests include checking of a general maps functionality on a de-serialized instance,
    // as well as the serialization tests for keySet/entrySet/values and iterators.
    // If possible, a map is tested in both 'empty' and 'filled' states
    //

    /**
     * A map-specific binding test, which tests general map functionality on the default map
     * instance (created by a public no-arg constructor) and a set of pre-defined test elements.
     *
     * @param mapClass the map class to test
     */
    @SuppressWarnings("unchecked")
    protected void testAsMap(Class<? extends Map> mapClass) {
        Map<String, String> mapInstance = createInstance(mapClass);
        mapInstance.putAll(mapsTestsElements);

        doTestAsMap(mapInstance);
    }

    /**
     * A map-specific binding test, which tests general map functionality on the specified map
     * instance, using the original map's elements as the test elements.
     *
     * @param mapInstance the map instance to test, filled with the test elements
     */
    protected <K, V> void testAsMap(Map<K, V> mapInstance) {
        doTestAsMap(mapInstance);
    }


    //
    // Implementation section
    //

    /**
     * Determines the current test class and method names, and return them in a format appropriate for logging.
     */
    private String getTestLogName(String[] extraLogPaths) {
        if (!printLogs) {
            return "";
        }

        String testClassName = getClass().getName();
        String testClassSimpleName = getClass().getSimpleName();
        String logName = testClassSimpleName;

        StackTraceElement[] stackTrace = new Throwable().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (stackTraceElement.getClassName().equals(testClassName)) {
                logName = testClassSimpleName + "::" + stackTraceElement.getMethodName();
                break;
            }
        }
        for (String extraPath : extraLogPaths) {
            logName += "." + extraPath;
        }
        return logName;
    }

    private <T> T doTest(Gson gson, T obj, Type deserializationType, String expectedJson,
                         EqualityCheckMode mode, String...extraLogPaths) {
        String logName = getTestLogName(extraLogPaths);
        if (gson == null) {
            gson = new YaGson();
        }
        if (deserializationType == null) {
            deserializationType = obj.getClass();
        }
        if (mode == null) {
            mode = EqualityCheckMode.AUTO;
        }
        String json = gson.toJson(obj, deserializationType);
        printLog(logName, "toJson()", json);
        assertNotNull(json);

        if (expectedJson != null) {
            assertEquals("toJson(obj) differs from the expected: ", expectedJson, json);
        }

        T obj2 = gson.fromJson(json, deserializationType);

        Collection<? extends EqualityChecker> checkers = new EqualityCheckersFactory().getEqualityCheckersFor(
                mode, obj, obj2);
        if (checkers.isEmpty()) {
            printLog(logName, "checks", "NO CHECKS!");
        }
        for (EqualityChecker checker : checkers) {
            checker.assertEquality(obj, obj2);
            printLog(logName, checker.toString(), "SUCCESS");
        }

        return obj2;
    }

    private <E> void doTestAsCollection(Collection<E> collInstance) {
        List<E> testElements = new ArrayList<E>(collInstance);

        // check of the original collection is modifiable
        boolean isModifiable = false;
        if (!collInstance.isEmpty()) {
            try {
                collInstance.clear();
                collInstance.addAll(testElements);
                isModifiable = true;
            } catch (Exception e) {
                // not modifiable
            }
        }

        // at first, if possible, test serialization of an empty collection
        if (isModifiable) {
            collInstance.clear();
            doTestCollectionSerializationAndFunctionality(collInstance, testElements,
                    true, false, "asCollection(empty)");
            collInstance.addAll(testElements);
        }

        // then, test serialization of a collection with all original elements
        doTestCollectionSerializationAndFunctionality(collInstance, testElements,
                isModifiable, false, "asCollection");
    }

    private <K,V> void doTestAsMap(Map<K,V> mapInstance) {
        LinkedHashMap<K, V> testElements = new LinkedHashMap<K, V>(mapInstance);

        // check of the original collection is modifiable
        boolean isModifiable = false;
        if (!mapInstance.isEmpty()) {
            try {
                mapInstance.clear();
                mapInstance.putAll(testElements);
                isModifiable = true;
            } catch (Exception e) {
                // not modifiable
            }
        }

        // at first, if possible, test serialization of an empty map
        if (isModifiable) {
            mapInstance.clear();
            doTestMapSerializationAndFunctionality(mapInstance, testElements,
                    true, false, "asMap(empty)");
            mapInstance.putAll(testElements);
        }

        // then, test serialization of a map with all original elements
        doTestMapSerializationAndFunctionality(mapInstance, testElements,
                isModifiable, false, "asMap");
    }

    private <E> void doTestCollectionSerializationAndFunctionality(Collection<E> c1, List<E> testElements,
                               boolean isModifiable, boolean isSubList, String...extraLogPaths) {

        int size = c1.size();

        // test successful binding and equality of the de-serialized and original instances
        Collection<E> c2 = doTest(null, c1, null, null, EqualityCheckMode.AUTO, extraLogPaths);

        // test sub-lists as collections. If possible, use empty, partial and full sub-lists
        if (!isSubList && (c2 instanceof List)) {
            List<E> l2 = (List<E>) c2;

            // empty sublist
            List<E> subList = l2.subList(0, 0);
            doTestCollectionSerializationAndFunctionality(subList, new ArrayList<E>(subList),
                    isModifiable, true, join(extraLogPaths, "sublist(empty)"));

            // partial sublist
            if (size > 1) {
                subList = l2.subList(1, 2);
                doTestCollectionSerializationAndFunctionality(subList, new ArrayList<E>(subList),
                        isModifiable, true, join(extraLogPaths, "sublist(partial)"));
            }

            // full sublist
            if (size > 0) {
                subList = l2.subList(0, l2.size());
                doTestCollectionSerializationAndFunctionality(subList, new ArrayList<E>(subList),
                        isModifiable, true, join(extraLogPaths, "sublist(full)"));
            }
        }

        // test serialization of iterators
        doTestCollectionIterators(c1, extraLogPaths);

        // test modification functionality
        if (isModifiable) {
            // check clear, addAll and add functionality
            if (!c2.isEmpty()) {
                // NOTE: isEmpty() check is performed because of a bug in JDK's CopyOnWriteArrayList's sublists,
                //       which cannot be cleared if already empty
                c2.clear();
                assertTrue(c2.isEmpty());
            }

            c2.addAll(testElements);
            assertEquals(testElements.size(), c2.size());

            if (!c2.isEmpty()) {
                c2.clear();
            }
            for (E e : testElements) {
                c2.add(e);
            }
            assertEquals(testElements.size(), c2.size());
        }
    }

    private <K, V> void doTestMapSerializationAndFunctionality(Map<K, V> m1, LinkedHashMap<K, V> testElements,
                               boolean isModifiable, boolean isSubMap, String...extraLogPaths) {

        // test successful binding and equality of the de-serialized and original instances
        Map<K, V> m2 = doTest(null, m1, null, null, EqualityCheckMode.AUTO, extraLogPaths);

        // test keys/values/entries as collections, but without change operations
        doTestCollectionSerializationAndFunctionality(m2.keySet(), null, false, false, "keySet");
        doTestCollectionSerializationAndFunctionality(m2.entrySet(), null, false, false, "entrySet");
        doTestCollectionSerializationAndFunctionality(m2.values(), null, false, false, "values");

        if (!isSubMap && m1 instanceof NavigableMap ) {
            NavigableMap<K,V> nm1 = (NavigableMap<K,V>) m1;

            doTestMapSerializationAndFunctionality(nm1.descendingMap(), testElements, isModifiable, true,
                    "descendingMap");
            if (!m1.isEmpty()) {
                K firstKey = m1.keySet().iterator().next();
                NavigableMap<K, V> subMap = nm1.subMap(firstKey, true, firstKey, true);
                doTestMapSerializationAndFunctionality(subMap, null, false, true, "subMap");
            }
        }

        if (isModifiable) {
            // check clear, putAll and put functionality
            m2.clear();
            assertTrue(m2.isEmpty());

            m2.putAll(testElements);
            assertEquals(testElements.size(), m2.size());

            m2.clear();
            for (Map.Entry<K, V> entry : testElements.entrySet()) {
                m2.put(entry.getKey(), entry.getValue());
            }
            assertEquals(testElements.size(), m2.size());
        }
    }

    private <E> void doTestCollectionIterators(Collection<E> c1, String[] extraLogPaths) {
//        if (c1 instanceof ArrayDeque) {
//            // TODO known issues with deque iterators, throwing ConcurrentModificationException, fix in 0.2
//            return;
//        }
        Iterator<E> it = c1.iterator();
        doTestIteratorSerializationAndFunctionality(it, join(extraLogPaths, "iterator(0)"));

        if (c1.size() > 1) {
            it = c1.iterator();
            it.next();
            doTestIteratorSerializationAndFunctionality(it, join(extraLogPaths, "iterator(1)"));
        }

        if (c1.size() > 0) {
            it = c1.iterator();
            while (it.hasNext()) {
                it.next();
            }
            doTestIteratorSerializationAndFunctionality(it, join(extraLogPaths, "iterator(end)"));
        }

        // same for list iterators
        if (c1 instanceof List) {
            List<E> l1 = (List<E>) c1;

            doTestIteratorSerializationAndFunctionality(l1.listIterator(),
                    join(extraLogPaths, "listIterator(0)"));

            if (c1.size() > 1) {
                doTestIteratorSerializationAndFunctionality(l1.listIterator(1),
                        join(extraLogPaths, "listIterator(1)"));
            }
            if (c1.size() > 0) {
                doTestIteratorSerializationAndFunctionality(l1.listIterator(l1.size()),
                        join(extraLogPaths, "listIterator(end)"));
            }
        }
    }

    private <E> void doTestIteratorSerializationAndFunctionality(Iterator<E> it1, String[] extraLogPaths) {
        // do not check iterators for equality, as (1) order is not guaranteed, (2) may use special serialization,
        //   (3) may contain weak references
        Iterator<E> it2 = doTest(null, it1, null, null, EqualityCheckMode.NONE, extraLogPaths);

        // make sure that has the same number of elements is left
        while (it1.hasNext() && it2.hasNext()) {
            it1.next();
            it2.next();
        }

        // TODO: known issues with some iterators, to be fixed in 0.2. After that, replace printLog to fail
        if (it1.hasNext()) {
            printLog(getTestLogName(extraLogPaths), "iteration",
                    "FAILURE! : De-serialized iterator has fewer elements than expected");
            // fail("De-serialized iterator has fewer elements than expected, at " + Arrays.toString(extraLogPaths));
        }
        if (it2.hasNext()) {
            printLog(getTestLogName(extraLogPaths), "iteration",
                    "FAILURE! : De-serialized iterator has more elements than expected");
//            fail("De-serialized iterator has more elements than expected, at " + Arrays.toString(extraLogPaths));
        }
    }

    private String[] join(String[] arr, String str) {
        String[] result = Arrays.copyOf(arr, arr.length + 1);
        result[arr.length] = str;
        return result;
    }

    private <E> E createInstance(Class<? extends E> clazz) {
        try {
            Constructor<? extends E> constr = clazz.getDeclaredConstructor();
            return constr.newInstance();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create an instance of " + clazz + ", please pass the " +
                    "created instance to the test method", e);
        }
    }


    private void printLog(String logName, String operation, String result) {
        if (printLogs) {
            String msg = "[" + logName + "] " + operation + " -> " + result;
            System.out.println(msg);
        }
    }
}
