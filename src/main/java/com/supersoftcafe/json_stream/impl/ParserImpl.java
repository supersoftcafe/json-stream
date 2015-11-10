package com.supersoftcafe.json_stream.impl;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.Parser;
import com.supersoftcafe.json_stream.Path;
import com.supersoftcafe.json_stream.TypeRef;

import java.io.*;
import java.util.*;
import java.util.function.*;


public final class ParserImpl implements Parser {
    private final ObjectReader objectReader;

    private boolean allowSubTrees = false;
    private final List<ElementMatcher<?>> elementMatchers;


    public ParserImpl(ObjectReader objectReader) {
        this.objectReader = objectReader;
        this.elementMatchers = new ArrayList<>();
    }






    public @Override <T> ParserImpl when(String path, Class<T> clazz, BiConsumer<? super Path, ? super T> handler) {
        return when(MatchRule.valueOf(path), TypeCache.constructType(clazz), handler);
    }

    public @Override <T> ParserImpl when(String path, TypeRef<T> type, BiConsumer<? super Path, ? super T> handler) {
        return when(MatchRule.valueOf(path), TypeCache.constructType(type), handler);
    }

    public @Override <T> ParserImpl when(String path, Class<T> clazz, Consumer<? super T> handler) {
        return when(MatchRule.valueOf(path), TypeCache.constructType(clazz), (Path x, T y) -> handler.accept(y));
    }

    public @Override <T> ParserImpl when(String path, TypeRef<T> type, Consumer<? super T> handler) {
        return when(MatchRule.valueOf(path), TypeCache.constructType(type), (Path x, T y) -> handler.accept(y));
    }


    public @Override ParserImpl nestedSubTrees(boolean enable) {
        allowSubTrees = enable;
        return this;
    }

    public @Override void parse(InputStream in) throws IOException {
        parse(in, objectReader.getFactory().createParser(in));
    }

    public @Override void parse(Reader in) throws IOException {
        parse(in, objectReader.getFactory().createParser(in));
    }




    private void parse(Closeable underlyingStream, JsonParser jsonParser) throws IOException {
        InternalParser parser = new InternalParser(elementMatchers, underlyingStream, jsonParser, allowSubTrees);
        try {
            while (parser.parseOne());
        } finally {
            parser.parseEnd();
        }
    }

    private <T> ParserImpl when(MatchRule matcher, JavaType type, BiConsumer<? super Path, ? super T> handler) {
        elementMatchers.add(new ElementMatcher<>(objectReader, matcher, type, handler));
        return this;
    }
}
