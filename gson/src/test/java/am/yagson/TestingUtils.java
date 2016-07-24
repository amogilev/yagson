package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

public class TestingUtils {

	static final Comparator<String> MY_STRING_CMP = new Comparator<String>() {
        public int compare(String s1, String s2) {
            int cmp = s1.length() - s2.length();
            if (cmp == 0) {
                cmp = s1.compareTo(s2);
            }
            return cmp;
        }
    };
	static final Comparator<Person> MY_PERSON_CMP = new Comparator<Person>() {
        public int compare(Person o1, Person o2) {
            int cmp = MY_STRING_CMP.compare(o1.family, o2.family);
            if (cmp == 0) {
                cmp = MY_STRING_CMP.compare(o1.name, o2.name);
            }
            return cmp;
        }
    };

	private static YaGson buildGson() {
		return buildGson(null);
	}

	private static YaGson buildGson(TypeInfoPolicy typeInfoPolicy) {
		if (typeInfoPolicy == null) {
			return new YaGson();
		} else {
			return new YaGsonBuilder()
					.setTypeInfoPolicy(typeInfoPolicy)
					.create();
		}
	}

	private static <T> T testBinding(Gson gson, T obj, Type deserializationType, String expected,
									 boolean cmpObjectsByToString, boolean cmpObjectsByEquals,
									 boolean skipSecondSerialization, boolean expectNull) {

		String json = gson.toJson(obj, deserializationType);
		assertNotNull(json);
		if (expected != null) {
			assertEquals("toJson(obj) differs from the expected", expected, json);
		}

		Object obj2 = deserialize(gson, json, deserializationType);
		if (obj == null || expectNull) {
			assertNull(obj2);
			return null;
		} else {
			assertNotNull(obj2);
		}

		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		if (obj2.getClass() != obj.getClass()) {
			assertEquals("Deserialized object's class is not equal to the original", obj.getClass(), obj2.getClass());
		}

		if (cmpObjectsByEquals && !objectsEqual(obj, obj2)) {
			assertEquals("Deserialized object is not equal to the original", obj, obj2);
		}
		if (cmpObjectsByToString) {
			String str1, str2;
			if (objClass.isArray() && obj2.getClass().isArray()) {
				str1 = Arrays.deepToString((Object[])obj);
				str2 = Arrays.deepToString((Object[])obj2);
			} else {
				str1 = obj.toString();
				str2 = obj2.toString();
			}
			assertEquals("Deserialized object's toString() is not equal to the original",
					str1, str2);
		}

		if (!skipSecondSerialization) {
			// additionally compare the first and the second serialization strings
			String str2 = gson.toJson(obj2, deserializationType);
			assertEquals("First toJson() differs from the toJson() of deserialized object",
					json, str2);
		}

		return (T)obj2;
	}

	private static <T> T deserialize(Gson gson, String json, Type objType) {
		if (objType == null) {
			objType = Object.class;
		}
		T obj2 = gson.fromJson(json, objType);
		return obj2;
	}

	public static <T> T testDeserialize(String json, Type deserializationType) {
		return deserialize(buildGson(), json, deserializationType);
	}

	/**
	 * Test with single serialization/deserialization action, and with no result comparison, except of the class equality.
     */
	public static <T> T test(T obj) { return test(obj, null); }

	public static <T> T test(T obj, String expected) {
		return testBinding(buildGson(), obj, obj.getClass(), expected, false, false, true, false);
	}

	/**
	 * Test for objects which includes unsorted tests/maps. All comparisons which rely on particular sorting are
	 * skipped.
     */
	public static <T> T testUnsorted(T obj) {
		return testBinding(buildGson(), obj, obj.getClass(), null, false,
				true, true, false);
	}

	public static <T> T testWithNullExpected(T obj, Class<?> deserializationClass, String expectedJson) {
		return testBinding(buildGson(), obj, deserializationClass, expectedJson, false,
				false, true, true);
	}

	public static <T> T testFully(T obj) {
		return testFully(obj, (TypeInfoPolicy) null, null);
	}

	public static <T> T testFully(T obj, String expected) {
		return testFully(obj, (TypeInfoPolicy) null, expected);
	}

	public static <T> T testFully(T obj, Class<?> deserializationClass, String expected) {
		return testBinding(buildGson(), obj, deserializationClass, expected, false, true, false, false);
	}

	public static <T> T testFully(T obj, TypeToken typeToken, String expected) {
		return testBinding(buildGson(), obj, toType(typeToken), expected, false, true, false, false);
	}

	private static Type toType(TypeToken typeToken) {
		return typeToken == null ? null : typeToken.getType();
	}

	public static <T> T testFully(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testBinding(buildGson(typeInfoPolicy), obj, obj.getClass(), expected, false, true, false, false);
	}

	public static <T> T testFully(Gson gson, T obj, String expected) {
		return testBinding(gson, obj, obj.getClass(), expected, false, true, false, false);
	}

	public static <T> T testFullyByToString(T obj) {
		return testFullyByToString(obj, null, null);
	}

	public static <T> T testFullyByToString(T obj, String expected) {
		return testFullyByToString(obj, null, expected);
	}

	public static <T> T testFullyByToString(Gson gson, T obj, String expected) {
		return testBinding(gson, obj, obj.getClass(), expected, true, false, false, false);
	}

	public static <T> T testFullyByToString(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testBinding(buildGson(typeInfoPolicy), obj, obj.getClass(), expected, true, false, false, false);
	}

	/**
	 * Replaces all single quotations marks characters (') to double (") and returns the resulting string.
	 * <p/>
	 * Used to make the expected JSON strings more readable in Java sources.
     */
	public static String jsonStr(String s) {
		return s.replace('\'', '"');
	}

