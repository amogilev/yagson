package am.yagson;

import java.util.Arrays;

public class ClassWithMixedArray<Base> {
    Base[] arr;

    public ClassWithMixedArray(Base...arr) {
        this.arr = arr;
    }

    public Base[] getArr() {
        return arr;
    }

    public void setArr(Base[] arr) {
        this.arr = arr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassWithMixedArray that = (ClassWithMixedArray) o;

        return TypeUtils.safeClassEquals(arr, that.arr) && Arrays.equals(arr, that.arr);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(arr);
    }

    @Override
    public String toString() {
        return "ClassWithMixedArray{" +
                "arr=" + Arrays.toString(arr) +
                '}';
    }
}
