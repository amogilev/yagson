package am.yagson;

import com.google.gson.Gson;
import com.google.gson.internal.$Gson$Types;
import com.google.gson.reflect.TypeToken;
import junit.framework.TestCase;

import java.beans.PropertyVetoException;
import java.beans.beancontext.BeanContextChildSupport;
import java.beans.beancontext.BeanContextSupport;
import java.util.*;
import java.util.concurrent.*;

import static am.yagson.TestingUtils.MY_STRING_CMP;
import static am.yagson.TestingUtils.jsonStr;

public class TestGsonBugFixes extends TestCase {
    static class Foo1<A> {
        Foo2<? extends A> foo2;
    }

    static class Foo2<B> {
        Foo1<? super B> foo1;
    }

    /**
     * On original Gson 2.4, throws StackOverflowError, as infinite recursion occurs on
     * getAdapter for types like 'Foo2&lt;? extends ? super ? extends ... ? extends A&gt;'
     * <p/>
     * Fixed by modifing $Gson$Types methods subtypeOf & supertypeOf
     */
    public void testInfiniteRecursionOfGetAdapter() {
        new Gson().getAdapter(Foo1.class);
    }

    public void testDoubleSupertype() {
        assertEquals($Gson$Types.supertypeOf(Number.class), $Gson$Types.supertypeOf($Gson$Types.supertypeOf(Number.class)));
    }

    public void testDoubleSubtype() {
        assertEquals($Gson$Types.subtypeOf(Number.class), $Gson$Types.subtypeOf($Gson$Types.subtypeOf(Number.class)));
    }

    public void testSuperSubtype() {
        assertEquals($Gson$Types.subtypeOf(Object.class), $Gson$Types.supertypeOf($Gson$Types.subtypeOf(Number.class)));
    }

    public void testSubSupertype() {
        assertEquals($Gson$Types.subtypeOf(Object.class), $Gson$Types.subtypeOf($Gson$Types.supertypeOf(Number.class)));
    }
}