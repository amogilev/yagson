package com.gilecode.yagson;

public class ChildClass {
	
	ParentClass parent;
	int val;
	
	public ChildClass(ParentClass parent, int val) {
		this.parent = parent;
		this.val = val;
	}

	@Override
	public String toString() {
		return "ChildClass [val=" + val + ", parent.id=" + parent.id + "]";
	}
}
