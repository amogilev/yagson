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

import com.gilecode.yagson.refs.PlaceholderUse;
import com.gilecode.yagson.refs.ReferencePlaceholder;
import com.gilecode.yagson.adapters.AdapterUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Provides implementations of {@link PlaceholderUse} for keys and values of a map.
 */
public class MapPlaceholderUse<K, V>  {
    private final Map<K, V> map;
    private final List<Object> keys;
    private final List<Object> values;
    private final int entryIdx;
    private final Field modCountField;

    private MapPlaceholderUse(Map<K, V> map, List<Object> keys, List<Object> values, int entryIdx, Field modCountField) {
        this.map = map;
        this.keys = keys;
        this.values = values;
        this.entryIdx = entryIdx;
        this.modCountField = modCountField;
    }

    public static <K,V> MapPlaceholderUse<K, V>.KeyUse keyUse(
            Map<K, V> map, List<Object> keys, List<Object> values, int entryIdx, Field modCountField) {
        return new MapPlaceholderUse<K,V>(map, keys, values, entryIdx, modCountField).new KeyUse();
    }

    public static <K,V> MapPlaceholderUse<K, V>.ValueUse valueUse(
            Map<K, V> map, List<Object> keys, List<Object> values, int entryIdx, Field modCountField) {
        return new MapPlaceholderUse<K,V>(map, keys, values, entryIdx, modCountField).new ValueUse();
    }

    public class KeyUse implements PlaceholderUse<K> {
        /**
         * If both key and value are available at this moment, adds them to the map. Otherwise, the map's update is
         * deferred to the corresponding {@link ValueUse}
         */
        public void applyActualObject(K actualObject) throws IOException {
            keys.set(entryIdx, actualObject);
            if (!(values.get(entryIdx) instanceof ReferencePlaceholder)) {
                updateMap();
            }
        }
    }

    public class ValueUse implements PlaceholderUse<V> {
        /**
         * If both key and value are available at this moment, adds them to the map. Otherwise, the map's update is
         * deferred to the corresponding {@link KeyUse}
         */
        public void applyActualObject(V actualObject) throws IOException {
            values.set(entryIdx, actualObject);
            if (!(keys.get(entryIdx) instanceof ReferencePlaceholder)) {
                updateMap();
            }
        }
    }

    private void updateMap() {
        map.clear();
        if (keys.size() != values.size()) {
            throw new AssertionError("Unexpected different sizes of keys and values");
        }
        for (int i = 0; i < keys.size(); i++) {
            Object keyOrPlaceholder = keys.get(i);
            Object valueOrPlaceholder = values.get(i);
            if (keyOrPlaceholder instanceof ReferencePlaceholder || valueOrPlaceholder instanceof ReferencePlaceholder) {
                // this entry is not ready yet, skip it
            } else {
                map.put((K)keyOrPlaceholder, (V)valueOrPlaceholder);
            }
        }
        AdapterUtils.clearModCount(modCountField, map);
    }
}
