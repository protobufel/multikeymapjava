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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.testing.Helpers;
import com.google.common.collect.testing.SampleElements;
import com.google.common.collect.testing.TestMapGenerator;

public final class MultiKeyMapGenerators {
  private MultiKeyMapGenerators() {}

  public static class MultiKeyMapTestGenerator<T, K extends Iterable<T>, V>
      implements TestMapGenerator<K, V> {
    private final MultiKeyMap<T, K, V> multiKeyMap;
    private final Supplier<T> subKeySupplier;
    private final Supplier<V> valueSupplier;
    private final Supplier<Iterable<T>> collectionSupplier;

    public MultiKeyMapTestGenerator(final Supplier<T> subKeySupplier,
        final Supplier<V> valueSupplier) {
      this(MultiKeyMaps.<T, K, V>newMultiKeyMap(), subKeySupplier, valueSupplier,
          ArrayList<T>::new);
    }

    public MultiKeyMapTestGenerator(final MultiKeyMap<T, K, V> multiKeyMap,
        final Supplier<T> subKeySupplier, final Supplier<V> valueSupplier,
        final Supplier<Iterable<T>> collectionSupplier) {
      super();
      this.multiKeyMap = multiKeyMap;
      this.subKeySupplier = subKeySupplier;
      this.valueSupplier = valueSupplier;
      this.collectionSupplier = collectionSupplier;
    }

    @Override
    public SampleElements<Entry<K, V>> samples() {
      return samples(subKeySupplier, valueSupplier, collectionSupplier);
    }

    protected SampleElements<Entry<K, V>> samples(final Supplier<T> subKeySupplier,
        final Supplier<V> valueSupplier, final Supplier<Iterable<T>> collectionSupplier) {
      return new SampleElements<>(
          Helpers.mapEntry(newKey(collectionSupplier, subKeySupplier, 0), valueSupplier.get()),
          Helpers.mapEntry(newKey(collectionSupplier, subKeySupplier, 1), valueSupplier.get()),
          Helpers.mapEntry(newKey(collectionSupplier, subKeySupplier, 2), valueSupplier.get()),
          Helpers.mapEntry(newKey(collectionSupplier, subKeySupplier, 3), valueSupplier.get()),
          Helpers.mapEntry(newKey(collectionSupplier, subKeySupplier, 4), valueSupplier.get()));
    }

    @SuppressWarnings("unchecked")
    protected K newKey(final Supplier<Iterable<T>> collectionSupplier,
        final Supplier<T> subKeySupplier, final int index) {
      final Collection<T> key = (Collection<T>) collectionSupplier.get();

      for (int i = 0; i <= index; i++) {
        key.add(subKeySupplier.get());
      }

      return (K) key;
    }

    @Override
    public Map<K, V> create(final Object... entries) {
      @SuppressWarnings("unchecked")
      final Entry<K, V>[] array = new Entry[entries.length];
      int i = 0;
      for (final Object o : entries) {
        @SuppressWarnings("unchecked")
        final Entry<K, V> e = (Entry<K, V>) o;
        array[i++] = e;
      }
      return create(array);
    }

    protected Map<K, V> create(@SuppressWarnings("unchecked") final Entry<K, V>... entries) {
      for (final Entry<K, V> entry : Objects.requireNonNull(entries)) {
        multiKeyMap.put(Objects.requireNonNull(entry).getKey(), entry.getValue());
      }

      return multiKeyMap;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Entry<K, V>[] createArray(final int length) {
      return (Entry<K, V>[]) new Entry<?, ?>[length];
    }

    @Override
    public Iterable<Entry<K, V>> order(final List<Entry<K, V>> insertionOrder) {
      return insertionOrder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public K[] createKeyArray(final int length) {
      return (K[]) new Object[length];
    }

    @SuppressWarnings("unchecked")
    @Override
    public V[] createValueArray(final int length) {
      return (V[]) new Object[length];
    }
  }

  public static class StringMultiKeyMapTestGenerator
      extends MultiKeyMapTestGenerator<String, Iterable<String>, String> {

    public StringMultiKeyMapTestGenerator() {
      super(new CircularStringSupplier(),
          new CircularStringSupplier(ImmutableList.of("1", "2", "3", "4", "5")));
    }

    public StringMultiKeyMapTestGenerator(
        final MultiKeyMap<String, Iterable<String>, String> multiKeyMap,
        final Supplier<String> subKeySupplier, final Supplier<String> valueSupplier,
        final Supplier<Iterable<String>> collectionSupplier) {
      super(multiKeyMap, subKeySupplier, valueSupplier, collectionSupplier);
    }

    @Override
    public SampleElements<Entry<Iterable<String>, String>> samples() {
      return new SampleElements<>(
          Helpers.mapEntry(Arrays.asList("one0", "one1", "one2"), "January"),
          Helpers.mapEntry(Arrays.asList("two0", "two1", "two2"), "February"),
          Helpers.mapEntry(Arrays.asList("three0", "three1", "three2"), "March"),
          Helpers.mapEntry(Arrays.asList("four0", "four1", "four2"), "April"),
          Helpers.mapEntry(Arrays.asList("five0", "five1", "five2"), "May"));
    }


    public static final class CircularStringSupplier implements Supplier<String> {
      private final List<String> source;
      private int current;

      public CircularStringSupplier() {
        this(ImmutableList.of("one", "two", "three", "four", "five"));
      }

      public CircularStringSupplier(final List<String> source) {
        super();
        this.source = ImmutableList.copyOf(Objects.requireNonNull(source));
        current = -1;
      }

      @Override
      public String get() {
        return source.get(++current < source.size() ? current : (current = 0));
      }
    }
  }
}
