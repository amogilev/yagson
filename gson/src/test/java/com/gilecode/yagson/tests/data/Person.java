package com.gilecode.yagson.tests.data;

/**
 * A test data class represented a general 'person' with the name and the family name.
 *
 * @author Andrey Mogilev
 */
public class Person {

	public String name;
	public String family;

	public Person(String name, String family) {
		this.name = name;
		this.family = family;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", family=" + family + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((family == null) ? 0 : family.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Person other = (Person) obj;
		if (family == null) {
			if (other.family != null)
				return false;
		} else if (!family.equals(other.family))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
