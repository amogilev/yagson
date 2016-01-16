package am.yagson.types;

import am.yagson.refs.ReferencesReadContext;
import am.yagson.refs.ReferencesWriteContext;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Collections.singleton;

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

    public static <T> T readTypeAdvisedValue(Gson context, JsonReader in, Type formalType,
                                             ReferencesReadContext rctx) throws IOException {
        Class valueClass = readTypeAdvice(in);
        Type type = TypeUtils.getParameterizedType(valueClass, formalType);
        return readTypeAdvisedValueAfterType(context, in, rctx, type);
    }

    public static <T> T readTypeAdvisedValueAfterTypeField(Gson gson, JsonReader in, Type formalType,
                                                           ReferencesReadContext rctx) throws IOException {
        Class valueClass = readTypeAdviceAfterTypeField(in);
        Type type = TypeUtils.getParameterizedType(valueClass, formalType);
        return readTypeAdvisedValueAfterType(gson, in, rctx, type);
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

    public static Class readTypeAdviceAfterTypeField(JsonReader in) throws IOException {
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

    private static <T> T readTypeAdvisedValueAfterType(Gson context, JsonReader in, ReferencesReadContext rctx,
                                                       Type valueType) throws IOException {
        consumeValueField(in);

        // use actual type adapter instead of delegate
        T result = (T) context.getAdapter(TypeToken.get(valueType)).read(in, rctx);

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

    /**
     * Determines whether the type information is necessary for successful de-serialization of the
     * object of the given actual class using the specified de-serialization type.
     * <p/>
     * Usually, the type information is <b>not</b> needed when the de-serialization type matches
     * the actual type, or if the actual type is <i>default</i> for the corresponding JSON representation.
     *
     * @param actualClass the actual class of the object being serialized
     * @param deserializationType the type which will be used for de-serialization, or {@code null} if not known
     * @param isMapKey whether the object is a key of some map
     *
     * @return whether the root type information is necessary
     */
    public static boolean isTypeInfoRequired(Class<?> actualClass, Type deserializationType, boolean isMapKey) {
        if (isDefaultDeserializationClass(actualClass, deserializationType, isMapKey)) {
            return false;
        } else if (deserializationType == null) {
            return true;
        } else {
            return typesDiffer(deserializationType, actualClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<?>> generalDefaultClasses = new HashSet<Class<?>>(Arrays.asList(
            String.class, Object.class, Double.class, double.class,
            Boolean.class, boolean.class, Long.class, long.class
    ));

    /**
     * Returns whether the actual class of an object is the default class for the corresponding
     * JSON representation, and so the type info may be skipped.
     */
    private static boolean isDefaultDeserializationClass(Class<?> actualClass, Type deserializationType,
                                                         boolean isMapKey) {
        // check if the class is default for all types
        if (isMapKey) {
            // as all keys are serialized as names, only actual string are default
            if (actualClass == String.class) {
                return true;
            }
        } else {
            if (generalDefaultClasses.contains(actualClass)) {
                return true;
            }
        }

        if (deserializationType == null) {
            return false;
        }

        if (isMapKey) {
            // the general long and double are default even for map keys only if the type is Number
            if (deserializationType == Number.class && (actualClass == Long.class || actualClass == Double.class)) {
                return true;
            }
        } else {
            // check some common collection cases (only the most common)
            // MUST be in sync with ConstructorConstructor.newDefaultImplementationConstructor() && ObjectTypeAdapter
            Class<?> rawType = $Gson$Types.getRawType(deserializationType);
            if (actualClass == ArrayList.class &&
                    (rawType == Object.class || rawType == Collection.class || rawType == List.class)) {
                return true;
            }
        }

        return false;
    }

    public static <T> void writeTypeWrapperAndValue(JsonWriter out, T value, TypeAdapter adapter,
                                                    ReferencesWriteContext rctx) throws IOException {
        out.beginObject();
        out.name("@type");
        out.value(value.getClass().getName());
        out.name("@val");
        adapter.write(out, value, rctx);
        out.endObject();
    }

    /**
     * Returns whether the provided class is the general class (i.e. not interface, enum, array or primitive) and
     * is not abstract.
     */
    public static boolean isGeneralNonAbstractClass(Class<?> c) {
        return c != null && !c.isInterface() && !c.isArray() && !c.isEnum() && !c.isPrimitive() &&
                !Modifier.isAbstract(c.getModifiers());
    }

    /**
     * Returns whether the class or one of its superclasses contains a field assignable to either of the specified
     * classes.
     *
     * @param c the class to check
     * @param fieldClassesToCheck the supposed classes for the fields of the class to check
     * @return if any field of the class to check belongs to any of the supposed field classes, return {@code true}
     */
    public static boolean containsField(Class<?> c, Class<?>...fieldClassesToCheck) {
        while (c != Object.class && c != null) {
            for (Field field : c.getDeclaredFields()) {
                for (Class<?> fc : fieldClassesToCheck) {
                    if (fc.isAssignableFrom(field.getType())) {
                        return true;
                    }
                }
            }
            c = c.getEnclosingClass();
        }
        return false;
    }

    /**
     * If possible, enriches the raw class type with the type parameters obtained from the super type
     * information.
     * <p/>
     * For example, if rawType is {@code HashMap.class} and the super-type is {@code Map&lt;String, String&gt;},
     * this method shall create and return the type of {@code HashMap&lt;String, String&gt;}
     *
     * @param rawType the raw class type
     * @param superType the type which is a super (or same level) type to the raw class type
     *
     * @return the raw type enriched with the parameter types obtained from the super-type if possible, or
     * the rawType otherwise
     */
    public static Type getParameterizedType(Class<?> rawType, Type superType) {
        if (superType == null || !(superType instanceof ParameterizedType)) {
            return rawType;
        }
        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        if (typeParameters.length == 0) {
            // no parameter types
            return rawType;
        }
        ParameterizedType parameterizedSuperType = (ParameterizedType) superType;
        Class<?> rawSuperType = $Gson$Types.getRawType(parameterizedSuperType.getRawType());
        if (!rawSuperType.isAssignableFrom(rawType)) {
            // illegal use - superType is not a super type for the raw type
            return rawType;
        }

        if (rawType == rawSuperType) {
            return parameterizedSuperType;
        }

        Type[] resolvedTypeArgs = new Type[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<?> typeVar = typeParameters[i];
            Collection<Type> lookupSuperTypes = getSuperTypes(rawType);
            Type resolvedType = lookupTypeArg(typeVar, lookupSuperTypes, parameterizedSuperType);
            resolvedTypeArgs[i] = resolvedType == null ? typeVar : resolvedType;
        }

        return $Gson$Types.newParameterizedTypeWithOwner(rawType.getDeclaringClass(), rawType, resolvedTypeArgs);
    }

    private static Collection<Type> getSuperTypes(Class<?> rawType) {
        Type[] genericInterfaces = rawType.getGenericInterfaces();
        Type genericSuperclass = rawType.getGenericSuperclass();

        List<Type> superTypes = new ArrayList<Type>(genericInterfaces.length + 1);
        superTypes.addAll(Arrays.asList(genericInterfaces));
        if (genericSuperclass != null) {
            superTypes.add(genericSuperclass);
        }
        return superTypes;
    }

    private static Type lookupTypeArg(TypeVariable<?> typeVar, Collection<Type> lookupSuperTypes,
                                      ParameterizedType knownParameterizedSuperType) {
        for (Type lookupType : lookupSuperTypes) {
            if (lookupType instanceof ParameterizedType) {
                Type[] typeArguments = ((ParameterizedType) lookupType).getActualTypeArguments();
                int foundIdx = -1;
                for (int i = 0; i < typeArguments.length; i++) {
                    if (typeVar == typeArguments[i]) {
                        foundIdx = i;
                        break;
                    }
                }

                if (foundIdx < 0) {
                    // not matched to any of type parameters of this lookup super-type, try next
                    continue;
                }

                Class<?> rawLookupType = $Gson$Types.getRawType(lookupType);
                if (rawLookupType == $Gson$Types.getRawType(knownParameterizedSuperType)) {
                    // matched up to the original parameterized super-type, use it
                    return knownParameterizedSuperType.getActualTypeArguments()[foundIdx];
                }

                // else continue lookup on a higher level
                Type resolved = lookupTypeArg(rawLookupType.getTypeParameters()[foundIdx], getSuperTypes(rawLookupType),
                        knownParameterizedSuperType);
                if (resolved != null) {
                    return resolved;
                }
            }
        }

        return null;
    }

    /**
     * Checks whether the input string would be treated as a long by the Gson's parsing algorithms,
     * see {@link JsonReader#peekNumber()}. The current rules are: the string should contain only digits and
     * optional sign, with no decimal point or exponents, and the resulting value should fit to long range.
     * <p/>
     * If approved to be treated as long, parses and returns the number; otherwise, returns {@code null}
     */
    public static Long parseNumberIfLong(String str) {
        if (str.indexOf('.') >= 0 || str.indexOf('E') >= 0 || str.indexOf('e') >= 0) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static EmitTypeInfoPredicate TYPE_INFO_SKIP = new EmitTypeInfoPredicate() {
        public boolean apply(Class<?> actualClass, Type formalType) {
            return false;
        }
    };

    private static EmitTypeInfoPredicate TYPE_INFO_GENERAL_RULE = new EmitTypeInfoPredicate() {
        public boolean apply(Class<?> actualClass, Type formalType) {
            return isTypeInfoRequired(actualClass, formalType, false);
        }
    };

    private static EmitTypeInfoPredicate TYPE_INFO_MAP_KEY_RULE = new EmitTypeInfoPredicate() {
        public boolean apply(Class<?> actualClass, Type formalType) {
            return isTypeInfoRequired(actualClass, formalType, true);
        }
    };

    public static EmitTypeInfoPredicate getEmitTypeInfoRule(Gson context, boolean isMapKey) {
        return !context.getTypeInfoPolicy().isEnabled() ? TYPE_INFO_SKIP :
                (isMapKey ? TYPE_INFO_MAP_KEY_RULE : TYPE_INFO_GENERAL_RULE);
    }
}
