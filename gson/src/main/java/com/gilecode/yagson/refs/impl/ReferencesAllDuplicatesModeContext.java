/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.refs.impl;

import java.io.IOException;
import java.util.*;

import com.gilecode.yagson.ReadContext;
import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.*;

import com.google.gson.*;
import com.gilecode.yagson.adapters.AdapterUtils;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import static com.gilecode.yagson.refs.References.REF_FIELD_PREFIX;
import static com.gilecode.yagson.refs.References.REF_ROOT;

/**
 * Provides {@link ReferencesReadContext} and {@link ReferencesWriteContext} for the
 * {@link ReferencesPolicy#DUPLICATE_OBJECTS} references policy.
 * <p/>
 * Used to find circular dependencies and duplicate objects during the
 * serialization, and write them as references.
 * <p/>
 * Neither this context, nor the Write or Read contexts are thread-safe!
 *
 * @author Andrey Mogilev
 */
class ReferencesAllDuplicatesModeContext {

    static ReferencesPolicy policy = ReferencesPolicy.DUPLICATE_OBJECTS;

    protected List<String> referencePaths = new ArrayList<String>();

    protected void addPathElement(String pathElement) {
        String parentRefPath = getCurrentReference();
        String newRefPath = parentRefPath == null ? pathElement : parentRefPath + "." + pathElement;
        referencePaths.add(newRefPath);
    }
    protected void removeLastPathElement() {
        referencePaths.remove(referencePaths.size() - 1);
    }

    protected String getCurrentReference() {
        if (referencePaths.size() > 0) {
            return referencePaths.get(referencePaths.size() - 1);
        }
        return null;
    }

    protected String getParentReference() {
        if (referencePaths.size() > 1) {
            return referencePaths.get(referencePaths.size() - 2);
        }
        return null;
    }

    class RefsWriteContext implements ReferencesWriteContext {
        protected IdentityHashMap<Object, String> references = new IdentityHashMap<Object, String>();
        protected Deque<Object> currentObjects = new ArrayDeque<Object>(); // used only for self-checks

        public RefsWriteContext(Object root) {
            init();
            if (root != null) {
                startObject(root, REF_ROOT);
            }
        }

        // inits subclasses (before constructor completion)
        protected void init() {
        }

        /**
         * Saves the object for the further possible references.
         *
         * @param value the object to be serialized next
         * @param pathElement the element corresponding to the object in the references path
         */
        protected void startObject(Object value, String pathElement) {
            currentObjects.addLast(value);
            addPathElement(pathElement);
            references.put(value, getCurrentReference());
        }

        /**
         * Must be invoked in pair to {@link #startObject(Object, String)} which returned null, after
         * serialization of the object is completed.
         *
         * @param value the object which serialization has been completed
         */
        protected void endObject(Object value) {
            if (value != null) {
                Object last = currentObjects.removeLast();
                if (last != value) {
                    throw new IllegalStateException("Out-of-order endObject()");
                }
                removeLastPathElement();
            }
        }

        protected final <T> boolean isPotentialReference(T value, TypeAdapter<T> valueTypeAdapter) {
            // avoid creating references to simple types even in "all duplicates" mode
            return value != null && !AdapterUtils.isSimpleTypeAdapter(valueTypeAdapter);
        }

        public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
            if (!isPotentialReference(value, valueTypeAdapter)) {
                return null;
            }

            String ref = references.get(value);
            if (ref != null) {
                String curRef = getCurrentReference();
                if (ref.startsWith(curRef + ".") && ref.indexOf('.', curRef.length() + 1) < 0) {
                    // use shorter 'sibling field' reference istsead of the full reference
                    // NOTE: unlike ReferencesPolicy.CIRCULAR_AND_SIBLINGS mode, not only 'fields' are
                    //  supported
                    String siblingRef = References.REF_FIELD_PREFIX + ref.substring(curRef.length() + 1);
                    return siblingRef;
                }
            }
            return ref;
        }

        public <T> JsonElement doToJsonTree(T value, TypeAdapter<T> valueTypeAdapter, String pathElement,
                                            WriteContext ctx) {
            String ref = getReferenceFor(value, valueTypeAdapter, pathElement);
            if (ref != null) {
                return makeReferenceElement(ref);
            } else if (isPotentialReference(value, valueTypeAdapter)) {
                startObject(value, pathElement);
                JsonElement el = valueTypeAdapter.toJsonTree(value, ctx);
                endObject(value);
                return el;
            } else {
                return valueTypeAdapter.toJsonTree(value, ctx);
            }
        }

        public <T> void doWrite(T value, TypeAdapter<T> valueTypeAdapter, String pathElement, JsonWriter out,
                                WriteContext ctx) throws IOException {

            // resolve required for correct check of isSimple (i.e. if isPotentialReference)
            valueTypeAdapter = AdapterUtils.resolve(valueTypeAdapter, value);

            String ref = getReferenceFor(value, valueTypeAdapter, pathElement);
            if (ref != null) {
                out.value(ref);
            } else if (isPotentialReference(value, valueTypeAdapter)) {
                startObject(value, pathElement);
                valueTypeAdapter.write(out, value, ctx);
                endObject(value);
            } else {
                valueTypeAdapter.write(out, value, ctx);
            }
        }

