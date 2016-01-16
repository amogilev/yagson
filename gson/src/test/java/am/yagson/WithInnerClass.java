package am.yagson;

public class WithInnerClass {
	
	class Inner {
		String innerStr = "inner";

		public String getOuterStr() {
			return WithInnerClass.this.outerStr;
		}

		@Override
		public String toString() {
			return "Inner{" +
					"outerStr='" + outerStr + '\'' +
					"innerStr='" + innerStr +
					'}';
		}
	}
	
	public String outerStr;
	public Inner inner;
	

	public WithInnerClass() {
		super();
	}

	public void makeInner(String str) {
		inner = new Inner();
		inner.innerStr = str;
	}

	@Override
	public String toString() {
		return "WithInnerClass{" +
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
		WithInnerClass other = (WithInnerClass) obj;
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
