package com.supersoftcafe.json_stream.impl;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.supersoftcafe.json_stream.TypeRef;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;


public final class InternalIterator<T> implements Iterator<T> {
    private InternalParser internalParser;
    private ArrayDeque<T> elements;
    private boolean nextFound;



    private static JsonParser createParser(InputStream in) {
        try {
            return CallbackParserImpl.JSON_FACTORY.createParser(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static JsonParser createParser(Reader in) {
        try {
            return CallbackParserImpl.JSON_FACTORY.createParser(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }




    public InternalIterator(InputStream in, TypeRef<T> type, String[] jsonPaths) {
        setup(in, createParser(in), TypeCache.constructType(type), jsonPaths);
    }

    public InternalIterator(Reader in, TypeRef<T> type, String[] jsonPaths) {
        setup(in, createParser(in), TypeCache.constructType(type), jsonPaths);
    }



    private void setup(Closeable underlyingStream, JsonParser jsonParser, JavaType type, String[] jsonPaths) {
        List<ElementMatcher<?>> matchers = new ArrayList<>();
        BiConsumer<PathImpl, T> consumer = (path, value) -> elements.addFirst(value);
        for (String jsonPath : jsonPaths)
            matchers.add(new ElementMatcher<>(MatchRule.valueOf(jsonPath), type, consumer));

        internalParser = new InternalParser(matchers, underlyingStream, jsonParser, false);
        elements = new ArrayDeque<>();
        nextFound = false;
    }

    public @Override boolean hasNext() {
        try {
            boolean result = nextFound || (nextFound = !elements.isEmpty() || internalParser.parseOne());
            if (result == false) internalParser.parseEnd();
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public @Override T next() {
        if (hasNext()) {
            nextFound = false;
            return elements.removeLast();
        } else {
            throw new NoSuchElementException();
        }
    }
}
