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

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toSet;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

class BaseMultiKeyMap<T, K extends Iterable<T>, V> implements MultiKeyMap<T, K, V> {
  static boolean enableParallelStreaming = false;

  static final boolean isEnableParallelStreaming() {
    return enableParallelStreaming;
  }

  static final void setEnableParallelStreaming(final boolean enableParallelStreaming) {
    BaseMultiKeyMap.enableParallelStreaming = enableParallelStreaming;
  }

  private Map<K, V> fullMap;
  private transient LiteSetMultimap<T, K> partMap;

  private transient Set<K> keySet;
  private transient Collection<V> values;
  private transient Set<Entry<K, V>> entrySet;

  BaseMultiKeyMap(final Map<K, V> fullMap, final LiteSetMultimap<T, K> partMap) {
    super();
    this.fullMap = fullMap;
    this.partMap = partMap;
  }

  BaseMultiKeyMap() {
    this(new HashMap<>(), LiteSetMultimap.newInstance());
  }

  // TODO remove or re-enable after comparing the performance comparison w/ the Java 8 based
  // implementation
  // @Override
  // public Optional<Stream<K>> getFullKeysByPartialKey(final Iterable<? extends T> partialKey) {
  // Objects.requireNonNull(partialKey);
  //
  // if (partMap.isEmpty()) {
  // return Optional.empty();
  // }
  //
  // final List<Set<K>> subResults = new ArrayList<>();
  // int minSize = Integer.MAX_VALUE;
  // int minPos = -1;
  //
  // for (final T subKey : partialKey) {
  // final Set<K> subResult = partMap.get(Objects.requireNonNull(subKey));
  //
  // if (subResult.size() == 0) {
  // return Optional.empty();
  // } else if (subResult.size() < minSize) {
  // minSize = subResult.size();
  // minPos = subResults.size();
  // }
  //
  // subResults.add(subResult);
  // }
  //
  // if (subResults.isEmpty()) {
  // return Optional.empty();
  // }
  //
  // final Set<K> result = new HashSet<>(subResults.get(minPos));
  //
  // if (subResults.size() == 1) {
  // return Optional.of(result.stream());
  // }
  //
  // for (int i = 0; i < subResults.size(); i++) {
  // if (i != minPos) {
  // if (result.retainAll(subResults.get(i)) && result.isEmpty()) {
  // return Optional.empty();
  // }
  // }
  // }
  //
  // return Optional.of(result.stream());
  // }

  @Override
  public Optional<Stream<K>> getFullKeysByPartialKey(final Iterable<? extends T> partialKey) {
    Objects.requireNonNull(partialKey);

    if (partMap.isEmpty()) {
      return Optional.empty();
    }

    if (!(partialKey instanceof Set)) {
      return getFullKeysByPartialKey(partialKey, Collections.emptyList());
    }
    
    final Set<Set<K>> sets = ((Set<? extends T>)partialKey).stream().unordered()
        .map(subKey -> partMap.get(Objects.requireNonNull(subKey))).collect(toSet());
    final Optional<Set<K>> smallestSet =
        sets.stream().unordered().min(comparingInt(set -> set.size()));

    if (!smallestSet.isPresent()) {
      return Optional.empty();
    }

    sets.remove(smallestSet.get());

    // TODO requires performance review, for small samples the sequential implementation is better
    final Set<K> result;

    if (isEnableParallelStreaming()) {
      result = sets.parallelStream().unordered()
          .collect(Collectors.setIntersecting(smallestSet.get(), true));
    } else {
      result =
          sets.stream().unordered().collect(Collectors.setIntersecting(smallestSet.get(), false));
    }

    return result.isEmpty() ? Optional.empty() : Optional.of(result.stream());
  }

  @Override
  public int size() {
    return fullMap.size();
  }

  @Override
  public boolean isEmpty() {
    return fullMap.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return fullMap.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return fullMap.containsValue(value);
  }

  @Override
  public V get(final Object key) {
    return fullMap.get(key);
  }

  @Override
  public V put(final K key, final V value) {
    final Object[] oldValue = {null};
    fullMap.compute(key, (k, v) -> {
      if (v == null) {
        putPartial(k);
      } else {
        oldValue[0] = v;
      }

      return value;
    });

    @SuppressWarnings("unchecked")
    final V oldV = (V) oldValue[0];
    return oldV;
  }

