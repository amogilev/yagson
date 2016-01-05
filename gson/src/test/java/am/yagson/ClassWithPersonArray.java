package am.yagson;

import am.yagson.types.TypeUtils;

import java.util.Arrays;

public class ClassWithPersonArray {
    Person[] arr;

    public ClassWithPersonArray(Person...arr) {
        this.arr = arr;
    }

    public Person[] getArr() {
        return arr;
    }

    public void setArr(Person[] arr) {
        this.arr = arr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassWithPersonArray that = (ClassWithPersonArray) o;

        return TypeUtils.safeClassEquals(arr, that.arr) && Arrays.equals(arr, that.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arr);
    }

    @Override
    public String toString() {
        return "ClassWithPersonArray{" +
                "arr=" + Arrays.toString(arr) +
                '}';
    }
}
