package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFStringTest {
    @Test
    void caseSensitivityDefaultTest() {
        var p = EBNF.parser("S = \"a\" , \"B\" ;");
        Assertions.assertTrue(p.parse("ab").isFailure());
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());
    }

    @Test
    void invalidStringTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = a\""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> EBNF.parser("S = \"\"\""));
    }
}
