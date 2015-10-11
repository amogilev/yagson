package am.yagson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class TestingUtils {
	
	@SuppressWarnings("unchecked")
	public static <T> T testJsonWithoutEquals(T obj) {
		return testJsonWithoutEquals(obj, null, null, false, false);
	}

	private static <T> T testJsonWithoutEquals(T obj, TypeInfoPolicy typeInfoPolicy, String expected,
											   boolean cmpObjectsByToString, boolean cmpObjectsByEquals) {
		GsonBuilder gb = new GsonBuilder();
		if (typeInfoPolicy != null) {
			gb.setTypeInfoPolicy(typeInfoPolicy);
		}
		Gson gson = gb.create();

		String str = gson.toJson(obj);
		assertNotNull(str);
		if (expected != null) {
			assertEquals("toJson(obj) differs from the expected", expected, str);
		}

		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		T obj2 = deserialize(str, objClass);
		assertNotNull(obj2);

		if (cmpObjectsByEquals) {
			assertEquals("Deserialized object is not equal to the original", obj, obj2);
		}
		if (cmpObjectsByToString) {
			assertEquals("Deserialized object's toString() is not equal to the original",
					obj.toString(), obj2.toString());
		}

		// additionally compare the first and the second serialization strings
		String str2 = gson.toJson(obj2);
		assertEquals("First toJson() differs from the toJson() of deserialized object",
				str, str2);

		return obj2;
	}

	private static <T> T deserialize(String json, Class<T> objClass) {
		Gson gson = new Gson();
		T obj2 = gson.fromJson(json, objClass);
		return obj2;
	}

	public static <T> T testDeserialize(String json, T obj) {
		Class<? extends T> objClass = (Class<? extends T>) obj.getClass();
		T obj2 = deserialize(json, objClass);
		assertEquals(obj, obj2);
		return obj2;
	}


	public static <T> T testFully(T obj) {
		return testFully(obj, null, null);
	}

	public static <T> T testFully(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testJsonWithoutEquals(obj, typeInfoPolicy, expected, false, true);
	}

	public static <T> T testFullyByToString(T obj) {
		return testFullyByToString(obj, null, null);
	}

	public static <T> T testFullyByToString(T obj, TypeInfoPolicy typeInfoPolicy, String expected) {
		return testJsonWithoutEquals(obj, typeInfoPolicy, expected, true, false);
	}

}