  @Override
  public V remove(final Object key) {
    @SuppressWarnings("unchecked")
    final K fullKey = (K) key;
    final Object[] oldValue = {null};
    fullMap.computeIfPresent(fullKey, (k, v) -> {
      deletePartial(k);
      oldValue[0] = v;
      return null;
    });

    @SuppressWarnings("unchecked")
    final V oldV = (V) oldValue[0];
    return oldV;
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    for (final Map.Entry<? extends K, ? extends V> entry : m.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  @Override
  public void clear() {
    fullMap.clear();
    partMap.clear();
  }

  @Override
  public boolean equals(final Object o) {
    return fullMap.equals(o);
  }

  @Override
  public int hashCode() {
    return fullMap.hashCode();
  }

  private void putPartial(final K key) {
    for (final T subKey : key) {
      partMap.put(subKey, key);
    }
  }

  private void deletePartial(final K key) {
    for (final T subKey : key) {
      partMap.remove(subKey, key);
    }
  }

  @Override
  public Collection<V> values() {
    if (values == null) {
      values = new Values();
    }

    return values;
  }

  final class Values extends AbstractCollection<V> {

    @Override
    public Iterator<V> iterator() {
      return new ValueIterator(fullMap.entrySet().iterator());
    }

    @Override
    public int size() {
      return fullMap.size();
    }

    @Override
    public Spliterator<V> spliterator() {
      return fullMap.values().spliterator();
    }

    @Override
    public void forEach(final Consumer<? super V> action) {
      fullMap.values().forEach(action);
    }

    @Override
    public boolean contains(final Object o) {
      return fullMap.values().contains(o);
    }

    @Override
    public void clear() {
      BaseMultiKeyMap.this.clear();
    }
  }

  final class ValueIterator implements Iterator<V> {
    private final Iterator<Entry<K, V>> it;
    private Entry<K, V> current;

    public ValueIterator(final Iterator<Entry<K, V>> it) {
      super();
      this.it = it;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public V next() {
      current = it.next();
      return current.getValue();
    }

    @Override
    public void remove() {
      it.remove();
      deletePartial(current.getKey());
    }
  }

  @Override
  public Set<K> keySet() {
    if (keySet == null) {
      keySet = new KeySet();
    }

    return keySet;
  }

  final class KeySet extends AbstractSet<K> {

    @Override
    public Iterator<K> iterator() {
      return new KeySetIterator(fullMap.keySet().iterator());
    }

    @Override
    public int size() {
      return fullMap.size();
    }

    @Override
    public Spliterator<K> spliterator() {
      return fullMap.keySet().spliterator();
    }

    @Override
    public void forEach(final Consumer<? super K> action) {
      fullMap.keySet().forEach(action);
    }

    @Override
    public boolean contains(final Object o) {
      return fullMap.keySet().contains(o);
    }

    @Override
    public boolean remove(final Object o) {
      if (fullMap.keySet().remove(o)) {
        @SuppressWarnings("unchecked")
        final K key = (K) o;
        deletePartial(key);
        return true;
      }

      return false;
    }

    @Override
    public void clear() {
      BaseMultiKeyMap.this.clear();
    }
  }

  final class KeySetIterator implements Iterator<K> {
    private final Iterator<K> it;
    private K current;

    public KeySetIterator(final Iterator<K> it) {
      super();
      this.it = it;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public K next() {
      current = it.next();
      return current;
    }

    @Override
    public void remove() {
      it.remove();
      deletePartial(current);
    }
  }

  @Override
  public Set<Map.Entry<K, V>> entrySet() {
    if (entrySet == null) {
      entrySet = new EntrySet();
    }

    return entrySet;
  }

  final class EntrySet extends AbstractSet<Entry<K, V>> {

    @Override
    public Iterator<Entry<K, V>> iterator() {
      return new EntrySetIterator(fullMap.entrySet().iterator());
    }

    @Override
    public int size() {
      return fullMap.size();
    }

    @Override
    public Spliterator<Entry<K, V>> spliterator() {
      return fullMap.entrySet().spliterator();
    }

    @Override
    public void forEach(final Consumer<? super Entry<K, V>> action) {
      fullMap.entrySet().forEach(action);
    }

    @Override
    public boolean contains(final Object o) {
      return fullMap.entrySet().contains(o);
    }

    @Override
    public boolean remove(final Object o) {
      if (fullMap.entrySet().remove(o)) {
        @SuppressWarnings("unchecked")
        final Entry<K, V> entry = (Entry<K, V>) o;
        deletePartial(entry.getKey());
        return true;
      }

      return false;
    }

    @Override
    public void clear() {
      BaseMultiKeyMap.this.clear();
    }
  }

  final class EntrySetIterator implements Iterator<Entry<K, V>> {
    private final Iterator<Entry<K, V>> it;
    private Entry<K, V> current;

    public EntrySetIterator(final Iterator<java.util.Map.Entry<K, V>> it) {
      super();
      this.it = it;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext();
    }

    @Override
    public Entry<K, V> next() {
      current = it.next();
      return current;
    }

    @Override
    public void remove() {
      it.remove();
      deletePartial(current.getKey());
    }
  }
}
