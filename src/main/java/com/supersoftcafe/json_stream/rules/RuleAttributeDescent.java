package com.supersoftcafe.json_stream.rules;

public class RuleAttributeDescent extends Rule {
    public RuleAttributeDescent() {
    }

    public @Override boolean test(Context context) {
        if (context.currentNode().isObject()) {
            if (context.next() || context.tryNextNode()) {
                return true;
            }
        }
        return false;
    }

    public @Override String toString() {
        return ".";
    }
}
