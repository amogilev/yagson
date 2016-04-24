package am.yagson;

import am.yagson.types.TypeInfoPolicy;
import am.yagson.types.TypeUtils;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

public class TestTypeUtils extends TestCase {

    public interface TestParamsClass1<A, B, C> {}
    public interface TestParamsClass2<A, B extends String, C extends Number> extends TestParamsClass1<C, B, A> {}
    public static class TestParamsClass3<D extends String> implements TestParamsClass2<Long, D, Integer>{}

    public void testIndexOfInheritedTypeVariable() {
        TypeVariable[] typeVariables = TestParamsClass1.class.getTypeParameters();
        TypeVariable class1VarA = typeVariables[0];
        TypeVariable class1VarB = typeVariables[1];
        TypeVariable class1VarC = typeVariables[2];

        assertEquals(2, TypeUtils.indexOfInheritedTypeVariable(
                class1VarA, TestParamsClass1.class, TestParamsClass2.class));

        assertEquals(1, TypeUtils.indexOfInheritedTypeVariable(
                class1VarB, TestParamsClass1.class, TestParamsClass2.class));

        assertEquals(0, TypeUtils.indexOfInheritedTypeVariable(
                class1VarC, TestParamsClass1.class, TestParamsClass2.class));

        assertEquals(-1, TypeUtils.indexOfInheritedTypeVariable(
                class1VarA, TestParamsClass1.class, TestParamsClass3.class));

        assertEquals(0, TypeUtils.indexOfInheritedTypeVariable(
                class1VarB, TestParamsClass1.class, TestParamsClass3.class));

        assertEquals(-1, TypeUtils.indexOfInheritedTypeVariable(
                class1VarC, TestParamsClass1.class, TestParamsClass3.class));

        assertEquals(0, TypeUtils.indexOfInheritedTypeVariable(
                class1VarA, TestParamsClass1.class, TestParamsClass1.class));

    }

    public void testGetParametrizedType1() {
        Type result = TypeUtils.mergeTypes(TestParamsClass3.class,
                new TypeToken<TestParamsClass1<Integer, String, Long>>() {}.getType());
        assertEquals(new TypeToken<TestParamsClass3<String>>() {}.getType(), result);
    }

    public void testGetParametrizedType2() {
        Type result = TypeUtils.mergeTypes(HashMap.class, new TypeToken<Map<Long, String>>() {}.getType());
        assertEquals(new TypeToken<HashMap<Long, String>>() {}.getType(), result);
    }

    public void testGetParametrizedType3() {
        Type result = TypeUtils.mergeTypes(HashMap.class, new TypeToken<HashMap<Long, String>>() {}.getType());
        assertEquals(new TypeToken<HashMap<Long, String>>() {}.getType(), result);
    }

    public void testMergeTypes1() {
        Type result = TypeUtils.mergeTypes(
                new TypeToken<EnumMap<TypeInfoPolicy, ?>>() {}.getType(),
                new TypeToken<Map<TypeInfoPolicy, String>>() {}.getType());
        assertEquals(new TypeToken<EnumMap<TypeInfoPolicy, String>>() {}.getType(), result);
    }

    public void testMergeTypes2() {
        Type result = TypeUtils.mergeTypes(
                new TypeToken<EnumMap<TypeInfoPolicy, ?>>() {}.getType(),
                new TypeToken<Map<?, String>>() {}.getType());
        assertEquals(new TypeToken<EnumMap<TypeInfoPolicy, String>>() {}.getType(), result);
    }

    public void testMergeTypes3() {
        Type result = TypeUtils.mergeTypes(
                new TypeToken<EnumMap<TypeInfoPolicy, ?>>() {}.getType(),
                Object.class);
        assertEquals(new TypeToken<EnumMap<TypeInfoPolicy, ?>>() {}.getType(), result);
    }

    public void testMergeTypes4() {
        Type result = TypeUtils.mergeTypes(
                EnumMap.class,
                new TypeToken<Map<?, String>>() {}.getType());
        assertEquals(new TypeToken<EnumMap<?, String>>() {}.getType(), result);
    }

    public void testMergeTypes5() {
        Type result = TypeUtils.mergeTypes(
                EnumMap.class,
                Map.class);
        assertEquals(EnumMap.class, result);
    }

    public void testIsMethodOverridden() throws NoSuchMethodException {
        Method abstractMapPut = AbstractMap.class.getDeclaredMethod("put", Object.class, Object.class);
        assertFalse(TypeUtils.isOverridden(AbstractMap.class, abstractMapPut));
        assertFalse(TypeUtils.isOverridden(SortedMap.class, abstractMapPut));
        assertFalse(TypeUtils.isOverridden(Collections.singletonMap("", "").getClass(), abstractMapPut));
        assertTrue(TypeUtils.isOverridden(TreeMap.class, abstractMapPut));
        assertFalse(TypeUtils.isOverridden(Dictionary.class, abstractMapPut));
        assertFalse(TypeUtils.isOverridden(Hashtable.class, abstractMapPut));
    }
}
