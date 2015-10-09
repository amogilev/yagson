package am.yagson;

import java.util.ArrayList;
import java.util.List;

public class ParentClass {
	
	List<ChildClass> children = new ArrayList<ChildClass>();
	int id;

	public ParentClass(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "ParentClass [children=" + children + ", id=" + id + "]";
	}

	public void addChild(ChildClass child) {
		children.add(child);
	}
}
