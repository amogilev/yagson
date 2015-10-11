package am.yagson;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TypeUtils {

    static Map<String, Class<?>> primitiveWrappers = new HashMap<String, Class<?>>();
    static {
        primitiveWrappers.put(boolean.class.getName(), Boolean.class);
        primitiveWrappers.put(byte.class.getName(), Byte.class);
        primitiveWrappers.put(short.class.getName(), Short.class);
        primitiveWrappers.put(int.class.getName(), Integer.class);
        primitiveWrappers.put(long.class.getName(), Long.class);
        primitiveWrappers.put(float.class.getName(), Float.class);
        primitiveWrappers.put(double.class.getName(), Double.class);
    }

    public static boolean typesDiffer(TypeToken<?> fieldType, Class<?> actualClass) {
        Class<?> rawType = fieldType.getRawType();
        return classesDiffer(rawType, actualClass);
    }

    public static boolean typesDiffer(Type type, Class<?> actualClass) {
        if (type instanceof GenericArrayType && actualClass.isArray()) {
            return typesDiffer(((GenericArrayType)type).getGenericComponentType(), actualClass.getComponentType());
        } else {
            return classesDiffer($Gson$Types.getRawType(type), actualClass);
        }
    }

    public static boolean classesDiffer(Class<?> declaredClass, Class<?> actualClass) {
        if (declaredClass == actualClass) {
            return false;
        }
        if (declaredClass.isPrimitive() || actualClass.isPrimitive()) {
            if (declaredClass.isPrimitive() && actualClass.isPrimitive()) {
                return true;
            }
            if (declaredClass.isPrimitive()) {
                return !actualClass.equals(primitiveWrappers.get(declaredClass.getName()));
            } else {
                return !declaredClass.equals(primitiveWrappers.get(actualClass.getName()));
            }
        }
        return true;
    }

    public static <T> T readTypeAdvisedValue(Gson context, JsonReader in, ReferencesReadContext rctx) throws IOException {
        Class valueType = readTypeAdvice(in);
        return readTypeAdvisedValueAfterType(context, in, rctx, valueType);
    }

    public static <T> T readTypeAdvicedValueAfterTypeField(Gson gson, JsonReader in, ReferencesReadContext rctx) throws IOException {
        Class valueType = readTypeAdviceAfterTypeField(in);
        return readTypeAdvisedValueAfterType(gson, in, rctx, valueType);
    }

    public static Class readTypeAdvice(JsonReader in) throws IOException {
        in.beginObject();
        if (!in.hasNext()) {
            throw new JsonSyntaxException("BEGIN_OBJECT is not expected at path " + in.getPath());
        }
        String name = in.nextName();
        if (!name.equals("@type")) {
            throw new JsonSyntaxException("BEGIN_OBJECT is not expected at path " + in.getPath());
        }
        return readTypeAdviceAfterTypeField(in);
    }

    private static Class readTypeAdviceAfterTypeField(JsonReader in) throws IOException {
        // Check whether next tokens are type advise, fail if not
        String advisedTypeStr = in.nextString();
        Class valueType;
        try {
            valueType = Class.forName(advisedTypeStr);
        } catch (ClassNotFoundException e) {
            throw new JsonSyntaxException("Missing class specified in @type info", e);
        }

        return valueType;
    }

    public static void consumeValueField(JsonReader in) throws IOException {
        if (!in.hasNext()) {
            throw new JsonSyntaxException("Expected @val at path " + in.getPath());
        }
        String name = in.nextName();
        if (!name.equals("@val")) {
            throw new JsonSyntaxException("Only @type and @val fields are expected at the type advice " +
                    "objects at path " + in.getPath());
        }
    }

    private static <T> T readTypeAdvisedValueAfterType(Gson context, JsonReader in, ReferencesReadContext rctx, Class valueType) throws IOException {
        consumeValueField(in);

        // use actual type adapter instead of delegate
        T result = (T) context.getAdapter(valueType).read(in, rctx);

        in.endObject();
        return result;
    }

    public static boolean safeClassEquals(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        } else if (obj1 == null || obj2 == null) {
            return false;
        } else {
            return obj1.getClass().equals(obj2.getClass());
        }
    }


}
