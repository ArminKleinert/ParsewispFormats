package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsCharsTest {
    @Test
    void numValHexBasic() {
        var g1 = "S = 'A'";
        Assertions.assertEquals(PT.create("S", "A"), QffMs.parser(g1).parse("A"));
        var g2 = "S = 'A' 'a'";
        Assertions.assertEquals(PT.create("S", "A", "a"), QffMs.parser(g2).parse("Aa"));
    }

    @Test
    void numValHexRange() {
        var p = QffMs.parser("S = 'A'..'Z'");
        for (char c = 'A'; c <= 'Z'; c++) {
            var s = String.valueOf(c);
            Assertions.assertEquals(PT.create("S", s), p.parse(s));
        }
        for (char c = 'a'; c <= 'z'; c++) {
            var s = String.valueOf(c);
            Assertions.assertTrue(p.parse(s).isFailure());
        }
    }

    @Test
    void numValHexRangeForAlphaNum() {
        var p = QffMs.parser("S = '0'..'9' | 'A'..'Z' | 'a'..'z'");
        for (char c = '0'; c <= '9'; c++) {
            var s = String.valueOf(c);
            Assertions.assertEquals(PT.create("S", s), p.parse(s));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            var s = String.valueOf(c);
            Assertions.assertEquals(PT.create("S", s), p.parse(s));
        }
        for (char c = 'a'; c <= 'z'; c++) {
            var s = String.valueOf(c);
            Assertions.assertEquals(PT.create("S", s), p.parse(s));
        }
    }

    @Test
    void numValHexAsString() {
        var parser = QffMs.parser("S = 'A' 'B' 'C'");
        Assertions.assertEquals(PT.create("S", "A","B","C"), parser.parse("ABC"));
    }

    @Test
    void numValBasicEquivalence() {
        var pHex = QffMs.parser("S = 'A'");
        var pDec = QffMs.parser("S = '\\u0041'");
        Assertions.assertEquals(pHex.grammar(), pDec.grammar());
    }

    @Test
    void numValRangeEquivalence() {
        var pHex = QffMs.parser("S ='A'..'Z'");
        var pDec = QffMs.parser("S = '\\u0041'..'\\u005A'");
        Assertions.assertEquals(pHex.grammar(), pDec.grammar());
    }

    @Test
    void invalidEmptySideNumValTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S ='A'.."));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->QffMs.parser("S = ..'A'"));
    }
}
