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
 * Test for instantiation of classes with no default constructor and with a 'bad'
 * default constructor which throws exceptions.
 *
 * @author Andrey Mogilev
 */
public class TestInstantiation extends BindingTestCase {
	
	public void testNoDefaultConstr() {
		test(new NoDefaultConstrClass("foo", 11));
	}
	
	public void testMinedDefaultConstr() {
		test(new MinedDefaultConstrClass("foo", 11));
	}

	private static class NoDefaultConstrClass {
        String str;

        NoDefaultConstrClass(String str, int dummy) {
            this.str = str;
        }
    }

	private static class MinedDefaultConstrClass {

        final String str;

        public MinedDefaultConstrClass() {
            throw new Error("BOOOOM!!!");
        }

        MinedDefaultConstrClass(String str, int dummy) {
            this.str = str;
        }
    }
}
