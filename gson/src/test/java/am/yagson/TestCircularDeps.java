package am.yagson;

import junit.framework.TestCase;

public class TestCircularDeps extends TestCase {

	public void testParentChild() {
		ParentClass parent = new ParentClass(10);

		parent.addChild(new ChildClass(parent, 1));
		parent.addChild(new ChildClass(parent, 2));
		parent.addChild(new ChildClass(parent, 3));

		TestingUtils.testFullyByToString(parent);
	}

	public void testSelfDependency1() {
		TestingUtils.testFullyByToString(new SelfDepClass(10));
	}

	public void testSelfDependency2() {
		Node n = new Node(1);
		n.connections.add(n);

		TestingUtils.testFully(n);
	}

	public void testDAG() {
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(3);


		n1.connections.add(n2);
		n2.connections.add(n3);
		n2.connections.add(n4);
		n3.connections.add(n4);

		TestingUtils.testFully(n1);
	}

	public void testCyclicGraph() {
		Node n1 = new Node(1);
		Node n2 = new Node(2);
		Node n3 = new Node(3);
		Node n4 = new Node(3);


		n1.connections.add(n2);
		n2.connections.add(n3);
		n2.connections.add(n4);
		n3.connections.add(n4);
		n4.connections.add(n2);

		TestingUtils.testFully(n1);
	}


}
