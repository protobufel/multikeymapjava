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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public interface LiteSetMultimap<K, V> {

  void clear();

  boolean remove(K key, V value);

  int size();

  boolean isEmpty();

  Set<V> get(K key);

  boolean put(K key, V value);

  static <K, V> LiteSetMultimap<K, V> newInstance() {
    return newInstance(new HashMap<K, Set<V>>());
  }

  static <K, V> LiteSetMultimap<K, V> newInstance(final Map<K, Set<V>> map) {
    return new LiteSetMultimap<K, V>() {
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
        return map.computeIfAbsent(Objects.requireNonNull(key), k -> new HashSet<>()).add(Objects.requireNonNull(value));
      }

      @Override
      public void clear() {
        map.clear();
      }

      @Override
      public boolean equals(final Object o) {
        return map.equals(o);
      }

      @Override
      public int hashCode() {
        return map.hashCode();
      }

      @Override
      public boolean remove(final K key, final V value) {
        final boolean[] removed = {false};
        map.computeIfPresent(Objects.requireNonNull(key), (k, v) -> 
        {
          if ((removed[0] = v.remove(value)) && v.isEmpty()) {
            return null;
          }
          
          return v;
        });

        return removed[0];
      }
    };
  }
}
