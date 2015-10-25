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

The stream is closed by the library. This is important behaviour, as we don't in all cases know when
the library has finished with the stream, and so can't easily encapsulate calls with open/close
pairs. To deal with this, all API methods consistently close the given stream.

If you don't want it closed, use a non-closing wrapper. This will be important if you happen to be
reading from a ZIP file, where 'closeEntry' is the preferred method of closing a single entry, and
not to close the underlying stream.

For convenience, the following is provided.

```
InputStream open() {
    return IOUtils.noClose(new FileInputStream("sample.json"));
}
```