package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.tests.util.BindingTestCase;

import java.util.*;

public class TestIncorrectCollections extends BindingTestCase {

    static class IncorrectSet extends AbstractSet<String> {

        @Override
        public Iterator<String> iterator() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return 1;
        }
    }

    static class IncorrectMap extends AbstractMap<String, String> {

        @Override
        public Set<Entry<String, String>> entrySet() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public String put(String key, String value) {
            return super.put(key, value);
        }
    }


    public void testIncorrectSet() {
        YaGson gson = new YaGson();
        Set<String> obj = new IncorrectSet();
        String str = gson.toJson(obj);
        assertEquals(
                jsonStr("{'@type':'com.gilecode.yagson.tests.TestIncorrectCollections$IncorrectSet','@val':{}}"),
                str);
        Set obj2 = gson.fromJson(str, Set.class);
        assertTrue(obj2 instanceof IncorrectSet);
    }

    public void testIncorrectMap() {
        YaGson gson = new YaGson();
        Map<String, String> obj = new IncorrectMap();
        String str = gson.toJson(obj);
        assertEquals(
                jsonStr("{'@type':'com.gilecode.yagson.tests.TestIncorrectCollections$IncorrectMap','@val':{}}"),
                str);
        Map obj2 = gson.fromJson(str, Map.class);
        assertTrue(obj2 instanceof IncorrectMap);
    }


}
