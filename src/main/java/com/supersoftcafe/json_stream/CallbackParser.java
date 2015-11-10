package com.supersoftcafe.json_stream;


import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public interface CallbackParser<OBJ> {
    <T> CallbackParser<OBJ> whenMatch(String path, Class<  T> type, MatchConsumer<? super OBJ, ? super T> handler);
    <T> CallbackParser<OBJ> whenMatch(String path, TypeRef<T> type, MatchConsumer<? super OBJ, ? super T> handler);

    CallbackParser<OBJ> allowNesting(boolean allowNesting);

    void parse(OBJ context, InputStream in) throws IOException;
    void parse(OBJ context, Reader      in) throws IOException;
}
