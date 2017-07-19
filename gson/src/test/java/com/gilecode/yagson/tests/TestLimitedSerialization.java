/*
 * Copyright (C) 2017 Andrey Mogilev
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

import com.gilecode.yagson.stream.StringOutputLimitExceededException;
import com.gilecode.yagson.tests.util.BindingTestCase;

/**
 * Test for limited JSON serialization, with a specified maximum number of output characters.
 *
 * @author Andrey Mogilev
 */
public class TestLimitedSerialization extends BindingTestCase {

	public void testLimitedString() {
		try {
			String json = defaultMapper.toJson("1234567890", 7);
			fail("StringOutputLimitExceededException is expected");
		} catch (StringOutputLimitExceededException e) {
			// expected
			assertEquals("\"123456", e.getLimitedResult());
		}
	}
}
