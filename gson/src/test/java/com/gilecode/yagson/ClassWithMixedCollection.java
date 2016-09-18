package com.gilecode.yagson;

import java.util.ArrayList;
import java.util.List;

public class ClassWithMixedCollection {
	
	List<Object> l = new ArrayList<Object>();
	
	public void add(Object n) {
		l.add(n);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((l == null) ? 0 : l.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassWithMixedCollection other = (ClassWithMixedCollection) obj;
		if (l == null) {
			if (other.l != null)
				return false;
		} else if (!l.equals(other.l))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClassWithMixedCollection [l=" + l + "]";
	}
}
