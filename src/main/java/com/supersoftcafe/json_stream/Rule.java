package com.supersoftcafe.json_stream;

abstract class Rule {
    Rule() {
    }

    abstract boolean test(Context context);

    interface Context {
        Path.Node currentNode();
        boolean retryRule();
        boolean nextRule();
        boolean skipRule();
    }
}