        private JsonElement makeReferenceElement(String ref) {
            return new JsonPrimitive(ref);
        }

        public ReferencesPolicy getPolicy() {
            return policy;
        }
    }


    class RefsReadContext implements ReferencesReadContext {

        protected boolean awaitsObjectRead = false;
        protected ReferencePlaceholder<?> lastReadPlaceholder = null;
        protected Map<String, Object> objectsByReference = new HashMap<String, Object>();

        protected void beforeObjectRead(String pathElement) {
            if (awaitsObjectRead) {
                throw new IllegalStateException("Awaits object, but get another path element: " + getCurrentReference());
            }
            if (lastReadPlaceholder != null) {
                throw new IllegalStateException("The last reference placeholder was not consumed, at " +
                        getCurrentReference());
            }
            addPathElement(pathElement);
            awaitsObjectRead = true;
        }

        /**
         * Registers an object corresponding to the path built by previous {@link #beforeObjectRead(String)} call.
         *
         * @param value the object created by de-serializers, optionally without fields/contents yet
         * @param fromSimpleTypeAdapter whether the value is read ny a simple type adapter. Such values are
         *                              never referenced by this context
         */
        public void registerObject(Object value, boolean fromSimpleTypeAdapter) {
            if (!awaitsObjectRead) {
                throw new IllegalStateException("registerObject() without corresponding beforeObjectRead(): " +
                        getCurrentReference());
            }
            awaitsObjectRead = false;
            if (value != null && !fromSimpleTypeAdapter) {
                final String ref = getCurrentReference();
                objectsByReference.put(ref, value);
                if (value instanceof ReferencePlaceholder) {
                    ((ReferencePlaceholder) value).registerUse(new PlaceholderUse() {
                        @Override
                        public void applyActualObject(Object actualObject) throws IOException {
                            objectsByReference.put(ref, actualObject);
                        }
                    });
                }
            }
        }

        private Object registerReferenceUse(String reference) {
            Object value = getObjectByReference(reference);
            // the object may now be reference both with the used and the current reference
            registerObject(value, false);
            return value;
        }

        protected void afterObjectRead() {
            if (awaitsObjectRead) {
                throw new IllegalStateException("afterObjectRead() without corresponding registerObject(): " +
                        getCurrentReference());
            }
            removeLastPathElement();
        }


        protected Object getObjectByReference(String reference) throws JsonSyntaxException {
            String key = reference;
            if (reference.startsWith(REF_FIELD_PREFIX)) {
                key = toFullReference(reference);
            }
            Object value = objectsByReference.get(key);
            if (value == null) {
                throw new JsonSyntaxException("Missing reference '" + reference + "'");
            }
            return value;
        }

        private String toFullReference(String fieldReference) {
            assert fieldReference.startsWith(REF_FIELD_PREFIX);
            return getParentReference() + "." + fieldReference.substring(REF_FIELD_PREFIX.length());
        }

        public <T> T doRead(JsonReader reader, TypeAdapter<T> typeAdapter, String pathElement,
                            ReadContext ctx) throws IOException {
            beforeObjectRead(pathElement);
            T fieldValue = typeAdapter.read(reader, ctx); // expected registerObject() for non-null reads
            if (fieldValue == null) {
                // registerObject is skipped for nulls in most cases, so clear 'awaits' flag
                awaitsObjectRead = false;
            }
            afterObjectRead();
            return fieldValue;
        }

        public boolean isReferenceString(String str) {
            if (str.startsWith(REF_ROOT)) {
                return objectsByReference.containsKey(str);
            } else if (str.startsWith(REF_FIELD_PREFIX)) {
                return objectsByReference.containsKey(toFullReference(str));
            } else {
                return false;
            }
        }

        @SuppressWarnings("unchecked")
        public <T> T getReferencedObject(String reference) throws IOException {
            try {
                Object value = registerReferenceUse(reference);
                if (value instanceof ReferencePlaceholder) {
                    lastReadPlaceholder = (ReferencePlaceholder<?>) value;
                    value = null;
                }

                return (T) value;
            } catch (ClassCastException e) {
                throw new JsonSyntaxException("Incompatible reference type used: " + reference, e);
            }
        }

        @SuppressWarnings("unchecked")
        public <T> ReferencePlaceholder<T> consumeLastPlaceholderIfAny() {
            ReferencePlaceholder<T> result = (ReferencePlaceholder<T>) lastReadPlaceholder;
            lastReadPlaceholder = null;
            return result;
        }

        public ReferencesPolicy getPolicy() {
            return policy;
        }
    }
}
