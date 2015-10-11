package com.supersoftcafe.json_stream.rules;

import com.supersoftcafe.json_stream.Path;

public class RuleArrayRange extends Rule {
    private final long minIndex, maxIndex, step;

    public RuleArrayRange(long minIndex, long maxIndex, long step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step must not be zero");
        }

        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.step = step;
    }

    public static RuleArrayRange valueOf(String expr) {
        int length = expr.length();
        if (length < 2 || expr.charAt(0) != '[' || expr.charAt(length-1) != ']') {
            throw new IllegalArgumentException();
        }

        expr = expr.substring(1, length-1);
        String[] parts = expr.split(":", -1);
        long minIndex = parts[0].isEmpty() ? 0 : Long.parseLong(parts[0]);
        long maxIndex = parts[1].isEmpty() ? Long.MAX_VALUE : Long.parseLong(parts[1]);
        long step     = parts.length<3 ? 1 : Long.parseLong(parts[2]);
        if (maxIndex < minIndex || minIndex < 0 || step < 1)
            throw new IllegalArgumentException("Invalid array slice");

        return new RuleArrayRange(minIndex, maxIndex, step);
    }

    public @Override boolean test(Context context) {
        Path.Node node = context.currentNode();
        if (node.isArray()) {
            long arrayIndex = node.getIndex();
            if (arrayIndex >= minIndex && arrayIndex <= maxIndex && (arrayIndex % step) == 0) {
                return context.next();
            }
        }
        return false;
    }

    public @Override String toString() {
        StringBuilder sb = new StringBuilder();
        if (minIndex != 0) sb.append(minIndex);
        sb.append(':');
        if (maxIndex != Long.MAX_VALUE) sb.append(maxIndex);
        if (step != 1) sb.append(':').append(step);
        return sb.toString();
    }
}
