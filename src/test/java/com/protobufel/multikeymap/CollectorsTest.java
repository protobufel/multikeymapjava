package com.protobufel.multikeymap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
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

  @After
  public void tearDown() throws Exception {}

  private <T> Set<T> getSmallest(final Iterable<Set<T>> sets) {
    return Streams.stream(sets).min(Comparator.comparingInt(set -> set.size()))
        .orElse(Collections.<T>emptySet());
  }

  @Test
  public void testSetIntersectingSetOfKBoolean() {
    collectorTesterHelper(powerSet, Collections.emptySet());
    collectorTesterHelper(nonEmpty, Collections.emptySet());
    collectorTesterHelper(ofSize2upList, Collections.emptySet());
    collectorTesterHelper(ofSize3upWithNonIntersecting, Collections.emptySet());
    collectorTesterHelper(nonEmptyResult1, ImmutableSet.of(2, 3));
  }

  @SuppressWarnings("unchecked")
  private void collectorTesterHelper(final Collection<Set<Integer>> source,
      final Set<Integer> expected) {
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
}
