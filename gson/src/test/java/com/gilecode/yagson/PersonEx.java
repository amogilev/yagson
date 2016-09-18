package com.gilecode.yagson;

public class PersonEx extends Person {

    String address;

    public PersonEx(String name, String family, String address) {
        super(name, family);
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PersonEx personEx = (PersonEx) o;

        return !(address != null ? !address.equals(personEx.address) : personEx.address != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (address != null ? address.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "PersonEx{" +
                "address='" + address + ", name=" + name + ", family=" + family +
                "}";
    }
}
