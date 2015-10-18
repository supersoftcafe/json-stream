package com.supersoftcafe.json_stream.impl;

abstract class Rule {
    Rule() {
    }

    abstract boolean test(Context context);

    interface Context {
        PathImpl.NodeImpl currentNode();
        boolean retryRule();
        boolean nextRule();
        boolean skipRule();
    }
}
