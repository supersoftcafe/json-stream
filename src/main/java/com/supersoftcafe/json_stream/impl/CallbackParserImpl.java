package com.supersoftcafe.json_stream.impl;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.Match;
import com.supersoftcafe.json_stream.CallbackParser;
import com.supersoftcafe.json_stream.TypeRef;

import java.io.*;
import java.util.*;
import java.util.function.*;


public final class CallbackParserImpl<CONTEXT> implements CallbackParser<CONTEXT> {
    private final ObjectReader objectReader;
    private final JsonFactory  jsonFactory;

    private boolean stopMatching = false;
    private boolean allowSubTrees = false;
    private final List<ElementMatcher<CONTEXT, ?>> matchers;


    public CallbackParserImpl(ObjectReader objectReader, JsonFactory jsonFactory) {
        this.objectReader = objectReader;
        this.jsonFactory  = jsonFactory ;
        this.matchers = new ArrayList<>();
    }


    public @Override <T> CallbackParserImpl<CONTEXT> whenMatch(
            String path, TypeRef<T> type, BiConsumer<? super CONTEXT, ? super Match<T>> handler) {
        MatchRule matchRule = MatchRule.valueOf(path);
        JavaType javaType = TypeCache.constructType(type);
        matchers.add(new ElementMatcher<>(objectReader, matchRule, javaType, handler));
        return this;
    }


    public @Override
    CallbackParserImpl<CONTEXT> allowNesting(boolean allowNesting) {
        allowSubTrees = allowNesting;
        return this;
    }


    public @Override void parse(CONTEXT context, InputStream in) throws IOException {
        parse(context, in, jsonFactory.createParser(in));
    }

    public @Override void parse(CONTEXT context, Reader in) throws IOException {
        parse(context, in, jsonFactory.createParser(in));
    }


    private void parse(CONTEXT context, Closeable closeHandle, JsonParser jsonParser) throws IOException {
        stopMatching = false;
        try (InternalParser<CONTEXT> parser = new InternalParser<>(context,
                matchers, closeHandle, jsonParser, allowSubTrees)) {
            while (parser.parseOne() && !stopMatching);
        }
    }
}
