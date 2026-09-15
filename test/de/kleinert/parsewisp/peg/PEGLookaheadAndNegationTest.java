package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGLookaheadAndNegationTest {
    @Test
    void lookaheadTest() {
        var pDefault = PEG.parser("S <- [ab]*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pLook = PEG.parser("S <- &\"a\" [ab]*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pLook.parse("ab"));
        Assertions.assertTrue(pLook.parse("ba").isFailure());
    }

    @Test
    void negationTest() {
        var pDefault = PEG.parser("S <- [ab]*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pNeg = Parsewisp.parser("S = !'a' #'[ab]'* ;");
        Assertions.assertTrue(pNeg.parse("ab").isFailure());
        Assertions.assertEquals(PT.create("S", "b", "a"), pNeg.parse("ba"));
    }
}
