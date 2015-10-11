package com.supersoftcafe.json_stream.rules;

import com.supersoftcafe.json_stream.Path;

public abstract class Rule {
    public Rule() {
    }

    public abstract boolean test(Context context);

    public interface Context {
        Path.Node currentNode();
        boolean tryNextNode();
        boolean next();
    }
}
