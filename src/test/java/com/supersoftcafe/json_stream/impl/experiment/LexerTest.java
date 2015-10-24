package com.supersoftcafe.json_stream.impl.experiment;


import org.junit.Test;

import static org.junit.Assert.*;


public class LexerTest {

    @Test
    public void testLex() throws Exception {
        String path = "$..fred.[\"b\\\"ill\"][45]";

        Lexer.Token[] tokens = Lexer.lex(path);

        assertEquals(10, tokens.length);
        assertArrayEquals(new Lexer.Token[] {
                new Lexer.Token(Lexer.Type.DOLLAR, "$", 0),
                new Lexer.Token(Lexer.Type.DOTDOT, "..", 1),
                new Lexer.Token(Lexer.Type.NAME, "fred", 3),
                new Lexer.Token(Lexer.Type.DOT, ".", 7),
                new Lexer.Token(Lexer.Type.OPENSQUARE, "[", 8),
                new Lexer.Token(Lexer.Type.STRING, "b\"ill", 9),
                new Lexer.Token(Lexer.Type.CLOSESQUARE, "]", 17),
                new Lexer.Token(Lexer.Type.OPENSQUARE, "[", 18),
                new Lexer.Token(Lexer.Type.INTEGER, 45l, 19),
                new Lexer.Token(Lexer.Type.CLOSESQUARE, "]", 21)
        }, tokens);
    }
}
