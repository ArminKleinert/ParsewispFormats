package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EBNFOptionalTest {
    @Test
    void basicOptionTest() {
        var p = EBNF.parser("S = [ \"A\" ] ;");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
    }

    @Test
    void optionInCatTest() {
        var p = EBNF.parser("S = \"A\" , [ \"B\" ] , \"C\" ;");
        Assertions.assertEquals(PT.create("S", "A", "C"), p.parse("AC"));
        Assertions.assertEquals(PT.create("S", "A", "B", "C"), p.parse("ABC"));
    }

    @Test
    void optionAroundRepetitionTest() {
        var p = EBNF.parser("S = [ \"A\" , { \"A\" } ] ;");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
        Assertions.assertEquals(PT.create("S", "A", "A", "A"), p.parse("AAA"));
    }
}
