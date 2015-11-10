package com.supersoftcafe.json_stream.impl;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectReader;
import com.supersoftcafe.json_stream.Match;
import com.supersoftcafe.json_stream.Path;

import java.io.IOException;
import java.util.function.BiConsumer;


final class ElementMatcher<CONTEXT, T> implements Match<T> {
    private final ObjectReader objectReader;
    private final MatchRule    matchRule;
    private final JavaType     type;

    private final BiConsumer<? super CONTEXT, ? super Match<T>> handler;

    private PathImpl matchedPath;
    private PathImpl matchedPathCopy;
    private T        matchedContent;


    ElementMatcher(ObjectReader objectReader, MatchRule matchRule, JavaType type,
                   BiConsumer<? super CONTEXT, ? super Match<T>> handler) {
        this.objectReader = objectReader;
        this.matchRule = matchRule;
        this.type = type;
        this.handler = handler;
    }


    JsonNode readTree(JsonParser jsonParser) throws IOException {
        return objectReader.readTree(jsonParser);
    }

    boolean doesPathMatch(PathImpl path) throws IOException {
        return matchRule.testPath(path);
    }

    boolean doesTreeMatch(PathImpl path, JsonNode jsonNode) {
        return matchRule.testNode(path, jsonNode);
    }

    void callWithData(CONTEXT context, PathImpl path, JsonNode jsonNode) throws IOException {
        matchedPath = path;
        matchedPathCopy = null;
        matchedContent = objectReader.forType(type).readValue(jsonNode);
        handler.accept(context, this);
    }


    @Override public Path getPath() {
        return matchedPathCopy == null ? matchedPathCopy = matchedPath.readOnlyCopy() : matchedPathCopy;
    }

    @Override public T getContent() {
        return matchedContent;
    }

    @Override public void stop() {

    }
}
