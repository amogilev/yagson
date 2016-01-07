package am.yagson;

import java.lang.reflect.Array;
import java.util.Arrays;

public class ClassWithObject {
	
	Object obj;

	public ClassWithObject(Object obj) {
		super();
		this.obj = obj;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((obj == null) ? 0 : obj.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (getClass() != o.getClass())
			return false;
		ClassWithObject other = (ClassWithObject) o;
		return TestingUtils.objectsEqual(this.obj, other.obj);
	}

	@Override
	public String toString() {
		return "ClassWithObject{" +
				"obj=" + (obj == null ? "null" : obj.getClass().isArray() ? Arrays.deepToString((Object[])obj) : obj) +
				'}';
	}
}
