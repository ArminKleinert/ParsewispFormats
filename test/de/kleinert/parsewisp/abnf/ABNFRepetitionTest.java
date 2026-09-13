package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFRepetitionTest {
    @Test
    void zeroOrMoreRepetitionTest() {
        var p = ABNF.parser("S = * \"a\"");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
    }

    @Test
    void onlyLeftRepetitionTest() {
        var p0 = ABNF.parser("S = 0* \"a\"");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p0.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p0.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p0.parse("aaa"));

        var p1 = ABNF.parser("S = 1* \"a\"");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p1.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p1.parse("aaa"));

        var p3 = ABNF.parser("S = 3* \"a\"");
        Assertions.assertTrue(p3.parse("").isFailure());
        Assertions.assertTrue(p3.parse("a").isFailure());
        Assertions.assertTrue(p3.parse("aa").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void onlyRightRepetitionTest() {
        var p0 = ABNF.parser("S = *0 \"a\"");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertTrue(p0.parse("a").isFailure());
        Assertions.assertTrue(p0.parse("aa").isFailure());
        Assertions.assertTrue(p0.parse("aaa").isFailure());

        var p1 = ABNF.parser("S = *1 \"a\"");
        Assertions.assertEquals(PT.create("S"), p1.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertTrue(p1.parse("aa").isFailure());
        Assertions.assertTrue(p1.parse("aaa").isFailure());

        var p3 = ABNF.parser("S = *3 \"a\"");
        Assertions.assertEquals(PT.create("S"), p3.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p3.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p3.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void bothSidesRepetitionTest() {
        var p00 = ABNF.parser("S = 0*0 \"a\"");
        Assertions.assertEquals(PT.create("S"), p00.parse(""));
        Assertions.assertTrue(p00.parse("a").isFailure());
        Assertions.assertTrue(p00.parse("aa").isFailure());
        Assertions.assertTrue(p00.parse("aaa").isFailure());

        var p01 = ABNF.parser("S = 0*1 \"a\"");
        Assertions.assertEquals(PT.create("S"), p01.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p01.parse("a"));
        Assertions.assertTrue(p01.parse("aa").isFailure());
        Assertions.assertTrue(p01.parse("aaa").isFailure());

        var p12 = ABNF.parser("S = 1*2 \"a\"");
        Assertions.assertTrue(p12.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p12.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p12.parse("aa"));
        Assertions.assertTrue(p12.parse("aaa").isFailure());

        var p22 = ABNF.parser("S = 2*2 \"a\"");
        Assertions.assertTrue(p22.parse("").isFailure());
        Assertions.assertTrue(p22.parse("a").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a"), p22.parse("aa"));
        Assertions.assertTrue(p22.parse("aaa").isFailure());
    }

    @Test
    void exactRepetitionTest() {
        var p0 = ABNF.parser("S = 0 \"a\"");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertTrue(p0.parse("a").isFailure());
        Assertions.assertTrue(p0.parse("aa").isFailure());
        Assertions.assertTrue(p0.parse("aaa").isFailure());

        var p1 = ABNF.parser("S = 1 \"a\"");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertTrue(p1.parse("aa").isFailure());
        Assertions.assertTrue(p1.parse("aaa").isFailure());

        var p3 = ABNF.parser("S = 3 \"a\"");
        Assertions.assertTrue(p3.parse("").isFailure());
        Assertions.assertTrue(p3.parse("a").isFailure());
        Assertions.assertTrue(p3.parse("aa").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void repetitionEquivalenceTest() {
        Assertions.assertEquals(
                ABNF.parser("S = 0*0 \"a\"").parse("a"),
                ABNF.parser("S = \"\"").parse("a"));

        Assertions.assertEquals(
                ABNF.parser("S = 0*3 \"a\"").parse("aaa"),
                ABNF.parser("S = \"a\" \"a\" \"a\"").parse("aaa"));

        Assertions.assertEquals(
                ABNF.parser("S = 0*3 \"a\"").parse("aaa"),
                ABNF.parser("S = 3* \"a\"").parse("aaa"));
    }

    @Test
    void reverseRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = 2*1 \"a\"").parse("a"));
    }

    @Test
    void lonelyRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = *").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = 2*").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = *1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = 2*1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = 2").parse("a"));
    }

    @Test
    void wrongSideRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = \"a\" *").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = \"a\" 2*").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = \"a\" *1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = \"a\" 2*1").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> ABNF.parser("S = \"a\" 2").parse("a"));
    }
}
