package com.github.protobufel.multikeymap;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.github.protobufel.multikeymap.MultiKeyMap;
import com.github.protobufel.multikeymap.MultiKeyMaps;

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
        .range(j, j + subKeySize).boxed().map(x -> String.valueOf(x)).collect(Collectors.joining()))
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
        .map(j -> IntStream.range(j, j + subKeySize).boxed().map(x -> String.valueOf(x))
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

  public static void main(final String[] args) throws RunnerException {
    final Options opt =
        new OptionsBuilder().include(JMHMultiKeyMapVsMap.class.getSimpleName()).build();
    new Runner(opt).run();
  }
}
