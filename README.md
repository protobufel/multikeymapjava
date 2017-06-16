[![Travis branch](https://img.shields.io/travis/protobufel/multikeymapjava/master.svg?style=plastic)](https://travis-ci.org/protobufel/multikeymapjava)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.protobufel/multikeymapjava.svg?style=plastic)](https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22multikeymapjava%22)

# MultiKeyMap Java Implementation #

Java 8 implementation of the multi-key map.  It behaves like a regular generic Map with the additional ability of getting its values 
by any combination of partial keys. For example, one can add any value with the complex key {"Hello", "the", "wonderful", "World!"} , 
and then query by any sequence of subkeys like {"wonderful", "Hello"}. In addition, you can query by a mixture of some 
any-position-sub-keys and positional sub-keys, as in the following example: 

```java
    MultiKeyMap<String, Iterable<String>, String> map = MultiKeyMaps.newMultiKeyMap();

    // add a record
    map.put(Arrays.asList("Hello", ",", "wonderful", "world"), "You found me!");

    // or copy some data from the compatible Map
    Map<Iterable<String>, String> dict = new HashMap<>();
    dict.put(Arrays.asList("okay", "I", "am", "here"), "or there!");
    dict.put(Arrays.asList("okay", "I", "am", "not", "here"), "for sure!");

    // adding the data from another Map or MultiKeyMap
    map.putAll(dict);

    // MultiKeyMap interface extends Map, and also adds get{FullKeys|Values|Entries}ByPartialKey
    // methods of its own
    String exactMatch = map.get(Arrays.asList("okay", "I", "am", "here"));

    if (exactMatch != null) {
      System.out.println(String.format(
          "This is a regular Map method, looking for exact full key. Let's see the actual value: %s",
          exactMatch));
    }

    // lets look by partial key anywhere within the full key (any sequence in any order of some
    // sub-keys of the original full key we're looking for)
    // should be 1 record with value = 'for sure!'. Let's see the actual one:
    map.getValuesByPartialKey(Arrays.asList("not", "I")).forEach(System.out::println);

    // lets look by partial key, wherein some sub-keys are looked at the particular 0-based
    // positions ( >= 0), and others anywhere ( < 0)
    // should be 1 record with value = 'or there!'. Let's see the actual one:
    map.getValuesByPartialKey(Arrays.asList("here", "I", "am"), Arrays.asList(3, -1, -1))
        .forEach(System.out::println);

    map.clear();
    // Happy using!

```

### For more see the [JavaDoc Documentation](https://protobufel.github.io/multikeymapjava/javadoc/ "JavaDoc and more").  

Happy coding,

David Tesler
