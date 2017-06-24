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

package com.github.protobufel.multikeymap;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.github.protobufel.multikeymap.Collectors.intersectSets;

class BaseMultiKeyMap<T, K extends Iterable<T>, V> implements MultiKeyMap<T, K, V>, Serializable {
    static boolean enableParallelStreaming = false;
    private Map<K, V> fullMap;
    private transient LiteSetMultimap<T, K> partMap;
    private transient Set<K> keySet;
    private transient Collection<V> values;
    private transient Set<Entry<K, V>> entrySet;

    BaseMultiKeyMap() {
        this(new HashMap<>(), LiteSetMultimap.newInstance());
    }

    BaseMultiKeyMap(final Map<K, V> sourceMap) {
        this(new HashMap<>(Objects.requireNonNull(sourceMap)), LiteSetMultimap.newInstance());
    }

    BaseMultiKeyMap(final Map<K, V> fullMap, final LiteSetMultimap<T, K> partMap) {
        super();
        this.fullMap = Objects.requireNonNull(fullMap);
        this.partMap = Objects.requireNonNull(partMap);
    }

    private void writeObject(java.io.ObjectOutputStream out)
            throws IOException {

    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        readObjectNoData();
        in.defaultReadObject();
        // FIXME: initialize partMap
        fullMap.forEach((k, v) -> putPartial(k));
    }

    private void readObjectNoData()
            throws ObjectStreamException {
// FIXME: implement or remove!
    }

    static boolean isEnableParallelStreaming() {
        return enableParallelStreaming;
    }

    static void setEnableParallelStreaming(final boolean enableParallelStreaming) {
        BaseMultiKeyMap.enableParallelStreaming = enableParallelStreaming;
    }

    // TODO remove or re-enable after comparing the performance w/ the Java 8 based
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
    public String toString() {
        return fullMap.toString();
    }

    @Override
    public Stream<K> getFullKeysByPartialKey(final Iterable<? extends T> partialKey) {
        Objects.requireNonNull(partialKey);

        if (partMap.isEmpty()) {
            return Stream.empty();
        }

        if (!(partialKey instanceof Set)) {
            return getFullKeysByPartialKey(partialKey, Collections.emptyList());
        }

        // Java 8 doesn't allow to break the processing and also discourages stateful functions
        // untill Java 9 takeWhile!
        // final Set<Set<K>> sets = ((Set<? extends T>) partialKey).stream().unordered()
        // .map(subKey -> partMap.get(Objects.requireNonNull(subKey))).collect(toSet());

        final List<Set<K>> sets = new ArrayList<>();

        for (final T subKey : partialKey) {
            final Set<K> set = partMap.get(Objects.requireNonNull(subKey));

            if (set == null) {
                return Stream.empty();
            }

            sets.add(set);
        }

        if (sets.isEmpty()) {
            return Stream.empty();
        }

        final Set<K> result = intersectSets(sets, isEnableParallelStreaming());
        return result.isEmpty() ? Stream.empty() : result.stream();
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
        return fullMap.get(Objects.requireNonNull(key));
    }

    @Override
    public V put(final K key, final V value) {
        Objects.requireNonNull(value);
        final Object[] oldValue = {null};

        fullMap.compute(key, (k, v) -> {
            if (v == null) {
                putPartial(k);
            } else {
                oldValue[0] = v;
            }

            return value;
        });

        @SuppressWarnings("unchecked") final V oldV = (V) oldValue[0];
        return oldV;
    }

    @Override
    public V remove(final Object key) {
        @SuppressWarnings("unchecked") final K fullKey = (K) key;
        final Object[] oldValue = {null};
        fullMap.computeIfPresent(fullKey, (k, v) -> {
            deletePartial(k);
            oldValue[0] = v;
            return null;
        });

        @SuppressWarnings("unchecked") final V oldV = (V) oldValue[0];
        return oldV;
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        for (final Map.Entry<? extends K, ? extends V> entry : Objects.requireNonNull(m).entrySet()) {
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
        if (o == this) {
            return true;
        }

        if (!(o instanceof Map)) {
            return false;
        }

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

    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }

        return keySet;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }

        return entrySet;
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
                @SuppressWarnings("unchecked") final K key = (K) o;
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
                @SuppressWarnings("unchecked") final Entry<K, V> entry = (Entry<K, V>) o;
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
