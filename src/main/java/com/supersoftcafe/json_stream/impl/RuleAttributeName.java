package com.supersoftcafe.json_stream.impl;

import java.util.Arrays;
import java.util.Objects;

final class RuleAttributeName extends Rule {
    private final String[] names;

    RuleAttributeName(String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("Must be at least one name");
        }
        for (String name : names) Objects.requireNonNull(name);
        Arrays.sort(this.names = names.clone());
    }

    int size() {
        return names.length;
    }

    String nameAt(int index) {
        return names[index];
    }

    static RuleAttributeName valueOf(String expr) {
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

    @Override boolean test(Context context) {
        PathImpl.NodeImpl node = context.currentNode();
        if (node.isAttribute()) {
            String nodeName = node.getAttributeName();
            for (String name : names) {
                if (name.equals(nodeName)) {
                    return context.nextRule();
                }
            }
        }
        return false;
    }

    public @Override String toString() {
        if (names.length == 1) {
            return '.' + names[0];
        } else {
            StringBuilder sb = new StringBuilder();
            char separator = '[';
            for (String name : names) {
                sb.append(separator).append('\'').append(name).append('\'');
                separator = ',';
            }
            return sb.append(']').toString();
        }
    }

    public @Override boolean equals(Object o) {
        return this == o || (o instanceof RuleAttributeName && Arrays.equals(names, ((RuleAttributeName) o).names));
    }

    public @Override int hashCode() {
        return Arrays.hashCode(names);
    }
}
