package com.gilecode.yagson.types;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.bind.AdapterUtils;
import com.google.gson.internal.bind.MapTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

import static java.util.Arrays.asList;

public class TypeUtils {

    private static Map<String, Class<?>> primitiveWrappers = new HashMap<String, Class<?>>();
    static {
        primitiveWrappers.put(boolean.class.getName(), Boolean.class);
        primitiveWrappers.put(byte.class.getName(), Byte.class);
        primitiveWrappers.put(short.class.getName(), Short.class);
        primitiveWrappers.put(int.class.getName(), Integer.class);
        primitiveWrappers.put(long.class.getName(), Long.class);
        primitiveWrappers.put(float.class.getName(), Float.class);
        primitiveWrappers.put(double.class.getName(), Double.class);
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
                                             ReadContext ctx) throws IOException {
        Type advisedType = readTypeAdvice(in);
        Type type = TypeUtils.mergeTypes(advisedType, formalType);
        return readTypeAdvisedValueAfterType(context, in, ctx, type);
    }

    public static <T> T readTypeAdvisedValueAfterTypeField(Gson gson, JsonReader in, Type formalType,
                                                           ReadContext ctx) throws IOException {
        Type advisedType = readTypeAdviceAfterTypeField(in);
        Type type = TypeUtils.mergeTypes(advisedType, formalType);
        return readTypeAdvisedValueAfterType(gson, in, ctx, type);
    }

    public static Type readTypeAdvice(JsonReader in) throws IOException {
        in.beginObject();
        if (!in.hasNext()) {
            throw new JsonSyntaxException("BEGIN_OBJECT is not expected at path " + in.getPath());
        }
        String name = in.nextName();
        if (!name.equals("@type")) {
            throw new JsonSyntaxException("@type is expected at path " + in.getPath());
        }
        return readTypeAdviceAfterTypeField(in);
    }

    private static final WildcardType unknownType = $Gson$Types.subtypeOf(Object.class);

    public static Type readTypeAdviceAfterTypeField(JsonReader in) throws IOException {
        // Check whether next tokens are type advise, fail if not
        String advisedTypeStr = in.nextString();
        String advisedClassStr = advisedTypeStr;

        int i = advisedTypeStr.indexOf('<');
        if (i >= 0) {
            if (!advisedTypeStr.endsWith(">")) {
                throw new JsonSyntaxException("Incorrect advised type: '" + advisedTypeStr + "'");
            }
            advisedClassStr = advisedTypeStr.substring(0, i).trim();

            String parametersStr = advisedTypeStr.substring(i+ 1, advisedTypeStr.length() - 1).trim();
            String[] parameters = parametersStr.split(",");
            Type[] parameterTypes = new Type[parameters.length];
            boolean hasParameterTypes = false;
            for (int j = 0; j < parameters.length; j++) {
                Type type = toType(parameters[j].trim());
                parameterTypes[j] = type;
                if (type != unknownType) {
                    hasParameterTypes = true;
                }
            }

            if (hasParameterTypes) {
                Class advisedClass = (Class) toType(advisedClassStr);
                return $Gson$Types.newParameterizedTypeWithOwner(advisedClass.getEnclosingClass(), advisedClass, parameterTypes);
            }
        }
        Type valueType = toType(advisedClassStr);
        return valueType;
    }

