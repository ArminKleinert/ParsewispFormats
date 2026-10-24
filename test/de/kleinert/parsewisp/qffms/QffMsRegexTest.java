package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsRegexTest {
    @Test
    void basicRegex() {
        var p = QffMs.parser("S = ´[a-fA-F0-9]+´");
        Assertions.assertEquals(
                PT.create("S", "7F"),
                p.parse("7F")
        ); }

    @Test
    void singleOrDoubleQuotationEquivalenceForRegexes() {
        var p = QffMs.parser("""
                S = ´a´ ´b\\"c'´
                """);

        Assertions.assertEquals(PT.create("S","a","b\"c'"), p.parse("ab\"c'"));

        Assertions.assertEquals(p.parse("ab\"c'"), p.parse("ab\"c'"));
        Assertions.assertEquals(p.parse(""), p.parse(""));
        Assertions.assertEquals(p.grammar(), p.grammar());
    }

    @Test
    void invalidRegexTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S = ´"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S = ´a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S = a´"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S = ´´´"));
    }
}
