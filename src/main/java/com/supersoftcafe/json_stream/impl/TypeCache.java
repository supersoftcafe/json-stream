package com.supersoftcafe.json_stream.impl;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.supersoftcafe.json_stream.TypeRef;

import java.util.concurrent.ConcurrentHashMap;

public class TypeCache {
    private final static TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();
    private final static ConcurrentHashMap<Object, JavaType> TYPE_CACHE = new ConcurrentHashMap<>();



    static JavaType constructType(Class<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructSimpleType(type, type, new JavaType[0]));
    }

    static JavaType constructType(TypeRef<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructType(type.getType()));
    }
}
