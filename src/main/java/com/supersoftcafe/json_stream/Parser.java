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
import java.util.function.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Parser {
    private final static Logger logger = Logger.getLogger(Parser.class.getName());

    private final static TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();
    private final static JsonFactory JSON_FACTORY = new JsonFactory();
    private final static ObjectReader OBJECT_READER = new ObjectMapper().reader();
    private final List<ElementMatcher<?>> elementMatchers;


    private Parser() {
        this.elementMatchers = new ArrayList<>();
    }

    public static Parser create() {
        return new Parser();
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
        return StreamSupport.stream(iterable(() -> in, type, paths).spliterator(), false);
    }

    public static <T> Stream<T> stream(InputStream in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(iterable(() -> in, type, paths).spliterator(), false);
    }

    public static <T> Stream<T> stream(Reader in, Class<T> type, String... paths) {
        return StreamSupport.stream(iterable(() -> in, type, paths).spliterator(), false);
    }

    public static <T> Stream<T> stream(Reader in, TypeRef<T> type, String... paths) {
        return StreamSupport.stream(iterable(() -> in, type, paths).spliterator(), false);
    }



    public static <T> Iterator<T> iterator(InputStream in, Class<T> type, String... paths) {
        return iterable(() -> in, type, paths).iterator();
    }

    public static <T> Iterator<T> iterator(InputStream in, TypeRef<T> type, String... paths) {
        return iterable(() -> in, type, paths).iterator();
    }

    public static <T> Iterator<T> iterator(Reader in, Class<T> type, String... paths) {
        return iterable(() -> in, type, paths).iterator();
    }

    public static <T> Iterator<T> iterator(Reader in, TypeRef<T> type, String... paths) {
        return iterable(() -> in, type, paths).iterator();
    }



    public static <T> Iterable<T> iterable(SupplyInputStream<? extends InputStream> in, Class<T> clazz, String... paths) {
        return iterable(() -> new ParserPair(in.get()), constructType(clazz), paths);
    }

    public static <T> Iterable<T> iterable(SupplyInputStream<? extends InputStream> in, TypeRef<T> clazz, String... paths) {
        return iterable(() -> new ParserPair(in.get()), constructType(clazz), paths);
    }

    public static <T> Iterable<T> iterable(SupplyReader<? extends Reader> in, Class<T> clazz, String... paths) {
        return iterable(() -> new ParserPair(in.get()), constructType(clazz), paths);
    }

    public static <T> Iterable<T> iterable(SupplyReader<? extends Reader> in, TypeRef<T> clazz, String... paths) {
        return iterable(() -> new ParserPair(in.get()), constructType(clazz), paths);
    }




    public <T> Parser when(String path, Class<T> clazz, BiConsumer<Path, T> handler) {
        return when(new PathMatcher(path), constructType(clazz), handler);
    }

    public <T> Parser when(String path, TypeRef<T> type, BiConsumer<Path, T> handler) {
        return when(new PathMatcher(path), constructType(type), handler);
    }

    public <T> Parser when(String path, Class<T> clazz, Consumer<T> handler) {
        return when(new PathMatcher(path), constructType(clazz), (Path x, T y) -> handler.accept(y));
    }

    public <T> Parser when(String path, TypeRef<T> type, Consumer<T> handler) {
        return when(new PathMatcher(path), constructType(type), (Path x, T y) -> handler.accept(y));
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

    private static <T> Iterable<T> iterable(ParserPairSupplier in, JavaType type, String... paths) {
        PathMatcher[] matchers = new PathMatcher[paths.length];
        for (int index = paths.length; --index >= 0; )
            matchers[index] = new PathMatcher(paths[index]);

        return () -> {
            try {
                return new InternalIterator<T>(in.get(), type, matchers);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private <T> Parser when(Predicate<Path> matcher, JavaType type, BiConsumer<Path, T> handler) {
        elementMatchers.add(new ElementMatcher<>(matcher, (x,y) -> true, type, handler));
        return this;
    }

    private static JavaType constructType(Class<?> type) {
        return TYPE_FACTORY.constructSimpleType(type, type, new JavaType[0]);
    }

    private static JavaType constructType(TypeRef<?> type) {
        return TYPE_FACTORY.constructType(((ParameterizedType)type.getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
    }




    private static final class InternalIterator<T> implements Iterator<T>, BiConsumer<Path, T> {
        T element;
        boolean nextFound;
        InternalParser internalParser;

        InternalIterator(ParserPair parserPair, JavaType type, PathMatcher[] matchers) {
            Parser parser = new Parser();
            for (PathMatcher matcher : matchers) parser.when(matcher, type, this);
            internalParser = parser.new InternalParser(parserPair);
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
        private final Predicate<Path>             pathMatcher;
        private final BiPredicate<Path, JsonNode> treeMatcher;
        private final JavaType                    type;
        private final BiConsumer<Path, T>         handler;

        public ElementMatcher(Predicate<Path> pathMatcher, BiPredicate<Path, JsonNode> treeMatcher, JavaType type, BiConsumer<Path, T> handler) {
            this.pathMatcher = pathMatcher;
            this.treeMatcher = treeMatcher;
            this.type        = type;
            this.handler     = handler;
        }

        private boolean doesPathMatch(Path path) throws IOException {
            return pathMatcher.test(path);
        }

        private boolean doesTreeMatch(Path path, JsonNode jsonNode) {
            return treeMatcher.test(path, jsonNode);
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
            for (ElementMatcher<?> matcher : elementMatchers) {
                if (matcher.doesPathMatch(path)) {
                    // logger.info(() -> String.format("Realising JsonNode for path %s", path));

                    JsonNode jsonNode = OBJECT_READER.readTree(jsonParser());
                    JsonParser nestedParser = jsonNode.traverse();
                    nestedParser.nextToken();
                    pushParser(nestedParser);

                    if (matcher.doesTreeMatch(path, jsonNode)) {
                        matcher.callWithData(path, jsonNode);
                    }

                    return true;
                }
            }
            return false;
        }

        private void parseEnd() throws IOException {
            underlyingStream.close();
        }
    }
}
