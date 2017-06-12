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

import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import com.google.common.collect.testing.MapTestSuiteBuilder;
import com.google.common.collect.testing.features.CollectionFeature;
import com.google.common.collect.testing.features.CollectionSize;
import com.google.common.collect.testing.features.MapFeature;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

@RunWith(AllTests.class)
public class GuavaMultiKeyMapTests extends TestCase {

  public static Test suite() {
    final TestSuite suite = MapTestSuiteBuilder
        .using(new MultiKeyMapGenerators.StringMultiKeyMapTestGenerator())
        .named("MultiKeyMap of string keys and values").withFeatures(CollectionSize.ANY,
            // CollectionFeature.ALLOWS_NULL_VALUES,
            CollectionFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION,
            // CollectionFeature.SUPPORTS_ADD,
            CollectionFeature.SUPPORTS_ITERATOR_REMOVE,
            // CollectionFeature.SUPPORTS_REMOVE,
            // CollectionFeature.GENERAL_PURPOSE
            MapFeature.GENERAL_PURPOSE, 
            MapFeature.RESTRICTS_KEYS,
            MapFeature.FAILS_FAST_ON_CONCURRENT_MODIFICATION
            )
        .createTestSuite();
    return suite;
  }

}
