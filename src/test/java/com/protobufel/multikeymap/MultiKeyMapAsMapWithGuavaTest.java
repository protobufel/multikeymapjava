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

import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.testing.ForwardingWrapperTester;

public class MultiKeyMapAsMapWithGuavaTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testMultiMapForwardingToMap() {
    MultiKeyMaps.newMultiKeyMap();
    new ForwardingWrapperTester().includingEquals().testForwarding(Map.class,
        map -> MultiKeyMaps.<String, Iterable<String>, Integer>newMultiKeyMap(map,
            LiteSetMultimap.<String, Iterable<String>>newInstance()));


  }
}
