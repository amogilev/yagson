package am.yagson.refs.impl;

import am.yagson.refs.HashReferencePlaceholder;
import am.yagson.refs.PlaceholderUse;
import am.yagson.refs.ReferencePlaceholder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.bind.HasField;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility functions related to {@link am.yagson.refs.ReferencePlaceholder}
 */
public class PlaceholderUtils {

    /**
     * Finds hash and fields placeholders in the specified list of placeholders and resolve each of them, either
     * now (if no other placeholders prevent immediate resolution), or after other placeholders are resolved.
     *
     * @param instance the instance which contains the field placeholders
     * @param placeholders all placeholders for the fields of the instance object, either hash or non-hash
     * @param fieldsByName provider of fields by theirs (serialization) names
     *
     * @throws IOException
     */
    public static <T> void applyOrDeferHashAndFieldPlaceholders(final T instance,
                                                                final Map<Field, ReferencePlaceholder> placeholders,
                                                                Map<String, ? extends HasField> fieldsByName) throws IOException {

        // as FieldReferencePlaceholder may actually reference another placeholder, they may form a DAG, ending with
        //  either HashRef, other Ref or value. Use recursion to process DAGs from the end, and for each placeholder
        //  either resolve it (apply actual object if known), or move to the 'hash references' set, or to the
        //  'deferred references' set
        Set<Field> unresolvedFields = new HashSet<Field>(placeholders.keySet());
        final Set<Field> hashFields = new HashSet<Field>(placeholders.size());
        final Set<Field> deferredReferencesFields = new HashSet<Field>(placeholders.size());

        while (!unresolvedFields.isEmpty()) {
            Iterator<Field> it = unresolvedFields.iterator();
            Field fieldToResolve = it.next();
            it.remove();
            tryResolveFieldPlaceholder(fieldToResolve, placeholders, fieldsByName, instance,
                    unresolvedFields, hashFields, deferredReferencesFields);
        }

        // at this moment, all unresolved references are either 'hash' or 'deferred' placeholders (which are to be
        //  resolved elsewhere, or are chained).
        // here we need to process or chain the hash placeholders
        if (hashFields.isEmpty()) {
            return;
        }
        if (deferredReferencesFields.isEmpty()) {
            // no deferred non-hash placeholders, can calculate hash now
            int hashCode = instance.hashCode();
            for (Field f : hashFields) {
                ReferencePlaceholder p = placeholders.get(f);
                p.applyActualObject(hashCode);
            }
        } else {
            // apply hash when all non-hash references are resolved
            for (Field f : deferredReferencesFields) {
                ReferencePlaceholder p = placeholders.get(f);
                final AtomicBoolean isResolved = new AtomicBoolean();
                p.registerUse(new PlaceholderUse<Object>() {
                    public void applyActualObject(Object actualObject) throws IOException {
                        if (isResolved.get()) {
                            return;
                        }
                        for (Field checkedField : deferredReferencesFields) {
                            ReferencePlaceholder checkedPlaceholder = placeholders.get(checkedField);
                            if (checkedPlaceholder.getActualObject() == null) {
                                // not all placeholders are resolved yet
                                return;
                            }
                            isResolved.set(true);
                            int hashCode = instance.hashCode();
                            for (Field hashField : hashFields) {
                                ReferencePlaceholder hp = placeholders.get(hashField);
                                hp.applyActualObject(hashCode);
                            }
                        }
                    }
                });
            }
        }
    }

    private static <T> void tryResolveFieldPlaceholder(Field fieldToResolve, Map<Field, ReferencePlaceholder> placeholders,
                                                       Map<String, ? extends HasField> fieldsByName, T instance,
                                                       Set<Field> unresolvedFields, Set<Field> hashFields,
                                                       Set<Field> deferredReferencesFields) throws IOException {
        final ReferencePlaceholder placeholder = placeholders.get(fieldToResolve);
        assert placeholder != null;

        if (placeholder instanceof HashReferencePlaceholder) {
            hashFields.add(fieldToResolve);
        } else if (placeholder instanceof FieldReferencePlaceholder) {
            FieldReferencePlaceholder fieldRefPlaceholder = (FieldReferencePlaceholder) placeholder;
            String referencedFieldName = fieldRefPlaceholder.getReferencedFieldName();
            if (!fieldRefPlaceholder.isResolved()) {
                HasField fieldProvider = fieldsByName.get(referencedFieldName);
                if (fieldProvider == null) {
                    throw new JsonSyntaxException("No field found for the serialization name '" + referencedFieldName + "'");
                }
                fieldRefPlaceholder.setReferencedField(fieldProvider.getField());
            }

            Field referencedField = fieldRefPlaceholder.getReferencedField();

            // if there are no unresolved placeholders at the referenced field, we can use its value now
            ReferencePlaceholder chainedPlaceholder = placeholders.get(referencedField);
            Object referencedValue;
            if (chainedPlaceholder != null) {
                if (chainedPlaceholder.getActualObject() == null) {
                    // not resolved yet, check if met in processed sets
                    if (unresolvedFields.remove(referencedField)) {
                        // recursively process referenced placeholder
                        tryResolveFieldPlaceholder(fieldToResolve, placeholders, fieldsByName, instance,
                                unresolvedFields, hashFields, deferredReferencesFields);
                    }
                    if (chainedPlaceholder.getActualObject() == null) {
                        // still not resolved - must be either hash or deferred
                        if (hashFields.contains(referencedField)) {
                            // treat as hash reference
                            hashFields.add(referencedField);
                        } else if (deferredReferencesFields.contains(referencedField)) {
                            // chain to the deferred placeholder
                            deferredReferencesFields.add(referencedField);
                            chainedPlaceholder.registerUse(new PlaceholderUse() {
                                public void applyActualObject(Object actualObject) throws IOException {
                                    placeholder.applyActualObject(actualObject);
                                }
                            });
                        } else {
                            throw new IllegalStateException("The placeholder is expected to be already processed: " +
                                    chainedPlaceholder);
                        }
                        return;
                    }
                }
                referencedValue = chainedPlaceholder.getActualObject();
                assert referencedValue != null;
            } else {
                try {
                    referencedValue = referencedField.get(instance);
                } catch (IllegalAccessException e) {
                    throw new JsonSyntaxException("Failed to get the referenced reflective field value; field=" + referencedField);
                }
            }
            fieldRefPlaceholder.applyActualObject(referencedValue);
        } else {
            deferredReferencesFields.add(fieldToResolve);
        }
    }
}
