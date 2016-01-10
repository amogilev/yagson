package am.yagson;

import junit.framework.TestCase;

import java.util.*;

import static am.yagson.TestingUtils.jsonStr;

public class TestCustomCollections extends TestCase {

    public void testUnmodifiableCollection() {
        List<Long> l = new ArrayList<Long>();
        l.add(1L);

        Collection<Long> obj = Collections.unmodifiableCollection(l);
        TestingUtils.testFully(obj, "[1]");
    }

    // TODO: tests singleton, synchronized, and many others!
}
