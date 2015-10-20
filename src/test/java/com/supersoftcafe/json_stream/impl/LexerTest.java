package com.supersoftcafe.json_stream.impl;


import org.junit.Test;

import static org.junit.Assert.*;


public class LexerTest {

    @Test
    public void testLex() throws Exception {
        String path = "$..fred.[\"b\\\"ill\"][45]";

        Lexer.Token[] tokens = Lexer.lex(path);

        assertEquals(10, tokens.length);
        assertArrayEquals(new Lexer.Token[] {
                new Lexer.Token(Lexer.Type.DOLLAR, "$"),
                new Lexer.Token(Lexer.Type.DOTDOT, ".."),
                new Lexer.Token(Lexer.Type.NAME, "fred"),
                new Lexer.Token(Lexer.Type.DOT, "."),
                new Lexer.Token(Lexer.Type.OPENSQUARE, "["),
                new Lexer.Token(Lexer.Type.STRING, "b\"ill"),
                new Lexer.Token(Lexer.Type.CLOSESQUARE, "]"),
                new Lexer.Token(Lexer.Type.OPENSQUARE, "["),
                new Lexer.Token(Lexer.Type.INTEGER, 45l),
                new Lexer.Token(Lexer.Type.CLOSESQUARE, "]")
        }, tokens);
    }
}
