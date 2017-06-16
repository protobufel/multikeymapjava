package com.github.protobufel.multikeymap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.github.protobufel.multikeymap.MultiKeyMap;
import com.github.protobufel.multikeymap.MultiKeyMaps;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.testing.Helpers;

@RunWith(JUnit4.class)
public class MultiKeyMapSpecificTest {
  private static final List<String> KEY1 = ImmutableList.of("one", "two", "three");
  private static final List<String> KEY2 = ImmutableList.of("two", "three");
  private static final List<String> KEY3 = ImmutableList.of("two", "one", "three", "one");

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
  private ImmutableList<Integer> overlapedFirstSecondNegativePositions;

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
    overlapedFirstSecondNegativePositions = ImmutableList.of(0, 0, 1, 1, -1);
  }

  @Test
  public void testGetFullKeysByPartialKeyNoPositions() {
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(emptyPartialKey)).isEmpty();
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(absentPartialKey)).isEmpty();
    softly.assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(withNullsPartialKey))
        .isInstanceOf(NullPointerException.class);
    softly.assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(nullPartialKey))
        .isInstanceOf(NullPointerException.class);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey)).isNotEmpty()
        .hasSameSizeAs(multiKeyMap.keySet()).containsOnlyElementsOf(multiKeyMap.keySet());
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1)).isNotEmpty().hasSize(2)
        .containsOnly(KEY1, KEY3);
  }

  @Test
  public void testGetFullKeysByPartialKeyWithPositions() {
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(emptyPartialKey, manyNegativePositions))
        .isEmpty();
    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(absentPartialKey, oneNegativePosition))
        .isEmpty();
    softly
        .assertThatThrownBy(
            () -> multiKeyMap.getFullKeysByPartialKey(withNullsPartialKey, oneNegativePosition))
        .isInstanceOf(NullPointerException.class);
    softly
        .assertThatThrownBy(
            () -> multiKeyMap.getFullKeysByPartialKey(nullPartialKey, manyNegativePositions))
        .isInstanceOf(NullPointerException.class);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey, oneNegativePosition))
        .isNotEmpty().hasSameSizeAs(multiKeyMap.keySet())
        .containsOnlyElementsOf(multiKeyMap.keySet());

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey, firstPositivePosition))
        .isNotEmpty().containsOnly(KEY2, KEY3);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY2, firstThirdPositivePositions))
        .isNotEmpty().containsOnly(KEY3);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, twoWrongPositivePositions))
        .isEmpty();

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(ImmutableList.of("two", "three", "one"),
        firstNegativeSecondPositions)).isNotEmpty().containsOnly(KEY3);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(ImmutableList.of("one", "one", "two"),
        negativeSecondFirstPositions)).isNotEmpty().containsOnly(KEY3);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, wrongFirstNegativePositions))
        .isEmpty();

    softly.assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(
        ImmutableList.of("one", "two", "two", "one", "one"), overlapedFirstSecondNegativePositions))
        .isInstanceOf(IllegalArgumentException.class);

    softly.assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, manyPositivePositions)).isNotEmpty()
        .containsOnly(KEY1);
  }
}
