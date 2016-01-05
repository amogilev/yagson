package am.yagson;

import am.yagson.refs.ReferencesPolicy;
import am.yagson.types.TypeInfoPolicy;
import junit.framework.TestCase;

import static am.yagson.TestingUtils.jsonStr;

public class TestCircularDeps extends TestCase {

	// instances of YaGson for two references policies being tested
	// type info is disabled for convenience

	private static YaGson gsonCircularOnlyMode = new YaGsonBuilder()
			.setReferencesPolicy(ReferencesPolicy.CIRCULAR_ONLY)
			.setTypeInfoPolicy(TypeInfoPolicy.DISABLED)
			.create();

	private static YaGson gsonAllDuplicatesMode = new YaGsonBuilder()
			.setReferencesPolicy(ReferencesPolicy.DUPLICATE_OBJECTS)
			.setTypeInfoPolicy(TypeInfoPolicy.DISABLED)
			.create();

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

		TestingUtils.testFullyByToString(gsonCircularOnlyMode, parent, expected);
		TestingUtils.testFullyByToString(gsonAllDuplicatesMode, parent, expected);
	}

	public void testSelfDependency1() {
		SelfDepClass obj = new SelfDepClass(10);

		String expected = jsonStr("{'id':10,'self':'@root'}");
		TestingUtils.testFullyByToString(gsonCircularOnlyMode, obj, expected);
		TestingUtils.testFullyByToString(gsonAllDuplicatesMode, obj, expected);
	}

	public void testSelfDependency2() {
		Node n = new Node(1);
		n.connections.add(n);

		String expected = jsonStr("{'id':1,'connections':['@root']}");
		TestingUtils.testFully(gsonCircularOnlyMode, n, expected);
		TestingUtils.testFully(gsonAllDuplicatesMode, n, expected);
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

		TestingUtils.testFully(gsonCircularOnlyMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':[]}]}," +
						"{'id':4,'connections':[]}]}]}"));

		TestingUtils.testFully(gsonAllDuplicatesMode, n1, jsonStr(
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

		TestingUtils.testFully(gsonCircularOnlyMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':['@root.connections.0']}]}," +
						"{'id':4,'connections':['@root.connections.0']}]}]}"));

		TestingUtils.testFully(gsonAllDuplicatesMode, n1, jsonStr(
				"{'id':1,'connections':[{'id':2,'connections':[" +
						"{'id':3,'connections':[{'id':4,'connections':['@root.connections.0']}]}," +
						"'@root.connections.0.connections.0.connections.0']}]}"));

	}
}
