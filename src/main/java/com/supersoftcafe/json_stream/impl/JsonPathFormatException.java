package com.supersoftcafe.json_stream.impl;

import java.util.IllegalFormatException;


public class JsonPathFormatException extends RuntimeException {
    public JsonPathFormatException() {
    }

    public JsonPathFormatException(String message) {
        super(message);
    }

    public JsonPathFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public JsonPathFormatException(Throwable cause) {
        super(cause);
    }

    public JsonPathFormatException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
