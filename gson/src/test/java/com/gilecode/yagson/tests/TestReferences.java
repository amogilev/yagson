package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests various 'simple' cases with references, including parent-child relationship, DAGs,
 * cyclic graphs, self-dependency etc.
 *
 * @author Andrey Mogilev
 */
public class TestReferences extends BindingTestCase {

	public void testParentChild() {
		ParentClass parent = new ParentClass(10);

		parent.addChild(new ChildClass(parent, 1));
		parent.addChild(new ChildClass(parent, 2));
		parent.addChild(new ChildClass(parent, 3));

		String expected = jsonStr("{'children':[" +
				"{'parent':'@root','val':1}," +
				"{'parent':'@root','val':2}," +
				"{'parent':'@root','val':3}]," +
				"'id':10}");

		test(gsonCircularOnlyMode, parent, expected);
		test(gsonAllDuplicatesMode, parent, expected);
	}

	public void testSelfDependency1() {
		SelfDepClass obj = new SelfDepClass(10);

		String expected = jsonStr("{'id':10,'self':'@root'}");
		test(gsonCircularOnlyMode, obj, expected);
		test(gsonAllDuplicatesMode, obj, expected);
	}

	public void testSelfDependency2() {
		Node n = new Node(1);
		n.connections.add(n);

		String expected = jsonStr("{'id':1,'connections':['@root']}");
		test(gsonCircularOnlyMode, n, expected);
		test(gsonAllDuplicatesMode, n, expected);
	}

	public void testDAG() {
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(4);


		n1.connections.add(n2);
		n2.connections.add(n3);
		n2.connections.add(n4);
		n3.connections.add(n4);

		test(gsonCircularOnlyMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':[]}]}," +
						"{'id':4,'connections':[]}]}]}"));

		test(gsonAllDuplicatesMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':[]}]}," +
						"'@root.connections.0.connections.0.connections.0']}]}"));
	}

	public void testCyclicGraph() {
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(4);


		n1.connections.add(n2);
		n2.connections.add(n3);
		n2.connections.add(n4);
		n3.connections.add(n4);
		n4.connections.add(n2);

		test(gsonCircularOnlyMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':['@root.connections.0']}]}," +
						"{'id':4,'connections':['@root.connections.0']}]}]}"));

		test(gsonAllDuplicatesMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':['@root.connections.0']}]}," +
						"'@root.connections.0.connections.0.connections.0']}]}"));

	}

	// tests that 'simple' objects (like strings) are never serialized as references
	public void testDuplicateStringsNeverReferenced() {
		Object[] arr = new Object[2];
		arr[0] = arr[1] = "foo";
		test(gsonAllDuplicatesMode, arr, jsonStr("['foo','foo']"));
	}

	public void testObjectRefs() {
		Object[] arr = new Object[2];
		Node n = new Node(1);
		arr[0] = arr[1] = n;
		test(arr, jsonStr("[{'@type':'com.gilecode.yagson.tests.TestReferences$Node','@val':{'id':1,'connections':[]}},'@.0']"));
	}

	private static class ParentClass {

        List<ChildClass> children = new ArrayList<ChildClass>();
        int id;

        ParentClass(int id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "ParentClass [children=" + children + ", id=" + id + "]";
        }

        void addChild(ChildClass child) {
            children.add(child);
        }
    }

	private static class ChildClass {

        ParentClass parent;
        int val;

        ChildClass(ParentClass parent, int val) {
            this.parent = parent;
            this.val = val;
        }

        @Override
        public String toString() {
            return "ChildClass [val=" + val + ", parent.id=" + parent.id + "]";
        }
    }

    private static class SelfDepClass {
        int id;
        SelfDepClass self;

        SelfDepClass(int id) {
            this.id = id;
            this.self = this;
        }

        @Override
        public String toString() {
            return "SelfDepClass [id=" + id + ", self_check: " + (this == self ? "PASSED" : "FAILED") + "]";
        }
    }

    private static class Node {

        int id;
        List<Node> connections = new ArrayList<Node>();

        Node(int id) {
            this.id = id;
        }
    }
}
