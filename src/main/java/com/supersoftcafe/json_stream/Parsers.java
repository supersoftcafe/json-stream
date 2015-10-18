package com.supersoftcafe.json_stream;


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
    private Parsers() { }



    public static Parser create() {
        return new ParserImpl();
    }



    public static <T> T getOne(InputStream in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public static <T> T getOne(InputStream in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public static <T> T getOne(Reader in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }

    public static <T> T getOne(Reader in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst().get();
    }



    public static <T> Optional<T> getFirst(InputStream in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public static <T> Optional<T> getFirst(InputStream in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public static <T> Optional<T> getFirst(Reader in, Class<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }

    public static <T> Optional<T> getFirst(Reader in, TypeRef<T> type, String... paths) {
        return stream(in, type, paths).findFirst();
    }



    public static <T> Stream<T> stream(InputStream in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public static <T> Stream<T> stream(InputStream in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public static <T> Stream<T> stream(Reader in, Class<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }

    public static <T> Stream<T> stream(Reader in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(in, type, paths), 0), false);
    }



    public static <T> Iterator<T> iterator(InputStream in, Class<T> type, String... paths) {
        return new InternalIterator<T>(in, type, paths);
    }

    public static <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(in, type, paths);
    }

    public static <T> Iterator<T> iterator(Reader in, Class<T> type, String... paths) {
        return new InternalIterator<T>(in, type, paths);
    }

    public static <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(in, type, paths);
    }
}
