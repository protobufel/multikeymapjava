// package com.protobufel.multikeymap;
//
// import static org.junit.Assert.*;
//
// import java.util.Collections;
// import java.util.HashMap;
// import java.util.HashSet;
// import java.util.List;
// import java.util.Map;
// import java.util.Random;
// import java.util.function.Function;
// import java.util.stream.Collectors;
// import java.util.stream.Stream;
// import java.util.stream.StreamSupport;
//
// import org.junit.After;
// import org.junit.AfterClass;
// import org.junit.Before;
// import org.junit.BeforeClass;
// import org.junit.Test;
//
// import com.google.common.base.Predicates;
// import com.google.common.collect.Iterables;
// import com.google.common.collect.Streams;
//
// public class MultiKeyMapsTest {
// private MultiKeyMap<Employee<Integer>, Iterable<Employee<Integer>>, String> map;
// private Map<Iterable<Employee<Integer>>, String> dict;
//
// private Iterable<Iterable<Employee<Integer>>> searchKeys;
// private Iterable<Iterable<Employee<Integer>>> partKeys;
// private Iterable<Integer> positions;
// private Iterable<Integer> positivePositions;
//
// private final int SearchCount = 2;
// private final int RecordCount = 30;
// private final int KeySize = 10;
//
//
// @BeforeClass
// public static void setUpBeforeClass() throws Exception {}
//
// @AfterClass
// public static void tearDownAfterClass() throws Exception {}
//
// @Before
// public void setUp() throws Exception {
// var tuple = InitData(subKeyComparer, keyComparer, strategy, subKeyConverter, valueConverter,
// SearchCount, RecordCount, KeySize);
//
// var map = MultiKeyMaps.CreateMultiKeyDictionary<Employee<Integer>, Iterable<Employee<Integer>>,
// String>(subKeyComparer, keyComparer, strategy);
// map.CopyFrom(tuple.dict);
//
// }
//
// public static <T, K extends Iterable<? extends T>, V> void InitData(
// Function<Integer, T> subKeyConverter,
// Function<Integer, V> valueConverter,
// int searchCount,
// int recordCount,
// int keySize)
// {
// Map<K, V> dict = new HashMap<>();
// PopulateDictionary(dict as Map<Iterable<T>, V>, recordCount, keySize, subKeyConverter,
// valueConverter);
// var keys = dict.Keys.Where((x, index) => index % 3 == 0).Take(searchCount).ToList();
// (var partKeys, var positivePositions, var positions) = PopulatePartialKeys((dict as
// Map<Iterable<T>, V>).Keys, searchCount);
// return (dict, keys, partKeys, positivePositions, positions);
// }
//
//
// public static <T, V> void PopulateDictionary(Map<Iterable<T>, V> map, int count, int keySize,
// Function<Integer, T> subKeyConverter, Function<Integer, V> valueConverter)
// {
// for (int i = 0; i < count; i++)
// {
// map.put(Stream.iterate(i, j -> j + 1).limit(keySize -
// i).collect(Collectors.mapping(subKeyConverter, Collectors.toList())),
// valueConverter.apply(i));
// }
// }
//
// public static <T> PopulatePartialKeys(Iterable<? extends Iterable<T>> keys, int count)
// {
// int partKeySizePercent = 75;
// int nthKey = 7;
//
// Random random = new Random();
// HashSet<Integer> positions = new HashSet<Integer>();
//
// int keySize = Iterables.size(Iterables.getFirst(keys, Collections.emptyList()));
// int partKeySize = keySize * partKeySizePercent / 100;
//
// Stream.iterate(0, i -> i + 1).limit(partKeySize).forEach(positions.add(random.nextInt(keySize)));
//
// List<Integer> positivePositions = positions.stream().sorted().collect(Collectors.toList());
// List<Integer> mixedPositions = positivePositions.stream().map(x -> (x % 2 == 0) ? -1 :
// x).collect(Collectors.toList());
//
// Collectors.
// //Streams.stream(keys).filter(Predicates.)
//
// Streams.zip(Streams.stream(keys), , function) .mapWithIndex(Streams.stream(keys), (key, index) ->
// (index % nthKey == 0) ?
//
//
// List<Iterable<T>> partKeys = StreamSupport.stream(keys.spliterator(), false).collect(Collectors.
// filter(e((x, index) => index % nthKey == 0)
// .Take(count)
// .Select(key => key.Where((subKey, pos) => positions.Contains(pos)).ToList().AsEnumerable())
// .ToList();
//
// return (partKeys as IList<Iterable<T>>, positivePositions, mixedPositions);
// }
//
//
//
// @After
// public void tearDown() throws Exception {}
//
// @Test
// public void testNewMultiKeyMapSupplierOfMapOfKVSupplierOfLiteSetMultimapOfTK() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testNewMultiKeyMapMapOfKVLiteSetMultimapOfTK() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testNewMultiKeyMap() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testNewHashMultiKeyMap() {
// fail("Not yet implemented");
// }
//
// @Test
// public void testOf() {
// fail("Not yet implemented");
// }
//
//
//// [TestClass]
//// public class BenchmarkBasedTests
//// {
////
//// public void Init(MultiKeyMaps.MultiKeyCreationStrategy strategy, bool subKeyEqualityByRef, bool
// keyEqualityByRef)
//// {
//// (map, dict, searchKeys, partKeys, positivePositions, positions) = Setup(strategy,
// subKeyEqualityByRef, keyEqualityByRef);
//// }
////
//// public (
//// IMultiKeyMap<Employee<Integer>, Iterable<Employee<Integer>>, String> map,
//// Map<Iterable<Employee<Integer>>, String> dict,
//// Iterable<Iterable<Employee<Integer>>> keys,
//// IList<Iterable<Employee<Integer>>> partKeys,
//// Iterable<Integer> positivePositions,
//// Iterable<Integer> positions)
//// Setup(MultiKeyMaps.MultiKeyCreationStrategy strategy, bool subKeyEqualityByRef, bool
// keyEqualityByRef)
//// {
//// var subKeyComparer = subKeyEqualityByRef.SubKeyComparerFor<Employee<Integer>>();
//// var keyComparer = keyEqualityByRef.KeyComparerFor<Employee<Integer>>();
////
//// Function<Integer, Employee<Integer>> subKeyConverter = x => new
// Employee<Integer>(String.Join("", Enumerable.Range(x, 10)), x);
//// Function<Integer, String> valueConverter = x => x.ToString();
////
//// var tuple = InitData(subKeyComparer, keyComparer, strategy, subKeyConverter, valueConverter,
// SearchCount, RecordCount, KeySize);
////
//// var map = MultiKeyMaps.CreateMultiKeyDictionary<Employee<Integer>, Iterable<Employee<Integer>>,
// String>(subKeyComparer, keyComparer, strategy);
//// map.CopyFrom(tuple.dict);
//// return (map, tuple.dict, tuple.keys, tuple.partKeys, tuple.positivePositions, tuple.positions);
//// }
////
//// [DataTestMethod]
//// [DataRow(OptimizedForPositionalSearch, false, false)]
//// [DataRow(OptimizedForPositionalSearch, false, true)]
//// [DataRow(OptimizedForPositionalSearch, true, false)]
//// [DataRow(OptimizedForPositionalSearch, true, true)]
//// [DataRow(OptimizedForNonPositionalSearch, false, false)]
//// [DataRow(OptimizedForNonPositionalSearch, false, true)]
//// [DataRow(OptimizedForNonPositionalSearch, true, false)]
//// [DataRow(OptimizedForNonPositionalSearch, true, true)]
//// public void TryGetFullKeysByPartialKeyTest(MultiKeyMaps.MultiKeyCreationStrategy strategy, bool
// subKeyEqualityByRef, bool keyEqualityByRef)
//// {
//// Init(strategy, subKeyEqualityByRef, keyEqualityByRef);
//// var subKeyComparer = subKeyEqualityByRef.SubKeyComparerFor<Employee<Integer>>();
//// var keyComparer = keyEqualityByRef.KeyComparerFor<Employee<Integer>>();
////
//// foreach (var key in searchKeys)
//// {
//// bool result = map.TryGetFullKeysByPartialKey(key, out var value);
//// result.Should().Be(true);
//// value.Should().NotBeNullOrEmpty().And.HaveCount(1).And.HaveElementAt(0, key);
//// }
////
//// foreach (var partKey in partKeys)
//// {
//// bool result = map.TryGetFullKeysByPartialKey(partKey, out var value);
//// result.Should().Be(true);
//// value.Should().NotBeNullOrEmpty()
//// .And.HaveCount(x => x > 0)
//// .And.NotContainNulls()
//// .And.OnlyHaveUniqueItems()
//// .And.OnlyContain(key => map.Keys.Contains(key, keyComparer))
//// .And.OnlyContain(key => partKey.Intersect(key, subKeyComparer).Count() == partKey.Count());
//// }
//// }
////
//// [DataTestMethod]
//// [DataRow(OptimizedForPositionalSearch, false, false)]
//// [DataRow(OptimizedForPositionalSearch, false, true)]
//// [DataRow(OptimizedForPositionalSearch, true, false)]
//// [DataRow(OptimizedForPositionalSearch, true, true)]
//// [DataRow(OptimizedForNonPositionalSearch, false, false)]
//// [DataRow(OptimizedForNonPositionalSearch, false, true)]
//// [DataRow(OptimizedForNonPositionalSearch, true, false)]
//// [DataRow(OptimizedForNonPositionalSearch, true, true)]
//// public void TryGetFullKeysByPartialKeyMixedPositionsTest(MultiKeyMaps.MultiKeyCreationStrategy
// strategy, bool subKeyEqualityByRef, bool keyEqualityByRef)
//// {
//// Init(strategy, subKeyEqualityByRef, keyEqualityByRef);
//// var subKeyComparer = subKeyEqualityByRef.SubKeyComparerFor<Employee<Integer>>();
//// var keyComparer = keyEqualityByRef.KeyComparerFor<Employee<Integer>>();
////
//// foreach (var partKey in partKeys)
//// {
//// bool result = map.TryGetFullKeysByPartialKey(partKey, positions, out var value);
//// result.Should().Be(true);
//// value.Should().NotBeNullOrEmpty()
//// .And.HaveCount(x => x > 0)
//// .And.NotContainNulls()
//// .And.OnlyHaveUniqueItems()
//// .And.OnlyContain(key => map.Keys.Contains(key, keyComparer))
//// .And.OnlyContain(key => partKey.Intersect(key, subKeyComparer).Count() == partKey.Count());
//// }
//// }
////
//// [DataTestMethod]
//// [DataRow(OptimizedForPositionalSearch, false, false)]
//// [DataRow(OptimizedForPositionalSearch, false, true)]
//// [DataRow(OptimizedForPositionalSearch, true, false)]
//// [DataRow(OptimizedForPositionalSearch, true, true)]
//// [DataRow(OptimizedForNonPositionalSearch, false, false)]
//// [DataRow(OptimizedForNonPositionalSearch, false, true)]
//// [DataRow(OptimizedForNonPositionalSearch, true, false)]
//// [DataRow(OptimizedForNonPositionalSearch, true, true)]
//// public void
// TryGetFullKeysByPartialKeyPositivePositionsTest(MultiKeyMaps.MultiKeyCreationStrategy strategy,
// bool subKeyEqualityByRef, bool keyEqualityByRef)
//// {
//// Init(strategy, subKeyEqualityByRef, keyEqualityByRef);
//// var subKeyComparer = subKeyEqualityByRef.SubKeyComparerFor<Employee<Integer>>();
//// var keyComparer = keyEqualityByRef.KeyComparerFor<Employee<Integer>>();
////
//// foreach (var partKey in partKeys)
//// {
//// bool result = map.TryGetFullKeysByPartialKey(partKey, positivePositions, out var value);
//// result.Should().Be(true);
//// value.Should().NotBeNullOrEmpty()
//// .And.HaveCount(x => x > 0)
//// .And.NotContainNulls()
//// .And.OnlyHaveUniqueItems()
//// .And.OnlyContain(key => map.Keys.Contains(key, keyComparer))
//// .And.OnlyContain(key => partKey.Intersect(key, subKeyComparer).Count() == partKey.Count());
//// }
//// }
//// }
//
// public final class Employee<T>
// {
// private final String name;
// private final T department;
//
// public Employee(String name, T department)
// {
// this.name = name;
// this.department = department;
// }
//
// public String getName() {
// return name;
// }
//
// public T getDepartment() {
// return department;
// }
//
// @Override
// public String toString() {
// return "Employee [name=" + name + ", department=" + department + "]";
// }
//
// @Override
// public int hashCode() {
// final int prime = 31;
// int result = 1;
// result = prime * result + getOuterType().hashCode();
// result = prime * result + ((department == null) ? 0 : department.hashCode());
// result = prime * result + ((name == null) ? 0 : name.hashCode());
// return result;
// }
//
// @Override
// public boolean equals(Object obj) {
// if (this == obj) {
// return true;
// }
// if (obj == null) {
// return false;
// }
// if (!(obj instanceof Employee)) {
// return false;
// }
// Employee other = (Employee) obj;
// if (!getOuterType().equals(other.getOuterType())) {
// return false;
// }
// if (department == null) {
// if (other.department != null) {
// return false;
// }
// } else if (!department.equals(other.department)) {
// return false;
// }
// if (name == null) {
// if (other.name != null) {
// return false;
// }
// } else if (!name.equals(other.name)) {
// return false;
// }
// return true;
// }
//
// private MultiKeyMapsTest getOuterType() {
// return MultiKeyMapsTest.this;
// }
// }
// }
