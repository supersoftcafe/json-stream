package com.supersoftcafe.json_stream.impl;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.function.BiConsumer;


final class ElementMatcher<T> {
    final static ObjectReader OBJECT_READER = new ObjectMapper().reader();

    private final MatchRule matchRule;
    private final JavaType type;
    private final BiConsumer<? super PathImpl, ? super T> handler;


    ElementMatcher(MatchRule matchRule, JavaType type, BiConsumer<? super PathImpl, ? super T> handler) {
        this.matchRule = matchRule;
        this.type = type;
        this.handler = handler;
    }


    boolean doesPathMatch(PathImpl path) throws IOException {
        return matchRule.testPath(path);
    }

    boolean doesTreeMatch(PathImpl path, JsonNode jsonNode) {
        return matchRule.testNode(path, jsonNode);
    }

    void callWithData(PathImpl path, JsonNode jsonNode) throws IOException {
        T element = OBJECT_READER.forType(type).readValue(jsonNode);
        handler.accept(path.readOnlyCopy(), element);
    }

    JsonNode readTree(JsonParser jsonParser) throws IOException {
        return OBJECT_READER.readTree(jsonParser);
    }
}
