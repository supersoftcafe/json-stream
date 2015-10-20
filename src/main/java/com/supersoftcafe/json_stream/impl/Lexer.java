package com.supersoftcafe.json_stream.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * WORK IN PROGRESS!
 */
public final class Lexer {
    private final static Pattern pattern = buildPattern();
    private final static Type[] tokenTypes = new Type[Type.values().length+1];

    private static Pattern buildPattern() {
        StringBuffer sb = new StringBuffer();
        for (Type type : Type.values())
            sb.append("|(?<").append(type.name()).append('>').append(type.getPattern()).append(')');
        return Pattern.compile(sb.substring(1), Pattern.CASE_INSENSITIVE);
    }



    public static Token[] lex(String path) {
        List<Token> result = new ArrayList<>();

        Matcher matcher = pattern.matcher(path);
        while (matcher.find()) {
            for (Type type : Type.values()) {
                String value = matcher.group(type.name());
                if (value != null) {
                    result.add(new Token(type, type.convert(value)));
                    break;
                }
            }
        }

        return result.toArray(new Token[result.size()]);
    }


    private static String unescape(String value) {
        value = value.substring(1, value.length()-1);
        if (value.indexOf('\\') >= 0) {
            StringBuffer sb = new StringBuffer();
            for (int index = 0; index < value.length(); ++index) {
                char chr = value.charAt(index);
                sb.append(chr == '\\' ? value.charAt(++index) : chr);
            }
            value = sb.toString();
        }
        return value;
    }

    public enum Type {
        DOTDOT("\\.\\."),
        DOT("\\."),
        DOLLAR("\\$"),
        STAR("\\*"),
        OPENSQUARE("\\["),
        CLOSESQUARE("\\]"),

        INTEGER("[-+]?[0-9]+") {Object convert(String v) {return Long.parseLong(v);}},
        FLOAT("[-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?") {Object convert(String v) {return Double.parseDouble(v);}},
        STRING("\"([^\"]|(\\\"))*\"") {Object convert(String v) {return unescape(v);}},
        NAME("[^\\[\\].()\"$@:{}\\h]+"),

        WHITESPACE("\\h+"),
        UNKNOWN(".+");

        private final String pattern;

        Type(String pattern) {
            this.pattern = pattern;
            Pattern.compile(pattern);
        }

        String getPattern() {
            return pattern;
        }

        Object convert(String value) {
            return value;
        }
    }

    public final static class Token {
        private final Type type;
        private final Object value;

        public Token(Type type, Object value) {
            this.type = type;
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Token token = (Token) o;

            if (type != token.type) return false;
            return value.equals(token.value);

        }

        @Override public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + value.hashCode();
            return result;
        }

        @Override public String toString() {
            return "Token{" +
                    "type=" + type +
                    ", value=" + value +
                    '}';
        }
    }
}
