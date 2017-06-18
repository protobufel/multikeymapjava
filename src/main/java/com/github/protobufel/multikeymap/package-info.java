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

/**
 * <p>
 * Java 8 implementation of the {@link com.github.protobufel.multikeymap.MultiKeyMap}. It behaves like a
 * regular generic Map with the additional ability of getting its values by any combination of
 * partial keys.
 * <p>
 * {@link com.github.protobufel.multikeymap.MultiKeyMaps}'s static factory methods are main entries to this
 * package functionality. Use {@link com.github.protobufel.multikeymap.MultiKeyMaps#newMultiKeyMap()} to
 * get an instance of the default implementation. In addition, one can create a new
 * {@link com.github.protobufel.multikeymap.MultiKeyMap} based on the data copied from the provided map
 * using {@link com.github.protobufel.multikeymap.MultiKeyMaps#of(java.util.Map)} method.
 * <p>
 * For example, one can add any value with the complex key {"Hello", "the", "wonderful", "World!"},
 * and then query by any sequence of subkeys like {"wonderful", "Hello"}. In addition, you can query
 * by a mixture of some any-position-sub-keys and positional sub-keys, as in the following example:
 * <p>
 * <pre>
 * <code>
 * MultiKeyMap<String, Iterable<String>, String> map = MultiKeyMaps.newMultiKeyMap();
 *
 * // add a record
 * map.put(Arrays.asList("Hello", ",", "wonderful", "world"), "You found me!");
 *
 * // or copy some data from the compatible Map
 * Map<Iterable<String>, String> dict = new HashMap<>();
 * dict.put(Arrays.asList("okay", "I", "am", "here"), "or there!");
 * dict.put(Arrays.asList("okay", "I", "am", "not", "here"), "for sure!");
 *
 * // adding the data from another Map or MultiKeyMap
 * map.putAll(dict);
 *
 * // MultiKeyMap interface extends Map, and also adds get{FullKeys|Values|Entries}ByPartialKey
 * // methods of its own
 * String exactMatch = map.get(Arrays.asList("okay", "I", "am", "here"));
 *
 * if (exactMatch != null) {
 *   System.out.println(String.format("This is a regular Map method, looking for exact full key. Let's see the actual value: %s", exactMatch));
 * }
 *
 * // lets look by partial key anywhere within the full key (any sequence in any order of some
 * // sub-keys of the original full key we're looking for)
 * // should be 1 record with value = 'for sure!'. Let's see the actual one:
 * map.getValuesByPartialKey(Arrays.asList("not", "I")).forEach(System.out::println);
 *
 * // lets look by partial key, wherein some sub-keys are looked at the particular 0-based
 * // positions ( >= 0), and others anywhere ( < 0)
 * // should be 1 record with value = 'or there!'. Let's see the actual one:
 * map.getValuesByPartialKey(Arrays.asList("here", "I", "am"), Arrays.asList(3, -1, -1))
 *    .forEach(System.out::println);
 *
 * map.clear();
 * </code>
 * </pre>
 *
 * @author David Tesler
 */
package com.github.protobufel.multikeymap;
