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

import com.gilecode.yagson.tests.util.BindingTestCase;
import com.gilecode.yagson.tests.util.EqualityCheckMode;

import java.io.Serializable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

/**
 * Tests some simple (non-lambda) Java8 streams.
 *
 * @author Andrey Mogilev
 */
public class TestStreams extends BindingTestCase {

    public void testSimpleStream() {
        Stream<String> stream = Stream.of("foo", "bar", "bar").distinct().sorted();
        Stream<String> stream2 = test(stream);

        List<String> l = stream2.collect(Collectors.toList());
        assertEquals(asList("bar", "foo"), l);
    }

    public void testStreamWithLambda() {
        List<String> l = asList("foo", "barbaz");
        Stream<String> stream = l.stream().map((Function<String,String> & Serializable) s -> s + s.length());

        Stream<String> stream2 = test(stream, null, EqualityCheckMode.NONE);

        List<String> l2 = stream2.collect(Collectors.toList());
        assertEquals(asList("foo3", "barbaz6"), l2);
    }

}
