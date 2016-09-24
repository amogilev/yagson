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

import java.util.Arrays;

/**
 * A test data class used for testing binding with a formal 'Object' class
 *
 * @author Andrey Mogilev
 */
public class ClassWithObject {
	
	public Object obj;

	public ClassWithObject(Object obj) {
		super();
		this.obj = obj;
	}

	@Override
	public String toString() {
		return "ClassWithObject{" +
				"obj=" + (obj == null ? "null" : obj.getClass().isArray() ? Arrays.deepToString((Object[])obj) : obj) +
				'}';
	}
}