	private static ThreadLocal<IdentityHashMap<Object, Object>> visitedObjectsLocal =
			new ThreadLocal<IdentityHashMap<Object, Object>>();

	/**
	 * Returns whether the object are equal, supports self-referencing arrays.
     */
	public static boolean objectsEqual(Object o1, Object o2) {
		IdentityHashMap<Object, Object> visited = visitedObjectsLocal.get();
		boolean isFirst = false;
		if (visited == null) {
			isFirst = true;
			visited = new IdentityHashMap<Object, Object>();
			visitedObjectsLocal.set(visited);
		}
		try {
			return objectsEqual(o1, o2, visited);
		} finally {
			if (isFirst) {
				visitedObjectsLocal.set(null);
			}
		}
	}

	private static boolean objectsEqual(Object o1, Object o2, IdentityHashMap<Object, Object> visited) {
		if (o1 == o2) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		}

		if (o1.getClass() != o2.getClass()) {
			return false;
		}

		if (visited.containsKey(o1)) {
			return true;
		}
		visited.put(o1, o1);

		if (o1.getClass().isArray()) {
			int l1 = Array.getLength(o1);
			int l2 = Array.getLength(o2);
			if (l1 != l2) {
				return false;
			}
			for (int i = 0; i < l1; i++) {
				Object e1 = Array.get(o1, i);
				Object e2 = Array.get(o2, i);
				if (!objectsEqual(e1, e2, visited)) {
					return false;
				}
			}
			return true;
		} else if (o1 instanceof Collection) {
			Collection c1 = (Collection) o1;
			Collection c2 = (Collection) o2;

			boolean requireOrder = o1 instanceof List || o1 instanceof Queue || o1 instanceof SortedSet;
			if (!requireOrder) {
				return collectionsEqualIgnoreOrder(c1, c2, visited);
			}
			Iterator<Object> i1 = c1.iterator();
			Iterator<Object> i2 = c2.iterator();
			while (i1.hasNext() && i2.hasNext()) {
				Object e1 = i1.next();
				Object e2 = i2.next();
				if (!objectsEqual(e1, e2, visited)) {
					return false;
				}
			}
			return !(i1.hasNext() || i2.hasNext());

		} else if (o1 instanceof Map) {
			Map m1 = (Map) o1;
			Map m2 = (Map) o2;

			if (!(m1 instanceof SortedMap)) {
				return mapsEqualIgnoreOrder(m1, m2, visited);
			}
			Iterator<Map.Entry> i1 = m1.entrySet().iterator();
			Iterator<Map.Entry> i2 = m2.entrySet().iterator();
			while (i1.hasNext() && i2.hasNext()) {
				Map.Entry e1 = i1.next();
				Map.Entry e2 = i2.next();
				if (!objectsEqual(e1.getKey(), e2.getKey(), visited)) {
					return false;
				}
				if (!objectsEqual(e1.getValue(), e2.getValue(), visited)) {
					return false;
				}
			}
			return !(i1.hasNext() || i2.hasNext());

		} else {
			// objects which support self-containing should use objectsEqual() in theirs equals()
			return o1.equals(o2);
		}
	}

	private static boolean collectionsEqualIgnoreOrder(Collection c1, Collection c2,
													  IdentityHashMap<Object, Object> visited) {
		if (c1.size() != c2.size()) {
			return false;
		}
		LinkedList l1 = new LinkedList(c1);
		LinkedList l2 = new LinkedList(c2);
		Comparator hashCodesComparator = new Comparator() {
			@Override
			public int compare(Object o1, Object o2) {
				return o1.hashCode() - o2.hashCode();
			}
		};
		Collections.sort(l1, hashCodesComparator);
		Collections.sort(l2, hashCodesComparator);

		while (!l1.isEmpty()) {
			Object e1 = l1.removeFirst();
			Iterator i2 = l2.iterator();
			boolean equalElementFound = false;
			while (i2.hasNext()) {
				Object e2 = i2.next();
				IdentityHashMap<Object, Object> visitedClone = new IdentityHashMap<Object, Object>(visited);
				if (objectsEqual(e1, e2, visitedClone)) {
					visited.putAll(visitedClone);
					equalElementFound = true;
					i2.remove();
					break;
				}
			}
			if (!equalElementFound) {
				return false;
			}
		}

		return true;
	}

	private static boolean mapsEqualIgnoreOrder(Map m1, Map m2, IdentityHashMap<Object, Object> visited) {
		if (m1.size() != m2.size()) {
			return false;
		}
		LinkedList<Map.Entry> l1 = new LinkedList<Map.Entry>(m1.entrySet());
		LinkedList<Map.Entry> l2 = new LinkedList<Map.Entry>(m2.entrySet());
		Comparator<Map.Entry> keysHashCodesComparator = new Comparator<Map.Entry>() {
			@Override
			public int compare(Map.Entry e1, Map.Entry e2) {
				return e1.getKey().hashCode() - e2.getKey().hashCode();
			}
		};
		Collections.sort(l1, keysHashCodesComparator);
		Collections.sort(l2, keysHashCodesComparator);

		while (!l1.isEmpty()) {
			Map.Entry e1 = l1.removeFirst();
			Iterator<Map.Entry> i2 = l2.iterator();
			boolean equalKeyFound = false;
			while (i2.hasNext()) {
				Map.Entry e2 = i2.next();
				IdentityHashMap<Object, Object> visitedClone = new IdentityHashMap<Object, Object>(visited);
				if (objectsEqual(e1.getKey(), e2.getKey(), visitedClone)) {
					if (!objectsEqual(e1.getValue(), e2.getValue(), visitedClone)) {
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

}
