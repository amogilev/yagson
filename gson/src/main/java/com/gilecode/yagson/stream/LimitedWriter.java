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

import java.io.IOException;
import java.io.Writer;

/**
 * A writer which used to limit the JSON output to the specified number of characters. If a JSON output
 * length exceeds the limit, the output is stripped to match the limit and {@link StringOutputLimitExceededException}
 * is thrown to interrupt the serialization process.
 * <p/>
 * <b>NOTE: This writer is NOT THREAD-SAFE</b>
 *
 * @author Andrey Mogilev
 */
public class LimitedWriter extends Writer {

	protected final Writer delegate;
	protected final long charsLimit;
	protected long charsCount;

	public LimitedWriter(Writer delegate, long charsLimit) {
		this.delegate = delegate;
		this.charsLimit = charsLimit;
	}

	@Override
	public void write(char[] cbuf, int off, int len) throws IOException, OutputLimitExceededException {
		long newCount = charsCount + len;
		if (newCount > charsLimit) {
			len -= (newCount - charsLimit);
			charsCount = charsLimit;
			if (len > 0) {
				delegate.write(cbuf, off, len);
			}
			throwLimitExceededException();
			return;
		} else {
			charsCount = newCount;
			delegate.write(cbuf, off, len);
		}
	}

	protected void throwLimitExceededException() throws OutputLimitExceededException {
		throw new OutputLimitExceededException();
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}
}
