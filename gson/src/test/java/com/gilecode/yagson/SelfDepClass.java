package com.gilecode.yagson;

public class SelfDepClass {
	int id;
	SelfDepClass self;
	
	public SelfDepClass(int id) {
		this.id = id;
		this.self = this;
	}

	@Override
	public String toString() {
		return "SelfDepClass [id=" + id + ", self_check: " + (this == self ? "PASSED" : "FAILED") + "]";
	}
}