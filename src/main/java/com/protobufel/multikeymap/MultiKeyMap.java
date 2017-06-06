//   Copyright 2017 David Tesler
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.protobufel.multikeymap;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public interface MultiKeyMap<T extends Comparable<T>, K extends Iterable<T>, V> extends Map<K, V> {

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
    return getFullKeysByPartialKey(StreamSupport
        .stream(Objects.requireNonNull(partialKey).spliterator(), true).collect(Collectors.toSet()))
            .map(fullKeys -> fullKeys.filter(key -> matchesPartially(key, partialKey, positions)));
  }

  static <T> boolean matchesPartially(final Iterable<? extends T> source,
      final Iterable<? extends T> search, final Iterable<Integer> positions) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(search);
    Objects.requireNonNull(positions);

    final Map<T, Set<Integer>> symbols = new HashMap<>();
    final Iterator<Integer> it = positions.iterator();
    boolean donePositions = false;

    final int anyPosition = -1;

    for (final T el : search) {
      final int position =
          donePositions || (donePositions = !it.hasNext()) ? anyPosition : it.next();
      symbols.computeIfAbsent(el, k -> new HashSet<>()).add(position);
    }

    int i = 0;

    for (final T el : source) {
      if (symbols.isEmpty()) {
        return true;
      }

      final int position = i;
      symbols.computeIfPresent(el, (k, v) -> {
        if (v.remove(position) || v.remove(anyPosition)) {
          if (v.isEmpty()) {
            return null; // remove the entire record instead!
          }
        }

        return v;
      });

      i++;
    }

    return symbols.isEmpty();
  }

  static <T> boolean matchesPartially(final Iterable<? extends T> source,
      final Iterable<? extends T> search, final Pattern pattern) {
    Objects.requireNonNull(source);
    Objects.requireNonNull(search);
    Objects.requireNonNull(pattern);

    final Map<T, String> symbols = new HashMap<>();
    short i = 0;

    for (final T el : search) {
      symbols.put(el, String.valueOf(i++));
    }

    final String s = StreamSupport.stream(source.spliterator(), false)
        .collect(Collectors.mapping(x -> symbols.getOrDefault(x, ""), Collectors.joining(",")));
    return pattern.matcher(s).matches();
  }
}
