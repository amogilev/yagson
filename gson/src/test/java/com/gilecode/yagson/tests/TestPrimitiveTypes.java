package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Test for the serialization of primitive types, their auto-boxing and auto-cast.
 *
 * @author Andrey Mogilev
 */
public class TestPrimitiveTypes extends BindingTestCase {

    public void testInt() {
        Integer resultAsObj = test(2, int.class, "2");
        int resultAsInt = test(2, int.class, "2");

        assertEquals(2, resultAsInt);
        assertEquals(2, resultAsObj.intValue());
    }

    public void testInt2() {
        Integer resultAsObj = test(2, Integer.class, "2");
        int resultAsInt = test(2, Integer.class, "2");

        assertEquals(2, resultAsInt);
        assertEquals(2, resultAsObj.intValue());
    }

    public void testLong() {
        test(2L, long.class, "2");
        test(2L, Long.class, "2");
    }

    public void testShort() {
        test((short)2, short.class, "2");
        test((short)2, Short.class, "2");
    }

    public void testByte() {
        test((byte)2, byte.class, "2");
        test((byte)2, Byte.class, "2");
    }

    public void testFloat() {
        test(2.0f, float.class, "2.0");
        test(2.0f, Float.class, "2.0");
    }

    public void testDouble() {
        test(2.0, double.class, "2.0");
        test(2.0, Double.class, "2.0");
    }

    public void testBoolean() {
        test(false, boolean.class, "false");
        test(true, boolean.class, "true");
        test(true, Boolean.class, "true");
    }

    // test auto-conversion of primitive types to long

    public void testByteAsLong() {
        String json = defaultMapper.toJson((byte)2, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    public void testShortAsLong() {
        String json = defaultMapper.toJson((short)2, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    public void testIntAsLong() {
        String json = defaultMapper.toJson(2, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    public void testFloatAsLong() {
        String json = defaultMapper.toJson(2.0f, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    public void testDoubleAsLong() {
        String json = defaultMapper.toJson(2.0, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    public void testDoubleAsLongWithRounding() {
        String json = defaultMapper.toJson(2.1, long.class);
        assertEquals("2", json);

        long result = defaultMapper.fromJson(json, long.class);
        assertEquals(2L, result);
    }

    // test auto-conversion of primitive types to byte

    public void testLongAsByte() {
        String json = defaultMapper.toJson(2L, byte.class);
        assertEquals("2", json);

        byte result = defaultMapper.fromJson(json, byte.class);
        assertEquals((byte)2, result);
    }

    public void testShortAsByte() {
        String json = defaultMapper.toJson((short)2, byte.class);
        assertEquals("2", json);

        byte result = defaultMapper.fromJson(json, byte.class);
        assertEquals((byte)2, result);
    }

    public void testIntAsByte() {
        String json = defaultMapper.toJson(2, byte.class);
        assertEquals("2", json);

        byte result = defaultMapper.fromJson(json, byte.class);
        assertEquals((byte)2, result);
    }

    public void testFloatAsByte() {
        String json = defaultMapper.toJson(2.0f, byte.class);
        assertEquals("2", json);

        byte result = defaultMapper.fromJson(json, byte.class);
        assertEquals((byte)2, result);
    }

    public void testDoubleAsByte() {
        String json = defaultMapper.toJson(2.0, byte.class);
        assertEquals("2", json);

        byte result = defaultMapper.fromJson(json, byte.class);
        assertEquals((byte)2, result);
    }

    // test corner cases for FP numbers
    public void testSpecialDoubleNumbers() {
        test(Double.NaN, double.class, "NaN");
        test(Double.POSITIVE_INFINITY, double.class, "Infinity");
        test(Double.NEGATIVE_INFINITY, double.class, "-Infinity");

        test(Double.NaN, Double.class, "NaN");
        test(Double.POSITIVE_INFINITY, Double.class, "Infinity");
        test(Double.NEGATIVE_INFINITY, Double.class, "-Infinity");
    }

    public void testSpecialFloatNumbers() {
        test(Float.NaN, float.class, "NaN");
        test(Float.POSITIVE_INFINITY, float.class, "Infinity");
        test(Float.NEGATIVE_INFINITY, float.class, "-Infinity");

        test(Float.NaN, float.class, "NaN");
        test(Float.POSITIVE_INFINITY, Float.class, "Infinity");
        test(Float.NEGATIVE_INFINITY, Float.class, "-Infinity");
    }

    public void testSpecialFPNumbersAsMapKeys() {
        Map<Float, String> obj = new TreeMap<Float, String>();
        obj.put(Float.NaN, "1");
        obj.put(Float.POSITIVE_INFINITY, "2");
        obj.put(Float.NEGATIVE_INFINITY, "3");

        test(obj, new TypeToken<TreeMap<Float, String>>(){}.getType(),
                jsonStr("{'-Infinity':'3','Infinity':'2','NaN':'1'}"));
    }

    public void testMaxMinDoubleNumbers() {
        test(Double.MAX_VALUE, double.class, "1.7976931348623157E308");
        test(Double.MIN_VALUE, double.class, "4.9E-324");
        test(Double.MIN_NORMAL, double.class, "2.2250738585072014E-308");

        test(Double.MAX_VALUE, Double.class, "1.7976931348623157E308");
        test(Double.MIN_VALUE, Double.class, "4.9E-324");
        test(Double.MIN_NORMAL, Double.class, "2.2250738585072014E-308");
    }

    public void testMaxMinFloatNumbers() {
        test(Float.MAX_VALUE, float.class, "3.4028235E38");
        test(Float.MIN_VALUE, float.class, "1.4E-45");
        test(Float.MIN_NORMAL, float.class, "1.17549435E-38");
    }

    public void testAsSerializable() {
        test(1, Serializable.class, jsonStr("{'@type':'java.lang.Integer','@val':1}"));

        Object o = gsonCircularOnlyMode.fromJson("1", Serializable.class);
        assertEquals(1L, o);

        o = gsonCircularOnlyMode.fromJson("1.1", Comparable.class);
        assertEquals(1.1, o);

        o = gsonCircularOnlyMode.fromJson(jsonStr("'str'"), Comparable.class);
        assertEquals("str", o);
    }

}
