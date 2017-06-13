package com.protobufel.multikeymap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.testing.Helpers;

@RunWith(JUnit4.class)
public class MultiKeyMapSpecificTest {
  private static final List<String> KEY1 = ImmutableList.of("one", "two", "three");
  private static final List<String> KEY2 = ImmutableList.of("two", "three");
  private static final List<String> KEY3 = ImmutableList.of("two", "one", "three");

  private MultiKeyMap<String, Iterable<String>, Integer> multiKeyMap;

  @Rule
  public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

  private Iterable<String> emptyPartialKey;
  private ImmutableList<String> commonPartialKey;
  private ImmutableList<String> absentPartialKey;
  private List<String> withNullsPartialKey;
  private List<String> nullPartialKey;
  private List<Integer> manyNegativePositions;
  private List<Integer> oneNegativePosition;
  private List<Integer> manyPositivePositions;
  private List<Integer> firstPositivePosition;
  private ImmutableList<Integer> firstThirdPositivePositions;
  private ImmutableList<Integer> twoWrongPositivePositions;
  private ImmutableList<Integer> firstNegativeSecondPositions;
  private ImmutableList<Integer> negativeSecondFirstPositions;
  private ImmutableList<Integer> wrongFirstNegativePositions;

  @Before
  public void setUp() throws Exception {
    final List<Entry<Iterable<String>, Integer>> input = ImmutableList.of(Helpers.mapEntry(KEY1, 1),
        Helpers.mapEntry(KEY2, 2), Helpers.mapEntry(KEY3, 3));

    multiKeyMap = MultiKeyMaps.<String, Iterable<String>, Integer>newMultiKeyMap();
    input.stream().forEach(entry -> multiKeyMap.put(entry.getKey(), entry.getValue()));

    emptyPartialKey = Collections.emptyList();
    commonPartialKey = ImmutableList.of("two");
    absentPartialKey = ImmutableList.of("not", "here", "at", "all");
    withNullsPartialKey = Arrays.asList("a", null);
    nullPartialKey = null;

    manyNegativePositions = Collections.nCopies(5, -1);
    oneNegativePosition = Collections.nCopies(1, -1);

    manyPositivePositions = IntStream.rangeClosed(0, 5).boxed().collect(Collectors.toList());
    firstPositivePosition = Collections.nCopies(1, 0);
    firstThirdPositivePositions = ImmutableList.of(0, 2);
    twoWrongPositivePositions = ImmutableList.of(100, 50);

    firstNegativeSecondPositions = ImmutableList.of(0, -1, 1);
    negativeSecondFirstPositions = ImmutableList.of(-1, 1, 0);
    wrongFirstNegativePositions = ImmutableList.of(100, -1, -1);
  }


  @Test
  public void testGetFullKeysByPartialKeyNoPositions() {
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(emptyPartialKey)).isNotPresent();
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(absentPartialKey)).isNotPresent();
    softly.assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(withNullsPartialKey))
        .isInstanceOf(NullPointerException.class);
    softly.assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(nullPartialKey))
        .isInstanceOf(NullPointerException.class);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey)).isPresent()
        .hasValueSatisfying(stream -> {
          softly.assertThat(stream).hasSameSizeAs(multiKeyMap)
              .containsOnlyElementsOf(multiKeyMap.keySet());
        });
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1)).isPresent()
        .hasValueSatisfying(stream -> {
          softly.assertThat(stream).hasSize(1).containsOnly(KEY1);
        });
  }

  @Ignore
  @Test
  public void testGetFullKeysByPartialKeyWithPositions() {}

}
