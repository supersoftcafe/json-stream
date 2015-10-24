package com.supersoftcafe.json_stream.impl.experiment;


import java.util.function.Function;
import java.util.function.Supplier;

public class Model {



    static final LexerModel[] lexerTokens = new LexerModel[] {
            new LexerModel(DotDot      ::new, "\\.\\."),
            new LexerModel(Dot         ::new, "\\."),
            new LexerModel(Dollar      ::new, "\\$"),
            new LexerModel(Star        ::new, "\\*"),
            new LexerModel(OpenSquare  ::new, "\\["),
            new LexerModel(CloseSquare ::new, "\\]"),
            new LexerModel(Colon       ::new, ":"),

            new LexerModel(IntegerValue::new, "[-+]?[0-9]+"),
            new LexerModel(FloatValue  ::new, "[-+]?[0-9]*\\.?[0-9]+(e[-+]?[0-9]+)?"),
            new LexerModel(StringValue ::new, "\"([^\"]|(\\\"))*\""),
            new LexerModel(Attribute   ::new, "[^\\[\\].()\"$@:{}\\h]+"),

            new LexerModel(WhiteSpace  ::new, "\\h+"),
            new LexerModel(Unknown     ::new, ".+")
    };



    static class LexerModel {
        final Function<String, Node> create;
        final String regex;

        public LexerModel(Supplier<Node> create, String regex) {
            this.create = x -> create.get();
            this.regex = regex;
        }

        public LexerModel(Function<String, Node> create, String regex) {
            this.create = create;
            this.regex = regex;
        }
    }

    static abstract class ParserModel {

    }


    static class Node {

    }

    static class Dollar extends Node { }
    static class DotDot extends Node { }
    static class Dot extends Node { }
    static class Star extends Node { }
    static class Colon extends Node { }
    static class OpenSquare extends Node { }
    static class CloseSquare extends Node { }
    static class WhiteSpace extends Node { }
    static class Unknown extends Node { }

    static class IntegerValue extends Node {
        Long value;
        IntegerValue(String v) {
            value = Long.parseLong(v);
        }
    }

    static class FloatValue extends Node {
        Double value;
        FloatValue(String v) {
            value = Double.parseDouble(v);
        }
    }

    static class StringValue extends Node {
        String value;
        StringValue(String v) {
            value = v;
        }
    }

    static class Attribute extends Node {
        String value;
        Attribute(String v) {
            value = v;
        }
    }

}
