package com.supersoftcafe.json_stream;

import com.fasterxml.jackson.databind.JsonNode;
import com.supersoftcafe.json_stream.rules.*;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class MatchRule {
    private final static String RECURSIVE            = "\\.";
    private final static String ARRAY_LIST           = "\\[[0-9]+(,[0-9]+)*\\]";
    private final static String ARRAY_RANGE          = "\\[([0-9]+)?:([0-9]+)?(:[0-9]+)?\\]";
    private final static String ATTRIBUTE_ANY        = "\\.\\*";
    private final static String ATTRIBUTE_DOT_NAME   = "\\.(([a-z_][a-z0-9_]*(\\|[a-z_][a-z0-9_]*)*))";
    private final static String ATTRIBUTE_QUOTE_NAME = "\\['[a-z_][a-z0-9_]*'(,'[a-z_][a-z0-9_]*')*\\]";

    private final static Pattern pattern = Pattern.compile(
            "(" + ARRAY_LIST + ")|(" + ARRAY_RANGE + ")|(" + ATTRIBUTE_ANY + ")|(" + ATTRIBUTE_DOT_NAME + ")|(" + ATTRIBUTE_QUOTE_NAME + ")|(" + RECURSIVE + ")"
            , Pattern.CASE_INSENSITIVE);

    private static Map<String, MatchRule> MATCH_RULE_CACHE = new ConcurrentHashMap<>();


    private final String jsonPath;
    private final Rule[] rules;


    public MatchRule(String jsonPath) {
        this.rules = makeRulesFromPath(this.jsonPath = jsonPath);
    }

    public static MatchRule valueOf(String jsonPath) {
        return MATCH_RULE_CACHE.computeIfAbsent(jsonPath, MatchRule::new);
    }

    public @Override String toString() {
        return jsonPath;
    }



    public boolean testPath(final Path path) {
        return new Rule.Context() {
            int ruleIndex = -1;
            int pathIndex = -1;

            public @Override Path.Node currentNode() {
                return path.get(pathIndex);
            }

            public @Override boolean tryNextNode() {
                return internalNext(ruleIndex);
            }

            public @Override boolean next() {
                return internalNext(ruleIndex++);
            }

            private boolean internalNext(int savedRuleIndex) {
                boolean result;
                int savedPathIndex = pathIndex++;
                if (pathIndex >= path.size() || ruleIndex >= rules.length) {
                    result = pathIndex == path.size() && ruleIndex == rules.length;
                } else {
                    result = rules[ruleIndex].test(this);
                }
                ruleIndex = savedRuleIndex;
                pathIndex = savedPathIndex;
                return result;
            }
        }.next();
    }

    public boolean testNode(Path path, JsonNode node) {
        return true;
    }



    private static Rule[] makeRulesFromPath(String jsonPath) {
        if (jsonPath.length() <= 1 || jsonPath.charAt(0) != '$') {
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
            return new RuleAttributeDescent();
        } else if (".*".equals(expr)) {
            return new RuleAnyAttributeName();
        } else if (expr.startsWith("['") || expr.startsWith(".")) {
            return RuleAttributeName.valueOf(expr);
        } else if (expr.startsWith("[")) {
            if (expr.contains(":")) {
                return RuleArrayRange.valueOf(expr);
            } else {
                return RuleArrayIndex.valueOf(expr);
            }
        } else {
            throw new IllegalArgumentException();
        }
    }
}
