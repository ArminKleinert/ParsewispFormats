package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFRegexTest {
    @Test
    void basicRegex() {
        var p = ABNF.parser("S = #\"[a-fA-F0-9]+\"");
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        ); }

    @Test
    void singleOrDoubleQuotationEquivalenceForRegexes() {
        var pSingleQuoted = ABNF.parser("""
                S = #'a' #'b"c\\''
                """);
        var pDoubleQuoted = ABNF.parser("""
                S = #"a" #"b\\"c'"
                """);

        Assertions.assertEquals(PT.create("S","a","b\"c'"), pSingleQuoted.parse("ab\"c'"));
        Assertions.assertEquals(PT.create("S","a","b\"c'"), pDoubleQuoted.parse("ab\"c'"));

        Assertions.assertEquals(pSingleQuoted.parse("ab\"c'"), pDoubleQuoted.parse("ab\"c'"));
        Assertions.assertEquals(pSingleQuoted.parse(""), pDoubleQuoted.parse(""));
        Assertions.assertEquals(pSingleQuoted.grammar(), pDoubleQuoted.grammar());
    }

    @Test
    void invalidRegexTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = #\""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = #\"a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = #a\""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = #\"\"\""));
    }
}
