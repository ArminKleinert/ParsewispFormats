package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsStringTest {
    @Test
    void basicDoubleQuoteTest() {
        var p = QffMs.parser("S = \"a\" \"B\"");
        Assertions.assertTrue(p.parse("ab").isFailure());
        Assertions.assertTrue(p.parse("Ab").isFailure());
        Assertions.assertEquals(PT.create("S", "a", "B"), p.parse("aB"));
        Assertions.assertTrue(p.parse("AB").isFailure());

        Assertions.assertEquals(
                PT.create("S", "aB"),
                QffMs.parser("S = \"aB\"").parse("aB"));
    }

    @Test
    void invalidStringTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = \""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = \"a"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = a\""));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                () -> QffMs.parser("S = \"\"\""));
    }
}
