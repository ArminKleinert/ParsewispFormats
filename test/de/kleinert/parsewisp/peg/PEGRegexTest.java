package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGRegexTest {
    @Test
    void basicRegex() {
        var p = PEG.parser("S <- '[a-fA-F0-9]+'");
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        );
    }

    @Test
    void singleOrDoubleQuotationEquivalenceForRegexes() {
        var pSingleQuoted = PEG.parser("""
                S <- "a" 'b"c\\''
                """);
        var pDoubleQuoted = PEG.parser("""
                S <- "a" 'b"c\\''
                """);

        Assertions.assertEquals(PT.create("S", "a", "b\"c'"), pSingleQuoted.parse("ab\"c'"));
        Assertions.assertEquals(PT.create("S", "a", "b\"c'"), pDoubleQuoted.parse("ab\"c'"));

        Assertions.assertEquals(pSingleQuoted.parse("ab\"c'"), pDoubleQuoted.parse("ab\"c'"));
        Assertions.assertEquals(pSingleQuoted.parse(""), pDoubleQuoted.parse(""));
        Assertions.assertEquals(pSingleQuoted.grammar(), pDoubleQuoted.grammar());
    }

    @Test
    void invalidRegexTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- ' ;"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- 'a ;"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- 'a\\' ;"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> PEG.parser("S <- ''' ;"));
    }
}