    private static Type toType(String name) {
        if (name.equals("?")) {
            return unknownType;
        }
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new JsonSyntaxException("Missing class specified in @type info", e);
        }
    }

    /**
     * Runs {@link Class#forName(String)}, with a wrapping of {@link ClassNotFoundException} to
     * {@link IllegalStateException}, which allows to use it in initializers and other places where checked
     * exceptions are not allowed.
     */
    public static Class<?> classForName(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Missing class specified in @type info", e);
        }
    }

    public static boolean consumeValueField(JsonReader in) throws IOException {
        if (!in.hasNext()) {
            // no @val means actually null value, e.g. skipped by serialization
            return false;
        }
        String name = in.nextName();
        if (!name.equals("@val")) {
            throw new JsonSyntaxException("Only @type and @val fields are expected at the type advice " +
                    "objects at path " + in.getPath());
        }
        return true;
    }

    private static <T> T readTypeAdvisedValueAfterType(Gson gson, JsonReader in, ReadContext ctx,
                                                       Type valueType) throws IOException {
        T result = null;
        if (consumeValueField(in)) {
            // use actual type adapter instead of delegate
            TypeAdapter<?> typeAdapter = gson.getAdapter(TypeToken.get(valueType));
            result = (T) typeAdapter.read(in, ctx);
        }

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
        boolean isEnumSet = EnumSet.class.isAssignableFrom(actualClass);
        boolean isEnumCollection = isEnumSet || EnumMap.class.isAssignableFrom(actualClass);
        if (isEnumCollection) {
            // for EnumSet and EnumMap additionally check that the enum element type is specified in the deserialization type
            // also, consider RegularEnumSet and JumboEnumSet to be the default deserializaton classes for EnumSet
            if (deserializationType == null) {
                return true;
            }
            Class<?> rawType = $Gson$Types.getRawType(deserializationType);

            Type enumType = isEnumSet ? $Gson$Types.getCollectionElementType(deserializationType, rawType) :
                    $Gson$Types.getMapKeyAndValueTypes(deserializationType, rawType)[0];
            if (enumType == null || !$Gson$Types.getRawType(enumType).isEnum()) {
                return true;
            }
            if (isEnumSet) {
                // EnumSet may be extended only by same-package classes, i.e. RegularEnumSet and JumboEnumSet
                // The implementation class is chosen automatically, so no specification required
                return !EnumSet.class.isAssignableFrom(rawType);
            } else {
                // EnumMap may potentially be extended
                return typesDiffer(deserializationType, actualClass);
            }
        } else if (isDefaultDeserializationClass(actualClass, deserializationType, isMapKey)) {
            return false;
        } else if (deserializationType == null) {
            return true;
        } else if (Enum.class.isAssignableFrom(actualClass) && $Gson$Types.getRawType(deserializationType).isEnum()) {
            // ignore synthetic enum classes generated for the enum values with overridden methods
            return false;
        } else {
            return typesDiffer(deserializationType, actualClass);
        }
    }

    @SuppressWarnings("unchecked")
    private static Set<Class<?>> generalDefaultClasses = new HashSet<Class<?>>(asList(
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

    public static void writeTypeWrapperAndValue(JsonWriter out, Object value, TypeAdapter adapter,
                                                    WriteContext ctx) throws IOException {
        Class<?> actualClass = value.getClass();
        String parameterTypes = "";
        if (EnumSet.class.isAssignableFrom(actualClass)) {
            Class<? extends Enum> enumClass = getField(enumSetElementTypeField, value);
            // change the written class to EnumSet (from RegularEnumSet/JumboEnumSet)
            actualClass = EnumSet.class;
            // write parameter for EnumSet type
            parameterTypes = "<" + enumClass.getName() + ">";
            // replace the used adapter for the one with known element type
            Type enumSetType = $Gson$Types.newParameterizedTypeWithOwner(null, actualClass, enumClass);
            adapter = ctx.getGson().getAdapter(TypeToken.get(enumSetType));
        } else if (EnumMap.class.isAssignableFrom(actualClass)) {
            Class<? extends Enum> enumClass = getField(enumMapKeyTypeField, value);
            int mapKeyTypeVarIdx = -1;
            TypeVariable[] actualClassTypeVariables = actualClass.getTypeParameters();
            if (EnumMap.class.equals(actualClass)) {
                mapKeyTypeVarIdx = 0;
                parameterTypes = "<" + enumClass.getName() + ",?>";
            } else {
                // the parameters may be overridden in subclasses
                // print the parameters, only if the enum (key) type is still present in the parameters lists of the actuall class
                TypeVariable mapKeyTypeVar = EnumMap.class.getTypeParameters()[0];
                mapKeyTypeVarIdx = indexOfInheritedTypeVariable(mapKeyTypeVar, EnumMap.class, actualClass);
                if (mapKeyTypeVarIdx >= 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<");
                    for (int i = 0; i < actualClassTypeVariables.length; i++) {
                        if (i == mapKeyTypeVarIdx) {
                            sb.append(enumClass.getName());
                        } else {
                            sb.append('?');
                        }
                        sb.append(',');
                    }
                    sb.setCharAt(sb.length() - 1, '>'); // delete last ',' and add '>'
                    parameterTypes = sb.toString();
                }
            }

            if (mapKeyTypeVarIdx >= 0) {
                TypeAdapter resolvedAdapter = AdapterUtils.resolve(adapter, value);
                if (resolvedAdapter instanceof MapTypeAdapterFactory.Adapter) {
                    Type formalMapType = ((MapTypeAdapterFactory.Adapter) resolvedAdapter).getFormalMapType();
                    Type[] typeArgsWithExactKeyType = new Type[actualClassTypeVariables.length];
                    Arrays.fill(typeArgsWithExactKeyType, unknownType);
                    typeArgsWithExactKeyType[mapKeyTypeVarIdx] = enumClass;

                    Type mergedMapType = mergeTypes(
                            $Gson$Types.newParameterizedTypeWithOwner(actualClass.getEnclosingClass(), actualClass, typeArgsWithExactKeyType),
                            formalMapType);
                    adapter = ctx.getGson().getAdapter(TypeToken.get(mergedMapType));
                }
            }  // else key type is already fixed in subtypes, so do not print params and do not update adapter
        }

        // do not print type wrapper if the value is known to be skipped

        boolean isValueSkipped = AdapterUtils.isSkipSerializeTypeAdapter(adapter);
        if (!isValueSkipped) {
            out.beginObject();
            out.name("@type");
            out.value(actualClass.getName() + parameterTypes);
            out.name("@val");
        }

        adapter.write(out, value, ctx);
        if (!isValueSkipped) {
            out.endObject();
        }
    }

    public static Field getDeclaredField(Class declaringClass, String fieldName) {
        try {
            Field f = declaringClass.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Field '" + fieldName + "' is not found in " + declaringClass, e);
        }
    }

    /**
     * Gets field by name, declared in the class or its superclasses.
     */
    public static Field findField(Class c, String fieldName) {
        while (c != null) {
            for (Field f : c.getDeclaredFields()) {
                if (fieldName.equals(f.getName())) {
                    // found
                    f.setAccessible(true);
                    return f;
                }
            }
            c = c.getSuperclass();
        }
        return null;
    }

    public static <T> T getField(Field f, Object instance) {
        try {
            return (T) f.get(instance);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Failed to obtain the value of field " + f + " from object " + instance, e);
        }
    }

    private static final Field enumSetElementTypeField = getDeclaredField(EnumSet.class, "elementType");
    private static final Field enumMapKeyTypeField = getDeclaredField(EnumMap.class, "keyType");

    /**
     * Returns whether the provided class is the general class (i.e. not interface, enum, array or primitive) and
     * is not abstract.
     */
    public static boolean isGeneralNonAbstractClass(Class<?> c) {
        return c != null && !c.isInterface() && !c.isArray() && !c.isEnum() && !c.isPrimitive() &&
                !Modifier.isAbstract(c.getModifiers());
    }

    /**
     * Find in the given class and all its superclasses all non-static fields assignable to the specified
     * classes to find, except of the specified 'except' classes, and returns such fields as a list.
     *
     * @param c the class to check
     * @param allowTransient whether to check transient fields
     * @param max the max number of the fields to return, or 0 if unlimited
     * @param fieldClassesToFind the field classes to find
     * @param exceptClasses the field classes to skip from search
     *
     * @return all fields of the supposed field classes
     */
    private static List<Field> findFields(Class<?> c, boolean allowTransient, int max,
                                          Iterable<Class<?>> fieldClassesToFind,
                                          Iterable<Class<?>> exceptClasses) {
        List<Field> result = new ArrayList<Field>(max > 0 && max < 10 ? max : 10);
        while (c != Object.class && c != null) {
            for (Field field : c.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (!allowTransient && Modifier.isTransient(field.getModifiers())) {
                    continue;
                }
                findClassesLoop:
                for (Class<?> fc : fieldClassesToFind) {
                    if (fc.isAssignableFrom(field.getType())) {
                        if (exceptClasses != null) {
                            for (Class ec : exceptClasses) {
                                if (ec == field.getType()) {
                                    continue findClassesLoop;
                                }
                            }
                        }
                        result.add(field);
                        break;
                    }
                }
            }
            c = c.getSuperclass();

            // TODO: maybe also check enclosing class for non-static local classes, but it complicates reflective gets
        }
        return result;
    }

    /**
     * Find in the given class and all its superclasses all fields assignable to the specified
     * classes to find, and returns such fields as a list.
     *
     * @param c the class to check
     * @param fieldClassesToFind the field classes to find
     * @param exceptClasses the field classes to skip from search
     * @return all fields of the supposed field classes
     */
    public static List<Field> findFields(Class<?> c, boolean allowTransient, Iterable<Class<?>> fieldClassesToFind,
                                         Iterable<Class<?>> exceptClasses) {
        return findFields(c, allowTransient, 0, fieldClassesToFind, exceptClasses);
    }

    public static Field findOneFieldByType(Class<?> c, Class<?> fieldClassToFind) {
        List<Class<?>> fieldClassesToFind = Collections.<Class<?>>singletonList(fieldClassToFind);
        List<Field> found = findFields(c, true, 1, fieldClassesToFind, null);
        if (found.size() > 0) {
            Field foundField = found.get(0);
            foundField.setAccessible(true);
            return foundField;
        }

        return null;
    }

    /**
     * Returns whether the class or one of its superclasses contains a non-static field assignable to
     * one of the specified 'find' classes, but except of the specified 'except' classes.
     *
     * @param c the class to check
     * @param allowTransient whether to check transient fields
     * @param fieldClassesToFind the field classes to find
     * @param exceptClasses the field classes to skip from search
     * @return if any field of the class to check belongs to any of the supposed field classes, return {@code true}
     */
    public static boolean containsField(Class<?> c, boolean allowTransient,
                                        Iterable<Class<?>> fieldClassesToFind,
                                        Iterable<Class<?>> exceptClasses) {
        List<Field> found = findFields(c, allowTransient, 1, fieldClassesToFind, exceptClasses);
        return !found.isEmpty();
     }

    /**
     * Returns whether the class overrides the specified method, i.e. the class or one of its superclasses until
     * the declaring class of the method, contains the method with the same signature.
     *
     * @param c the class to check
     * @param m the method to check
     *
     * @return if the method is overridden
     */
    public static boolean isOverridden(Class<?> c, Method m) throws NoSuchMethodException {
        Method foundMethod = c.getMethod(m.getName(), m.getParameterTypes());
        return foundMethod != null && !m.equals(foundMethod)
                && !Modifier.isAbstract(foundMethod.getModifiers())
                && m.getDeclaringClass().isAssignableFrom(foundMethod.getDeclaringClass());
    }

    /**
     * Merges the exact raw type and optional parameter types information from the formal type (which should be
     * same type or one of the supertypes) into a single type.
     * If the parameter types are present, the resulting type is {@link ParameterizedType}, otherwise it is the
     * raw type (i.e. Class).
     * <p/>
     * The raw type is always taken from rawTypeSource, the parameter types may be taken from both sources and
     * then merged.
     * <p/>
     * Used to enrich the raw class (mostly from advised type information) with the type parameters available in
     * the formal deserizalization type. For example, if the advised type is {@code HashMap.class} and the formal
     * type is {@code Map&lt;String, String&gt;}, this method shall create and return the type of
     * {@code HashMap&lt;String, String&gt;}
     *
     * @param rawTypeSource the source of the raw class type, with optional parameter types
     * @param parametersTypesSource the type which is used as a source of the parameter types
     *
     * @return the result of merging both types, with a raw type equal to {@code rawTypeSource.} which raw tyraw type enriched with the parameter types obtained from the super-type if possible, or
     * the rawType otherwise
     */
    public static Type mergeTypes(Type rawTypeSource, Type parametersTypesSource) {
        if (parametersTypesSource == null || !(parametersTypesSource instanceof ParameterizedType)) {
            return rawTypeSource;
        }
        Class rawType = $Gson$Types.getRawType(rawTypeSource);
        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
        if (typeParameters.length == 0) {
            // no parameter types
            return rawTypeSource;
        }
        ParameterizedType parametersSource = (ParameterizedType) parametersTypesSource;
        Class<?> parametersSourceRawType = $Gson$Types.getRawType(parametersSource.getRawType());
        if (!parametersSourceRawType.isAssignableFrom(rawType)) {
            // illegal use - superType is not a super type for the raw type
            return rawTypeSource;
        }

        if (rawTypeSource == parametersSourceRawType) {
            return parametersTypesSource;
        }

        // if rawTypeSource is ParameterizedType too, use it as an additional parameters source
        ParameterizedType extraParametersSource = rawTypeSource instanceof ParameterizedType ?
                (ParameterizedType)rawTypeSource : null;

        Type[] resolvedTypeArgs = new Type[typeParameters.length];
        for (int i = 0; i < typeParameters.length; i++) {
            TypeVariable<?> typeVar = typeParameters[i];
            Collection<Type> lookupSuperTypes = getGenericSuperTypes(rawType);
            Type resolvedType = lookupTypeArg(typeVar, lookupSuperTypes, parametersSource);
            if (extraParametersSource != null && (resolvedType == null ||
                    resolvedType instanceof WildcardType || resolvedType instanceof TypeVariable)) {
                // prefer alternative parameters source
                resolvedType = extraParametersSource.getActualTypeArguments()[i];
            }
            resolvedTypeArgs[i] = resolvedType == null ? typeVar : resolvedType;
        }

        return $Gson$Types.newParameterizedTypeWithOwner(rawType.getEnclosingClass(), rawType, resolvedTypeArgs);
    }

    private static Collection<Type> getGenericSuperTypes(Class<?> rawType) {
        Type[] genericInterfaces = rawType.getGenericInterfaces();
        Type genericSuperclass = rawType.getGenericSuperclass();

        List<Type> superTypes = new ArrayList<Type>(genericInterfaces.length + 1);
        if (genericSuperclass != null) {
            superTypes.add(genericSuperclass);
        }
        superTypes.addAll(asList(genericInterfaces));
        return superTypes;
    }

    public static int indexOfInheritedTypeVariable(TypeVariable superTypeVar, Class rawSuperType, Class inheritedClass) {
        // build the inheritance chain of supertypes, ending on the generic version of supertype
        List<Type> inheritanceChain = new ArrayList<Type>();
        fillGenericInheritanceChain(inheritanceChain, rawSuperType, inheritedClass);

        // iterate the chain in the reverse order, matching the interested type variable on each step
        Collections.reverse(inheritanceChain);
        TypeVariable var = superTypeVar;
        for (Type t : inheritanceChain) {
            if (!(t instanceof ParameterizedType)) {
                return -1;
            }

            TypeVariable[] typeVariables = $Gson$Types.getRawType(t).getTypeParameters();
            int matchedVarIdx = findTypeVariableIndex(var, typeVariables);
            assert matchedVarIdx >= 0 : "Missing typeVar " + var + " in " + t;

            Type[] typeArguments = ((ParameterizedType) t).getActualTypeArguments();
            Type matchedTypeArg = typeArguments[matchedVarIdx];
            if (!(matchedTypeArg instanceof TypeVariable)) {
                // resolved to non-variable, hence not present in the inherited class
                return -1;
            }
            // continue on the one type level down
            var = (TypeVariable)matchedTypeArg;
        }

        return findTypeVariableIndex(var, inheritedClass.getTypeParameters());
    }

    private static int findTypeVariableIndex(TypeVariable typeVarToFind, TypeVariable[] typeVariables) {
        for (int i = 0; i < typeVariables.length; i++) {
            if (typeVarToFind == typeVariables[i]) {
                return i;
            }
        }
        return -1;
    }

    private static void fillGenericInheritanceChain(List<Type> inheritanceChain, Class superType, Class inheritedClass) {
        for (Type t : getGenericSuperTypes(inheritedClass)) {
            Class c = $Gson$Types.getRawType(t);
            if (superType.isAssignableFrom(c)) {
                inheritanceChain.add(t);
                if (c != superType) {
                    fillGenericInheritanceChain(inheritanceChain, superType, c);
                }
                break;
            }
        }
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
                Type resolved = lookupTypeArg(rawLookupType.getTypeParameters()[foundIdx], getGenericSuperTypes(rawLookupType),
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

    public static Iterable<Class<?>> classOf(Class<?> c) {
        return Collections.<Class<?>>singleton(c);
    }

    public static Iterable<Class<?>> classes(Class<?>...classes) {
        return asList(classes);
    }

    public static void initEnumMapKeyType(EnumMap<?,?> instance, Class<?> keyType) {
        if (!keyType.isEnum()) {
            throw new JsonSyntaxException("Only enum keys are allowed for EnumMap, but got " + keyType);
        }
        EnumMap otherInstance = new EnumMap(keyType);
        copyFields(instance, otherInstance, EnumMap.class, "keyType", "keyUniverse", "vals");
    }

    private static void copyFields(Object to, Object from, Class<?> declaringClass, String...fieldNames) {
        for (String fname : fieldNames) {
            try {
                Field f = declaringClass.getDeclaredField(fname);
                f.setAccessible(true);
                f.set(to, f.get(from));
            } catch (Exception e) {
                throw new IllegalStateException("Failed to initialize field " + fname + " of " + declaringClass);
            }
        }
    }


}
