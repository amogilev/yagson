package com.gilecode.yagson;

public class ClassWithPerson {
    Person person;

    public ClassWithPerson(Person person) {
        this.person = person;
    }

    public Person getPerson() {
        return person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassWithPerson that = (ClassWithPerson) o;

        return !(person != null ? !person.equals(that.person) : that.person != null);

    }

    @Override
    public int hashCode() {
        return person != null ? person.hashCode() : 0;
    }
}
