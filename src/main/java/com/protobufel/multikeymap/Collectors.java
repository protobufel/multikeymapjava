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

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.StreamSupport;

final class Collectors {
  private Collectors() {}

  public static <K> Optional<Set<K>> intersectSets(final Iterable<? extends Set<K>> source,
      final boolean parallel) {
    return StreamSupport.stream(Objects.requireNonNull(source).spliterator(), parallel).unordered()
        .min(comparingInt(set -> Objects.requireNonNull(set).size()))
        .map(smallestSet -> StreamSupport.stream(source.spliterator(), parallel).unordered()
            .collect(setIntersecting(smallestSet, parallel)));
  }

  public static <K> Collector<Set<K>, Set<K>, Set<K>> setIntersecting(final Set<K> smallestSet,
      final boolean parallel) {
    return setIntersecting(() -> smallestSet, parallel);
  }

  public static <K> Collector<Set<K>, Set<K>, Set<K>> setIntersecting(
      final Supplier<Set<K>> smallestSetSupplier, final boolean parallel) {
    return parallel ? new ConcurrentSetIntersecting<>(smallestSetSupplier)
        : new SequentialSetIntersecting<>(smallestSetSupplier);
  }

  static final class ConcurrentSetIntersecting<K> implements Collector<Set<K>, Set<K>, Set<K>> {
    private final Supplier<Set<K>> smallestSetSupplier;

    ConcurrentSetIntersecting(final Supplier<Set<K>> smallestSetSupplier) {
      super();
      this.smallestSetSupplier = Objects.requireNonNull(smallestSetSupplier);
    }

    @Override
    public Supplier<Set<K>> supplier() {
      return () -> {
        final Set<K> smallestSet = smallestSetSupplier.get();
        final KeySetView<K, Boolean> newKeySet = ConcurrentHashMap.<K>newKeySet(smallestSet.size());
        newKeySet.addAll(smallestSet);
        return newKeySet;
      };
    }

    @Override
    public BiConsumer<Set<K>, Set<K>> accumulator() {
      return (set1, set2) -> {
        if ((set2 != smallestSetSupplier.get()) && !set1.isEmpty() && !set2.isEmpty()) {
          set1.retainAll(set2);
        }
      };
    }

    @Override
    public BinaryOperator<Set<K>> combiner() {
      return (set1, set2) -> set1.isEmpty() || set2.isEmpty()
          || (set1.retainAll(set2) && set1.isEmpty()) ? Collections.emptySet() : set1;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections.unmodifiableSet(EnumSet.allOf(Characteristics.class));
    }

    @Override
    public Function<Set<K>, Set<K>> finisher() {
      return Function.identity();
    }
  }

  static final class SequentialSetIntersecting<K> implements Collector<Set<K>, Set<K>, Set<K>> {
    private final Supplier<Set<K>> smallestSetSupplier;

    SequentialSetIntersecting(final Supplier<Set<K>> smallestSetSupplier) {
      super();
      this.smallestSetSupplier = Objects.requireNonNull(smallestSetSupplier);
    }

    @Override
    public Supplier<Set<K>> supplier() {
      return () -> new HashSet<>(smallestSetSupplier.get());
    }

    @Override
    public BiConsumer<Set<K>, Set<K>> accumulator() {
      return (set1, set2) -> {
        if ((set2 != smallestSetSupplier.get()) && !set1.isEmpty() && !set2.isEmpty()) {
          set1.retainAll(set2);
        }
      };
    }

    @Override
    public BinaryOperator<Set<K>> combiner() {
      return (set1, set2) -> set1.isEmpty() || set2.isEmpty()
          || (set1.retainAll(set2) && set1.isEmpty()) ? Collections.emptySet() : set1;
    }

    @Override
    public Set<Characteristics> characteristics() {
      return Collections
          .unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH, Characteristics.UNORDERED));
    }

    @Override
    public Function<Set<K>, Set<K>> finisher() {
      return Function.identity();
    }
  }

  public static <T, E, K extends Iterable<E>, U> Collector<T, ?, MultiKeyMap<E, K, U>> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends U> valueMapper) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, (k, v) -> {
      throw new IllegalStateException(String.format("duplicate key %s", k));
    }, MultiKeyMaps::<E, K, U>newMultiKeyMap);
  }

  public static <T, E, K extends Iterable<E>, U> Collector<T, ?, MultiKeyMap<E, K, U>> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, mergeFunction,
        MultiKeyMaps::<E, K, U>newMultiKeyMap);
  }

  public static <T, E, K extends Iterable<E>, U, M extends MultiKeyMap<E, K, U>> Collector<T, ?, M> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends U> valueMapper, final BinaryOperator<U> mergeFunction,
      final Supplier<M> multiKeyMapSupplier) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, mergeFunction,
        multiKeyMapSupplier);
  }
}
