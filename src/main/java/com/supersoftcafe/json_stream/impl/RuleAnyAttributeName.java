package com.supersoftcafe.json_stream.impl;

final class RuleAnyAttributeName extends Rule {
    private static final RuleAnyAttributeName SINGLETON = new RuleAnyAttributeName();

    RuleAnyAttributeName() {
    }

    static RuleAnyAttributeName valueOf(String expr) {
        if (!".*".equals(expr)) {
            throw new IllegalArgumentException();
        }
        return SINGLETON;
    }

    @Override boolean test(Context context) {
        if (context.currentNode().isAttribute()) {
            return context.nextRule();
        }
        return false;
    }

    public @Override String toString() {
        return ".*";
    }

    public @Override int hashCode() {
        return RuleAnyAttributeName.class.hashCode();
    }

    public @Override boolean equals(Object o) {
        return this == o || o instanceof RuleAnyAttributeName;
    }
}
