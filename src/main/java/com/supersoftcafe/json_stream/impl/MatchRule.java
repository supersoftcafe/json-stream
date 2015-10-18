package com.supersoftcafe.json_stream.impl;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


final class MatchRule extends AbstractList<Rule> {
    private final static String RECURSIVE            = "\\.";
    private final static String ARRAY_ANY            = "\\[\\*\\]";
    private final static String ARRAY_LIST           = "\\[[0-9]+(,[0-9]+)*\\]";
    private final static String ARRAY_RANGE          = "\\[([0-9]+)?:([0-9]+)?(:[0-9]+)?\\]";
    private final static String ATTRIBUTE_ANY        = "\\.\\*";
    private final static String ATTRIBUTE_DOT_NAME   = "\\.(([a-z_][a-z0-9_]*(\\|[a-z_][a-z0-9_]*)*))";
    private final static String ATTRIBUTE_QUOTE_NAME = "\\['[a-z_][a-z0-9_]*'(,'[a-z_][a-z0-9_]*')*\\]";

    private final static Pattern pattern = Pattern.compile(
            "(" + ARRAY_ANY + ")|(" + ARRAY_LIST + ")|(" + ARRAY_RANGE + ")|(" + ATTRIBUTE_ANY + ")|(" + ATTRIBUTE_DOT_NAME + ")|(" + ATTRIBUTE_QUOTE_NAME + ")|(" + RECURSIVE + ")"
            , Pattern.CASE_INSENSITIVE);

    private static Map<String, MatchRule> MATCH_RULE_CACHE = new ConcurrentHashMap<>();


    private String toStringResult;
    private final Rule[] rules;



    MatchRule(Rule... rules) {
        for (Rule rule : this.rules = rules.clone()) Objects.requireNonNull(rule);
    }

    MatchRule(String jsonPath) {
        this(makeRulesFromPath(jsonPath));
    }

    static MatchRule valueOf(String jsonPath) {
        return MATCH_RULE_CACHE.computeIfAbsent(jsonPath, MatchRule::new);
    }

    public @Override String toString() {
        if (toStringResult == null) {
            StringBuilder sb = new StringBuilder("$");
            for (Rule rule : rules) sb.append(rule);
            toStringResult = sb.toString();
        }
        return toStringResult;
    }


    public @Override Rule get(int index) {
        return rules[index];
    }

    public @Override int size() {
        return rules.length;
    }


    boolean testPath(final PathImpl path) {
        return new Rule.Context() {
            int ruleIndex = -1;
            int pathIndex = -1;

            public @Override
            PathImpl.NodeImpl currentNode() {
                return path.get(pathIndex);
            }

            public @Override boolean skipRule() {
                return internalNext(ruleIndex++, pathIndex);
            }

            public @Override boolean retryRule() {
                return internalNext(ruleIndex, pathIndex++);
            }

            public @Override boolean nextRule() {
                return internalNext(ruleIndex++, pathIndex++);
            }

            private boolean internalNext(int savedRuleIndex, int savedPathIndex) {
                boolean result;
                if (pathIndex >= path.size() || ruleIndex >= rules.length) {
                    result = pathIndex == path.size() && ruleIndex == rules.length;
                } else {
                    result = rules[ruleIndex].test(this);
                }
                ruleIndex = savedRuleIndex;
                pathIndex = savedPathIndex;
                return result;
            }
        }.nextRule();
    }

    boolean testNode(PathImpl path, JsonNode node) {
        return true;
    }


    private static Rule[] makeRulesFromPath(String jsonPath) {
        if (jsonPath.length() == 0 || jsonPath.charAt(0) != '$') {
            throw new IllegalArgumentException("Expression not rooted");
        }

        int regionStart = 1;
        Matcher matcher = pattern.matcher(jsonPath);
        ArrayList<String> subExpressions = new ArrayList<>();

        while (regionStart < matcher.regionEnd()) {
            matcher.region(regionStart, matcher.regionEnd());
            if (!matcher.lookingAt()) {
                throw new IllegalArgumentException("Invalid sub-expression at " + regionStart);
            }

            String expr = matcher.group();
            subExpressions.add(expr);

            regionStart += expr.length();
        }

        return subExpressions.stream()
                .map(MatchRule::makeRule)
                .toArray(Rule[]::new);
    }

    private static Rule makeRule(String expr) {
        if (".".equals(expr)) {
            return RuleDescent.valueOf(expr);
        } else if (".*".equals(expr)) {
            return RuleAnyAttributeName.valueOf(expr);
        } else if (expr.startsWith("['") || expr.startsWith(".")) {
            return RuleAttributeName.valueOf(expr);
        } else if (expr.startsWith("[")) {
            if (expr.equals("[*]")) {
                return RuleAnyArrayIndex.valueOf(expr);
            } else if (expr.contains(":")) {
                return RuleArrayRange.valueOf(expr);
            } else {
                return RuleArrayIndex.valueOf(expr);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}
