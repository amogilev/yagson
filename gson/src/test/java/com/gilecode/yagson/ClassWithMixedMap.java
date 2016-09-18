package com.gilecode.yagson;

import java.util.Map;

public class ClassWithMixedMap {
    Map<Object, Object> map;

    public ClassWithMixedMap(Map<Object, Object> map) {
        this.map = map;
    }

    public Map<Object, Object> getMap() {
        return map;
    }

    public void setMap(Map<Object, Object> map) {
        this.map = map;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassWithMixedMap that = (ClassWithMixedMap) o;

        return !(map != null ? !map.equals(that.map) : that.map != null);

    }

    @Override
    public int hashCode() {
        return map != null ? map.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "ClassWithMixedMap{" +
                "map=" + map +
                '}';
    }
}
