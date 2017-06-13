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

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface MultiKeyMap<T, K extends Iterable<T>, V> extends Map<K, V> {

  Optional<Stream<K>> getFullKeysByPartialKey(Iterable<? extends T> partialKey);

  default Optional<Stream<V>> getValuesByPartialKey(final Iterable<? extends T> partialKey) {
    return getFullKeysByPartialKey(Objects.requireNonNull(partialKey))
        .map(fullKeys -> fullKeys.map(key -> get(key)));
  }

  default Optional<Stream<Entry<K, V>>> getEntriesByPartialKey(
      final Iterable<? extends T> partialKey) {
    return getFullKeysByPartialKey(Objects.requireNonNull(partialKey))
        .map(fullKeys -> fullKeys.map(key -> new SimpleImmutableEntry<>(key, get(key))));
  }

  default Optional<Stream<V>> getValuesByPartialKey(final Iterable<? extends T> partialKey,
      final Iterable<Integer> positions) {
    return getFullKeysByPartialKey(partialKey, positions)
        .map(fullKeys -> fullKeys.map(key -> get(key)));
  }

  default Optional<Stream<Entry<K, V>>> getEntriesByPartialKey(
      final Iterable<? extends T> partialKey, final Iterable<Integer> positions) {
    return getFullKeysByPartialKey(partialKey, positions)
        .map(fullKeys -> fullKeys.map(key -> new SimpleImmutableEntry<>(key, get(key))));
  }

  default Optional<Stream<K>> getFullKeysByPartialKey(final Iterable<? extends T> partialKey,
      final Iterable<Integer> positions) {
    final Optional<Stream<K>> keyStream = getFullKeysByPartialKey(
        StreamSupport.stream(Objects.requireNonNull(partialKey).spliterator(), true)
            .collect(Collectors.toSet()));

    class IterableMatcher {
      final Map<Integer, Set<T>> symbols;
      final Map<T, Integer> counters;
      final int totalCount;

      IterableMatcher() {
        this.symbols = new HashMap<>();
        this.counters = new HashMap<>();

        final Iterator<Integer> it = positions.iterator();
        boolean morePositions = true;
        int totalCount = 0;

        for (final T el : partialKey) {
          totalCount++;
          final int position;

          if (morePositions && (morePositions = it.hasNext()) && ((position = it.next()) >= 0)) {
            symbols.computeIfAbsent(position, k -> new HashSet<>()).add(el);
          } else {
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

          final Set<T> fixedPositionSet = symbols.get(i);

          if (fixedPositionSet == null) {
            final boolean[] found = {false};
            counters.computeIfPresent(el, (subKey, count) -> {
              found[0] = true;
              return (--count == 0) ? null : count;
            });

            if (found[0] && (--totalCount == 0)) {
              return true;
            }
          } else if (fixedPositionSet.contains(el)) {
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

    return keyStream.map(fullKeys -> {
      final IterableMatcher matcher = new IterableMatcher();
      return fullKeys.filter(key -> matcher.matches(key));
    });
  }
}
