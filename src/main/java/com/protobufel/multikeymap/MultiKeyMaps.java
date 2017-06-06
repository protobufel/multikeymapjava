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
import java.util.Map;
import java.util.function.Supplier;

public final class MultiKeyMaps {
  private MultiKeyMaps() {}
  
  public static <T extends Comparable<T>, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newMultiKeyMap(
      Supplier<Map<K, V>> mapSupplier, Supplier<LiteSetMultimap<T, K>> multimapSupplier) {
    return new BaseMultiKeyMap<T,K,V>(mapSupplier.get(), multimapSupplier.get());
  }

  public static <T extends Comparable<T>, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newMultiKeyMap(
      Map<K, V> map, LiteSetMultimap<T, K> setMultimap) {
    return new BaseMultiKeyMap<T,K,V>(map, setMultimap);
  }

  public static <T extends Comparable<T>, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newMultiKeyMap() {
    return new BaseMultiKeyMap<T,K,V>();
  }

  public static <T extends Comparable<T>, K extends Iterable<T>, V> MultiKeyMap<T, K, V> newHashMultiKeyMap() {
    return new BaseMultiKeyMap<T,K,V>(new HashMap<K, V>(), LiteSetMultimap.newInstance());
  }
  
  public static <T extends Comparable<T>, K extends Iterable<T>, V> MultiKeyMap<T, K, V> of(Map<K, V> map) {
    return new BaseMultiKeyMap<T,K,V>(new HashMap<K,V>(map), LiteSetMultimap.newInstance());
  }
}
