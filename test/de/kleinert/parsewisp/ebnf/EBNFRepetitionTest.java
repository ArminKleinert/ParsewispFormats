package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFRepetitionTest {
    @Test
    void zeroOrMoreRepetitionTest() {
        var p = EBNF.parser("S = { \"a\" } ;");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
    }

    @Test
    void onceOrMoreRepetitionTest() {
        var p1 = EBNF.parser("S = \"a\" , { \"a\" } ;");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p1.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p1.parse("aaa"));
    }

    @Test
    void exactRepetitionTest() {
        var p0 = EBNF.parser("S = 0* \"a\" ;");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertTrue(p0.parse("a").isFailure());
        Assertions.assertTrue(p0.parse("aa").isFailure());
        Assertions.assertTrue(p0.parse("aaa").isFailure());

        var p1 = EBNF.parser("S = 1* \"a\" ;");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertTrue(p1.parse("aa").isFailure());
        Assertions.assertTrue(p1.parse("aaa").isFailure());

        var p3 = EBNF.parser("S = 3* \"a\" ;");
        Assertions.assertTrue(p3.parse("").isFailure());
        Assertions.assertTrue(p3.parse("a").isFailure());
        Assertions.assertTrue(p3.parse("aa").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void reverseRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = 2*1 \"a\" ;").parse("a"));
    }

    @Test
    void lonelyRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = * ;").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = *1 ;").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = 2*1 ;").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = 2 ;").parse("a"));
    }

    @Test
    void wrongSideRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a\" *").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a\" 2*").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a\" *1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a\" 2*1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a\" 2").parse("a"));
    }
}
