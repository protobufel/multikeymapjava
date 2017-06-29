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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.testing.Helpers;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RunWith(JUnit4.class)
public class MultiKeyMapSpecificTest {
    private static final List<String> KEY1 = ImmutableList.of("one", "two", "three");
    private static final List<String> KEY2 = ImmutableList.of("two", "three");
    private static final List<String> KEY3 = ImmutableList.of("two", "one", "three", "one");
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    private MultiKeyMap<String, Iterable<String>, Integer> multiKeyMap;
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
        final List<Entry<Iterable<String>, Integer>> input =
                ImmutableList.of(
                        Helpers.mapEntry(KEY1, 1), Helpers.mapEntry(KEY2, 2), Helpers.mapEntry(KEY3, 3));

        multiKeyMap = MultiKeyMaps.newMultiKeyMap();
        input.forEach(entry -> multiKeyMap.put(entry.getKey(), entry.getValue()));

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
        softly
                .assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(withNullsPartialKey))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(() -> multiKeyMap.getFullKeysByPartialKey(nullPartialKey))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.keySet())
                .containsOnlyElementsOf(multiKeyMap.keySet());
        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1))
                .isNotEmpty()
                .hasSize(2)
                .containsOnly(KEY1, KEY3);
    }

    @Test
    public void testGetValuesByPartialKeyNoPositions() {
        softly.assertThat(multiKeyMap.getValuesByPartialKey(emptyPartialKey)).isEmpty();
        softly.assertThat(multiKeyMap.getValuesByPartialKey(absentPartialKey)).isEmpty();
        softly
                .assertThatThrownBy(() -> multiKeyMap.getValuesByPartialKey(withNullsPartialKey))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(() -> multiKeyMap.getValuesByPartialKey(nullPartialKey))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(commonPartialKey))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.values())
                .containsOnlyElementsOf(multiKeyMap.values());
        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(KEY1))
                .isNotEmpty()
                .hasSize(2)
                .containsOnly(multiKeyMap.get(KEY1), multiKeyMap.get(KEY3));
    }

    @Test
    public void testGetEntriesByPartialKeyNoPositions() {
        softly.assertThat(multiKeyMap.getEntriesByPartialKey(emptyPartialKey)).isEmpty();
        softly.assertThat(multiKeyMap.getEntriesByPartialKey(absentPartialKey)).isEmpty();
        softly
                .assertThatThrownBy(() -> multiKeyMap.getEntriesByPartialKey(withNullsPartialKey))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(() -> multiKeyMap.getEntriesByPartialKey(nullPartialKey))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(commonPartialKey))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.keySet())
                .containsOnlyElementsOf(multiKeyMap.entrySet());
        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(KEY1))
                .isNotEmpty()
                .hasSize(2)
                .containsOnly(entryOf(multiKeyMap, KEY1), entryOf(multiKeyMap, KEY3));
    }

    @Test
    public void testGetFullKeysByPartialKeyWithPositions() {
        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(emptyPartialKey, manyNegativePositions))
                .isEmpty();
        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(absentPartialKey, oneNegativePosition))
                .isEmpty();
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getFullKeysByPartialKey(withNullsPartialKey, oneNegativePosition))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getFullKeysByPartialKey(nullPartialKey, manyNegativePositions))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey, oneNegativePosition))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.keySet())
                .containsOnlyElementsOf(multiKeyMap.keySet());

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(commonPartialKey, firstPositivePosition))
                .isNotEmpty()
                .containsOnly(KEY2, KEY3);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(KEY2, firstThirdPositivePositions))
                .isNotEmpty()
                .containsOnly(KEY3);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, twoWrongPositivePositions))
                .isEmpty();

        softly
                .assertThat(
                        multiKeyMap.getFullKeysByPartialKey(
                                ImmutableList.of("two", "three", "one"), firstNegativeSecondPositions))
                .isNotEmpty()
                .containsOnly(KEY3);

        softly
                .assertThat(
                        multiKeyMap.getFullKeysByPartialKey(
                                ImmutableList.of("one", "one", "two"), negativeSecondFirstPositions))
                .isNotEmpty()
                .containsOnly(KEY3);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, wrongFirstNegativePositions))
                .isEmpty();

        softly
                .assertThatThrownBy(
                        () ->
                                multiKeyMap.getFullKeysByPartialKey(
                                        ImmutableList.of("one", "two", "two", "one", "one"),
                                        overlapedFirstSecondNegativePositions))
                .isInstanceOf(IllegalArgumentException.class);

        softly
                .assertThat(multiKeyMap.getFullKeysByPartialKey(KEY1, manyPositivePositions))
                .isNotEmpty()
                .containsOnly(KEY1);
    }

    @Test
    public void testGetValuesByPartialKeyWithPositions() {
        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(emptyPartialKey, manyNegativePositions))
                .isEmpty();
        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(absentPartialKey, oneNegativePosition))
                .isEmpty();
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getValuesByPartialKey(withNullsPartialKey, oneNegativePosition))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getValuesByPartialKey(nullPartialKey, manyNegativePositions))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(commonPartialKey, oneNegativePosition))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.keySet())
                .containsOnlyElementsOf(multiKeyMap.values());

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(commonPartialKey, firstPositivePosition))
                .isNotEmpty()
                .containsOnly(multiKeyMap.get(KEY2), multiKeyMap.get(KEY3));

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(KEY2, firstThirdPositivePositions))
                .isNotEmpty()
                .containsOnly(multiKeyMap.get(KEY3));

        softly.assertThat(multiKeyMap.getValuesByPartialKey(KEY1, twoWrongPositivePositions)).isEmpty();

        softly
                .assertThat(
                        multiKeyMap.getValuesByPartialKey(
                                ImmutableList.of("two", "three", "one"), firstNegativeSecondPositions))
                .isNotEmpty()
                .containsOnly(multiKeyMap.get(KEY3));

        softly
                .assertThat(
                        multiKeyMap.getValuesByPartialKey(
                                ImmutableList.of("one", "one", "two"), negativeSecondFirstPositions))
                .isNotEmpty()
                .containsOnly(multiKeyMap.get(KEY3));

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(KEY1, wrongFirstNegativePositions))
                .isEmpty();

        softly
                .assertThatThrownBy(
                        () ->
                                multiKeyMap.getValuesByPartialKey(
                                        ImmutableList.of("one", "two", "two", "one", "one"),
                                        overlapedFirstSecondNegativePositions))
                .isInstanceOf(IllegalArgumentException.class);

        softly
                .assertThat(multiKeyMap.getValuesByPartialKey(KEY1, manyPositivePositions))
                .isNotEmpty()
                .containsOnly(multiKeyMap.get(KEY1));
    }

    @Test
    public void testGetEntriesByPartialKeyWithPositions() {
        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(emptyPartialKey, manyNegativePositions))
                .isEmpty();
        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(absentPartialKey, oneNegativePosition))
                .isEmpty();
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getEntriesByPartialKey(withNullsPartialKey, oneNegativePosition))
                .isInstanceOf(NullPointerException.class);
        softly
                .assertThatThrownBy(
                        () -> multiKeyMap.getEntriesByPartialKey(nullPartialKey, manyNegativePositions))
                .isInstanceOf(NullPointerException.class);

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(commonPartialKey, oneNegativePosition))
                .isNotEmpty()
                .hasSameSizeAs(multiKeyMap.keySet())
                .containsOnlyElementsOf(multiKeyMap.entrySet());

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(commonPartialKey, firstPositivePosition))
                .isNotEmpty()
                .containsOnly(entryOf(multiKeyMap, KEY2), entryOf(multiKeyMap, KEY3));

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(KEY2, firstThirdPositivePositions))
                .isNotEmpty()
                .containsOnly(entryOf(multiKeyMap, KEY3));

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(KEY1, twoWrongPositivePositions))
                .isEmpty();

        softly
                .assertThat(
                        multiKeyMap.getEntriesByPartialKey(
                                ImmutableList.of("two", "three", "one"), firstNegativeSecondPositions))
                .isNotEmpty()
                .containsOnly(entryOf(multiKeyMap, KEY3));

        softly
                .assertThat(
                        multiKeyMap.getEntriesByPartialKey(
                                ImmutableList.of("one", "one", "two"), negativeSecondFirstPositions))
                .isNotEmpty()
                .containsOnly(entryOf(multiKeyMap, KEY3));

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(KEY1, wrongFirstNegativePositions))
                .isEmpty();

        softly
                .assertThatThrownBy(
                        () ->
                                multiKeyMap.getEntriesByPartialKey(
                                        ImmutableList.of("one", "two", "two", "one", "one"),
                                        overlapedFirstSecondNegativePositions))
                .isInstanceOf(IllegalArgumentException.class);

        softly
                .assertThat(multiKeyMap.getEntriesByPartialKey(KEY1, manyPositivePositions))
                .isNotEmpty()
                .containsOnly(entryOf(multiKeyMap, KEY1));
    }

    private <K, V> Entry<K, V> entryOf(Map<K, V> map, K key) {
        return new SimpleImmutableEntry<>(key, map.get(key));
    }
}
