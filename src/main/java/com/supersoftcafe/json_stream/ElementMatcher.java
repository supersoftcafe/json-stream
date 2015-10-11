package com.supersoftcafe.json_stream;

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
    private final BiConsumer<Path, T> handler;


    ElementMatcher(MatchRule matchRule, JavaType type, BiConsumer<Path, T> handler) {
        this.matchRule = matchRule;
        this.type = type;
        this.handler = handler;
    }


    boolean doesPathMatch(Path path) throws IOException {
        return matchRule.testPath(path);
    }

    boolean doesTreeMatch(Path path, JsonNode jsonNode) {
        return matchRule.testNode(path, jsonNode);
    }

    void callWithData(Path path, JsonNode jsonNode) throws IOException {
        T element = OBJECT_READER.forType(type).readValue(jsonNode);
        handler.accept(path.readOnlyCopy(), element);
    }

    JsonNode readTree(JsonParser jsonParser) throws IOException {
        return OBJECT_READER.readTree(jsonParser);
    }
}
