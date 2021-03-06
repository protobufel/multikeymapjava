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

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A lite wrapper of a mutable Map which values are Set of actual values
 *
 * @param <K> the type of Map's key
 * @param <V> the type of actual values
 * @author David Tesler
 */
interface LiteSetMultimap<K, V> {

    /**
     * Creates a new default instance of LiteSetMultimap.
     *
     * @return a new instance of the LiteSetMultimap's default implementation.
     */
    static <K, V> LiteSetMultimap<K, V> newInstance() {
        return newInstance(false);
    }

    /**
     * Creates a new instance of LiteSetMultimap based on the provided empty map.
     *
     * @param concurrent create a concurrent instance if true, un-synchronized, regular instance,
     *                   otherwise
     * @return a new instance of LiteSetMultimap based on the provided empty map
     */
    static <K, V> LiteSetMultimap<K, V> newInstance(boolean concurrent) {
        return concurrent ? new ConcurrentLiteSetMultimap<>() : new RegularLiteSetMultimap<>();
    }

    /** Clears all data */
    void clear();

    /**
     * Finds the record with the specified key and removes the value, if present, from the set of its
     * values. If there are no values left in the set, removes the entire record from the
     * LiteSetMultimap.
     *
     * @param key   the key to search for
     * @param value the value to be removed corresponding to the search key
     * @return true if value is removed, false, otherwise
     */
    boolean remove(K key, V value);

    /**
     * Gets the number of records in the LiteSetMultimap.
     *
     * @return the size of the the LiteSetMultimap, zero if empty
     */
    int size();

    /**
     * Tells whether the LiteSetMultimap is empty.
     *
     * @return true if the LiteSetMultimap is empty, false, otherwise
     */
    boolean isEmpty();

    /**
     * Gets the live Set of the values corresponding to the search key.
     *
     * @param key the key to search for
     * @return the Set of the values corresponding to the search key
     */
    Set<V> get(K key);

    /**
     * Adds the value to the set of values corresponding to the search key. If no such record is
     * found, creates a new record with the set initialized to the value.
     *
     * @param key the key to search for
     * @param value the value to add to the set of values corresponding to the search key
     * @return true if value is added, false, otherwise
     */
    boolean put(K key, V value);

    /**
     * Returns whether this class supports concurrent access.
     */
    default boolean isConcurrent() {
        return getClass().isAssignableFrom(ConcurrentLiteSetMultimap.class);
    }

    class ConcurrentLiteSetMultimap<K, V> extends BaseLiteSetMultimap<K, V> implements Serializable {
        private static final long serialVersionUID = -5018582265479564955L;

        public ConcurrentLiteSetMultimap() {
            super(new ConcurrentHashMap<>());
        }

        @Override
        protected Set<V> newSet() {
            return new ConcurrentHashMap().keySet(true);
        }
    }

    class RegularLiteSetMultimap<K, V> extends BaseLiteSetMultimap<K, V> implements Serializable {
        private static final long serialVersionUID = -1557045174464645635L;

        public RegularLiteSetMultimap() {
            super(new HashMap<>());
        }

        @Override
        protected Set<V> newSet() {
            return new HashSet<>();
        }
    }

    abstract class BaseLiteSetMultimap<K, V> implements LiteSetMultimap<K, V> {
        /**
         * The base map this class is wrapping
         *
         * @serial
         */
        private final Map<K, Set<V>> map;

        public BaseLiteSetMultimap(Map<K, Set<V>> map) {
            this.map = map;
        }

        abstract Set<V> newSet();

        @Override
        public int size() {
            return map.size();
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }

        @Override
        public Set<V> get(final Object key) {
            return map.get(key);
        }

        @Override
        public boolean put(final K key, final V value) {
            return map.computeIfAbsent(Objects.requireNonNull(key), k -> newSet())
                    .add(Objects.requireNonNull(value));
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public boolean remove(final K key, final V value) {
            final boolean[] removed = {false};
            map.computeIfPresent(
                    Objects.requireNonNull(key),
                    (k, v) -> {
                        if ((removed[0] = v.remove(value)) && v.isEmpty()) {
                            return null;
                        }

                        return v;
                    });

            return removed[0];
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof Map)) {
                return false;
            }

            return map.equals(o);
        }

        @Override
        public int hashCode() {
      return map.hashCode();
    }
  }
}
