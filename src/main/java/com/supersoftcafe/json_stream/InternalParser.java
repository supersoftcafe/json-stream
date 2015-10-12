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


    InternalParser(List<ElementMatcher<?>> elementMatchers, Closeable underlyingStream, JsonParser jsonParser) {
        this.elementMatchers = elementMatchers;
        this.parserStack = new ArrayList<>();
        this.underlyingStream = Objects.requireNonNull(underlyingStream);
        this.path = new Path();

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
