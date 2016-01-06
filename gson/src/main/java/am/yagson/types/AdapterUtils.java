package am.yagson.types;

import com.google.gson.Gson;
import com.google.gson.SimpleTypeAdapter;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.internal.bind.TypeAdvisableComplexTypeAdapter;

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

    public static <T> TypeAdvisableComplexTypeAdapter toTypeAdvisable(TypeAdapter<T> typeAdapter) {
        if (typeAdapter instanceof TypeAdvisableComplexTypeAdapter) {
            return (TypeAdvisableComplexTypeAdapter) typeAdapter;
        } else if (typeAdapter instanceof Gson.FutureTypeAdapter) {
            return toTypeAdvisable(((Gson.FutureTypeAdapter)typeAdapter).getDelegate());
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
}
