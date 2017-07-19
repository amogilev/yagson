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

/**
 * An exception which thrown by {@link LimitedStringWriter} when the JSON output length exceeds
 * the specified limit. In such case, the output is stripped to match the limit and this
 * exception is thrown to interrupt the serialization process.
 * <p/>
 * The stripped output may be obtained using the method {@link #getLimitedResult()}
 *
 * @author Andrey Mogilev
 */
public class StringOutputLimitExceededException extends OutputLimitExceededException {

	private final String limitedResult;

	public StringOutputLimitExceededException(String limitedResult) {
		this.limitedResult = limitedResult;
	}

	public String getLimitedResult() {
		return limitedResult;
	}

}
