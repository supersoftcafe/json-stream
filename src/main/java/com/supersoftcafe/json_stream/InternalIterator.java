package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;

import java.io.*;
import java.util.*;
import java.util.function.BiConsumer;


final class InternalIterator<T> implements Iterator<T> {
    private InternalParser internalParser;
    private ArrayDeque<T> elements;
    private boolean nextFound;


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


    private void setup(Closeable underlyingStream, JsonParser jsonParser, JavaType type, String[] jsonPaths) {
        List<ElementMatcher<?>> matchers = new ArrayList<>();
        BiConsumer<Path, T> consumer = (path, value) -> elements.addFirst(value);
        for (String jsonPath : jsonPaths)
            matchers.add(new ElementMatcher<>(MatchRule.valueOf(jsonPath), type, consumer));

        internalParser = new InternalParser(matchers, underlyingStream, jsonParser);
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
