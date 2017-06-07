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

    if (!keyStream.isPresent()) {
      return Optional.empty();
    }

    class IterableMatcher {
      final Map<T, Set<Integer>> symbols;
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
          counters.merge(el, 1, (oldValue, value) -> oldValue + 1);

          if (morePositions && (morePositions = it.hasNext())) {
            final int position = it.next();
            symbols.computeIfAbsent(el, k -> new HashSet<>()).add(position);
          }
        }

        this.totalCount = totalCount;
      }

      boolean matchesPartially(final K fullKey) {
        Objects.requireNonNull(fullKey);
        final Map<T, Integer> counters = new HashMap<>(this.counters);
        int totalCount = this.totalCount;

        int i = -1;

        for (final T el : fullKey) {
          i++;

          final Set<Integer> fixedPositions = symbols.get(el);

          if ((fixedPositions != null) && fixedPositions.contains(i)) {
            if (--totalCount == 0) {
              return true;
            }
          } else {
            final int counter = counters.getOrDefault(el, -1);

            if (counter < 0) {
              continue;
            }

            if (--totalCount == 0) {
              return true;
            }

            if (counter == 1) {
              counters.remove(el);
            } else {
              counters.put(el, counter - 1);
            }
          }
        }

        return totalCount == 0;
      }
    }

    return keyStream.map(fullKeys -> {
      final IterableMatcher matcher = new IterableMatcher();
      return fullKeys.filter(key -> matcher.matchesPartially(key));
    });
  }
}