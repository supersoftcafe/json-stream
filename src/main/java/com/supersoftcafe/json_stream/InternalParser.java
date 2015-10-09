package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


final class InternalParser {
    private final List<ElementMatcher<?>> elementMatchers;
    private final Path path;
    private final Closeable underlyingStream;
    private final ArrayList<JsonParser> parserStack;


    InternalParser(List<ElementMatcher<?>> elementMatchers, Closeable underlyingStream, JsonParser jsonParser) {
        this.elementMatchers = elementMatchers;
        this.path = new Path();
        this.underlyingStream = Objects.requireNonNull(underlyingStream);
        this.parserStack = new ArrayList<>();

        pushParser(jsonParser);
    }


    boolean parseOne() throws IOException {
        return !parserStack.isEmpty() && nextJsonElement();
    }

    void parseEnd() throws IOException {
        underlyingStream.close();
    }


    private void pushParser(JsonParser jsonParser) {
        parserStack.add(jsonParser);
    }

    private void popParser() {
        parserStack.remove(parserStack.size() - 1);
    }

    private JsonParser jsonParser() {
        return parserStack.get(parserStack.size() - 1);
    }

    private boolean nextJsonElement() throws IOException {
        boolean consumed = false;
        do {
            JsonToken token = jsonParser().nextToken();
            if (token == null) {
                popParser();
                if (!parserStack.isEmpty()) continue;
                return false;
            }

            // Offer to consumers
            switch (token) {
                case START_ARRAY:
                case START_OBJECT:
                case VALUE_STRING:
                case VALUE_NUMBER_FLOAT:
                case VALUE_NUMBER_INT:
                case VALUE_FALSE:
                case VALUE_TRUE:
                case VALUE_NULL:
                    updateArrayIndex();
                    consumed = tryConsumeElement();
            }

            // Try to proceed
            switch (token) {
                case START_ARRAY:
                    pushArray();
                    break;
                case START_OBJECT:
                    pushObject();
                    break;
                case END_ARRAY:
                    popArray();
                    break;
                case END_OBJECT:
                    popObject();
                    break;
                case FIELD_NAME:
                    updateAttributeName(jsonParser().getCurrentName());
                    break;
            }
        } while (!consumed);
        return true;
    }

    private void updateArrayIndex() {
        Path.Node node = path.peek();
        if (node != null && node.isArray())
            path.pushArrayIndex(path.pop().getIndex() + 1);
    }

    private void updateAttributeName(String name) {
        Path.Node node = popObject();
        if (!node.isObject()) throw new IllegalStateException();
        path.pushAttributeName(name);
    }

    private void pushArray() {
        path.pushArrayIndex(-1);
    }

    private void pushObject() {
        path.pushAttributeName("-null-");
    }

    private Path.ArrayIndex popArray() {
        Path.Node node = path.pop();
        if (!node.isArray()) throw new IllegalStateException();
        return (Path.ArrayIndex) node;
    }

    private Path.AttributeName popObject() {
        Path.Node node = path.pop();
        if (!node.isObject()) {
            throw new IllegalStateException();
        }
        return (Path.AttributeName) node;
    }

    private boolean tryConsumeElement() throws IOException {
        boolean consumed = false;

        for (ElementMatcher<?> matcher : elementMatchers) {
            if (matcher.doesPathMatch(path)) {
                JsonNode jsonNode = matcher.readTree(jsonParser());
                JsonParser nestedParser = jsonNode.traverse();
                nestedParser.nextToken();
                pushParser(nestedParser);

                if (matcher.doesTreeMatch(path, jsonNode)) {
                    matcher.callWithData(path, jsonNode);
                    consumed = true;
                }
            }
        }

        return consumed;
    }
}
