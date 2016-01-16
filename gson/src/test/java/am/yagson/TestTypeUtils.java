package am.yagson;

import am.yagson.types.TypeUtils;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TestTypeUtils extends TestCase {

    public interface TestParamsClass1<A, B, C> {}
    public interface TestParamsClass2<A, B, C> extends TestParamsClass1<C, B, A> {}
    public static class TestParamsClass3<D> implements TestParamsClass2<Long, D, Integer>{}


    public void testGetParametrizedType1() {
        Type result = TypeUtils.getParameterizedType(TestParamsClass3.class,
                new TypeToken<TestParamsClass1<Integer, String, Long>>() {}.getType());
        assertEquals(new TypeToken<TestParamsClass3<String>>() {}.getType(), result);
    }

    public void testGetParametrizedType2() {
        Type result = TypeUtils.getParameterizedType(HashMap.class, new TypeToken<Map<Long, String>>() {}.getType());
        assertEquals(new TypeToken<HashMap<Long, String>>() {}.getType(), result);
    }

    public void testGetParametrizedType3() {
        Type result = TypeUtils.getParameterizedType(HashMap.class, new TypeToken<HashMap<Long, String>>() {}.getType());
        assertEquals(new TypeToken<HashMap<Long, String>>() {}.getType(), result);
    }
}
