package am.yagson;

import am.yagson.refs.References;
import am.yagson.refs.ReferencesPolicy;
import am.yagson.types.TypeInfoPolicy;
import com.google.gson.*;
import com.google.gson.internal.Excluder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The builder to create YaGson instances with non-default configuration. It provides similar
 * creation and usage patterns as the basic {@link GsonBuilder}, but creates instances of
 * {@link YaGson} instead, and the references emitting and the type info emitting are enabled
 * by default.
 */
public class YaGsonBuilder extends GsonBuilder {

    public YaGsonBuilder() {
        setReferencesPolicy(References.defaultPolicy());
        setTypeInfoPolicy(TypeInfoPolicy.defaultPolicy());
        if (TypeInfoPolicy.defaultPolicy().isEnabled()) {
            enableComplexMapKeySerialization();
        }

    }

    /**
     * Creates a {@link YaGson} instance based on the current configuration. This method is free of
     * side-effects to this {@code GsonBuilder} instance and hence can be called multiple times.
     *
     * @return an instance of YaGson configured with the options currently set in this builder
     */
    @Override
    public YaGson create() {
        return new YaGson(excluder, fieldNamingPolicy, instanceCreators,
                serializeNulls, complexMapKeySerialization,
                generateNonExecutableJson, escapeHtmlChars, prettyPrinting,
                serializeSpecialFloatingPointValues, longSerializationPolicy,
                createTypeAdapterFactories(),
                referencesPolicy, typeInfoPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setReferencesPolicy(ReferencesPolicy referencesPolicy) {
        return (YaGsonBuilder) super.setReferencesPolicy(referencesPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setTypeInfoPolicy(TypeInfoPolicy typeInfoPolicy) {
        return (YaGsonBuilder) super.setTypeInfoPolicy(typeInfoPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setVersion(double ignoreVersionsAfter) {
        return (YaGsonBuilder) super.setVersion(ignoreVersionsAfter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder excludeFieldsWithModifiers(int... modifiers) {
        return (YaGsonBuilder) super.excludeFieldsWithModifiers(modifiers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder generateNonExecutableJson() {
        return (YaGsonBuilder) super.generateNonExecutableJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder excludeFieldsWithoutExposeAnnotation() {
        return (YaGsonBuilder) super.excludeFieldsWithoutExposeAnnotation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder serializeNulls() {
        return (YaGsonBuilder) super.serializeNulls();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder enableComplexMapKeySerialization() {
        return (YaGsonBuilder) super.enableComplexMapKeySerialization();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder disableInnerClassSerialization() {
        return (YaGsonBuilder) super.disableInnerClassSerialization();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setLongSerializationPolicy(LongSerializationPolicy serializationPolicy) {
        return (YaGsonBuilder) super.setLongSerializationPolicy(serializationPolicy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setFieldNamingPolicy(FieldNamingPolicy namingConvention) {
        return (YaGsonBuilder) super.setFieldNamingPolicy(namingConvention);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setFieldNamingStrategy(FieldNamingStrategy fieldNamingStrategy) {
        return (YaGsonBuilder) super.setFieldNamingStrategy(fieldNamingStrategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setExclusionStrategies(ExclusionStrategy... strategies) {
        return (YaGsonBuilder) super.setExclusionStrategies(strategies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder addSerializationExclusionStrategy(ExclusionStrategy strategy) {
        return (YaGsonBuilder) super.addSerializationExclusionStrategy(strategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder addDeserializationExclusionStrategy(ExclusionStrategy strategy) {
        return (YaGsonBuilder) super.addDeserializationExclusionStrategy(strategy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setPrettyPrinting() {
        return (YaGsonBuilder) super.setPrettyPrinting();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder disableHtmlEscaping() {
        return (YaGsonBuilder) super.disableHtmlEscaping();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setDateFormat(String pattern) {
        return (YaGsonBuilder) super.setDateFormat(pattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setDateFormat(int style) {
        return (YaGsonBuilder) super.setDateFormat(style);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder setDateFormat(int dateStyle, int timeStyle) {
        return (YaGsonBuilder) super.setDateFormat(dateStyle, timeStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder registerTypeAdapter(Type type, Object typeAdapter) {
        return (YaGsonBuilder) super.registerTypeAdapter(type, typeAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder registerTypeAdapterFactory(TypeAdapterFactory factory) {
        return (YaGsonBuilder) super.registerTypeAdapterFactory(factory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder registerTypeHierarchyAdapter(Class<?> baseType, Object typeAdapter) {
        return (YaGsonBuilder) super.registerTypeHierarchyAdapter(baseType, typeAdapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public YaGsonBuilder serializeSpecialFloatingPointValues() {
        return (YaGsonBuilder) super.serializeSpecialFloatingPointValues();
    }
}
