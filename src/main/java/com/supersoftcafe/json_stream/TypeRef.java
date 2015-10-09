package com.supersoftcafe.json_stream;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;


public abstract class TypeRef<T> {
    private final static TypeFactory TYPE_FACTORY = TypeFactory.defaultInstance();
    private final static ConcurrentHashMap<Object, JavaType> TYPE_CACHE = new ConcurrentHashMap<>();


    public final Type getType() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public final String toString() {
        return "TypeRef<" + getType() + ">";
    }

    public final int hashCode() {
        return getType().hashCode();
    }

    public final boolean equals(Object other) {
        return other instanceof TypeRef && getType().equals(((TypeRef)other).getType());
    }


    static JavaType constructType(Class<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructSimpleType(type, type, new JavaType[0]));
    }

    static JavaType constructType(TypeRef<?> type) {
        return TYPE_CACHE.computeIfAbsent(type, x -> TYPE_FACTORY.constructType(type.getType()));
    }
}
