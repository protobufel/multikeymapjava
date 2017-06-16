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

package com.github.protobufel.multikeymap;

import static java.util.Comparator.comparingInt;

import java.util.Collection;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * The utility class providing the MultiKeyMap and multiple sets' intersection related collectors.
 *
 * @author David Tesler
 *
 */
public final class Collectors {
  private Collectors() {}

  /**
   * Returns the Optional result of the intersection of all sets in the supplied Iterable.
   *
   * @param source the Iterable of the sets to intersect with each other
   * @param parallel use parallel processing if true, sequential if false
   * @param <T> the type of the elements of the sets
   * @return the result of the intersection wrapped in Optional, or the empty Optional.
   */
  public static <T> Optional<Set<T>> intersectSets(final Iterable<? extends Set<T>> source,
      final boolean parallel) {
    return streamOf(Objects.requireNonNull(source), parallel)
        .min(comparingInt(set -> Objects.requireNonNull(set).size())).flatMap(smallestSet -> {
          final Set<T> result =
              streamOf(source, parallel).collect(setIntersecting(smallestSet, parallel));
          return result.isEmpty() ? Optional.empty() : Optional.of(result);
        });
  }

  static <T> Stream<T> streamOf(final Iterable<T> source, final boolean parallel) {
    if (source instanceof Collection) {
      final Collection<T> collection = (Collection<T>) source;
      return (parallel ? collection.parallelStream() : collection.stream()).unordered();
    } else {
      return StreamSupport.stream(Objects.requireNonNull(source).spliterator(), parallel)
          .unordered();
    }
  }

  /**
   * Gets a collector which intersects the stream of sets and returns the resulting set.
   *
   * @param smallestSet the smallest in size element of the stream of sets, any if there are several
   *        of the same size
   * @param parallel use parallel processing if true, sequential if false
   * @param <T> the type of the elements of the sets
   * @return the collector to perform the intersection of all elements of the stream of sets
   */
  public static <T> Collector<Set<T>, Set<T>, Set<T>> setIntersecting(final Set<T> smallestSet,
      final boolean parallel) {
    return setIntersecting(() -> smallestSet, parallel);
  }

  /**
   * Gets a collector which intersects the stream of sets and returns the resulting set.
   *
   * @param smallestSetSupplier the supplier of the smallest in size element of the stream of sets,
   *        any if there are several of the same size
   * @param parallel use parallel processing if true, sequential if false
   * @param <T> the type of the elements of the sets
   * @return the collector to perform the intersection of all elements of the stream of sets
   */
  public static <T> Collector<Set<T>, Set<T>, Set<T>> setIntersecting(
      final Supplier<Set<T>> smallestSetSupplier, final boolean parallel) {
    return parallel ? new ConcurrentSetIntersecting<>(smallestSetSupplier)
        : new SequentialSetIntersecting<>(smallestSetSupplier);
  }

  /**
   * Gets a MultiKeyMap producing collector.
   *
   * @param keyMapper the key producing mapping function
   * @param valueMapper the value producing mapping function
   * @param <T> the type of the stream elements
   * @param <E> the type of the sub-keys of the MultiKeyMap
   * @param <K> the type of the keys of the MultiKeyMap
   * @param <V> the type of the values of the MultiKeyMap
   * @return the MultiKeyMap producing collector
   */
  public static <T, E, K extends Iterable<E>, V> Collector<T, ?, MultiKeyMap<E, K, V>> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, (k, v) -> {
      throw new IllegalStateException(String.format("duplicate key %s", k));
    }, MultiKeyMaps::<E, K, V>newMultiKeyMap);
  }

  /**
   * Gets a MultiKeyMap producing collector.
   *
   * @param keyMapper the key producing mapping function
   * @param valueMapper the value producing mapping function
   * @param mergeFunction the value merging function
   * @param <T> the type of the stream elements
   * @param <E> the type of the sub-keys of the MultiKeyMap
   * @param <K> the type of the keys of the MultiKeyMap
   * @param <V> the type of the values of the MultiKeyMap
   * @return the MultiKeyMap producing collector
   */
  public static <T, E, K extends Iterable<E>, V> Collector<T, ?, MultiKeyMap<E, K, V>> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper, final BinaryOperator<V> mergeFunction) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, mergeFunction,
        MultiKeyMaps::<E, K, V>newMultiKeyMap);
  }

  /**
   * Gets a MultiKeyMap producing collector.
   *
   * @param keyMapper the key producing mapping function
   * @param valueMapper the value producing mapping function
   * @param mergeFunction the value merging function
   * @param multiKeyMapSupplier the MultiKeyMap supplier
   * @param <T> the type of the stream elements
   * @param <E> the type of the sub-keys of the MultiKeyMap
   * @param <K> the type of the keys of the MultiKeyMap
   * @param <V> the type of the values of the MultiKeyMap
   * @return the MultiKeyMap producing collector
   */
  public static <T, E, K extends Iterable<E>, V, M extends MultiKeyMap<E, K, V>> Collector<T, ?, M> toMultiKeyMap(
      final Function<? super T, ? extends K> keyMapper,
      final Function<? super T, ? extends V> valueMapper, final BinaryOperator<V> mergeFunction,
      final Supplier<M> multiKeyMapSupplier) {
    return java.util.stream.Collectors.toMap(keyMapper, valueMapper, mergeFunction,
        multiKeyMapSupplier);
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
}
