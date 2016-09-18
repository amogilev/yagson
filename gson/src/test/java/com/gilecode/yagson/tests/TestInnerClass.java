package com.gilecode.yagson.tests;

import com.gilecode.yagson.tests.util.BindingTestCase;

/**
 * Tests serialization of inner and local classes.
 *
 * @author Andrey Mogilev
 */
public class TestInnerClass extends BindingTestCase {
	
	public void testClassWithInner() {
		OuterClass1 obj = new OuterClass1();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		OuterClass1 found = test(obj, jsonStr(
				"{'outerStr':'bar','inner':{'innerStr':'foo','this$0':'@root'}}"));
		assertEquals("bar", found.inner.getOuterStr());
		
	}
	
	public void testInner() {
		OuterClass1 obj = new OuterClass1();
		obj.outerStr = "bar";
		obj.makeInner("foo");
		
		OuterClass1.Inner found = test(obj.inner, jsonStr(
				"{'innerStr':'foo','this$0':{'outerStr':'bar','inner':'@root'}}"));

		assertEquals("foo", found.innerStr);
		assertEquals("bar", found.getOuterStr());
	}

	public void testLocalClass() {
		testString = "foo";
		LocalClass2 obj = new LocalClass2("outerStr1", "innerStr1", "outerStr2", "innerStr2");
		test(obj, jsonStr(
				"{" +
						"'s':'outerStr2'," +
						"'lic2':{'s':'innerStr2','this$1':'@root','s^1':'innerStr1','this$1^1':'@root'}," +
						"'this$0':{'testString':'foo','fName':'testLocalClass'}," +
						"'s^1':'outerStr1'," +
						"'lic1':{'s':'innerStr1','this$1':'@root'},'this$0^1':'@.this$0'}"));
	}

	private static class OuterClass1 {

        class Inner {
            String innerStr = "inner";

            String getOuterStr() {
                return OuterClass1.this.outerStr;
            }

            @Override
            public String toString() {
                return "Inner{" +
                        "outerStr='" + outerStr + '\'' +
                        "innerStr='" + innerStr +
                        '}';
            }
        }

        String outerStr;
        Inner inner;


        OuterClass1() {
            super();
        }

        void makeInner(String str) {
            inner = new Inner();
            inner.innerStr = str;
        }

        @Override
        public String toString() {
            return "OuterClass1{" +
                    "outerStr='" + outerStr + '\'' +
                    ", innerStr=" + inner.innerStr +
                    '}';
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((inner == null) ? 0 : inner.hashCode());
            result = prime * result
                    + ((outerStr == null) ? 0 : outerStr.hashCode());
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
            OuterClass1 other = (OuterClass1) obj;
            if (inner == null) {
                if (other.inner != null)
                    return false;
            } else if (inner.innerStr != other.inner.innerStr && (inner.innerStr == null || !inner.innerStr.equals(other.inner.innerStr)))
                return false;
            if (outerStr == null) {
                if (other.outerStr != null)
                    return false;
            } else if (!outerStr.equals(other.outerStr))
                return false;
            return true;
        }
    }

	private String testString;

	private class LocalClass1 {
		String s;
		LocalInnerClass1 lic1;

		LocalClass1(String outerStr1, String innerStr1) {
			this.s = outerStr1;
			lic1 = new LocalInnerClass1(innerStr1);
		}

		class LocalInnerClass1 {
			String s;

			LocalInnerClass1(String s) {
				this.s = s;
			}
		}

		@Override
		public String toString() {
			return "LocalClass1{" + "outerStr='" + s + "', innerStr='" + lic1.s + "', testString='" + testString + "'}";
		}
	}

	private class LocalClass2 extends LocalClass1 {
		String s;
		LocalInnerClass2 lic2;

		LocalClass2(String outerStr1, String innerStr1, String outerStr2, String innerStr2) {
			super(outerStr1, innerStr1);
			this.s = outerStr2;
			this.lic2 = new LocalInnerClass2(innerStr1, innerStr2);
		}

		private class LocalInnerClass2 extends LocalInnerClass1 {
			String s;

			LocalInnerClass2(String lic1_s, String s) {
				super(lic1_s);
				this.s = s;
			}
		}

		@Override
		public String toString() {
			return "LocalClass2{" +
					"outer='" + s + '\'' +
					", inner='" + lic2.s +
					"'} " + super.toString();
		}
	}
}
