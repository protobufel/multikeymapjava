//// Copyright 2017 David Tesler
////
//// Licensed under the Apache License, Version 2.0 (the "License");
//// you may not use this file except in compliance with the License.
//// You may obtain a copy of the License at
////
//// http://www.apache.org/licenses/LICENSE-2.0
////
//// Unless required by applicable law or agreed to in writing, software
//// distributed under the License is distributed on an "AS IS" BASIS,
//// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//// See the License for the specific language governing permissions and
//// limitations under the License.
//
//package com.protobufel.multikeymap;
//
//import static org.junit.Assert.*;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.Objects;
//
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.google.common.collect.ImmutableMap;
//import com.google.common.collect.testing.Helpers;
//import com.google.common.collect.testing.MapTestSuiteBuilder;
//import com.google.common.collect.testing.SampleElements;
//import com.google.common.collect.testing.TestMapGenerator;
//import com.google.common.collect.testing.TestStringMapGenerator;
//import com.google.common.collect.testing.features.CollectionFeature;
//import com.google.common.collect.testing.features.CollectionSize;
//import com.google.common.collect.testing.google.MapGenerators;
//import com.google.common.collect.testing.google.MapGenerators.ImmutableMapGenerator;
//import com.google.common.testing.ForwardingWrapperTester;
//
//import junit.framework.TestSuite;
//
//public class MultiKeyMapAsMapWithGuavaTest {
//
//  @Before
//  public void setUp() throws Exception {}
//
//  @After
//  public void tearDown() throws Exception {}
//
//  @Test
//  public void testMultiMapForwardingToMap() {
//    MultiKeyMaps.newMultiKeyMap();
//    new ForwardingWrapperTester().includingEquals().testForwarding(Map.class,
//        map -> MultiKeyMaps.<String, Iterable<String>, Integer>newMultiKeyMap(map,
//            LiteSetMultimap.<String, Iterable<String>>newInstance()));
//  }
//
//  @Test
//  public void testMultiKeyMapAsMap() throws Exception {
//    MultiKeyMap<Integer, List<Integer>, Integer> multiKeyMap = MultiKeyMaps.<Integer,List<Integer>, Integer>newMultiKeyMap();
//    
//    TestSuite testSuite = MapTestSuiteBuilder.using(new MultiKeyMapGenerators.StringMultiKeyMapTestGenerator())
//    .named("MultiKeyMap of string keys and values")
//    .withFeatures(
//        CollectionSize.ANY,
////        CollectionFeature.ALLOWS_NULL_VALUES,
////        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
////        CollectionFeature.SUPPORTS_ADD,
////        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
////        CollectionFeature.SUPPORTS_REMOVE,
//        CollectionFeature.GENERAL_PURPOSE
//        ).createTestSuite();
//  }
//}
