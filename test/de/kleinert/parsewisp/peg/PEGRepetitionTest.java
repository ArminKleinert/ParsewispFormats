package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGRepetitionTest {
    @Test
    void zeroOrMoreRepetitionTest() {
        var p = PEG.parser("S <- \"a\"*");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p.parse("aaa"));
    }

    @Test
    void onceOrMoreRepetitionTest() {
        var p1 = PEG.parser("S <- \"a\"+");
        Assertions.assertTrue(p1.parse("").isFailure());
        Assertions.assertEquals(PT.create("S", "a"), p1.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), p1.parse("aa"));
        Assertions.assertEquals(PT.create("S", "a", "a", "a"), p1.parse("aaa"));
    }

    @Test
    void lonelyRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- +").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- *").parse("a"));
    }

    @Test
    void wrongSideRepetitionInvalidTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- +\"a\"").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- *\"a\"").parse("a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- ?\"a\"").parse("a"));
    }
}
