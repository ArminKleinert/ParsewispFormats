package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFNumValTest {
    @Test
    void numValHexBasic() {
        var g1 = "S = %x41";
        Assertions.assertEquals(PT.create("S", "A"), ABNF.parser(g1).parse("A"));
        var g2 = "S = %x41 %x61";
        Assertions.assertEquals(PT.create("S", "A", "a"), ABNF.parser(g2).parse("Aa"));
    }

    @Test
    void numValHexRange() {
        var p = ABNF.parser("S = %x41-5A"); // A..Z
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
        var p = ABNF.parser("S = %x30-39 / %x41-5A / %x61-7A"); // 0..9 and A..Z and a..z
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
        var parser = ABNF.parser("S = %x41.42.43");
        Assertions.assertEquals(PT.create("S", "ABC"), parser.parse("ABC"));
    }

    @Test
    void numValDecBasic() {
        var g1 = "S = %d65";
        Assertions.assertEquals(PT.create("S", "A"), ABNF.parser(g1).parse("A"));
        var g2 = "S = %d65 %d97";
        Assertions.assertEquals(PT.create("S", "A", "a"), ABNF.parser(g2).parse("Aa"));
    }

    @Test
    void numValDecRange() {
        var p = ABNF.parser("S = %d65-90"); // A..Z
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
    void numValDecRangeForAlphaNum() {
        var p = ABNF.parser("S = %d48-57 / %d65-90 / %d97-122"); // 0..9 and A..Z and a..z
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
    void numValDecAsString() {
        var parser = ABNF.parser("S = %d65.66.67");
        Assertions.assertEquals(PT.create("S", "ABC"), parser.parse("ABC"));
    }

    @Test
    void numValBinBasic() {
        var g1 = "S = %b1000001";
        Assertions.assertEquals(PT.create("S", "A"), ABNF.parser(g1).parse("A"));
        var g2 = "S = %b1000001 %b1100001";
        Assertions.assertEquals(PT.create("S", "A", "a"), ABNF.parser(g2).parse("Aa"));
    }

    @Test
    void numValBinRange() {
        var p = ABNF.parser("S = %d65-90"); // A..Z
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
    void numValBinRangeForAlphaNum() {
        var p = ABNF.parser("S = %b110000-111001 / %b1000001-1011010 / %b1100001-1111010"); // 0..9 and A..Z and a..z
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
    void numValBinAsString() {
        var parser = ABNF.parser("S = %b1000001.1000010.1000011");
        Assertions.assertEquals(PT.create("S", "ABC"), parser.parse("ABC"));
    }

    @Test
    void numValBasicEquivalence() {
        var pHex = ABNF.parser("S = %x30");
        var pDec = ABNF.parser("S = %d48");
        var pBin = ABNF.parser("S = %b110000");
        Assertions.assertEquals(pHex, pDec);
        Assertions.assertEquals(pHex, pBin);
    }

    @Test
    void numValRangeEquivalence() {
        var pHex = ABNF.parser("S = %x30-39");
        var pDec = ABNF.parser("S = %d48-57");
        var pBin = ABNF.parser("S = %b110000-111001");
        Assertions.assertEquals(pHex, pDec);
        Assertions.assertEquals(pHex, pBin);
    }

    @Test
    void invalidEmptySideNumValTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %b1000001-"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %b-1000001"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %d65-"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %d-65"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %x41-"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %x-41"));
    }

    @Test
    void invalidNumbersNumValTest() {
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %b1000000-65"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %d10-F"));
        Assertions.assertThrows(
                ParserCreationFailure.class,
                ()->ABNF.parser("S = %xE-G"));
    }
}
