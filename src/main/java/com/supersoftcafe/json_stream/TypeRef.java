package com.supersoftcafe.json_stream;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


public abstract class TypeRef<T> {
    private final Type    type;
    private final boolean simple;


    private TypeRef(Class<T> type) {
        this.type = type;
        this.simple = true;
    }

    public TypeRef() {
        this.type = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.simple = type instanceof Class;
    }


    public static <T> TypeRef<T> of(final Class<T> type) {
        return new TypeRef<T>(type) { };
    }


    public final boolean isSimple() {
        return simple;
    }

    public final Type getType() {
        return type;
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
