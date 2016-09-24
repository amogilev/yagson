/*
 * Copyright (C) 2016 Andrey Mogilev
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gilecode.yagson.tests.data;

/**
 * A test data class represented an extended 'person', with an addition of the address.
 *
 * @author Andrey Mogilev
 */
public class PersonEx extends Person {

    private String address;

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
