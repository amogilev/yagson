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
package com.gilecode.yagson.stream;

import java.io.StringWriter;

/**
 * A writer to String, which limits the output to the specified number of characters. If a JSON output
 * length exceeds the limit, the output is stripped to match the limit and {@link StringOutputLimitExceededException}
 * is thrown to interrupt the serialization process.
 * <p/>
 * In order to obtain the resulting string, use {@link #toString()} method.
 * <p/>
 * <b>NOTE: This writer is NOT THREAD-SAFE</b>
 *
 * @author Andrey Mogilev
 */
public class LimitedStringWriter extends LimitedWriter {

	public LimitedStringWriter(long charsLimit) {
		super(new StringWriter(), charsLimit);
	}

	@Override
	protected void throwLimitExceededException() throws StringOutputLimitExceededException {
		throw new StringOutputLimitExceededException(delegate.toString());
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
}
