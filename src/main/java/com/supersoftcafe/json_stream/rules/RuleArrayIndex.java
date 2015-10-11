package com.supersoftcafe.json_stream.rules;

import com.supersoftcafe.json_stream.Path;

public class RuleArrayIndex extends Rule {
    private final long[] indexes;

    public RuleArrayIndex(long... indexes) {
        if (indexes.length == 0) {
            throw new IllegalArgumentException("Must be at least one array index");
        }
        this.indexes = indexes;
    }

    public static RuleArrayIndex valueOf(String expr) {
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

    public @Override boolean test(Context context) {
        Path.Node node = context.currentNode();

        if (node.isArray()) {
            long arrayIndex = node.getIndex();
            for (long matchIndex : indexes) {
                if (arrayIndex == matchIndex) {
                    return context.next();
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
}
