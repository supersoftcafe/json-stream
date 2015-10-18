package com.supersoftcafe.json_stream.impl;

/**
 * Created by mbrown on 18/10/2015.
 */
public class PathToken {
    private final PathTokenType tokenType;
    private final String token;

    public PathToken(PathTokenType tokenType, String token) {
        this.tokenType = tokenType;
        this.token = token;
    }

    public PathTokenType getTokenType() {
        return tokenType;
    }

    public String getToken() {
        return token;
    }
}
