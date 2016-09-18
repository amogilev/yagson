package com.gilecode.yagson.tests.checkers;

import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.tests.util.Pair;
import com.gilecode.yagson.tests.util.Triple;

import java.lang.ref.Reference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.gilecode.yagson.tests.util.TestUtils.hasMethod;
import static junit.framework.Assert.failNotEquals;

/**
 * Safe equality checker with an additional special processing of arrays and collections to provide a
 * protection against infinite recursion and ordering issues in unsorted sets and maps.
 * <p/>
 * For comparing the non-collection non-array objects, it uses either {@link Object#equals(Object)} or
 * a custom reflective field-by-field comparison, depending on the specified {@link EqualityCheckMode}.
 * Supports {@link EqualityCheckMode#EQUALS}, {@link EqualityCheckMode#REFLECTIVE} and {@link EqualityCheckMode#AUTO}
 * modes.
 */
public class SafeEqualsChecker implements EqualityChecker {

    private final EqualityCheckMode mode;

    private List<String> pathElements = new ArrayList<String>();

    private Object[] foundDifferences = new Object[2];
    private String foundDifferencesPath;

    private static Set<String> ignoredFieldNames = new HashSet<String>(Arrays.asList(
            "expectedModCount", "expectedArray"));

    public SafeEqualsChecker(EqualityCheckMode mode) {
        this.mode = mode;
        switch (mode) {
            case AUTO:
            case EQUALS:
            case REFLECTIVE:
                break;
            default:
                throw new IllegalStateException("Unsupported EqualityCheckMode: " + mode);
        }
    }

    @Override
    public void assertEquality(Object o1, Object o2) {
        pathElements.clear();

        IdentityHashMap<Object, Object> visited = new IdentityHashMap<Object, Object>();
        if (!objectsEqual(o1, o2, visited, "")) {
            if (foundDifferencesPath.isEmpty()) {
                // root objects differ
                failNotEquals("Deserialized object is not equal to the original", o1, o2);
            } else {
                failNotEquals("Sub-elements of the deserialized object at path '" + foundDifferencesPath +
                        "' is not equal to the original", foundDifferences[0], foundDifferences[1]);
            }
        }
    }

