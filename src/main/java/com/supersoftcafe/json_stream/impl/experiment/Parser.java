package com.supersoftcafe.json_stream.impl.experiment;


import com.supersoftcafe.json_stream.impl.JsonPathFormatException;
import com.supersoftcafe.json_stream.impl.PathImpl;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class Parser {
    private int         currentIndex;
    private Lexer.Token[]     tokens;
    private Lexer.Token currentToken;



    private Parser(Lexer.Token[] tokens) {
        this.currentIndex = 0;
        this.tokens = Arrays.stream(tokens)
                .filter(x -> x.getType() != Lexer.Type.WHITESPACE)
                .collect(Collectors.toList())
                .toArray(new Lexer.Token[0]);
    }


    private boolean term() {
        return currentIndex == tokens.length;
    }

    private boolean expect(Lexer.Type type) {
        Lexer.Token next;
        if (currentIndex < tokens.length && (next = tokens[currentIndex]).getType() == type) {
            currentToken = next;
            currentIndex ++;
            return true;
        }
        return false;
    }

    private final int start() {
        return currentIndex;
    }

    private final <T> T fail(int index) {
        currentIndex = index;
        return null;
    }



    private void parseFail() {
        if (currentToken != null) {
            throw new JsonPathFormatException("Failed parse near offset " + currentToken.getPosition());
        } else {
            throw new JsonPathFormatException("Failed parse near offset 0");
        }
    }


    private boolean parsePathElement(PathImpl path) {
        int ci = currentIndex;
/*
        parseDotDotAttribute(path);
        parseDotDotArray(path);
        parseDotDotStar(path);

        parseDotAttribute(path);
        parseArray(path);
        parseDotStar(path);
*/

        boolean hasRecursion;
        if (expect(Lexer.Type.DOTDOT)) {
            hasRecursion = true;
        } else if (expect(Lexer.Type.DOT)) {
            hasRecursion = false;
        } else {
            currentIndex = ci;
            return false;
        }

        return false;
    }

    private PathImpl parsePath() {
        PathImpl path = new PathImpl();
        if (!expect(Lexer.Type.DOLLAR)) parseFail();
        while (!term()) parsePathElement(path);
        return path;
    }


    public static PathImpl parse(Lexer.Token[] tokens) {
        return new Parser(tokens).parsePath();
    }
}
