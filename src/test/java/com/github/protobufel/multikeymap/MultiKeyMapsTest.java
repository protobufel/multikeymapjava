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

import com.google.common.testing.ClassSanityTester;
import com.google.common.testing.EqualsTester;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MultiKeyMapsTest {

    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void newMultiKeyMap() throws Exception {
        miscHelper(MultiKeyMaps.newMultiKeyMap());
    }

    @Test
    public void of() throws Exception {
        HashMap<Iterable<String>, Integer> map = new HashMap<>();
        HashMap<Iterable<String>, Integer> expected = new HashMap<>();

        softly.assertThat(MultiKeyMaps.of(map)).isNotNull().isEqualTo(map).isEqualTo(expected);

        map = new HashMap<Iterable<String>, Integer>() {{
            put(Arrays.asList("1", "2"), 1);
        }};
        expected = new HashMap<Iterable<String>, Integer>() {{
            put(Arrays.asList("1", "2"), 1);
        }};

        softly.assertThat(MultiKeyMaps.of(map)).isNotNull().isEqualTo(map).isEqualTo(expected);
    }

    @Test
    public void classSanityTest() throws Exception {
        new ClassSanityTester()
                .setDefault(String.class, "")
                .setDistinctValues(String.class, "1", "2")
                .setDefault(Integer.class, 0)
                .setDistinctValues(Integer.class, 1, 2)
                .setDefault(Map.class, new HashMap<String, Integer>())
                .setDistinctValues(Map.class,
                        new HashMap<String, Integer>() {{
                            put("1", 1);
                        }},
                        new HashMap<String, Integer>() {{
                            put("2", 2);
                        }}
                )
                .forAllPublicStaticMethods(MultiKeyMaps.class)
                .thatReturn(MultiKeyMap.class)
                //.testSerializable()
                //.testEqualsAndSerializable()
                //.testNulls()
                .testEquals();
    }

    private <T, K extends Iterable<T>, V> void equalityHelper(
            MultiKeyMap<T, K, V> empty, MultiKeyMap<T, K, V> one, MultiKeyMap<T, K, V> two) {
        new EqualsTester()
                .addEqualityGroup(empty, MultiKeyMaps.of(empty))
                .addEqualityGroup(one, MultiKeyMaps.of(one))
                .addEqualityGroup(two, MultiKeyMaps.of(two))
                .testEquals();
    }

    private <T, K extends Iterable<T>, V> void miscHelper(
            MultiKeyMap<T, K, V> empty) {
        softly.assertThat(empty)
                .isNotNull()
                .isInstanceOf(Map.class)
                .isInstanceOf(MultiKeyMap.class)
                .hasSize(0);

    }
}