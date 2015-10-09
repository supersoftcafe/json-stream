package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.BiConsumer;


final class InternalIterator<T> implements Iterator<T>, BiConsumer<Path, T> {
    T element;
    boolean nextFound;
    InternalParser internalParser;


    InternalIterator(JsonFactory jsonFactory, InputStream in, JavaType type, String[] jsonPaths) {
        try {
            setup(in, jsonFactory.createParser(in), type, jsonPaths);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    InternalIterator(JsonFactory jsonFactory, Reader in, JavaType type, String[] jsonPaths) {
        try {
            setup(in, jsonFactory.createParser(in), type, jsonPaths);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    void setup(Closeable underlyingStream, JsonParser jsonParser, JavaType type, String[] jsonPaths) {
        List<ElementMatcher<?>> matchers = new ArrayList<>();
        for (String jsonPath : jsonPaths)
            matchers.add(new ElementMatcher<>(MatchRule.create(jsonPath), type, this));
        internalParser = new InternalParser(matchers, underlyingStream, jsonParser);
    }

    public @Override void accept(Path nodes, T t) {
        element = t;
    }

    public @Override boolean hasNext() {
        try {
            boolean result = nextFound || (nextFound = internalParser.parseOne());
            if (result == false) internalParser.parseEnd();
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public @Override T next() {
        if (hasNext()) {
            nextFound = false;
            return element;
        } else {
            throw new NoSuchElementException();
        }
    }
}
