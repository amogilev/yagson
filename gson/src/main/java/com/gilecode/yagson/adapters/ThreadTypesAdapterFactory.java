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
package com.gilecode.yagson.adapters;

import com.gilecode.yagson.WriteContext;
import com.gilecode.yagson.refs.PlaceholderUse;
import com.gilecode.yagson.refs.ReferencePlaceholder;
import com.google.gson.*;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Provides type adapters for {@link Thread}, {@link ThreadGroup} and {@link ThreadLocal}s.
 * <p/>
 * Threads and thread groups are serialized by their full names (starting from the root thread group).
 * <p/>
 * For thread locals, only the value for the current thread is stored. Deserialized thread locals are always
 * registered as new to the current thread, no "merge" attempts to existing thread locals is currently performed
 * (but it may be changed in future)
 */
public class ThreadTypesAdapterFactory implements TypeAdapterFactory {

    private static final char SEPARATOR_CHAR = '.';

    // do not use '/' as escape char as it is double-escaped by writer
    private static final char ESCAPE_CHAR = '_';

    /**
     * The backup thread group returned if no exact match for the read path is found
     */
    private static final ThreadGroup BACKUP_GROUP = null;

    /**
     * The backup thread returned if no exact match for the read path is found
     */
    private static final Thread BACKUP_THREAD = null;

    private final Method threadLocalGetMapMethod;
    private final Method threadLocalMapGetEntryMethod;
    private final Field threadLocalEntryValueField;

