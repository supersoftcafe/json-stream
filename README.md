JSON Stream
===========

License
-------

This software is distributed under the New BSD (3-clause) license as described in LICENSE.txt.

About
-----

This is a **push** API for **streaming** JSON with **JSONPath** selectors for the data you are interested in.

It uses **Java 8** features for code brevity and API simplicity.

A ~~picture~~ code sample speaks a thousand words.

```
InputStream open() {
    return new FileInputStream("sample.json");
}

void usingTheParser() {
    Parsers.create()
        .when("$.metadata", new TypeRef<Map<String,Object>>(){}, this::doSomethingWithMetadata)
        .when("$..thingy", Thingy.class, this::doSomethingWithThingy)
        .parse(open());
}

void usingTheStaticAPI() {
    Map<String, Object> metadata = Parsers.getOne(open(), new TypeRef<Map<String,Object>>(){}, "$.metadata");
    List<Thingy> thingies = Parsers.stream(open(), Thingy.class, "$..thingy").collect(Collectors.asList());
    
    doSomethingWithMetadata(metadata);
    thingies.forEach(this::doSomethingWithThingy);
}

void somethingMoreSpecific() {
    Thingy thingy = Parsers.getOne(open(), Thingy.class, "$..someArray[3].thingy");
    doSomethingWithThingy(thingy);
}
```

