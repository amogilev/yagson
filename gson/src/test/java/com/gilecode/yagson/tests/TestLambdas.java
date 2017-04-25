package com.gilecode.yagson.tests;

import com.gilecode.yagson.YaGson;
import com.gilecode.yagson.YaGsonBuilder;
import com.gilecode.yagson.tests.data.ClassWithObject;
import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;
import com.gilecode.yagson.types.NSLambdaPolicy;
import com.gilecode.yagson.types.NonSerializableLambdaException;
import com.gilecode.yagson.types.TypeUtils;
import com.google.gson.Gson;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.*;

/**
 * Test for serializable and non-serializable lambdas.
 * Only available for Java 8 and higher  - shall be commented out otherwise.
 *
 * @author Andrey Mogilev
 */
public class TestLambdas extends BindingTestCase {

    public void testLambdaDetection() {
        Supplier s1 = (Supplier) () -> "foo";
        assertTrue(TypeUtils.isLambdaClass(s1.getClass()));

        Predicate<Object> mref1 = this::equals;
        assertTrue(TypeUtils.isLambdaClass(mref1.getClass()));

        Consumer<String> mref2 = System.out::println;
        assertTrue(TypeUtils.isLambdaClass(mref2.getClass()));

        LongSupplier mref3 = System::currentTimeMillis;
        assertTrue(TypeUtils.isLambdaClass(mref3.getClass()));

        assertFalse(TypeUtils.isLambdaClass(Object.class));
        assertFalse(TypeUtils.isLambdaClass(this.getClass()));
    }

    public void testNonSerializableLambdaSkipMode() {
        Supplier s1 = (Supplier) () -> "foo";
        test(s1, "null", EqualityCheckMode.EXPECT_NULL);

        Supplier[] arr = {s1};
        test(arr, jsonStr("[null]"), EqualityCheckMode.NONE);

        ClassWithObject obj = new ClassWithObject(s1);
        test(obj, jsonStr("{}"), EqualityCheckMode.NONE);
    }

    public void testNonSerializableLambdaErrorMode() {
        Gson gson = new YaGsonBuilder().setNsLambdaPolicy(NSLambdaPolicy.ERROR).create();
        Supplier s1 = (Supplier) () -> "foo";
        try {
            test(gson, s1);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }

        Supplier[] arr = {s1};
        try {
            test(gson, arr);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }

        ClassWithObject obj = new ClassWithObject(s1);
        try {
            test(gson, obj);
            fail("NonSerializableLambdaException expected!");
        } catch (NonSerializableLambdaException e) {
            // expected;
        }
    }

    public void testNSLambdaInComparator() {
        Comparator<String> cmp1 = (s1, s2) -> s1.length() - s2.length();
        Comparator<String> cmp2 = Comparator.nullsFirst(cmp1);
        Set<String> set = new TreeSet<>(cmp2);
        test(set, "[]", EqualityCheckMode.NONE);
    }

/*
    public void testSerializableLambda() {

        // TODO:

        // 1) support lambdas inside comparators - skip comparator if so

        // 2) check if Serializable. If so - invoke writeReplace() to get SerializedLambda
        // 3) find out what needs to be saved (maybe everything). At least implClass + implMethodName + capturedArgs


        Supplier s1 = (Supplier & Serializable) () -> "foo";
        Class<?> c1 = s1.getClass();

        Pattern lambdaClassNamePattern = Pattern.compile("^.+\\$\\$Lambda\\$\\d+/\\d+$");
        boolean isSynthetic = c1.isSynthetic();

        System.out.println("anon=" + c1.isAnonymousClass());
        System.out.println("if=" + c1.isInterface());
        System.out.println("synth=" + c1.isSynthetic());

        boolean pMatches = lambdaClassNamePattern.matcher(c1.getName()).matches();
        System.out.println("pMatches = " + pMatches);
//        Supplier s2 = () -> "foo";
//        ByteArrayOutputStream baos = new ByteArrayOutputStream(10_000);
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(s1);
//        System.out.println("baos = " + baos);

        System.out.println("s1 = " + s1.getClass());
//        System.out.println("s2 = " + s2.getClass());

        System.out.println("class methods = " + Arrays.toString(TestLambdas.class.getDeclaredMethods()));
        System.out.println("class fields = " + Arrays.toString(TestLambdas.class.getDeclaredFields()));
    }
*/
}
