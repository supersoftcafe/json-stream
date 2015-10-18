package com.supersoftcafe.json_stream.impl;

import java.util.Arrays;

final class RuleArrayIndex extends Rule {
    private final long[] indexes;

    RuleArrayIndex(long... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException("Must be at least one array index");
        }
        Arrays.sort(this.indexes = indexes.clone());
    }

    int size() {
        return indexes.length;
    }

    long indexAt(int index) {
        return indexes[index];
    }

    static RuleArrayIndex valueOf(String expr) {
        int length = expr.length();
        if (length < 3 || expr.charAt(0) != '[' || expr.charAt(length-1) != ']') {
            throw new IllegalArgumentException();
        }

        expr = expr.substring(1, expr.length()-1);
        String[] parts = expr.split(",");
        long[] indexes = new long[parts.length];
        for (int index = parts.length; --index >= 0; )
            indexes[index] = Long.parseLong(parts[index]);

        return new RuleArrayIndex(indexes);
    }

    @Override boolean test(Context context) {
        PathImpl.NodeImpl node = context.currentNode();

        if (node.isArray()) {
            long arrayIndex = node.getIndex();
            for (long matchIndex : indexes) {
                if (arrayIndex == matchIndex) {
                    return context.nextRule();
                }
            }
        }
        return false;
    }

    public @Override String toString() {
        StringBuilder sb = new StringBuilder();
        char seperator = '[';
        for (long index : indexes) {
            sb.append(seperator).append(index);
            seperator = ',';
        }
        return sb.append(']').toString();
    }

    public @Override int hashCode() {
        return Arrays.hashCode(indexes);
    }

    public @Override boolean equals(Object o) {
        return this == o || o instanceof RuleArrayIndex && Arrays.equals(indexes, ((RuleArrayIndex)o).indexes);
    }
}
