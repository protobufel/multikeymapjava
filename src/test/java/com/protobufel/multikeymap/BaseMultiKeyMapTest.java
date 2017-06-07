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

import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BaseMultiKeyMapTest {

  @Before
  public void setUp() throws Exception {}

  @After
  public void tearDown() throws Exception {}

  @Test
  public void testGetValuesByPartialKey() {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetEntriesByPartialKey() {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testGetFullKeysByPartialKey() {

    final MultiKeyMap<String, Iterable<String>, String> map = MultiKeyMaps.newMultiKeyMap();

    // add a record
    map.put(Arrays.asList("Hello", ",", "wonderful", "world"), "You found me!");

    // or copy some data from the compatible Map
    final Map<Iterable<String>, String> dict = new HashMap<>();
    dict.put(Arrays.asList("okay", "I", "am", "here"), "or there!");
    dict.put(Arrays.asList("okay", "I", "am", "not", "here"), "for sure!");

    // adding the data from another Map or MultiKeyMap
    map.putAll(dict);

    // MultiKeyMap interface extends Map, and also adds get{FullKeys|Values|Entries}ByPartialKey
    // methods of its own
    final String exactMatch = map.get(Arrays.asList("okay", "I", "am", "here"));

    if (exactMatch != null) {
      System.out.println(String.format(
          "This is a regular Map method, looking for exact full key. Let's see the actual value: %s",
          exactMatch));
    }

    // lets look by partial key anywhere within the full key (any sequence in any order of some
    // sub-keys of the original full key we're looking for)
    // should be 1 record with value = 'for sure!'. Let's see the actual one:
    map.getValuesByPartialKey(Arrays.asList("not", "I")).ifPresent(System.out::println);

    // lets look by partial key, wherein some sub-keys are looked at the particular 0-based
    // positions ( >= 0), and others anywhere ( < 0)
    // should be 1 record with value = 'or there!'. Let's see the actual one:
    map.getValuesByPartialKey(Arrays.asList("here", "I", "am"), Arrays.asList(3, -1, -1))
        .ifPresent(System.out::println);

    map.clear();
    // Happy using!
  }

}
