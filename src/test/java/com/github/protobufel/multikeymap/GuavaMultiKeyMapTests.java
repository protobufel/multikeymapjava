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

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RunWith(AllTests.class)
public class GuavaMultiKeyMapTests extends TestCase {

    public static Test suite() {
        final TestSuite suite =
                MapTestSuiteBuilder.using(new MultiKeyMapGenerators.StringMultiKeyMapTestGenerator())
                        .named("MultiKeyMap of strings by default constructor").withFeatures(CollectionSize.ANY,
                        // CollectionFeature.ALLOWS_NULL_VALUES,
                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        // CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        // CollectionFeature.SUPPORTS_REMOVE,
                        // CollectionFeature.GENERAL_PURPOSE
                        MapFeature.GENERAL_PURPOSE,
                        MapFeature.RESTRICTS_KEYS,
                        MapFeature.RESTRICTS_VALUES,
                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
                        .createTestSuite();

        //TODO: investigate why it fails!
//        final TestSuite suite2 =
//                MapTestSuiteBuilder.using(new MultiKeyMapGenerators.StringMultiKeyMapTestGenerator(
//                        MultiKeyMaps.newMultiKeyMap(
//                                TreeMap<Iterable<String>, String>::new,
//                                HashMap<String, Set<Iterable<String>>>::new)
//                ))
//                        .named("MultiKeyMap of strings by TreeMap").withFeatures(CollectionSize.ANY,
//                        // CollectionFeature.ALLOWS_NULL_VALUES,
//                        CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
//                        // CollectionFeature.SUPPORTS_ADD,
//                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
//                        // CollectionFeature.SUPPORTS_REMOVE,
//                        // CollectionFeature.GENERAL_PURPOSE
//                        MapFeature.GENERAL_PURPOSE,
//                        MapFeature.RESTRICTS_KEYS,
//                        MapFeature.RESTRICTS_VALUES,
//                        MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION)
//                        .createTestSuite();
//
//        suite.addTest(suite2);


        final TestSuite suite3 =
                MapTestSuiteBuilder.using(new MultiKeyMapGenerators.StringMultiKeyMapTestGenerator(
                        MultiKeyMaps.newMultiKeyMap(
                                ConcurrentHashMap<Iterable<String>, String>::new,
                                ConcurrentHashMap<String, Set<Iterable<String>>>::new)
                ))
                        .named("MultiKeyMap of strings by ConcurrentHashMap").withFeatures(CollectionSize.ANY,
                        // CollectionFeature.ALLOWS_NULL_VALUES,
                        //CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        // CollectionFeature.SUPPORTS_ADD,
                        CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
                        // CollectionFeature.SUPPORTS_REMOVE,
                        // CollectionFeature.GENERAL_PURPOSE
                        //MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
                        MapFeature.GENERAL_PURPOSE,
                        MapFeature.RESTRICTS_KEYS,
                        MapFeature.RESTRICTS_VALUES
                )
                        .createTestSuite();

        suite.addTest(suite3);

        return suite;
    }
}
