package am.yagson;

import junit.framework.Assert;

import com.google.gson.Gson;

public class TestingUtils {
	
	public static void testToJson(Object obj) {
		String str = new Gson().toJson(obj);
		Assert.assertNotNull(str);
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T testJsonWithoutEquals(T obj) {
		Gson gson = new Gson();
		
		String str = gson.toJson(obj);
		Assert.assertNotNull(str);
		
		Class<T> objClass = (Class<T>) obj.getClass();
		T obj2 = gson.fromJson(str, objClass);
		Assert.assertNotNull(obj2);
		
		String str2 = gson.toJson(obj2);
		Assert.assertEquals(str, str2);
		
		return obj2;
	}
	
	public static <T> T testFully(T obj) {
		T obj2 = testJsonWithoutEquals(obj);
		Assert.assertEquals(obj, obj2);
		return obj2;
	}
	
	public static <T> T testFullyByToString(T obj) {
		T obj2 = testJsonWithoutEquals(obj);
		Assert.assertEquals(obj.toString(), obj2.toString());
		return obj2;
	}

}
