package com.supersoftcafe.json_stream;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class PathMatcher implements Predicate<Path> {

    private final static String RECURSIVE            = "\\.";
    private final static String ARRAY_LIST           = "\\[[0-9]+(,[0-9]+)*\\]";
    private final static String ARRAY_RANGE          = "\\[([0-9]+)?:([0-9]+)?(:[0-9]+)?\\]";
    private final static String ATTRIBUTE_ANY        = "\\.\\*";
    private final static String ATTRIBUTE_DOT_NAME   = "\\.(([a-z_][a-z0-9_]*(\\|[a-z_][a-z0-9_]*)*))";
    private final static String ATTRIBUTE_QUOTE_NAME = "\\['[a-z_][a-z0-9_]*'(,'[a-z_][a-z0-9_]*')*\\]";




    private final static Pattern pattern = Pattern.compile(
            "(" + ARRAY_LIST + ")|(" + ARRAY_RANGE + ")|(" + ATTRIBUTE_ANY + ")|(" + ATTRIBUTE_DOT_NAME + ")|(" + ATTRIBUTE_QUOTE_NAME + ")|(" + RECURSIVE + ")"
            , Pattern.CASE_INSENSITIVE);

    private final Rule rules;

    /**
     * <pre>
     *   "$[:]"              = Any array element directly below the root."
     *   "*.phraseMatches[]" = Any array element that is a child of 'phraseMatches'.
     *   "$.metadata.?.name" = Any 2nd level sub-object of metadata, when at the root, called 'name'.
     *   "$.metadata*.name"  = Any attribute under metadata called 'name', anywhere.
     *   "$.array[0-3].name" = Name of array elements 0,1,2,3
     *   "[]"                = Any array element
     *   "[][3]"             = 3rd element of an array that is a direct descendent of an array
     *   ".fred|bill[]"      = Array element that is under attribute fred or bill
     * </pre>
     *
     * @param path
     */
    PathMatcher(String path) {
        if (path.length() <= 1 || path.charAt(0) != '$')
            throw new IllegalArgumentException("Expression not rooted");

        int regionStart = 1;
        Matcher matcher = pattern.matcher(path);
        ArrayList<String> subExpressions = new ArrayList<>();

        while (regionStart < matcher.regionEnd()) {
            matcher.region(regionStart, matcher.regionEnd());
            if (!matcher.lookingAt())
                throw new IllegalArgumentException("Invalid sub-expression at " + regionStart);

            String expr = matcher.group();
            subExpressions.add(expr);

            regionStart += expr.length();
        }

        Rule nextRule = null;
        for (int index = subExpressions.size(); --index >= 0; )
            nextRule = makeRule(nextRule, subExpressions.get(index));

        if (nextRule == null)
            throw new IllegalArgumentException("No sub-expressions");
        rules = new RuleStart(nextRule);
    }


    public @Override boolean test(Path path) {
        return rules.test(path, -1);
    }



    private Rule makeRule(Rule nextRule, String expr) {
        if (".".equals(expr)) {
            return new RuleAttributeDescent(nextRule);
        } else if (".*".equals(expr)) {
            return new RuleAnyAttributeName(nextRule);
        } else if (expr.startsWith("['")) {
            return makeRuleFromQuotedNames(nextRule, expr);
        } else if (expr.startsWith(".")) {
            return makeRuleFromDottedNames(nextRule, expr);
        } else if (expr.startsWith("[") && expr.contains(":")) {
            return makeRuleFromIndexRange(nextRule, expr);
        } else {
            return makeRuleFromIndexList(nextRule, expr);
        }
    }

    private Rule makeRuleFromQuotedNames(Rule nextRule, String expr) {
        expr = expr.substring(1, expr.length()-1);
        String[] names = expr.split(",");
        for (int index = names.length; --index >= 0; ) {
            String name = names[index];
            names[index] = name.substring(1, name.length()-1);
        }
        return new RuleAttributeName(nextRule, names);
    }

    private Rule makeRuleFromDottedNames(Rule nextRule, String expr) {
        expr = expr.substring(1);
        String[] names = expr.split("\\|");
        return new RuleAttributeName(nextRule, names);
    }

    private Rule makeRuleFromIndexRange(Rule nextRule, String expr) {
        expr = expr.substring(1, expr.length()-1);
        String[] parts = expr.split(":", -1);
        long minIndex = parts[0].isEmpty() ? 0 : Long.parseLong(parts[0]);
        long maxIndex = parts[1].isEmpty() ? Long.MAX_VALUE : Long.parseLong(parts[1]);
        long step     = parts.length<3 ? 1 : Long.parseLong(parts[2]);
        if (maxIndex < minIndex || minIndex < 0 || step < 1)
            throw new IllegalArgumentException("Invalid array slice");
        return new RuleArrayRange(nextRule, minIndex, maxIndex, step);
    }

    private Rule makeRuleFromIndexList(Rule nextRule, String expr) {
        expr = expr.substring(1, expr.length()-1);
        String[] parts = expr.split(",");
        long[] indexes = new long[parts.length];
        for (int index = parts.length; --index >= 0; )
            indexes[index] = Long.parseLong(parts[index]);
        return new RuleArrayIndex(nextRule, indexes);
    }



    private abstract static class Rule {
        final Rule nextRule;

        Rule(Rule nextRule) {
            this.nextRule = nextRule;
        }

        boolean applyNext(Path path, int index) {
            int size = path.size();
            index = index + 1;

            if (nextRule == null) {
                return index == size;
            } else {
                return index < size && nextRule.test(path, index);
            }
        }

        abstract boolean test(Path path, int index);
    }

    private static class RuleStart extends Rule {
        public RuleStart(Rule nextRule) {
            super(nextRule);
        }

        @Override boolean test(Path path, int index) {
            return applyNext(path, index);
        }
    }

    private static class RuleAttributeDescent extends Rule {
        RuleAttributeDescent(Rule nextRule) {
            super(nextRule);
        }

        @Override boolean test(Path path, int index) {
            for (; index < path.size() && path.get(index).isObject(); ++index)
                if (applyNext(path, index))
                    return true;
            return false;
        }
    }

    private static class RuleAnyAttributeName extends Rule {
        RuleAnyAttributeName(Rule nextRule) {
            super(nextRule);
        }

        @Override boolean test(Path path, int index) {
            return path.get(index).isObject() && applyNext(path, index);
        }
    }

    private static class RuleAttributeName extends Rule {
        final String[] names;
        RuleAttributeName(Rule nextRule, String[] names) {
            super(nextRule);
            this.names = names;
        }

        @Override boolean test(Path path, int index) {
            Path.Node node = path.get(index);
            if (node.isObject()) {
                String nodeName = node.getName();
                for (String name : names)
                    if (name.equals(nodeName))
                        return applyNext(path, index);
            }
            return false;
        }
    }

    private static class RuleArrayIndex extends Rule {
        private final long[] indexes;
        RuleArrayIndex(Rule nextRule, long[] indexes) {
            super(nextRule);
            this.indexes = indexes;
        }

        @Override boolean test(Path path, int index) {
            Path.Node node = path.get(index);
            if (node.isArray()) {
                long arrayIndex = node.getIndex();
                for (long matchIndex : indexes)
                    if (arrayIndex == matchIndex)
                        return applyNext(path, index);
            }
            return false;
        }
    }

    private static class RuleArrayRange extends Rule {
        final long minIndex, maxIndex, step;
        RuleArrayRange(Rule nextRule, long minIndex, long maxIndex, long step) {
            super(nextRule);
            this.minIndex = minIndex;
            this.maxIndex = maxIndex;
            this.step     = step;
        }

        @Override boolean test(Path path, int index) {
            Path.Node node = path.get(index);
            if (node.isArray()) {
                long arrayIndex = node.getIndex();
                if (arrayIndex >= minIndex && arrayIndex <= maxIndex && (arrayIndex % step) == 0)
                    return applyNext(path, index);
            }
            return false;
        }
    }
}
