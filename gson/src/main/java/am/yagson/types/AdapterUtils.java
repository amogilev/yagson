package am.yagson.types;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.SimpleTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdapterRuntimeTypeWrapper;
import com.google.gson.internal.bind.TypeAdvisableComplexTypeAdapter;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Utility methods for working with type adapters and delegates.
 */
public class AdapterUtils {

    public static <T> boolean isSimpleTypeAdapter(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof SimpleTypeAdapter) {
            return true;
        } else if (typeAdapter instanceof Gson.FutureTypeAdapter) {
            return isSimpleTypeAdapter(((Gson.FutureTypeAdapter)typeAdapter).getDelegate());
        } else {
            return false;
        }
    }

    public static <T> boolean isTypeAdvisable(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof TypeAdvisableComplexTypeAdapter) {
            return true;
        } else if (typeAdapter instanceof Gson.FutureTypeAdapter) {
            return isTypeAdvisable(((Gson.FutureTypeAdapter)typeAdapter).getDelegate());
        } else {
            return false;
        }
    }

    public static <T> TypeAdvisableComplexTypeAdapter<T> toTypeAdvisable(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof TypeAdvisableComplexTypeAdapter) {
            return (TypeAdvisableComplexTypeAdapter<T>) typeAdapter;
        } else if (typeAdapter instanceof Gson.FutureTypeAdapter) {
            return toTypeAdvisable(((Gson.FutureTypeAdapter<T>)typeAdapter).getDelegate());
        } else {
            throw new IllegalStateException("Not TypeAdvisableComplexTypeAdapter: " + typeAdapter);
        }
    }

    public static <T> boolean isReflective(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof ReflectiveTypeAdapterFactory.Adapter) {
            return true;
        } else if (typeAdapter instanceof Gson.FutureTypeAdapter) {
            return isReflective(((Gson.FutureTypeAdapter)typeAdapter).getDelegate());
        } else {
            return false;
        }
    }

    /**
     * Returns the fields which needs to be saved for the special container object (map, set or collection) except
     * of the standard collection entries, if these fields have non-default values in the actual serialized objects.
     * <p/>
     * For example, non-null comparators in maps/sets need to be saved.
     *
     * @param containerClass the class containing the reflective fields; e.g. HashMap.class
     * @param formalContainerType the formal type of the container, e.g. Map&ltString,String&gt;
     * @param defaultObjectProvider the provider of the default container object for checking the default values
     * @param fieldClassesToFind the field classes to find
     * @param exceptClasses the field classes to skip from search
     */
    public static <T> Map<String, FieldInfo> buildReflectiveFieldsInfo(Gson gson, Class<? extends T> containerClass,
                                                                       Type formalContainerType,
                                                                       ObjectProvider<? extends T> defaultObjectProvider,
                                                                       Iterable<Class<?>> fieldClassesToFind,
                                                                       Iterable<Class<?>> exceptClasses) {
        TypeToken<?> typeToken = TypeToken.get(TypeUtils.getParameterizedType(containerClass, formalContainerType));

        List<Field> fields = TypeUtils.findFields(containerClass, true, fieldClassesToFind, exceptClasses);
        if (fields.isEmpty()) {
            return Collections.emptyMap();
        }

        T defaultInstance = defaultObjectProvider.get();

        Map<String, FieldInfo> result = new LinkedHashMap<String, FieldInfo>(fields.size());

        for (Field f : fields) {
            String fname = f.getName();
            if (result.containsKey(fname)) {
                // skip duplicate names, get/set only 'latest' versions (closest to the actual class)
                continue;
            }
            f.setAccessible(true);

            TypeToken<?> fieldType = TypeToken.get($Gson$Types.resolve(typeToken.getType(), containerClass, f.getGenericType()));
            TypeAdapter<Object> fieldAdapter = new TypeAdapterRuntimeTypeWrapper(gson, gson.getAdapter(fieldType),
                    fieldType.getType(), TypeUtils.getEmitTypeInfoRule(gson, false));

            Object defaultValue;
            try {
                defaultValue = f.get(defaultInstance);
            } catch (IllegalAccessException e) {
                throw new JsonSyntaxException("Failed to get the Map reflective field value; mapType=" + containerClass +
                        "; field=" + f);
            }
            result.put(fname, new FieldInfo(f, defaultValue, fieldAdapter));
        }

        return result;
    }

}
