package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFStringTest {
    @Test
    void caseSensitivityDefaultTest() {
        var p = ABNF.parser("S = \"a\" \"B\"");
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("ab"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("Ab"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("AB"));
    }

    @Test
    void caseSensitivityExplicitInsensitiveTest() {
        var p = ABNF.parser("S = %i\"a\" %i\"B\"");
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("ab"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("Ab"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("AB"));
    }

    @Test
    void caseSensitivityExplicitSensitiveTest() {
        var p = ABNF.parser("S = %s\"a\" %s\"B\"");
        Assertions.assertTrue(p.parse("ab").isFailure());
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());
    }

    @Test
    void caseSensitivityExplicitMixTest() {
        var p = ABNF.parser("S = %s\"a\" %i\"B\"");
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("ab"));
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());
    }

    @Test
    void invalidStringTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = \""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = \"a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = a\""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = \"\"\""));
    }
}
