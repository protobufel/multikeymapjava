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
import com.google.common.testing.NullPointerTester;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MultiKeyMapsTest {
    @Rule
    public final JUnitSoftAssertions softly = new JUnitSoftAssertions();
    private MapSupplier<Iterable<String>, Integer> hashMapSupplier;
    private MapSupplier<Iterable<String>, Integer> treeMapSupplier;
    private MapSupplier<Iterable<String>, Integer> concurrentMapSupplier;

    @Before
    public void setUp() throws Exception {
        hashMapSupplier = HashMap<Iterable<String>, Integer>::new;
        treeMapSupplier = TreeMap<Iterable<String>, Integer>::new;
        concurrentMapSupplier = ConcurrentHashMap<Iterable<String>, Integer>::new;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void utilityClassTest() throws Exception {
        softly.assertThat(MultiKeyMaps.class).isFinal().isPublic().satisfies(
                clazz -> softly.assertThat(clazz.getConstructors()).isEmpty()
        );
    }

    @Test
    public void newMultiKeyMap() throws Exception {
        miscHelper(MultiKeyMaps.newMultiKeyMap());
    }

    @Test
    public void newMultiKeyMapWithSuppliers() throws Exception {
        miscHelper(MultiKeyMaps.newMultiKeyMap(hashMapSupplier, false));
    }

    @Test
    public void nullPointerTest() throws Exception {
        new NullPointerTester()
                .setDefault(MapSupplier.class, hashMapSupplier)
                .setDefault(boolean.class, false)
                .setDefault(Map.class, new HashMap<Iterable<String>, Integer>())
                .testAllPublicStaticMethods(MultiKeyMaps.class);
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
    public void ofSanityTest() throws Exception {
        new ClassSanityTester()
                //.setDefault(Map.class, new HashMap<Iterable<String>, Integer>())
                .setDistinctValues(Map.class,
                        new HashMap<Iterable<String>, Integer>() {{
                            put(Arrays.asList("1", "2", "3"), 1);
                            put(Arrays.asList("1", "2", "3", "4"), 2);
                        }},
                        new HashMap<Iterable<String>, Integer>() {{
                            put(Arrays.asList("2", "3", "4"), 1);
                            put(Arrays.asList("1", "2", "3"), 2);
                        }}
                )
                .forAllPublicStaticMethods(MultiKeyMapFactory.class)
                .thatReturn(MultiKeyMap.class)
                //.testNulls()
                .testEqualsAndSerializable();
    }

    private static class MultiKeyMapFactory {
        public static MultiKeyMap<String, Iterable<String>, Integer> of(final Map<Iterable<String>, Integer> map) {
            return MultiKeyMaps.of(map);
        }
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

    @FunctionalInterface
    public interface MapSupplier<K, V> extends Supplier<Map<K, V>> {
        @Override
        Map<K, V> get();
    }
}