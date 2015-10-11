package com.supersoftcafe.json_stream.rules;

import com.supersoftcafe.json_stream.Path;

public class RuleAttributeName extends Rule {
    private final String[] names;

    public RuleAttributeName(String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("Must be at least one name");
        }
        this.names = names.clone();
    }

    public static RuleAttributeName valueOf(String expr) {
        String[] names;
        int length = expr.length();

        if (expr.startsWith("['")) {
            expr = expr.substring(1, length - 1);
            names = expr.split(",");
            for (int index = names.length; --index >= 0; ) {
                String name = names[index];
                names[index] = name.substring(1, name.length() - 1);
            }
        } else {
            expr = expr.substring(1);
            names = expr.split("\\|");
        }

        return new RuleAttributeName(names);
    }

    public @Override boolean test(Context context) {
        Path.Node node = context.currentNode();
        if (node.isObject()) {
            String nodeName = node.getName();
            for (String name : names) {
                if (name.equals(nodeName)) {
                    return context.next();
                }
            }
        }
        return false;
    }

    public @Override String toString() {
        StringBuilder sb = new StringBuilder();
        char separator = '.';
        for (String name : names) {
            sb.append(separator).append(name);
            separator = '|';
        }
        return sb.substring(0, sb.length()-1);
    }
}
