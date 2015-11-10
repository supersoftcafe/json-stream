package com.supersoftcafe.json_stream.impl;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.Match;
import com.supersoftcafe.json_stream.CallbackParser;
import com.supersoftcafe.json_stream.Parser;
import com.supersoftcafe.json_stream.TypeRef;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;


public class ParserImpl implements Parser {
    private ObjectReader objectReader;
    private JsonFactory  jsonFactory;


    public ParserImpl(ObjectReader backend) {
        objectReader = Objects.requireNonNull(objectReader);
        jsonFactory  = objectReader.getFactory();
    }


    @Override
    public CallbackParser parser() {
        return new CallbackParserImpl(objectReader, jsonFactory);
    }


    @Override public <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return new InternalIterator(objectReader, jsonFactory, in, type, paths);
    }

    @Override public <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return new InternalIterator(objectReader, jsonFactory, in, type, paths);
    }

    @Override public <T> Optional<T> getFirst(InputStream in, TypeRef<T> type, String... paths) throws IOException {
        return new BiConsumer<Object, Match<T>>() {
            private Optional<T> result = Optional.empty();

            public @Override void accept(Object o, Match<T> match) {
                if (!result.isPresent()) result = Optional.of(match.getContent());
                match.stop();
            }

            Optional<T> process() throws IOException {
                CallbackParser parser = parser();
                for (String path : paths) parser.whenMatch(path, type, this);
                parser.parse(in);
                return result;
            }
        }.process();
    }

    @Override public <T> Optional<T> getFirst(Reader in, TypeRef<T> type, String... paths) throws IOException {
        return new BiConsumer<Object, Match<T>>() {
            private Optional<T> result = Optional.empty();

            public @Override void accept(Object o, Match<T> match) {
                if (!result.isPresent()) result = Optional.of(match.getContent());
                match.stop();
            }

            Optional<T> process() throws IOException {
                CallbackParser parser = parser();
                for (String path : paths) parser.whenMatch(path, type, this);
                parser.parse(in);
                return result;
            }
        }.process();
    }
}
