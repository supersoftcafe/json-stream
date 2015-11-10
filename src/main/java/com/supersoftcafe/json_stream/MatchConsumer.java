package com.supersoftcafe.json_stream;

@FunctionalInterface
public interface MatchConsumer<CONTEXT, T> {
    void matching(CONTEXT context, Match<T> match);
}
