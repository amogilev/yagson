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
package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;

/**
 * Tests serialization of object fields with the duplicate (override) names.
 *
 * @author Andrey Mogilev
 */
public class TestDuplicateFields extends BindingTestCase {

    private static class C1 {
        private String s;

        C1(String s) {
            this.s = s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof C1)) return false;

            C1 c1 = (C1) o;
            return s.equals(c1.s);
        }

        @Override
        public int hashCode() {
            return s.hashCode();
        }
    }

    private static class C2 extends C1 {
        C2(String c1_s) {
            super(c1_s);
        }
    }

    private static class C3 extends C2 {
        private String s;

        C3(String c1_s, String с3_s) {
            super(c1_s);
            this.s = с3_s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof C3)) return false;
            if (!super.equals(o)) return false;

            C3 c3 = (C3) o;

            return s.equals(c3.s);

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }
    }

    private static class C4 extends C3 {
        private String s;

        C4(String c1_s, String с3_s, String c4_s) {
            super(c1_s, с3_s);
            this.s = c4_s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof C4)) return false;
            if (!super.equals(o)) return false;

            C4 c4 = (C4) o;

            return s.equals(c4.s);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + s.hashCode();
            return result;
        }
    }

    private static class C5 extends C4 {
        C5(String c1_s, String с3_s, String c4_s) {
            super(c1_s, с3_s, c4_s);
        }
    }


    public void testDuplicateFields() {
        C5 obj = new C5("foo", "bar", "baz");
        test(obj, jsonStr("{'s':'baz','s^2':'bar','s^4':'foo'}"));
    }
}
