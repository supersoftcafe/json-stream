package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Parser {
    private final static Logger logger = Logger.getLogger(Parser.class.getName());

    private final static TypeFactory           TYPE_FACTORY  = TypeFactory.defaultInstance();
    private final static JsonFactory           JSON_FACTORY  = new JsonFactory();
    private final static ObjectReader          OBJECT_READER = new ObjectMapper().reader();
    private final static Map<Object, JavaType> TYPE_CACHE    = new ConcurrentHashMap<>();

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
        return new InternalIterator<T>(() -> new ParserPair(in), constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(() -> new ParserPair(in), constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(Reader in, Class<T> type, String... paths) {
        return new InternalIterator<T>(() -> new ParserPair(in), constructType(type), paths);
    }

    public static <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return new InternalIterator<T>(() -> new ParserPair(in), constructType(type), paths);
    }



    public <T> Parser when(String path, Class<T> clazz, BiConsumer<Path, T> handler) {
        return when(MatchRule.create(path), constructType(clazz), handler);
    }

    public <T> Parser when(String path, TypeRef<T> type, BiConsumer<Path, T> handler) {
        return when(MatchRule.create(path), constructType(type), handler);
    }

    public <T> Parser when(String path, Class<T> clazz, Consumer<T> handler) {
        return when(MatchRule.create(path), constructType(clazz), (Path x, T y) -> handler.accept(y));
    }

    public <T> Parser when(String path, TypeRef<T> type, Consumer<T> handler) {
        return when(MatchRule.create(path), constructType(type), (Path x, T y) -> handler.accept(y));
    }



    public static Parser create() {
        return new Parser();
    }

    public void parse(InputStream in) throws IOException {
        parse(new ParserPair(in));
    }

    public void parse(Reader in) throws IOException {
        parse(new ParserPair(in));
    }




    private void parse(ParserPair parserPair) throws IOException {
        InternalParser parser = new InternalParser(parserPair);
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

    private static JavaType constructType(Class<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructSimpleType(type, type, new JavaType[0]));
    }

    private static JavaType constructType(TypeRef<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructType(((ParameterizedType)type.getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
    }




    private static final class InternalIterator<T> implements Iterator<T>, BiConsumer<Path, T> {
        T element;
        boolean nextFound;
        InternalParser internalParser;

        InternalIterator(ParserPairSupplier parserPairSupplier, JavaType type, String[] jsonPaths) {
            Parser parser = new Parser();
            for (String jsonPath : jsonPaths)
                parser.when(MatchRule.create(jsonPath), type, this);

            try {
                internalParser = parser.new InternalParser(parserPairSupplier.get());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public @Override void accept(Path nodes, T t) {
            element = t;
        }

        public @Override boolean hasNext() {
            try {
                boolean result = nextFound || (nextFound = internalParser.parseOne());
                if (result == false) internalParser.parseEnd();
                return result;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public @Override T next() {
            if (hasNext()) {
                nextFound = false;
                return element;
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    @FunctionalInterface
    private interface ParserPairSupplier {
        ParserPair get() throws IOException;
    }

    private static class ParserPair {
        private final Closeable underlyingStream;
        private final JsonParser jsonParser;

        public ParserPair(InputStream in) throws IOException {
            this.underlyingStream = Objects.requireNonNull(in);
            this.jsonParser = JSON_FACTORY.createParser(in);
        }

        public ParserPair(Reader in) throws IOException {
            this.underlyingStream = Objects.requireNonNull(in);
            this.jsonParser = JSON_FACTORY.createParser(in);
        }
    }

    private static class ElementMatcher<T> {
        private final MatchRule           matchRule;
        private final JavaType            type;
        private final BiConsumer<Path, T> handler;

        public ElementMatcher(MatchRule matchRule, JavaType type, BiConsumer<Path, T> handler) {
            this.matchRule = matchRule;
            this.type      = type;
            this.handler   = handler;
        }

        private boolean doesPathMatch(Path path) throws IOException {
            return matchRule.testPath(path);
        }

        private boolean doesTreeMatch(Path path, JsonNode jsonNode) {
            return matchRule.testNode(path, jsonNode);
        }

        private void callWithData(Path path, JsonNode jsonNode) throws IOException {
            T element = OBJECT_READER.forType(type).readValue(jsonNode);
            handler.accept(path.copy(), element);
        }
    }

    private class InternalParser {
        private final Path path;
        private final Closeable underlyingStream;
        private final ArrayList<JsonParser> parserStack;

        private InternalParser(ParserPair parserPair) {
            this.path = new Path();
            this.underlyingStream = Objects.requireNonNull(parserPair.underlyingStream);
            this.parserStack = new ArrayList<>();

            pushParser(parserPair.jsonParser);
        }

        private boolean parseOne() throws IOException {
            return !parserStack.isEmpty() && nextJsonElement();
        }

        private void pushParser(JsonParser jsonParser) {
            parserStack.add(jsonParser);
        }

        private void popParser() {
            parserStack.remove(parserStack.size()-1);
        }

        private JsonParser jsonParser() {
            return parserStack.get(parserStack.size()-1);
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
            return (Path.ArrayIndex)node;
        }

        private Path.AttributeName popObject() {
            Path.Node node = path.pop();
            if (!node.isObject()) {
                throw new IllegalStateException();
            }
            return (Path.AttributeName)node;
        }

        private boolean tryConsumeElement() throws IOException {
            boolean consumed = false;

            for (ElementMatcher<?> matcher : elementMatchers) {
                if (matcher.doesPathMatch(path)) {
                    // logger.info(() -> String.format("Realising JsonNode for path %s", path));

                    JsonNode jsonNode = OBJECT_READER.readTree(jsonParser());
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

        private void parseEnd() throws IOException {
            underlyingStream.close();
        }
    }
}
