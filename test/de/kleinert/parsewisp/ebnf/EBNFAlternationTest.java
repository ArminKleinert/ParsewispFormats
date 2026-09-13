package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

class EBNFAlternationTest {
    @Test
    void testAlternativeFirstBranch() {
        var p = EBNF.parser("S = \"cat\" | \"dog\" ;");

        Assertions.assertEquals(PT.create("S", "cat"), p.parse("cat"));
        Assertions.assertEquals(PT.create("S", "dog"), p.parse("dog"));
        Assertions.assertTrue(p.parse("bird").isFailure());
    }

    @Test
    void testAlternativeCreatesAmbiguity() {
        var p = EBNF.parser("S = \"a\" | S , S ;");

        var trees = Set.of(
                PT.create("S",
                        PT.create("S", "a"),
                        PT.create("S", PT.create("S", "a"), PT.create("S", "a"))),
                PT.create("S",
                        PT.create("S", PT.create("S", "a"), PT.create("S", "a")),
                        PT.create("S", "a")));

        Assertions.assertEquals(trees, new HashSet<>(p.parses("aaa")));
    }
}
