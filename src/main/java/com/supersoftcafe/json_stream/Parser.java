package com.supersoftcafe.json_stream;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public interface Parser {
    <T> Parser when(String path, Class<  T> type, BiConsumer<? super Path, ? super T> handler);
    <T> Parser when(String path, TypeRef<T> type, BiConsumer<? super Path, ? super T> handler);
    <T> Parser when(String path, Class<  T> type, Consumer<? super T> handler);
    <T> Parser when(String path, TypeRef<T> type, Consumer<? super T> handler);

    Parser nestedSubTrees(boolean enable);

    void parse(InputStream in) throws IOException;
    void parse(Reader in) throws IOException;
}
