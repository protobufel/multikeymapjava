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

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@State(Scope.Benchmark)
public class JMHMultiKeyMapVsMap {

    @Param({"1000"})
    public int readContainerSize = 1000;

    @Param({"10"})
    public int keySize = 10;

    @Param({"10"})
    public int subKeySize = 10;

    @Param({"100"})
    public int containerSize = 100;

    private Random random;

    private Map<Iterable<String>, Integer> readMap;
    private MultiKeyMap<String, Iterable<String>, Integer> readMultiKeymap;
    private Iterable<String> searchKey;

    private Map<Iterable<String>, Integer> map;
    private MultiKeyMap<String, Iterable<String>, Integer> multiKeymap;

    private List<String> addKey;

    private int addValue;

    private List<String> removeKey;

    private List<String> partialKey;

    private List<Integer> partialKeyPositions;

    public static void main(final String[] args) throws RunnerException {
        final Options opt =
                new OptionsBuilder().include(JMHMultiKeyMapVsMap.class.getSimpleName()).build();
        new Runner(opt).run();
    }

    @Setup(Level.Trial)
    public void trialSetup() {
        readMap = generateMap(readContainerSize);
        readMultiKeymap = MultiKeyMaps.of(readMap);
        random = new Random();
    }

    @Setup(Level.Iteration)
    public void iterationSetup() {
        map = generateMap(containerSize);
        multiKeymap = MultiKeyMaps.of(map);
        searchKey = generateKey(random.nextInt(readContainerSize));
        addKey = generateKey(containerSize + 1);
        addValue = random.nextInt();
        removeKey = generateKey(random.nextInt(containerSize));
        final int partialKeyIndex = random.nextInt(readContainerSize);
        partialKey = generatePartialKey(partialKeyIndex, 2, 2);
        partialKeyPositions = generatePartialKeyPositions(partialKeyIndex, 2, 2);
    }

    Map<Iterable<String>, Integer> generateMap(final int containerSize) {
        return IntStream.range(0, containerSize).boxed()
                .map(i -> new SimpleImmutableEntry<>(generateKey(i), i))
                .collect(toMap(SimpleImmutableEntry::getKey, SimpleImmutableEntry::getValue));
    }

    List<String> generateKey(final Integer recordIndex) {
        return IntStream.range(recordIndex, recordIndex + keySize).boxed().map(j -> IntStream
                .range(j, j + subKeySize).boxed().map(String::valueOf).collect(Collectors.joining()))
                .collect(toList());
    }

    List<Integer> generatePartialKeyPositions(final Integer recordIndex,
                                              final int nthPositivePosition, final int nthSkippedPosition) {
        return IntStream.range(0, keySize).filter(pos -> (pos % nthSkippedPosition) != 0)
                .map(pos -> (pos % nthPositivePosition) == 0 ? pos : -1).boxed().collect(toList());
    }

    List<String> generatePartialKey(final Integer recordIndex, final int nthPositivePosition,
                                    final int nthSkippedPosition) {
        return IntStream.range(recordIndex, recordIndex + keySize)
                .filter(pos -> (pos % nthSkippedPosition) != 0)
                .map(pos -> (pos % nthPositivePosition) == 0 ? pos : -1).boxed()
                .map(j -> IntStream.range(j, j + subKeySize).boxed().map(String::valueOf)
                        .collect(Collectors.joining()))
                .collect(toList());
    }

    @Benchmark
    public Integer baseline_getValue() {
        return readMap.get(searchKey);
    }

    @Benchmark
    public Integer getValue() {
        return readMultiKeymap.get(searchKey);
    }

    @Benchmark
    public Integer baseline_put() {
        return map.put(addKey, addValue);
    }

    @Benchmark
    public Integer put() {
        return multiKeymap.put(addKey, addValue);
    }

    @Benchmark
    public Integer baseline_remove() {
        return map.remove(removeKey);
    }

    @Benchmark
    public Integer remove() {
        return multiKeymap.remove(removeKey);
    }

    @Benchmark
    public Stream<Iterable<String>> baseline_getFullKeysByPartialKeyWithPositions() {
        return multiKeymap.getFullKeysByPartialKey(partialKey, partialKeyPositions);
    }

    @Benchmark
    public List<Iterable<String>> getFullKeysByPartialKeyWithPositions() {
        return multiKeymap.getFullKeysByPartialKey(partialKey, partialKeyPositions).collect(toList());
    }
}
