package com.supersoftcafe.json_stream.impl;


import org.junit.Test;

import static org.junit.Assert.*;


public class MatchRuleTest {

    @Test
    public void testValueOf_emptyRule() throws Exception {
        MatchRule matchRule = MatchRule.valueOf("$");

        assertEquals(0, matchRule.size());
    }

    @Test
    public void testValueOf_attribute() throws Exception {
        MatchRule matchRule = MatchRule.valueOf("$.fred");

        assertEquals(1, matchRule.size());
        assertEquals(matchRule.get(0), new RuleAttributeName("fred"));
    }

    @Test
    public void testValueOf_attributeSetAndArray() throws Exception {
        MatchRule matchRule = MatchRule.valueOf("$['fred','bill','bert'][1,2,3]");

        assertEquals(2, matchRule.size());
        assertEquals(matchRule.get(0), new RuleAttributeName("fred", "bill", "bert"));
        assertEquals(matchRule.get(1), new RuleArrayIndex(1, 2, 3));
    }

    @Test
    public void testTestPath_deepAttribute() throws Exception {
        PathImpl pathGood    = PathImpl.valueOf("$.bill.frank.fred");
        PathImpl pathGoodIsh = PathImpl.valueOf("$.bill.frank.fred.another");
        PathImpl pathBad     = PathImpl.valueOf("$.bill.jane.frank");
        PathImpl pathArray   = PathImpl.valueOf("$.bill[3].fred");
        MatchRule matchRule = new MatchRule(
                new RuleDescent(),
                new RuleAttributeName("fred")
        );

        assertTrue(matchRule.testPath(pathGood));
        assertFalse(matchRule.testPath(pathGoodIsh));
        assertFalse(matchRule.testPath(pathBad));
        assertTrue(matchRule.testPath(pathArray));
    }

    @Test
    public void testTestPath_complex() throws Exception {
        MatchRule matchRule = MatchRule.valueOf("$..fred[3,4,5][1:10:2].*[*]");

        assertTrue(matchRule.testPath(PathImpl.valueOf("$.granny.fred[4][3].group[123]")));
        assertTrue(matchRule.testPath(PathImpl.valueOf("$.granny.uncle.fred[4][3].group[123]")));
        assertTrue(matchRule.testPath(PathImpl.valueOf("$.fred[4][3].group[123]")));
        assertFalse(matchRule.testPath(PathImpl.valueOf("$.granny.fred[4][4].group[123]")));
        assertFalse(matchRule.testPath(PathImpl.valueOf("$.granny.fred[2][3].group[123]")));
        assertFalse(matchRule.testPath(PathImpl.valueOf("$.granny.fredy[4][3].group[123]")));
    }


    @Test
    public void testToString() throws Exception {
        String expectedString = "$.oneName['oneOfTwo','twoOfTwo'][1][1,2,3][*][1:10][1:10:2].*.";
        MatchRule expectedRule = new MatchRule(
                new RuleAttributeName("oneName"),
                new RuleAttributeName("oneOfTwo", "twoOfTwo"),
                new RuleArrayIndex(1),
                new RuleArrayIndex(1,2,3),
                new RuleAnyArrayIndex(),
                new RuleArrayRange(1, 10, 1),
                new RuleArrayRange(1, 10, 2),
                new RuleAnyAttributeName(),
                new RuleDescent()
        );

        String resultString = expectedRule.toString();
        MatchRule resultRule = MatchRule.valueOf(expectedString);

        assertEquals(expectedString, resultString);
        assertEquals(expectedRule, resultRule);
    }
}