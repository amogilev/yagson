package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

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

	@SuppressWarnings("unchecked")
	public static <T> T testJsonWithoutEquals(T obj, String expected) {
		return testJsonWithoutEquals(buildGson(), obj, expected, false, false);
	}

	private static <T> T testJsonWithoutEquals(Gson gson, T obj, String expected,
											   boolean cmpObjectsByToString, boolean cmpObjectsByEquals) {

		String str = gson.toJson(obj, obj.getClass());
		assertNotNull(str);
		if (expected != null) {
			assertEquals("toJson(obj) differs from the expected", expected, str);
		}

		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		T obj2 = deserialize(gson, str, objClass);
		assertNotNull(obj2);

		if (cmpObjectsByEquals) {
			assertEquals("Deserialized object is not equal to the original", obj, obj2);
		}
		if (cmpObjectsByToString) {
			assertEquals("Deserialized object's toString() is not equal to the original",
					obj.toString(), obj2.toString());
		}

		// additionally compare the first and the second serialization strings
		String str2 = gson.toJson(obj2, obj.getClass());
		assertEquals("First toJson() differs from the toJson() of deserialized object",
				str, str2);

		return obj2;
	}

	private static <T> T deserialize(Gson gson, String json, Class<T> objClass) {
		T obj2 = gson.fromJson(json, objClass);
		return obj2;
	}

	public static <T> T testDeserialize(Gson gson, String json, T obj) {
		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		T obj2 = deserialize(gson, json, objClass);
		assertEquals(obj, obj2);
		return obj2;
	}


	public static <T> T testFully(T obj) {
		return testFully(obj, null, null);
	}

	public static <T> T testFully(T obj, String expected) {
		return testFully(obj, null, expected);
	}

	public static <T> T testFully(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testJsonWithoutEquals(buildGson(typeInfoPolicy), obj, expected, false, true);
	}

	public static <T> T testFully(Gson gson, T obj, String expected) {
		return testJsonWithoutEquals(gson, obj, expected, false, true);
	}

	public static <T> T testFullyByToString(T obj) {
		return testFullyByToString(obj, null, null);
	}

	public static <T> T testFullyByToString(T obj, String expected) {
		return testFullyByToString(obj, null, expected);
	}

	public static <T> T testFullyByToString(Gson gson, T obj, String expected) {
		return testJsonWithoutEquals(gson, obj, expected, true, false);
	}

	public static <T> T testFullyByToString(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testJsonWithoutEquals(buildGson(typeInfoPolicy), obj, expected, true, false);
	}

	/**
	 * Replaces all single quotations marks characters (') to double (") and returns the resulting string.
	 * <p/>
	 * Used to make the expected JSON strings more readable in Java sources.
     */
	public static String jsonStr(String s) {
		return s.replace('\'', '"');
	}
}
