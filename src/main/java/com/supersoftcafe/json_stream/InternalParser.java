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
    private final ArrayList<JsonParser> parserStack;
    private final Closeable underlyingStream;
    private final Path path;
    private final boolean allowSubTrees;


    InternalParser(List<ElementMatcher<?>> elementMatchers, Closeable underlyingStream, JsonParser jsonParser, boolean allowSubTrees) {
        this.elementMatchers = elementMatchers;
        this.parserStack = new ArrayList<>();
        this.underlyingStream = Objects.requireNonNull(underlyingStream);
        this.path = new Path();
        this.allowSubTrees = allowSubTrees;

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
                    path.advanceArrayIndex();
                    consumed = tryConsumeElement();
                    if (consumed && !allowSubTrees) {
                        return true;
                    }
            }

            // Try to proceed
            switch (token) {
                case START_ARRAY:
                    path.pushDummyArray();
                    break;
                case START_OBJECT:
                    path.pushDummyObject();
                    break;
                case END_ARRAY:
                    path.popArray();
                    break;
                case END_OBJECT:
                    path.popObject();
                    break;
                case FIELD_NAME:
                    path.updateAttributeName(jsonParser().getCurrentName());
                    break;
            }
        } while (!consumed);

        return true;
    }


    private boolean tryConsumeElement() throws IOException {
        JsonNode jsonNode = null;

        for (ElementMatcher<?> matcher : elementMatchers) {
            if (matcher.doesPathMatch(path)) {
                if (jsonNode == null) {
                    jsonNode = matcher.readTree(jsonParser());
                }

                matcher.callWithData(path, jsonNode);
            }
        }

        if (jsonNode != null && allowSubTrees) {
            JsonParser nestedParser = jsonNode.traverse();
            nestedParser.nextToken();
            pushParser(nestedParser);
        }

        return jsonNode != null;
    }
}
