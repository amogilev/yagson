package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;

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
}
