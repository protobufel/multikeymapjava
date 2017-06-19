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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a generic Map of composite keys plus the methods to query it by any combination of
 * sub-keys.
 *
 * @param <T> the type of a sub-key the key consist of
 * @param <K> the type of a full key, which is an Iterable of its sub-keys, with usage as in a
 *            regular Map
 * @param <V> the type of a value which stored in the MultiKeyMap under the corresponding key
 * @see java.util.Map
 * @apiNote All implementations assumed to support only {@code @NotNullable values} unless specifically stated!
 * @author David Tesler
 */
public interface MultiKeyMap<T, K extends Iterable<T>, V> extends Map<K, V> {

    /**
     * Gets all full keys that contain the partial key set in any order.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @return a stream of the full keys satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    Stream<K> getFullKeysByPartialKey(Iterable<? extends T> partialKey);

    /**
     * Gets all values for which their full keys contain the partial key set in any order.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @return a stream of the values satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    default Stream<V> getValuesByPartialKey(final Iterable<? extends T> partialKey) {
        return getFullKeysByPartialKey(Objects.requireNonNull(partialKey)).map(this::get);
    }

    /**
     * Gets all entries for which their full keys contain the partial key set in any order.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @return a stream of the entries satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    default Stream<Entry<K, V>> getEntriesByPartialKey(final Iterable<? extends T> partialKey) {
        return getFullKeysByPartialKey(Objects.requireNonNull(partialKey))
                .map(key -> new SimpleImmutableEntry<>(key, get(key)));
    }

    /**
     * Gets all values for which their full keys contain the partial key according to the specified
     * positions.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @param positions  the sequence of positions corresponding to the sequence of partialKey's
     *                   sub-keys, wherein the negative position signifies a non-positional sub-key to search for
     *                   anywhere within the full key, otherwise, its exact position within the full key. The
     *                   size of this list can be smaller than the partialKey list, meaning the rest of the
     *                   partialKey sub-keys are non-positional.
     * @return a stream of the values satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    default Stream<V> getValuesByPartialKey(final Iterable<? extends T> partialKey,
                                            final Iterable<Integer> positions) {
        return getFullKeysByPartialKey(partialKey, positions).map(this::get);
    }

    /**
     * Gets all entries for which their full keys contain the partial key according to the specified
     * positions.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @param positions  the sequence of positions corresponding to the sequence of partialKey's
     *                   sub-keys, wherein the negative position signifies a non-positional sub-key to search for
     *                   anywhere within the full key, otherwise, its exact position within the full key. The
     *                   size of this list can be smaller than the partialKey list, meaning the rest of the
     *                   partialKey sub-keys are non-positional.
     * @return a stream of the entries satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    default Stream<Entry<K, V>> getEntriesByPartialKey(final Iterable<? extends T> partialKey,
                                                       final Iterable<Integer> positions) {
        return getFullKeysByPartialKey(partialKey, positions)
                .map(key -> new SimpleImmutableEntry<>(key, get(key)));
    }

    /**
     * Gets all full keys that contain the partial key according to the specified positions.
     *
     * @param partialKey the combination of the sub-keys to search for.
     * @param positions  the sequence of positions corresponding to the sequence of partialKey's
     *                   sub-keys, wherein the negative position signifies a non-positional sub-key to search for
     *                   anywhere within the full key, otherwise, its exact position within the full key. The
     *                   size of this list can be smaller than the partialKey list, meaning the rest of the
     *                   partialKey sub-keys are non-positional.
     * @return a stream of the full keys satisfying the partial key criteria, otherwise, the empty
     * stream.
     */
    default Stream<K> getFullKeysByPartialKey(final Iterable<? extends T> partialKey,
                                              final Iterable<Integer> positions) {
        final Stream<K> keyStream = getFullKeysByPartialKey(com.github.protobufel.multikeymap.Collectors
                .streamOf(Objects.requireNonNull(partialKey), true).collect(Collectors.toSet()));

        class IterableMatcher {
            final Map<Integer, T> symbols;
            final Map<T, Integer> counters;
            final int totalCount;

            IterableMatcher() {
                this.symbols = new HashMap<>();
                this.counters = new HashMap<>();

                final Iterator<Integer> it = positions.iterator();
                boolean morePositions = true;
                int totalCount = 0;

                for (final T el : partialKey) {
                    final int position;

                    if (morePositions && (morePositions = it.hasNext()) && ((position = it.next()) >= 0)) {
                        if (symbols.put(position, el) != null) {
                            throw new IllegalArgumentException(
                                    String.format("duplicate positive position %s", position));
                        } else {
                            totalCount++;
                        }
                    } else {
                        totalCount++;
                        counters.merge(el, 1, (oldValue, value) -> oldValue + 1);
                    }
                }

                this.totalCount = totalCount;
            }

            boolean matches(final K fullKey) {
                Objects.requireNonNull(fullKey);
                final Map<T, Integer> counters = new HashMap<>(this.counters);
                int totalCount = this.totalCount;

                int i = -1;

                for (final T el : fullKey) {
                    i++;

                    final T fixedPositionSubKey = symbols.get(i);

                    if (fixedPositionSubKey == null) {
                        final boolean[] found = {false};
                        counters.computeIfPresent(el, (subKey, count) -> {
                            found[0] = true;
                            return (--count == 0) ? null : count;
                        });

                        if (found[0] && (--totalCount == 0)) {
                            return true;
                        }
                    } else if (fixedPositionSubKey.equals(el)) {
                        if (--totalCount == 0) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                }

                return totalCount == 0;
            }
        }

        final IterableMatcher matcher = new IterableMatcher();
        return keyStream.filter(matcher::matches);
    }
}
