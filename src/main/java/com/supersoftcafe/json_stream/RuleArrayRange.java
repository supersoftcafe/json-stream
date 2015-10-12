package com.supersoftcafe.json_stream;

final class RuleArrayRange extends Rule {
    private static final RuleArrayRange ALL_ENTRIES_SINGLETON = new RuleArrayRange(0, Long.MAX_VALUE, 1);

    private final long minIndex, maxIndex, step;

    RuleArrayRange(long minIndex, long maxIndex, long step) {
        if (step == 0) {
            throw new IllegalArgumentException("Step must not be zero");
        }

        this.minIndex = minIndex;
        this.maxIndex = maxIndex;
        this.step = step;
    }

    static RuleArrayRange valueOf(String expr) {
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

    @Override boolean test(Context context) {
        Path.Node node = context.currentNode();
        if (node.isArray()) {
            long arrayIndex = node.getIndex();
            if (arrayIndex >= minIndex && arrayIndex < maxIndex && ((arrayIndex-minIndex) % step) == 0) {
                return context.nextRule();
            }
        }
        return false;
    }

    public @Override String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (minIndex != 0) sb.append(minIndex);
        sb.append(':');
        if (maxIndex != Long.MAX_VALUE) sb.append(maxIndex);
        if (step != 1) sb.append(':').append(step);
        return sb.append("]").toString();
    }

    public @Override boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || !(other instanceof RuleArrayRange)) return false;

        RuleArrayRange that = (RuleArrayRange) other;

        if (minIndex != that.minIndex) return false;
        if (maxIndex != that.maxIndex) return false;
        return step == that.step;

    }

    public @Override int hashCode() {
        int result = (int) (minIndex ^ (minIndex >>> 32));
        result = 31 * result + (int) (maxIndex ^ (maxIndex >>> 32));
        result = 31 * result + (int) (step ^ (step >>> 32));
        return result;
    }
}
