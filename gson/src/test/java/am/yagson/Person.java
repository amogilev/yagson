package am.yagson;

public class Person {
	
	String name;
	String family;
	
	public Person(String name, String family) {
		this.name = name;
		this.family = family;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", family=" + family + "]";
	}
}
