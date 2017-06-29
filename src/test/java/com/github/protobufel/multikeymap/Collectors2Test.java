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

import com.google.common.collect.*;
import com.google.common.testing.CollectorTester;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@RunWith(Parameterized.class)
public class Collectors2Test {
    private static final Set<Set<Integer>> powerSet;
    private static final Set<Set<Integer>> nonEmpty;
    private static final Set<Set<Integer>> ofSize2upList;
    private static final Set<Set<Integer>> ofSize3upWithNonIntersecting;
    private static final Set<Set<Integer>> nonEmptyResult1;

    static {
        powerSet =
                ImmutableSet.<Set<Integer>>builder()
                        .addAll(Sets.powerSet(ImmutableSet.of(1, 2, 3, 4, 5)))
                        .build();
        nonEmpty = powerSet.stream().filter(set -> set.size() > 0).collect(toSet());
        ofSize2upList = nonEmpty.stream().filter(set -> set.size() > 1).collect(toSet());
        ofSize3upWithNonIntersecting =
                ImmutableSet.<Set<Integer>>builder().addAll(nonEmpty).add(ImmutableSet.of(6, 7, 8)).build();
        nonEmptyResult1 =
                ImmutableSet.<Set<Integer>>builder()
                        .add(ImmutableSortedSet.of(1, 2, 3, 4, 5))
                        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(1, 3)))
                        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(2, 4)))
                        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(2, 5)))
                        .build();
    }

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Parameter(0)
    public Set<Set<Integer>> input;

    @Parameter(1)
    public Set<Integer> expected;

    @Parameters()
    public static Collection<Object[]> data() {
        return ImmutableList.of(
                new Object[]{powerSet, Collections.emptySet()},
                new Object[]{nonEmpty, Collections.emptySet()},
                new Object[]{ofSize2upList, Collections.emptySet()},
                new Object[]{ofSize3upWithNonIntersecting, Collections.emptySet()},
                new Object[]{nonEmptyResult1, ImmutableSet.of(2, 3)});
    }

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testIntersectSets() {
        softly
                .assertThat(Collectors.intersectSets(input, false))
                .hasSameElementsAs(Collectors.intersectSets(input, true));

        ImmutableList.of(false, true)
                .forEach(
                        parallel ->
                                softly
                                        .assertThat(Collectors.intersectSets(input, parallel))
                                        .doesNotContainNull()
                                        .hasSameElementsAs(expected));
    }

    @Test
    public void testSetIntersectingSet() {
        collectorTesterHelper(input, expected);
    }

    private void collectorTesterHelper(
            final Collection<Set<Integer>> source, final Set<Integer> expected) {
        @SuppressWarnings("unchecked") final Set<Integer>[] castArray = (Set<Integer>[]) source.toArray(new Set<?>[0]);
        softly
                .assertThatCode(
                        () ->
                                CollectorTester.of(Collectors.setIntersecting(getSmallest(source), false))
                                        .expectCollects(expected, castArray))
                .doesNotThrowAnyException();

        softly
                .assertThatCode(
                        () ->
                                CollectorTester.of(Collectors.setIntersecting(getSmallest(source), true))
                                        .expectCollects(expected, castArray))
                .doesNotThrowAnyException();
    }

    private <T> Set<T> getSmallest(final Iterable<Set<T>> sets) {
        return Streams.stream(sets)
                .min(Comparator.comparingInt(Set::size))
                .orElse(Collections.emptySet());
    }
}
