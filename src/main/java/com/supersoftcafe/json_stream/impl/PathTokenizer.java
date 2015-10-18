package com.supersoftcafe.json_stream.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.supersoftcafe.json_stream.impl.PathTokenType.*;

/**
 * WORK IN PROGRESS!
 */
public final class PathTokenizer {
    private final Pattern pattern;
    private final PathTokenType tokenType;



    private PathTokenizer(PathTokenType tokenType, String pattern) {
        this.tokenType = tokenType;
        this.pattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
    }

    private static PathTokenizer x(PathTokenType tokenType, String pattern) {
        return new PathTokenizer(tokenType, pattern);
    }



    private static final PathTokenizer[] tokens = new PathTokenizer[] {
            x(DOLLAR      , "\\$"),
            x(STAR        , "\\*"),
            x(DOT_DOT     , "\\.\\."),
            x(DOT         , "\\."),
            x(OPEN_SQUARE , "["),
            x(CLOSE_SQUARE, "]"),
            x(NUMBER      , "[-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?"),
            x(LITERAL     , "\"([^\"]|(\\\"))*\"")
    };



    public PathToken[] parse(String path) {
        List<PathToken> result = new ArrayList<>();

        while (path.length() > 0) {
            for (PathTokenizer tokenizer : tokens) {
                Matcher matcher = tokenizer.pattern.matcher(path);
                if (matcher.lookingAt()) {
                    String t = matcher.group();
                    path = path.substring(t.length());
                    result.add(new PathToken(tokenizer.tokenType, t));
                    break;
                }
                throw new JsonPathFormatException();
            }
        }

        return result.toArray(new PathToken[result.size()]);
    }
}
