package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.impl.InternalIterator;
import com.supersoftcafe.json_stream.impl.ParserImpl;

import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public final class Parsers {
    private final ObjectReader objectReader;

    private Parsers(ObjectReader objectReader) {
        this.objectReader = objectReader;
    }


    public static Parsers newInstance() {
        return new Parsers(new ObjectMapper().reader());
    }

    public static Parsers newInstance(ObjectReader objectReader) {
        return new Parsers(objectReader);
    }



    public Parser parser() {
        return new ParserImpl(objectReader);
    }



    public <T> T getOne(InputStream in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public <T> T getOne(InputStream in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public <T> T getOne(Reader in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public <T> T getOne(Reader in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }



    public <T> Optional<T> getFirst(InputStream in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public <T> Optional<T> getFirst(InputStream in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public <T> Optional<T> getFirst(Reader in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public <T> Optional<T> getFirst(Reader in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }



    public <T> Stream<T> stream(InputStream in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public <T> Stream<T> stream(InputStream in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public <T> Stream<T> stream(Reader in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public <T> Stream<T> stream(Reader in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }



    public <T> Iterator<T> iterator(InputStream in, Class<T> type, String... paths) {
        return new InternalIterator<T>(objectReader, in, type, paths);
    }

    public <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(objectReader, in, type, paths);
    }

    public <T> Iterator<T> iterator(Reader in, Class<T> type, String... paths) {
        return new InternalIterator<T>(objectReader, in, type, paths);
    }

    public <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(objectReader, in, type, paths);
    }
}
