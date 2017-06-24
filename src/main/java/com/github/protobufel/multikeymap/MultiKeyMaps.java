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

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Provides the factory methods of the generic MultiKeyMap implementations.
 *
 * @author David Tesler
 */
public final class MultiKeyMaps {
    private MultiKeyMaps() {
    }

    /**
     * Creates a new MultiKeyMap based on the provided suppliers.
     * <p>NOTE: Use with caution. This is the advanced functionality.
     *
     * @param mapSupplier a supplier of {@code Map<K, V>} this MultiKeyMap is based on
     * @param concurrent  create a concurrent instance if true, un-synchronized, regular instance, otherwise
     * @param <T>         the type of a sub-key the key consist of
     * @param <K>         the type of a full key, which is an Iterable of its sub-keys, with usage as in a
     *                    regular Map
     * @param <V>         the type of a value which stored in the MultiKeyMap under the corresponding key
     * @return a new instance of the implementation of MultiKeyMap
     */
    public static <T, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newMultiKeyMap(
            final Supplier<Map<K, V>> mapSupplier, boolean concurrent) {
        return new BaseMultiKeyMap(Objects.requireNonNull(mapSupplier).get(),
                LiteSetMultimap.newInstance(concurrent));
    }

    /**
     * Creates a new default instance of MultiKeyMap.
     *
     * @param <T> the type of a sub-key the key consist of
     * @param <K> the type of a full key, which is an Iterable of its sub-keys, with usage as in a
     *            regular Map
     * @param <V> the type of a value which stored in the MultiKeyMap under the corresponding key
     * @return a new instance of the default implementation of MultiKeyMap
     */
    public static <T, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newMultiKeyMap() {
        return new BaseMultiKeyMap<>();
    }

    /**
     * Creates a new default instance of MultiKeyMap initialized off the data by the supplied Map.
     *
     * @param map a Map instance to copy data from; the data copied shallowly.
     * @param <T> the type of a sub-key the key consist of
     * @param <K> the type of a full key, which is an Iterable of its sub-keys, with usage as in a
     *            regular Map
     * @param <V> the type of a value which stored in the MultiKeyMap under the corresponding key
     * @return a new instance of the default implementation of MultiKeyMap initialized with the map's
     * data
     */
    public static <T, K extends Iterable<T>, V> MultiKeyMap<T, K, V> of(final Map<K, V> map) {
        return new BaseMultiKeyMap<>(Objects.requireNonNull(map));
    }
}
