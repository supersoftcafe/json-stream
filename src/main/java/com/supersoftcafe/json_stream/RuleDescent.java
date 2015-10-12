package com.supersoftcafe.json_stream;

class RuleDescent extends Rule {
    private static final RuleDescent SINGLETON = new RuleDescent();

    RuleDescent() {
    }

    static RuleDescent valueOf(String expr) {
        if (!".".equals(expr)) {
            throw new IllegalArgumentException();
        }
        return SINGLETON;
    }

    @Override boolean test(Context context) {
        return context.skipRule() || context.retryRule();
    }

    public @Override String toString() {
        return ".";
    }

    public @Override int hashCode() {
        return RuleDescent.class.hashCode();
    }

    public @Override boolean equals(Object other) {
        return this == other || other instanceof RuleDescent;
    }
}
