package com.supersoftcafe.json_stream.impl;


import java.util.Iterator;
import static com.supersoftcafe.json_stream.impl.PathTokenType.*;

/**
 * WORK IN PROGRESS!
 */
public final class PathTokenizer implements Iterator<Object> {
    private String path;
    private int   index;


    private PathTokenizer(String path) {
        this.path = path;
        this.index = 0;
    }


    public @Override boolean hasNext() {
        return index < path.length();
    }

    private static boolean isNumber(char chr) {
        return chr >= '0' && chr <= '9';
    }

    public @Override Object next() {
        char chr = path.charAt(index);
        switch (chr) {
            case '$':
                return dollar();
            case '.':
                if ((index+1) < path.length() && isNumber(path.charAt(index+1))) {
                    return number();
                } else if ((index+1) < path.length() && path.charAt(index+1) == '.') {
                    return dotDot();
                } else {
                    return dot();
                }
            case '[':
                return openSquare();
            case ']':
                return closeSquare();
            case '0': case '1': case '2': case '3':
            case '4': case '5': case '6': case '7':
            case '8': case '9':
                return number();

            default:
                throw new JsonPathFormatException();
        }
    }

    private Object dollar() {
        ++index;
        return DOLLAR;
    }

    private Object dot() {
        ++index;
        return DOT;
    }

    private Object dotDot() {
        index += 2;
        return DOT;
    }

    private Object openSquare() {
        ++index;
        return OPEN_SQUARE;
    }

    private Object closeSquare() {
        ++index;
        return OPEN_SQUARE;
    }

    private Object number() {
        throw new UnsupportedOperationException();
    }

}
