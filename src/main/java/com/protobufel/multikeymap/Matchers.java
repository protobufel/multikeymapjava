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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

final class Matchers {
  private Matchers() {}

  public static <T> boolean matchesByPositions(
      final Iterable<? extends Iterable<? extends T>> source, final Iterable<? extends T> search,
      final Iterable<Integer> positions) {
    Objects.requireNonNull(source);

    final IterablePositionMatcher<T> matcher = new IterablePositionMatcher<>(
        Objects.requireNonNull(search), Objects.requireNonNull(positions));

    for (final Iterable<? extends T> item : source) {
      if (!matcher.matches(item)) {
        return false;
      }
    }

    return true;
  }

  public static <T> boolean matchesByPositionalPattern(
      final Iterable<? extends Iterable<? extends T>> source, final Iterable<? extends T> search,
      final Pattern pattern) {
    Objects.requireNonNull(source);

    final IterablePatternMatcher<T> matcher = new IterablePatternMatcher<>(
        Objects.requireNonNull(search), Objects.requireNonNull(pattern));

    for (final Iterable<? extends T> item : source) {
      if (!matcher.matches(item)) {
        return false;
      }
    }

    return true;
  }

  static class IterablePositionMatcher<T> {
    final Map<T, Set<Integer>> symbols;
    final Map<T, Integer> counters;
    final int totalCount;

    IterablePositionMatcher(final Iterable<? extends T> search, final Iterable<Integer> positions) {
      Objects.requireNonNull(search);
      Objects.requireNonNull(positions);

      this.symbols = new HashMap<>();
      this.counters = new HashMap<>();

      final Iterator<Integer> it = positions.iterator();
      boolean morePositions = true;
      int totalCount = 0;

      for (final T el : search) {
        totalCount++;
        counters.merge(el, 1, (oldValue, value) -> oldValue + 1);

        if (morePositions && (morePositions = it.hasNext())) {
          final int position = it.next();
          symbols.computeIfAbsent(el, k -> new HashSet<>()).add(position);
        }
      }

      this.totalCount = totalCount;
    }

    boolean matches(final Iterable<? extends T> source) {
      Objects.requireNonNull(source);
      final Map<T, Integer> counters = new HashMap<>(this.counters);
      int totalCount = this.totalCount;

      int i = -1;

      for (final T el : source) {
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

  static class IterablePatternMatcher<T> {
    final Map<T, String> symbols;
    final Pattern pattern;

    IterablePatternMatcher(final Iterable<? extends T> search, final Pattern pattern) {
      Objects.requireNonNull(search);
      this.pattern = Objects.requireNonNull(pattern);
      this.symbols = new HashMap<>();

      short i = 0;

      for (final T el : search) {
        symbols.put(el, String.valueOf(i++));
      }
    }

    boolean matches(final Iterable<? extends T> source) {
      return matches(source, pattern);
    }

    boolean matches(final Iterable<? extends T> source, final Pattern pattern) {
      Objects.requireNonNull(pattern);
      final String s = StreamSupport.stream(Objects.requireNonNull(source).spliterator(), false)
          .collect(Collectors.mapping(x -> symbols.getOrDefault(x, ""), Collectors.joining(",")));
      return pattern.matcher(s).matches();
    }
  }
}
