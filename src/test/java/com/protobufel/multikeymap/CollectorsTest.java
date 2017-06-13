// Copyright 2017 David Tesler
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.protobufel.multikeymap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import com.google.common.collect.testing.Helpers;
import com.google.common.testing.CollectorTester;

public class CollectorsTest {
  private Set<Set<Integer>> powerSet;
  private Set<Set<Integer>> nonEmpty;
  private List<Set<Integer>> ofSize2upList;
  private Set<Set<Integer>> ofSize3upWithNonIntersecting;
  private Set<Set<Integer>> nonEmptyResult1;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  @Before
  public void setUp() throws Exception {
    powerSet = ImmutableSet.<Set<Integer>>builder()
        .addAll(Sets.powerSet(ImmutableSet.of(1, 2, 3, 4, 5))).build();
    nonEmpty = powerSet.stream().filter(set -> set.size() > 0).collect(toSet());
    ofSize2upList = nonEmpty.stream().filter(set -> set.size() > 1).collect(toList());
    ofSize3upWithNonIntersecting =
        ImmutableSet.<Set<Integer>>builder().addAll(nonEmpty).add(ImmutableSet.of(6, 7, 8)).build();
    nonEmptyResult1 = ImmutableSet.<Set<Integer>>builder().add(ImmutableSortedSet.of(1, 2, 3, 4, 5))
        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(1, 3)))
        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(2, 4)))
        .add(Sets.subSet(ImmutableSortedSet.of(1, 2, 3, 4, 5), Range.closed(2, 5))).build();
  }

  @Test
  public void testToMultiKeyMap() {
    final List<Entry<Iterable<String>, Integer>> input =
        Arrays.asList(Helpers.mapEntry(Arrays.asList("one", "two", "three"), 1),
            Helpers.mapEntry(Arrays.asList("two", "three"), 2),
            Helpers.mapEntry(Arrays.asList("two", "one", "three"), 3));

    @SuppressWarnings("unchecked")
    final Entry<Iterable<String>, Integer>[] castArray =
        (Entry<Iterable<String>, Integer>[]) input.toArray();

    final MultiKeyMap<String, Iterable<String>, Integer> expected =
        MultiKeyMaps.<String, Iterable<String>, Integer>newMultiKeyMap();
    input.stream().forEach(entry -> expected.put(entry.getKey(), entry.getValue()));

    final Function<? super Entry<Iterable<String>, Integer>, ? extends Iterable<String>> keyMapper =
        entry -> entry.getKey();
    final Function<? super Entry<Iterable<String>, Integer>, ? extends Integer> valueMapper =
        entry -> entry.getValue();
    final BinaryOperator<Integer> mergeFunction = (k, v) -> v;
    final Supplier<MultiKeyMap<String, Iterable<String>, Integer>> multiKeyMapSupplier =
        MultiKeyMaps::<String, Iterable<String>, Integer>newMultiKeyMap;

    softly.assertThatCode(() -> {
      CollectorTester.of(Collectors
          .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer, MultiKeyMap<String, Iterable<String>, Integer>>toMultiKeyMap(
              keyMapper, valueMapper, mergeFunction, multiKeyMapSupplier))
          .expectCollects(expected, castArray);
    }).doesNotThrowAnyException();


    softly.assertThatCode(() -> {
      CollectorTester.of(Collectors
          .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer>toMultiKeyMap(
              keyMapper, valueMapper, mergeFunction))
          .expectCollects(expected, castArray);
    }).doesNotThrowAnyException();

    softly.assertThatCode(() -> {
      CollectorTester.of(Collectors
          .<Entry<Iterable<String>, Integer>, String, Iterable<String>, Integer>toMultiKeyMap(
              keyMapper, valueMapper))
          .expectCollects(expected, castArray);
    }).doesNotThrowAnyException();
  }

  @Test
  public void testSetIntersectingSet() {
    collectorTesterHelper(powerSet, Collections.emptySet());
    collectorTesterHelper(nonEmpty, Collections.emptySet());
    collectorTesterHelper(ofSize2upList, Collections.emptySet());
    collectorTesterHelper(ofSize3upWithNonIntersecting, Collections.emptySet());
    collectorTesterHelper(nonEmptyResult1, ImmutableSet.of(2, 3));
  }

  private void collectorTesterHelper(final Collection<Set<Integer>> source,
      final Set<Integer> expected) {
    @SuppressWarnings("unchecked")
    final Set<Integer>[] castArray = (Set<Integer>[]) source.toArray(new Set<?>[0]);
    softly.assertThatCode(() -> {
      CollectorTester.of(Collectors.setIntersecting(getSmallest(source), false))
          .expectCollects(expected, castArray);
    }).doesNotThrowAnyException();

    softly.assertThatCode(() -> {
      CollectorTester.of(Collectors.setIntersecting(getSmallest(source), true))
          .expectCollects(expected, castArray);
    }).doesNotThrowAnyException();
  }

  private <T> Set<T> getSmallest(final Iterable<Set<T>> sets) {
    return Streams.stream(sets).min(Comparator.comparingInt(set -> set.size()))
        .orElse(Collections.<T>emptySet());
  }
}
