package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsRepetitionTest {
    @Test
    void zeroOrMoreRepetitionTest() {
        var p = QffMs.parser("S = \"a\"{,}");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
    }

    @Test
    void onlyLeftRepetitionTest() {
        var p0 = QffMs.parser("S = \"a\"{0,}");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p0.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p0.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p0.parse("aaa"));

        var p1 = QffMs.parser("S = \"a\"{1,}");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p1.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p1.parse("aaa"));

        var p3 = QffMs.parser("S = \"a\"{3,}");
        Assertions.assertTrue(p3.parse("").isFailure());
        Assertions.assertTrue(p3.parse("a").isFailure());
        Assertions.assertTrue(p3.parse("aa").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void onlyRightRepetitionTest() {
        var p0 = QffMs.parser("S = \"a\"{,0}");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertTrue(p0.parse("a").isFailure());
        Assertions.assertTrue(p0.parse("aa").isFailure());
        Assertions.assertTrue(p0.parse("aaa").isFailure());

        var p1 = QffMs.parser("S = \"a\"{,1}");
        Assertions.assertEquals(PT.create("S"), p1.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertTrue(p1.parse("aa").isFailure());
        Assertions.assertTrue(p1.parse("aaa").isFailure());

        var p3 = QffMs.parser("S = \"a\"{,3}");
        Assertions.assertEquals(PT.create("S"), p3.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p3.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p3.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void bothSidesRepetitionTest() {
        var p00 = QffMs.parser("S = \"a\"{0,0}");
        Assertions.assertEquals(PT.create("S"), p00.parse(""));
        Assertions.assertTrue(p00.parse("a").isFailure());
        Assertions.assertTrue(p00.parse("aa").isFailure());
        Assertions.assertTrue(p00.parse("aaa").isFailure());

        var p01 = QffMs.parser("S = \"a\"{0,1}");
        Assertions.assertEquals(PT.create("S"), p01.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p01.parse("a"));
        Assertions.assertTrue(p01.parse("aa").isFailure());
        Assertions.assertTrue(p01.parse("aaa").isFailure());

        var p12 = QffMs.parser("S = \"a\"{1,2}");
        Assertions.assertTrue(p12.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p12.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p12.parse("aa"));
        Assertions.assertTrue(p12.parse("aaa").isFailure());

        var p22 = QffMs.parser("S = \"a\"{2,2}");
        Assertions.assertTrue(p22.parse("").isFailure());
        Assertions.assertTrue(p22.parse("a").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a"), p22.parse("aa"));
        Assertions.assertTrue(p22.parse("aaa").isFailure());
    }

    @Test
    void exactRepetitionTest() {
        var p0 = QffMs.parser("S = \"a\"{0}");
        Assertions.assertEquals(PT.create("S"), p0.parse(""));
        Assertions.assertTrue(p0.parse("a").isFailure());
        Assertions.assertTrue(p0.parse("aa").isFailure());
        Assertions.assertTrue(p0.parse("aaa").isFailure());

        var p1 = QffMs.parser("S = \"a\"{1}");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertTrue(p1.parse("aa").isFailure());
        Assertions.assertTrue(p1.parse("aaa").isFailure());

        var p3 = QffMs.parser("S = \"a\"{3}");
        Assertions.assertTrue(p3.parse("").isFailure());
        Assertions.assertTrue(p3.parse("a").isFailure());
        Assertions.assertTrue(p3.parse("aa").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p3.parse("aaa"));
    }

    @Test
    void repetitionEquivalenceTest() {
        Assertions.assertEquals(
                QffMs.parser("S = \"a\"{0,0}").parse("a"),
                QffMs.parser("S = \"\"").parse("a"));

        Assertions.assertEquals(
                QffMs.parser("S = \"a\"{0,3}").parse("aaa"),
                QffMs.parser("S = \"a\" \"a\" \"a\"").parse("aaa"));

        Assertions.assertEquals(
                QffMs.parser("S = \"a\"{0,3}").parse("aaa"),
                QffMs.parser("S = \"a\"{3,}").parse("aaa"));
    }

    @Test
    void reverseRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = \"a\"{2,1}").parse("a"));
    }

    @Test
    void lonelyRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = {,}").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = {2,}").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = {,1}").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = {2,1}").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = {2}").parse("a"));
    }
}
