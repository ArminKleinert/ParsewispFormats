package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGStringTest {
    @Test
    void basicDoubleQuoteTest() {
        var p = PEG.parser("S <- \"a\" \"B\"");
        Assertions.assertTrue(p.parse("ab").isFailure());
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());

        Assertions.assertEquals(
                PT.create("S", "aB"),
                PEG.parser("S <- \"aB\"").parse("aB"));
    }

    @Test
    void basicSingleQuoteTest() {
        var p = PEG.parser("S <- ´a´ ´B´");
        Assertions.assertTrue(p.parse("ab").isFailure());
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());

        Assertions.assertEquals(
                PT.create("S", "aB"),
                PEG.parser("S <- ´aB´").parse("aB"));
    }

    @Test
    void charAsRegexTest() {
        // [a]+ is treated as repetition #"[a]"+
        Assertions.assertEquals(
                PT.create("S", "a", "a"),
                PEG.parser("S <- [a]+").parse("aa"));

        var opts = new PEG.PEGOptions(null, null, true);

        // [a]+ is treated as regex #"[a]+"
        Assertions.assertEquals(
                PT.create("S", "aa"),
                PEG.parser("S <- [a]+", opts).parse("aa"));
    }
}
