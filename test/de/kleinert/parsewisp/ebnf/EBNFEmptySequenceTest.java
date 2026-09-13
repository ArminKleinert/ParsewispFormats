package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFEmptySequenceTest {
    @Test
    void basicTest() {
        var p = EBNF.parser("S = ;");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
    }
    @Test
    void inAlternationTest() {
        var p = EBNF.parser("S = | 'a';");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), p.parse("a"));
    }
}
