package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;

import java.io.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;


public final class Parser {
    private final static JsonFactory JSON_FACTORY = new JsonFactory();

    private final List<ElementMatcher<?>> elementMatchers;


    private Parser() {
        this.elementMatchers = new ArrayList<>();
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
        return new InternalIterator<T>(JSON_FACTORY, in, TypeRef.constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(JSON_FACTORY, in, TypeRef.constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(Reader in, Class<T> type, String... paths) {
        return new InternalIterator<T>(JSON_FACTORY, in, TypeRef.constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(JSON_FACTORY, in, TypeRef.constructType(type), paths);
    }



    public <T> Parser when(String path, Class<T> clazz, BiConsumer<Path, T> handler) {
        return when(MatchRule.valueOf(path), TypeRef.constructType(clazz), handler);
    }

    public <T> Parser when(String path, TypeRef<T> type, BiConsumer<Path, T> handler) {
        return when(MatchRule.valueOf(path), TypeRef.constructType(type), handler);
    }

    public <T> Parser when(String path, Class<T> clazz, Consumer<T> handler) {
        return when(MatchRule.valueOf(path), TypeRef.constructType(clazz), (Path x, T y) -> handler.accept(y));
    }

    public <T> Parser when(String path, TypeRef<T> type, Consumer<T> handler) {
        return when(MatchRule.valueOf(path), TypeRef.constructType(type), (Path x, T y) -> handler.accept(y));
    }



    public static Parser create() {
        return new Parser();
    }

    public void parse(InputStream in) throws IOException {
        parse(in, JSON_FACTORY.createParser(in));
    }

    public void parse(Reader in) throws IOException {
        parse(in, JSON_FACTORY.createParser(in));
    }




    private void parse(Closeable underlyingStream, JsonParser jsonParser) throws IOException {
        InternalParser parser = new InternalParser(elementMatchers, underlyingStream, jsonParser);
        try {
            while (parser.parseOne());
        } finally {
            parser.parseEnd();
        }
    }

    private <T> Parser when(MatchRule matcher, JavaType type, BiConsumer<Path, T> handler) {
        elementMatchers.add(new ElementMatcher<>(matcher, type, handler));
        return this;
    }
}
