package am.yagson.refs.impl;

import am.yagson.refs.References;
import am.yagson.refs.ReferencesPolicy;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

import java.util.*;

public class ReferencesCircularAndSiblingsModeContext extends ReferencesAllDuplicatesModeContext {
    static ReferencesPolicy policy = ReferencesPolicy.CIRCULAR_AND_SIBLINGS;

    class RefsWriteContext extends ReferencesAllDuplicatesModeContext.RefsWriteContext {

        private Deque<Map<Object, String>> siblingReferencesStack;
        private Deque<Map<Object,String>> disposedRefsMapsCache;

        @Override
        protected void init() {
            super.init();
            siblingReferencesStack = new ArrayDeque<Map<Object, String>>();
            disposedRefsMapsCache = new ArrayDeque<Map<Object, String>>();
        }

        public RefsWriteContext(Object root) {
            super(root);
        }

        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }

        @Override
        public <T> String getReferenceFor(T value, TypeAdapter<T> valueTypeAdapter, String pathElement) {
            if (!isPotentialReference(value, valueTypeAdapter)) {
                return null;
            }

            boolean isField = pathElement.length() > 0 && Character.isJavaIdentifierStart(pathElement.charAt(0));
            if (isField) {
                Map<Object, String> siblingReferences = siblingReferencesStack.getLast();
                String ref = siblingReferences.get(value);
                if (ref != null) {
                    return References.REF_FIELD_PREFIX + ref;
                }
            }
            return super.getReferenceFor(value, valueTypeAdapter, pathElement);
        }

        @Override
        protected void startObject(Object value, String pathElement) {
            boolean isField = pathElement.length() > 0 && Character.isJavaIdentifierStart(pathElement.charAt(0));
            if (isField) {
                Map<Object, String> siblingReferences = siblingReferencesStack.getLast();
                siblingReferences.put(value, pathElement);
            }
            super.startObject(value, pathElement);
            siblingReferencesStack.add(getEmptyRefsMap());
       }

        @Override
        protected void endObject(Object value) {
            if (value != null) {
                disposeRefsMap(siblingReferencesStack.removeLast());
                super.endObject(value);
                references.remove(value);
            }
        }

        private void disposeRefsMap(Map<Object,String> map) {
            map.clear();
            disposedRefsMapsCache.add(map);
        }

        private Map<Object,String> getEmptyRefsMap() {
            if (disposedRefsMapsCache.isEmpty()) {
                return new IdentityHashMap<Object, String>();
            } else {
                return disposedRefsMapsCache.removeLast();
            }
        }
    }

    class RefsReadContext extends ReferencesAllDuplicatesModeContext.RefsReadContext {

        @Override
        public ReferencesPolicy getPolicy() {
            return policy;
        }

        @Override
        protected void afterObjectRead() {
            objectsByReference.remove(getCurrentReference());
            super.afterObjectRead();
        }

        @Override
        protected Object getObjectByReference(String reference) throws JsonSyntaxException {
            if (reference.startsWith(References.REF_FIELD_PREFIX)) {
                String fieldName = reference.substring(References.REF_FIELD_PREFIX.length());
                return new FieldReferencePlaceholder(fieldName);
            } else {
                Object value = objectsByReference.get(reference);
                if (value == null) {
                    if (!getCurrentReference().contains(reference)) {
                        throw new JsonSyntaxException("The reference cannot be read, as the current ReferencesPolicy " +
                                "allows only circular and sibling references: '" + reference + "'");
                    }
                    throw new JsonSyntaxException("Missing reference '" + reference + "'");
                }
                return value;
            }
        }
    }
}
