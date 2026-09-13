package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFLookaheadAndNegationTest {
    @Test
    void lookaheadTest() {
        var opts = new EBNF.EBNFOptions(null, null, true, false, false, null);

        var pDefault = EBNF.parser("S = { #'[ab]' } ;");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pLook = EBNF.parser("S = &\"a\" , { #'[ab]' } ;", opts);
        Assertions.assertEquals(PT.create("S", "a", "b"), pLook.parse("ab"));
        Assertions.assertTrue(pLook.parse("ba").isFailure());
    }

    @Test
    void negationTest() {
        var opts = new EBNF.EBNFOptions(null, null, true, false, false, null);

        var pDefault = EBNF.parser("S = { #'[ab]' } ;");
        Assertions.assertEquals(PT.create("S", "a", "b"), pDefault.parse("ab"));
        Assertions.assertEquals(PT.create("S", "b", "a"), pDefault.parse("ba"));

        var pNeg = Parsewisp.parser("S = !'a' , { #'[ab]' } ;", opts);
        Assertions.assertTrue(pNeg.parse("ab").isFailure());
        Assertions.assertEquals(PT.create("S", "b", "a"), pNeg.parse("ba"));
    }
}
