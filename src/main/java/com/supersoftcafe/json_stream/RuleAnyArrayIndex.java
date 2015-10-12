package com.supersoftcafe.json_stream;

final class RuleAnyArrayIndex extends Rule {
    private static final RuleAnyArrayIndex SINGLETON = new RuleAnyArrayIndex();

    RuleAnyArrayIndex() {
    }

    static RuleAnyArrayIndex valueOf(String expr) {
        if (!expr.equals("[*]")) {
            throw new IllegalArgumentException();
        }
        return SINGLETON;
    }

    @Override boolean test(Context context) {
        if (context.currentNode().isArray()) {
            return context.nextRule();
        }
        return false;
    }

    public @Override String toString() {
        return "[*]";
    }

    public @Override int hashCode() {
        return RuleAnyArrayIndex.class.hashCode();
    }

    public @Override boolean equals(Object o) {
        return this == o || o instanceof RuleAnyArrayIndex;
    }
}
