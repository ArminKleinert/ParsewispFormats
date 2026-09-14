package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class PEGAlternationTest {
    @Test
    void testAlternativeFirstBranch() {
        var p = PEG.parser("S <- \"cat\" / \"dog\"");

        Assertions.assertEquals(PT.create("S", "cat"), p.parse("cat"));
        Assertions.assertEquals(PT.create("S", "dog"), p.parse("dog"));
        Assertions.assertTrue(p.parse("bird").isFailure());
    }

    @Test
    void testAlternativeCreatesAmbiguity() {
        var p = PEG.parser("S <- \"a\" / S S");

        var trees = List.of(
                PT.create("S",
                        PT.create("S", "a"),
                        PT.create("S", PT.create("S", "a"), PT.create("S", "a"))),
                PT.create("S",
                        PT.create("S", PT.create("S", "a"), PT.create("S", "a")),
                        PT.create("S", "a")));

        Assertions.assertEquals(trees, p.parses("aaa"));
    }
}
