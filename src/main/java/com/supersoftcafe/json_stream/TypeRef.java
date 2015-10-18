package com.supersoftcafe.json_stream;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class TypeRef<T> {
    public TypeRef() {
    }

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
}
