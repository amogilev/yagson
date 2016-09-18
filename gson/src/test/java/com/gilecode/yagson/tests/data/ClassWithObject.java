package com.gilecode.yagson.tests.data;

import java.util.Arrays;

/**
 * A test data class used for testing binding with a formal 'Object' class
 *
 * @author Andrey Mogilev
 */
public class ClassWithObject {
	
	public Object obj;

	public ClassWithObject(Object obj) {
		super();
		this.obj = obj;
	}

	@Override
	public String toString() {
		return "ClassWithObject{" +
				"obj=" + (obj == null ? "null" : obj.getClass().isArray() ? Arrays.deepToString((Object[])obj) : obj) +
				'}';
	}
}
