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

	private static <T> T testBinding(Gson gson, T obj, TypeToken deserializationType, String expected,
									 boolean cmpObjectsByToString, boolean cmpObjectsByEquals) {

		if (deserializationType == null) {
			deserializationType = TypeToken.get(obj.getClass());
		}
		String json = gson.toJson(obj, deserializationType.getType());
		assertNotNull(json);
		if (expected != null) {
			assertEquals("toJson(obj) differs from the expected", expected, json);
		}

		Object obj2 = deserialize(gson, json, deserializationType.getType());
		if (obj == null) {
			assertNull(obj2);
			return null;
		}

		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		assertNotNull(obj2);
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

		// additionally compare the first and the second serialization strings
		String str2 = gson.toJson(obj2, deserializationType.getType());
		assertEquals("First toJson() differs from the toJson() of deserialized object",
				json, str2);

		return (T)obj2;
	}

	private static <T> T deserialize(Gson gson, String json, Type objType) {
		T obj2 = gson.fromJson(json, objType);
		return obj2;
	}

	public static <T> T testDeserialize(Gson gson, String json, T obj) {
		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		T obj2 = deserialize(gson, json, objClass);
		assertEquals(obj, obj2);
		return obj2;
	}


	public static <T> T testFully(T obj) {
		return testFully(obj, (TypeInfoPolicy) null, null);
	}

	public static <T> T testFully(T obj, String expected) {
		return testFully(obj, (TypeInfoPolicy) null, expected);
	}

	public static <T> T testFully(T obj, Class<?> deserializationType, String expected) {
		return testBinding(buildGson(null), obj, TypeToken.get(deserializationType), expected, false, true);
	}

	public static <T> T testFully(T obj, TypeToken typeToken, String expected) {
		return testBinding(buildGson(null), obj, typeToken, expected, false, true);
	}

	public static <T> T testFully(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testBinding(buildGson(typeInfoPolicy), obj, null, expected, false, true);
	}

	public static <T> T testFully(Gson gson, T obj, String expected) {
		return testBinding(gson, obj, null, expected, false, true);
	}

	public static <T> T testFullyByToString(T obj) {
		return testFullyByToString(obj, null, null);
	}

	public static <T> T testFullyByToString(T obj, String expected) {
		return testFullyByToString(obj, null, expected);
	}

	public static <T> T testFullyByToString(Gson gson, T obj, String expected) {
		return testBinding(gson, obj, null, expected, true, false);
	}

	public static <T> T testFullyByToString(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testBinding(buildGson(typeInfoPolicy), obj, null, expected, true, false);
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
		if (o1 == o2) {
			return true;
		} else if (o1 == null || o2 == null) {
			return false;
		}

		if (o1.getClass() != o2.getClass()) {
			return false;
		}

		IdentityHashMap<Object, Object> visited = visitedObjectsLocal.get();
		boolean isFirst = false;
		if (visited == null) {
			isFirst = true;
			visited = new IdentityHashMap<Object, Object>();
			visitedObjectsLocal.set(visited);
		}
		if (visited.containsKey(o1)) {
			return true;
		}
		visited.put(o1, o1);

		try {
			if (o1.getClass().isArray()) {
				int l1 = Array.getLength(o1);
				int l2 = Array.getLength(o2);
				if (l1 != l2) {
					return false;
				}
				for (int i = 0; i < l1; i++) {
					Object e1 = Array.get(o1, i);
					Object e2 = Array.get(o2, i);
					if (!objectsEqual(e1, e2)) {
						return false;
					}
				}
				return true;
			} else if (o1 instanceof Collection) {
				Iterator<Object> i1 = ((Collection) o1).iterator();
				Iterator<Object> i2 = ((Collection) o2).iterator();
				while (i1.hasNext() && i2.hasNext()) {
					Object e1 = i1.next();
					Object e2 = i2.next();
					if (!objectsEqual(e1, e2)) {
						return false;
					}
				}
				return !(i1.hasNext() || i2.hasNext());

			} else if (o1 instanceof Map) {
				Iterator<Map.Entry> i1 = ((Map) o1).entrySet().iterator();
				Iterator<Map.Entry> i2 = ((Map) o2).entrySet().iterator();
				while (i1.hasNext() && i2.hasNext()) {
					Map.Entry e1 = i1.next();
					Map.Entry e2 = i2.next();
					if (!objectsEqual(e1.getKey(), e2.getKey())) {
						return false;
					}
					if (!objectsEqual(e1.getValue(), e2.getValue())) {
						return false;
					}
				}
				return !(i1.hasNext() || i2.hasNext());

			} else {
				// objects which support self-containing should use objectsEqual() in theirs equals()
				return o1.equals(o2);
			}
		} finally {
			if (isFirst) {
				visitedObjectsLocal.set(null);
			}
		}
	}
}