    public ThreadTypesAdapterFactory() {
        try {
            threadLocalGetMapMethod = ThreadLocal.class.getDeclaredMethod("getMap", Thread.class);
            threadLocalGetMapMethod.setAccessible(true);

            Class<?> threadLocalMapClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap");
            Class<?> threadLocalMapEntryClass = Class.forName("java.lang.ThreadLocal$ThreadLocalMap$Entry");

            threadLocalMapGetEntryMethod = threadLocalMapClass.getDeclaredMethod("getEntry", ThreadLocal.class);
            threadLocalMapGetEntryMethod.setAccessible(true);

            threadLocalEntryValueField = threadLocalMapEntryClass.getDeclaredField("value");
            threadLocalEntryValueField.setAccessible(true);

        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize ThreadTypesAdapterFactory");
        }
    }

    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (ThreadGroup.class.isAssignableFrom(rawType)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            TypeAdapter<T> result = (TypeAdapter)new ThreadGroupAdapter();
            return result;
        } else if (Thread.class.isAssignableFrom(rawType)) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            TypeAdapter<T> result = (TypeAdapter)new ThreadAdapter();
            return result;
        } else if (ThreadLocal.class.isAssignableFrom(rawType)) {
            TypeToken<?> fieldType = TypeToken.get($Gson$Types.getSingleParameterType(typeToken.getType(),
                    typeToken.getRawType(), ThreadLocal.class));

            ReflectiveTypeAdapterFactory.BoundField valueField = new ReflectiveTypeAdapterFactory.DefaultBoundField(
                    "@.value", null, true, true, gson, fieldType) {
                @Override
                protected boolean writeField(Object value, WriteContext ctx) throws IOException, IllegalAccessException {
                    if (value == null) {
                        return false;
                    }
                    Object threadLocalMap = getCurrentThreadMap(value);
                    return threadLocalMap != null && getThreadLocalEntry(threadLocalMap, value) != null;
                }

                @Override
                @SuppressWarnings("unchecked")
                protected void applyReadFieldValue(Object value, Object fieldValue) throws IllegalAccessException {
                    if (fieldValue != null) {
                        ((ThreadLocal)value).set(fieldValue);
                    }
                }

                @Override
                protected Object getFieldValue(Object value) throws IllegalAccessException {
                    Object threadLocalMap = getCurrentThreadMap(value);
                    if (threadLocalMap != null) {
                        Object threadLocalEntry = getThreadLocalEntry(threadLocalMap, value);
                        if (threadLocalEntry != null) {
                            return threadLocalEntryValueField.get(threadLocalEntry);
                        }
                    }
                    return null;
                }

                @SuppressWarnings("unchecked")
                protected void applyReadPlaceholder(final Object value, Map<Field, ReferencePlaceholder> fieldPlaceholders,
                                                    ReferencePlaceholder fieldValuePlaceholder) {
                    // the thread local's "value" is not a real field, so do not add it to 'fieldPlaceholders' map
                    fieldValuePlaceholder.registerUse(new PlaceholderUse() {
                        @Override
                        public void applyActualObject(Object actualObject) throws IOException {
                            try {
                                applyReadFieldValue(value, actualObject);
                            } catch (IllegalAccessException e) {
                                throw new IOException(e);
                            }
                        }
                    });
                }
            };
            return gson.getReflectiveTypeAdapterFactory().createSpecial(gson, typeToken,
                    Collections.singletonList(valueField), null);
        }
        return null;
    }

    private Object getCurrentThreadMap(Object value) {
        try {
            return threadLocalGetMapMethod.invoke(value, Thread.currentThread());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke getCurrentThreadMap", e);
        }
    }

    private Object getThreadLocalEntry(Object threadLocalMap, Object value) {
        try {
            return threadLocalMapGetEntryMethod.invoke(threadLocalMap, value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to invoke getThreadLocalEntry", e);
        }
    }


    private class ThreadGroupAdapter extends SimpleTypeAdapter<ThreadGroup> {

        @Override
        public void write(JsonWriter out, ThreadGroup tg) throws IOException {
            if (tg == null) {
                out.nullValue();
                return;
            }

            out.value(getThreadGroupPath(tg));
        }

        @Override
        public ThreadGroup read(JsonReader in) throws IOException {
            String path = in.nextString();
            List<String> groupNames = split(path, SEPARATOR_CHAR, ESCAPE_CHAR);
            ThreadGroup matchedGroup = matchThreadGroup(groupNames);
            return matchedGroup == null ? BACKUP_GROUP : matchedGroup;
        }
    }

    private class ThreadAdapter extends SimpleTypeAdapter<Thread> {

        @Override
        public void write(JsonWriter out, Thread t) throws IOException {
            if (t == null) {
                out.nullValue();
                return;
            }

            String groupPath = getThreadGroupPath(t.getThreadGroup());
            StringBuilder sb = new StringBuilder(groupPath);
            sb.append(SEPARATOR_CHAR);
            appendEscaped(sb, t.getName(), ESCAPE_CHAR, ESCAPE_CHAR, SEPARATOR_CHAR);
            out.value(sb.toString());
        }

        @Override
        public Thread read(JsonReader in) throws IOException {
            String path = in.nextString();
            List<String> groupAndThreadNames = split(path, SEPARATOR_CHAR, ESCAPE_CHAR);
            if (groupAndThreadNames.size() < 2) {
                throw new JsonSyntaxException("Expected both thread group and thread names in '" + path + "'");
            }
            ThreadGroup matchedGroup = matchThreadGroup(groupAndThreadNames.subList(0, groupAndThreadNames.size() - 1));
            if (matchedGroup == null) {
                return BACKUP_THREAD;
            }

            String threadName = groupAndThreadNames.get(groupAndThreadNames.size() - 1);
            Thread[] threads = new Thread[matchedGroup.activeCount()];
            matchedGroup.enumerate(threads, false);

            Thread matchedThread = findNamedIn(threadName, threads);
            return matchedThread == null ? BACKUP_THREAD : matchedThread;
        }
    }

    private ThreadGroup matchThreadGroup(List<String> groupNames) {
        ThreadGroup rootGroup = getSystemThreadGroup();

        if (groupNames.size() <= 0 || !rootGroup.getName().equals(groupNames.get(0))) {
            return null;
        }

        ThreadGroup curMatchedGroup = rootGroup;
        for (int i = 1; i < groupNames.size() && curMatchedGroup != null; i++) {
            String groupName = groupNames.get(i);
            ThreadGroup[] childGroups = new ThreadGroup[curMatchedGroup.activeGroupCount()];
            rootGroup.enumerate(childGroups, false);

            curMatchedGroup = findNamedIn(groupName, childGroups);
        }

        return curMatchedGroup;
    }

    private ThreadGroup findNamedIn(String groupName, ThreadGroup[] groups) {
        for (ThreadGroup group : groups) {
            if (group != null && groupName.equals(group.getName())) {
                return group;
            }
        }
        return null;
    }

    private Thread findNamedIn(String threadName, Thread[] threads) {
        for (Thread thread : threads) {
            if (thread != null && threadName.equals(thread.getName())) {
                return thread;
            }
        }
        return null;
    }

    private ThreadGroup getSystemThreadGroup() {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        while (tg.getParent() != null) {
            tg = tg.getParent();
        }
        return tg;
    }

    private String getThreadGroupPath(ThreadGroup tg) {
        List<ThreadGroup> groups = new ArrayList<ThreadGroup>(4);
        while (tg != null) {
            groups.add(tg);
            tg = tg.getParent();
        }
        List<String> groupNamesReversed = new ArrayList<String>(groups.size());
        for (int i = groups.size() - 1; i >= 0; i--) {
            groupNamesReversed.add(groups.get(i).getName());
        }
        return join(groupNamesReversed, SEPARATOR_CHAR, ESCAPE_CHAR);
    }

    private static String join(Iterable<String> parts, char separatorChar, char escapeChar) {
        StringBuilder sb = new StringBuilder();
        char[] charsToEscape = escapeChar == separatorChar ? new char[]{escapeChar} : new char[]{escapeChar, separatorChar};
        for (String part : parts) {
            appendEscaped(sb, part, escapeChar, charsToEscape);
            sb.append(separatorChar);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    private static List<String> split(String str, char separatorChar, char escapeChar) {
        List<String> result = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();

        boolean escaped = false;
        for (char ch : str.toCharArray()) {
            if (escaped) {
                escaped = false;
            } else if (ch == escapeChar) {
                escaped = true;
                continue;
            } else if (ch == separatorChar) {
                result.add(sb.toString());
                sb.setLength(0);
                continue;
            }
            sb.append(ch);
        }
        result.add(sb.toString());

        return result;
    }

    private static void appendEscaped(StringBuilder sb, String str, char escapeChar, char...charsToEscape) {
        for (char ch : str.toCharArray()) {
            if (isInArray(ch, charsToEscape)) {
                sb.append(escapeChar);
            }
            sb.append(ch);
        }
    }

    private static boolean isInArray(char ch, char[] charArr) {
        for (char element : charArr) {
            if (ch == element) {
                return true;
            }
        }
        return false;
    }
}
