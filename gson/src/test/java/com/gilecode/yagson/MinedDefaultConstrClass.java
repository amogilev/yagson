package com.gilecode.yagson;

public class MinedDefaultConstrClass {
	
	final String str;
	
	public MinedDefaultConstrClass() {
		throw new Error("BOOOOM!!!"); 
	}

	public MinedDefaultConstrClass(String str, int dummy) {
		this.str = str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((str == null) ? 0 : str.hashCode());
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
		MinedDefaultConstrClass other = (MinedDefaultConstrClass) obj;
		if (str == null) {
			if (other.str != null)
				return false;
		} else if (!str.equals(other.str))
			return false;
		return true;
	}
}