    private void registerDifferences(Object o1, Object o2, String...extraPathElements) {
        foundDifferences[0] = o1;
        foundDifferences[1] = o2;
        StringBuilder sb = new StringBuilder(64);

        List<String> l = new ArrayList<String>(pathElements);
        l.addAll(Arrays.asList(extraPathElements));

        for (String e : l) {
            if (!e.isEmpty()) {
                sb.append(e).append('.');
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        foundDifferencesPath = sb.toString();
    }

    private boolean objectsEqual(Object o1, Object o2, IdentityHashMap<Object, Object> visited, String pathElement) {
        pathElements.add(pathElement);
        try {
            if (o1 == o2) {
                return true;
            } else if (o1 == null || o2 == null) {
                registerDifferences(o1, o2);
                return false;
            }

            if (o1.getClass() != o2.getClass()) {
                registerDifferences(o1.getClass(), o2.getClass(), "class");
                return false;
            }
            Class<?> objClass = o1.getClass();

            if (visited.containsKey(o1)) {
                return true;
            }
            visited.put(o1, o1);

            if (objClass.isArray()) {
                return arraysEqual(o1, o2, visited);
            } else if (o1 instanceof Collection) {
                return collectionsEqual((Collection) o1, (Collection) o2, visited);
            } else if (o1 instanceof Map) {
                return mapsEqual((Map) o1, (Map) o2, visited);
            } else if (o1 instanceof Map.Entry) {
                Map.Entry e1 = (Map.Entry) o1;
                Map.Entry e2 = (Map.Entry) o2;
                return objectsEqual(e1.getKey(), e2.getKey(), visited, "key")
                        && objectsEqual(e1.getValue(), e2.getValue(), visited, "value");
            } else {
                // objects which support self-containing should use objectsEqual() in theirs equals()
                boolean isPrimitiveOrSpecial = objClass.isPrimitive() || objClass == String.class
                        || objClass == Class.class || objClass == ClassLoader.class
                        || objClass == Object.class || Enum.class.isAssignableFrom(objClass)
                        || Number.class.isAssignableFrom(objClass) || objClass == Boolean.class;

                boolean useEquals = isPrimitiveOrSpecial || mode == EqualityCheckMode.EQUALS
                        || (mode == EqualityCheckMode.AUTO && hasMethod(objClass, "equals", Object.class)
                            && !Iterator.class.isAssignableFrom(objClass));
                if (useEquals) {
                    if (!o1.equals(o2)) {
                        registerDifferences(o1, o2);
                        return false;
                    }
                    return true;
                } else {
                    return reflectiveObjectsEqual(objClass, o1, o2, visited);
                }
            }
        } finally {
            pathElements.remove(pathElements.size() - 1);
        }
    }

    private boolean reflectiveObjectsEqual(Class<?> objClass, Object o1, Object o2,
                                           IdentityHashMap<Object, Object> visited) {
        // compare all non-static non-transient fields in the class and its parents
        try {
            while (objClass != null) {
                for (Field f : objClass.getDeclaredFields()) {
                    if (Modifier.isStatic(f.getModifiers()) || Modifier.isTransient(f.getModifiers())) {
                        continue;
                    }
                    if (ignoredFieldNames.contains(f.getName())) {
                        continue;
                    }
                    f.setAccessible(true);
                    Object e1 = f.get(o1);
                    Object e2 = f.get(o2);
                    if (!objectsEqual(e1, e2, visited, f.getName())) {
                        return false;
                    }
                }
                objClass = objClass.getSuperclass();
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        return true;
    }

    private <K,V> boolean mapsEqual(Map<K, V> m1, Map<K, V> m2, IdentityHashMap<Object, Object> visited) {
        if (!(m1 instanceof SortedMap)) {
            return mapsEqualIgnoreOrder(m1, m2, visited);
        }
        Iterator<? extends Map.Entry<K, V>> i1 = m1.entrySet().iterator();
        Iterator<? extends Map.Entry<K, V>> i2 = m2.entrySet().iterator();
        int i = 0;
        while (i1.hasNext() && i2.hasNext()) {
            Map.Entry<K, V> e1 = i1.next();
            Map.Entry<K, V> e2 = i2.next();
            if (!objectsEqual(e1.getKey(), e2.getKey(), visited, i + "-key")) {
                return false;
            }
            if (!objectsEqual(e1.getValue(), e2.getValue(), visited, i + "-value")) {
                return false;
            }
            i++;
        }
        if (i1.hasNext()) {
            return objectsEqual(i1.next().getKey(), null, visited, Integer.toString(i)); // returns false
        } else if (i2.hasNext()) {
            return objectsEqual(null, i2.next().getKey(), visited, Integer.toString(i)); // returns false
        }
        return true;
    }

    private <E> boolean collectionsEqual(Collection<E> c1, Collection<E> c2, IdentityHashMap<Object, Object> visited) {
        boolean requireOrder = c1 instanceof List || c1 instanceof Queue || c1 instanceof SortedSet;
        if (!requireOrder) {
            return collectionsEqualIgnoreOrder(c1, c2, visited);
        }
        Iterator<?> i1 = c1.iterator();
        Iterator<?> i2 = c2.iterator();
        int i = 0;
        while (i1.hasNext() && i2.hasNext()) {
            Object e1 = i1.next();
            Object e2 = i2.next();
            if (!objectsEqual(e1, e2, visited, Integer.toString(i))) {
                // differences already registered
                return false;
            }
            i++;
        }
        if (i1.hasNext()) {
            return objectsEqual(i1.next(), null, visited, Integer.toString(i)); // returns false
        } else if (i2.hasNext()) {
            return objectsEqual(null, i2.next(), visited, Integer.toString(i)); // returns false
        }
        return true;
    }

    private boolean arraysEqual(Object o1, Object o2, IdentityHashMap<Object, Object> visited) {
        int l1 = Array.getLength(o1);
        int l2 = Array.getLength(o2);
        if (l1 != l2) {
            registerDifferences(l1, l2, "length");
            return false;
        }
        for (int i = 0; i < l1; i++) {
            Object e1 = Array.get(o1, i);
            Object e2 = Array.get(o2, i);
            if (!objectsEqual(e1, e2, visited, Integer.toString(i))) {
                // differences already registered
                return false;
            }
        }
        return true;
    }

    private <E> boolean collectionsEqualIgnoreOrder(Collection<E> c1, Collection<E> c2,
                                                       IdentityHashMap<Object, Object> visited) {
        if (c1.size() != c2.size()) {
            registerDifferences(c1.size(), c2.size(), "size()");
            return false;
        }

        // create and sort list of pairs [original_index, element]
        // TODO: if a collection of Map.Entries, use triples (or Pair of Pair) instead.
        LinkedList<Pair<Integer, ?>> l1 = toListWithIndices(c1);
        LinkedList<Pair<Integer, ?>> l2 = toListWithIndices(c2);

        Comparator<Pair<Integer, ?>> hashCodesComparator = new Comparator<Pair<Integer, ?>>() {
            @Override
            public int compare(Pair<Integer, ?> p1, Pair<Integer, ?> p2) {
                return p1.getSecond().hashCode() - p2.getSecond().hashCode();
            }
        };
        Collections.sort(l1, hashCodesComparator);
        Collections.sort(l2, hashCodesComparator);

        while (!l1.isEmpty()) {
            Pair<Integer, ?> p1 = l1.removeFirst();
            Iterator<Pair<Integer, ?>> i2 = l2.iterator();
            boolean equalElementFound = false;
            while (i2.hasNext()) {
                Pair<Integer, ?> p2 = i2.next();
                IdentityHashMap<Object, Object> visitedClone = new IdentityHashMap<Object, Object>(visited);
                if (objectsEqual(p1.getSecond(), p2.getSecond(), visitedClone, Integer.toString(p1.getFirst()))) {
                    visited.putAll(visitedClone);
                    equalElementFound = true;
                    i2.remove();
                    break;
                }
            }
            if (!equalElementFound) {
                // use the last registered difference
                return false;
            }
        }

        return true;
    }

    private LinkedList<Pair<Integer, ?>> toListWithIndices(Collection<?> c) {
        LinkedList<Pair<Integer, ?>> result = new LinkedList<Pair<Integer, ?>>();
        int i = 0;
        for (Object e : c) {
            if (e instanceof Map.Entry) {
                // some map entries are re-usable (e.g. for IdentityHashMap.entrySet()), so
                // we need to "extract" key/value for a correct ignoreOrder comparison
                Map.Entry entry = (Map.Entry) e;
                e = Pair.of(entry.getKey(), entry.getValue());
            }
            result.add(Pair.of(i++, e));
        }
        return result;
    }

    private <K,V> LinkedList<Triple<Integer,K,V>> toListWithIndices(Map<K,V> m) {
        LinkedList<Triple<Integer,K,V>> result = new LinkedList<Triple<Integer,K,V>>();
        int i = 0;
        for (Map.Entry<K, V> e : m.entrySet()) {
            result.add(Triple.of(i++, e.getKey(), e.getValue()));
        }
        return result;
    }

    private <K, V> boolean mapsEqualIgnoreOrder(Map<K, V> m1, Map<K, V> m2, IdentityHashMap<Object, Object> visited) {
        if (m1.size() != m2.size()) {
            return false;
        }
        LinkedList<Triple<Integer,K, V>> l1 = toListWithIndices(m1);
        LinkedList<Triple<Integer,K, V>> l2 = toListWithIndices(m2);
        Comparator<Triple<Integer,K, V>> keysHashCodesComparator = new Comparator<Triple<Integer,K, V>>() {
            @Override
            public int compare(Triple<Integer,K, V> e1, Triple<Integer,K, V> e2) {
                return e1.getSecond().hashCode() - e2.getSecond().hashCode();
            }
        };
        Collections.sort(l1, keysHashCodesComparator);
        Collections.sort(l2, keysHashCodesComparator);

        while (!l1.isEmpty()) {
            Triple<Integer,K, V> p1 = l1.removeFirst();
            Iterator<Triple<Integer,K, V>> i2 = l2.iterator();
            boolean equalKeyFound = false;
            while (i2.hasNext()) {
                Triple<Integer,K, V> p2 = i2.next();
                IdentityHashMap<Object, Object> visitedClone = new IdentityHashMap<Object, Object>(visited);
                Integer origIndex1 = p1.getFirst();
                if (objectsEqual(p1.getSecond(), p2.getSecond(), visitedClone, origIndex1 + "-key")) {
                    if (!objectsEqual(p1.getThird(), p2.getThird(), visitedClone, origIndex1 + "-value")) {
                        return false;
                    }

                    visited.putAll(visitedClone);
                    equalKeyFound = true;
                    i2.remove();
                    break;
                }
            }
            if (!equalKeyFound) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "SafeEqualsChecker{" +
                "mode=" + mode +
                '}';
    }
}
