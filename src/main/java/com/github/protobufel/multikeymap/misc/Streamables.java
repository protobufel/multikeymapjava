/*
 *    Copyright 2017 David Tesler
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.protobufel.multikeymap.misc;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

public final class Streamables {
    private Streamables() {
    }

    @FunctionalInterface
    public interface Streamable<T> {
        Stream<T> stream();
    }

    @FunctionalInterface
    public interface ParallelStreamable<T> {
        Stream<T> parallelStream();
    }

    @FunctionalInterface
    public interface Restreamable<T> {
        Restream<T> restream();
    }

    public interface Restream<T> extends Stream<T>, Streamable<T>, Restreamable<T> {

        static <T> Restream<T> empty() {
            return BaseRestream.empty();
        }

        static <T> Restream<T> of(T t) {
            return new BaseRestream<>(() -> Stream.of(t));
        }

        @SafeVarargs
        static <T> Restream<T> of(T... values) {
            return new BaseRestream<>(() -> Stream.of(values));
        }

        static <T> Restream<T> of(Collection<T> collection) {
            Objects.requireNonNull(collection);
            return new BaseRestream<>(collection::stream);
        }

        static <T> Restream<T> of(Iterable<T> iterable) {
            if (Objects.requireNonNull(iterable) instanceof Collection) {
                return of((Collection<T>) iterable);
            }

            return new BaseRestream<>(() -> StreamSupport.stream(iterable.spliterator(), false));
        }

        static <T> Restream<T> of(Streamable<T> streamable, Function<Stream<T>, Stream<T>> mapper) {
            Objects.requireNonNull(streamable);
            Objects.requireNonNull(mapper);
            return new BaseRestream<>(() -> mapper.apply(streamable.stream()));
        }

        static <T> Restream<T> iterate(T seed, UnaryOperator<T> f) {
            Objects.requireNonNull(f);
            return new BaseRestream<>(() -> Stream.iterate(seed, f));
        }

        static <T> Restream<T> generate(Supplier<T> s) {
            Objects.requireNonNull(s);
            return new BaseRestream<>(() -> Stream.generate(s));
        }

        static <T> Restream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
            Objects.requireNonNull(a);
            Objects.requireNonNull(b);
            return new BaseRestream<>(() -> Stream.concat(a, b));
        }
    }

    static class BaseStreamable<T> implements Streamable<T>, ParallelStreamable<T> {
        private static final BaseStreamable<?> EMPTY = new BaseStreamable<>(Stream::empty);
        private final Streamable<T> delegate;

        BaseStreamable(Streamable<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Stream<T> stream() {
            return delegate.stream();
        }

        public Stream<T> parallelStream() {
            return delegate.stream().parallel();
        }

        @SuppressWarnings("unchecked")
        public static <T> BaseStreamable<T> empty() {
            return (BaseStreamable<T>) EMPTY;
        }
    }

    static class BaseRestream<T> implements Restream<T>, Restreamable<T> {
        private static final BaseRestream<?> EMPTY = new BaseRestream<>(Stream::empty);
        private final Streamable<T> delegate;
        private Stream<T> stream;

        BaseRestream(Streamable<T> delegate) {
            this.delegate = delegate;
            stream = this.delegate.stream();
        }

        @SuppressWarnings("unchecked")
        static <T> BaseRestream<T> empty() {
            return (BaseRestream<T>) EMPTY;
        }

        protected Stream<T> getStream() {
            return stream;
        }

        @Override
        public Stream<T> stream() {
            return delegate.stream();
        }

        @Override
        public Restream<T> restream() {
            return new BaseRestream<>(delegate);
        }

        @Override
        public Stream<T> filter(Predicate<? super T> predicate) {
            return getStream().filter(predicate);
        }

        @Override
        public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
            return getStream().map(mapper);
        }

        @Override
        public IntStream mapToInt(ToIntFunction<? super T> mapper) {
            return getStream().mapToInt(mapper);
        }

        @Override
        public LongStream mapToLong(ToLongFunction<? super T> mapper) {
            return getStream().mapToLong(mapper);
        }

        @Override
        public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
            return getStream().mapToDouble(mapper);
        }

        @Override
        public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
            return getStream().flatMap(mapper);
        }

        @Override
        public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
            return getStream().flatMapToInt(mapper);
        }

        @Override
        public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
            return getStream().flatMapToLong(mapper);
        }

        @Override
        public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
            return getStream().flatMapToDouble(mapper);
        }

        @Override
        public Stream<T> distinct() {
            return getStream().distinct();
        }

        @Override
        public Stream<T> sorted() {
            return getStream().sorted();
        }

        @Override
        public Stream<T> sorted(Comparator<? super T> comparator) {
            return getStream().sorted(comparator);
        }

        @Override
        public Stream<T> peek(Consumer<? super T> action) {
            return getStream().peek(action);
        }

        @Override
        public Stream<T> limit(long maxSize) {
            return getStream().limit(maxSize);
        }

        @Override
        public Stream<T> skip(long n) {
            return getStream().skip(n);
        }

        @Override
        public void forEach(Consumer<? super T> action) {
            getStream().forEach(action);
        }

        @Override
        public void forEachOrdered(Consumer<? super T> action) {
            getStream().forEachOrdered(action);
        }

        @Override
        public Object[] toArray() {
            return getStream().toArray();
        }

        @Override
        public <A> A[] toArray(IntFunction<A[]> generator) {
            return getStream().toArray(generator);
        }

        @Override
        public T reduce(T identity, BinaryOperator<T> accumulator) {
            return getStream().reduce(identity, accumulator);
        }

        @Override
        public Optional<T> reduce(BinaryOperator<T> accumulator) {
            return getStream().reduce(accumulator);
        }

        @Override
        public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
            return getStream().reduce(identity, accumulator, combiner);
        }

        @Override
        public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
            return getStream().collect(supplier, accumulator, combiner);
        }

        @Override
        public <R, A> R collect(Collector<? super T, A, R> collector) {
            return getStream().collect(collector);
        }

        @Override
        public Optional<T> min(Comparator<? super T> comparator) {
            return getStream().min(comparator);
        }

        @Override
        public Optional<T> max(Comparator<? super T> comparator) {
            return getStream().max(comparator);
        }

        @Override
        public long count() {
            return getStream().count();
        }

        @Override
        public boolean anyMatch(Predicate<? super T> predicate) {
            return getStream().anyMatch(predicate);
        }

        @Override
        public boolean allMatch(Predicate<? super T> predicate) {
            return getStream().allMatch(predicate);
        }

        @Override
        public boolean noneMatch(Predicate<? super T> predicate) {
            return getStream().noneMatch(predicate);
        }

        @Override
        public Optional<T> findFirst() {
            return getStream().findFirst();
        }

        @Override
        public Optional<T> findAny() {
            return getStream().findAny();
        }

        @Override
        public Iterator<T> iterator() {
            return getStream().iterator();
        }

        @Override
        public Spliterator<T> spliterator() {
            return getStream().spliterator();
        }

        @Override
        public boolean isParallel() {
            return getStream().isParallel();
        }

        @Override
        public Stream<T> sequential() {
            return getStream().sequential();
        }

        @Override
        public Stream<T> parallel() {
            return getStream().parallel();
        }

        @Override
        public Stream<T> unordered() {
            return getStream().unordered();
        }

        @Override
        public Stream<T> onClose(Runnable closeHandler) {
            return getStream().onClose(closeHandler);
        }

        @Override
        public void close() {
            getStream().close();
        }
    }
}
