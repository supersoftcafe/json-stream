package com.supersoftcafe.json_stream.rules;

public class RuleAnyAttributeName extends Rule {
    public RuleAnyAttributeName() {
    }

    public @Override boolean test(Context context) {
        if (context.currentNode().isObject()) {
            return context.next();
        }
        return false;
    }

    public @Override String toString() {
        return ".*";
    }
}
