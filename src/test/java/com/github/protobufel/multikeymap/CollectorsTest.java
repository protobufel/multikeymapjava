/*
 *    Copyright 2017 David Tesler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.protobufel.multikeymap;

import com.google.common.collect.testing.Helpers;
import com.google.common.testing.CollectorTester;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public class CollectorsTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void utilityClassTest() throws Exception {
        softly
                .assertThat(Collectors.class)
                .isFinal()
                .isPublic()
                .satisfies(clazz -> softly.assertThat(clazz.getConstructors()).isEmpty());
    }

    @Test
    public void testToMultiKeyMap() {
        final List<Entry<Iterable<String>, Integer>> input =
                Arrays.asList(
                        Helpers.mapEntry(Arrays.asList("one", "two", "three"), 1),
                        Helpers.mapEntry(Arrays.asList("two", "three"), 2),
                        Helpers.mapEntry(Arrays.asList("two", "one", "three"), 3));

        @SuppressWarnings("unchecked") final Entry<Iterable<String>, Integer>[] castArray =
                (Entry<Iterable<String>, Integer>[]) input.toArray();

        final MultiKeyMap<String, Iterable<String>, Integer> expected = MultiKeyMaps.newMultiKeyMap();
        input.stream().forEach(entry -> expected.put(entry.getKey(), entry.getValue()));

        final Function<? super Entry<Iterable<String>, Integer>, ? extends Iterable<String>> keyMapper =
                Entry::getKey;
        final Function<? super Entry<Iterable<String>, Integer>, ? extends Integer> valueMapper =
                Entry::getValue;
        final BinaryOperator<Integer> mergeFunction = (k, v) -> v;
        final Supplier<MultiKeyMap<String, Iterable<String>, Integer>> multiKeyMapSupplier =
                MultiKeyMaps::<String, Iterable<String>, Integer>newMultiKeyMap;

        softly
                .assertThatCode(
                        () ->
                                CollectorTester.of(
                                        Collectors
                                                .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer,
                                                        MultiKeyMap<String, Iterable<String>, Integer>>
                                                        toMultiKeyMap(
                                                        keyMapper, valueMapper, mergeFunction, multiKeyMapSupplier))
                                        .expectCollects(expected, castArray))
                .doesNotThrowAnyException();

        softly
                .assertThatCode(
                        () ->
                                CollectorTester.of(
                                        Collectors
                                                .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer>
                                                        toMultiKeyMap(keyMapper, valueMapper, mergeFunction))
                                        .expectCollects(expected, castArray))
                .doesNotThrowAnyException();

        softly
                .assertThatCode(
                        () ->
                                CollectorTester.of(
                                        Collectors
                                                .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer>
                                                        toMultiKeyMap(keyMapper, valueMapper))
                                        .expectCollects(expected, castArray))
                .doesNotThrowAnyException();
    }
}
