package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsLookaheadAndNegationTest {
    @Test
    void lookaheadTest() {
        var pDefault = QffMs.parser("S = ´[ab]´*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pLook = QffMs.parser("S = &\"a\" ´[ab]´*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pLook.parse("ab"));
        Assertions.assertTrue(pLook.parse("ba").isFailure());
    }

    @Test
    void negationTest() {
        var pDefault = QffMs.parser("S = ´[ab]´*");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pNeg = QffMs.parser("S = !'a' ´[ab]´* ");
        Assertions.assertTrue(pNeg.parse("ab").isFailure());
        Assertions.assertEquals(PT.create("S", "b", "a"), pNeg.parse("ba"));
    }
}
