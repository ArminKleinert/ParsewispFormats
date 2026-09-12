package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFCoreTest {
    @Test
    void coreAvailableTest() {
        Assertions.assertDoesNotThrow(() -> ABNF.parser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP"));
    }

    @Test
    void coreTestALPHA() {
        var text = "a";
        Assertions.assertEquals(
                PT.create("S", PT.create("ALPHA", text)),
                ABNF.parser("S = ALPHA").parse(text));
    }

    @Test
    void coreTestBIT() {
        var text = "0";
        Assertions.assertEquals(
                PT.create("S", PT.create("BIT", text)),
                ABNF.parser("S = BIT").parse(text));
    }

    @Test
    void coreTestCHAR() {
        var text = "b";
        Assertions.assertEquals(
                PT.create("S", PT.create("CHAR", text)),
                ABNF.parser("S = CHAR").parse(text));
    }

    @Test
    void coreTestCR() {
        var text = "\r";
        Assertions.assertEquals(
                PT.create("S", PT.create("CR", text)),
                ABNF.parser("S = CR").parse(text));
    }

    @Test
    void coreTestCRLF() {
        var text = "\r\n";
        Assertions.assertEquals(
                PT.create("S", PT.create("CRLF", text)),
                ABNF.parser("S = CRLF").parse(text));
    }

    @Test
    void coreTestCTL() {
        var text = "\u0001";
        Assertions.assertEquals(
                PT.create("S", PT.create("CTL", text)),
                ABNF.parser("S = CTL").parse(text));
    }

    @Test
    void coreTestDIGIT() {
        var text = "5";
        Assertions.assertEquals(
                PT.create("S", PT.create("DIGIT", text)),
                ABNF.parser("S = DIGIT").parse(text));
    }

    @Test
    void coreTestDQUOTE() {
        var text = "\"";
        Assertions.assertEquals(
                PT.create("S", PT.create("DQUOTE", text)),
                ABNF.parser("S = DQUOTE").parse(text));
    }

    @Test
    void coreTestHEXDIG() {
        var text = "F";
        Assertions.assertEquals(
                PT.create("S", PT.create("HEXDIG", text)),
                ABNF.parser("S = HEXDIG").parse(text));
    }

    @Test
    void coreTestHTAB() {
        var text = "\t";
        Assertions.assertEquals(
                PT.create("S", PT.create("HTAB", text)),
                ABNF.parser("S = HTAB").parse(text));
    }

    @Test
    void coreTestLF() {
        var text = "\n";
        Assertions.assertEquals(
                PT.create("S", PT.create("LF", text)),
                ABNF.parser("S = LF").parse(text));
    }

    @Test
    void coreTestLWSP() {
        var text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("LWSP", text)),
                ABNF.parser("S = LWSP").parse(text));
    }

    @Test
    void coreTestOCTET() {
        var text = "A";
        Assertions.assertEquals(
                PT.create("S", PT.create("OCTET", text)),
                ABNF.parser("S = OCTET").parse(text));
    }

    @Test
    void coreTestSP() {
        var text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("SP", text)),
                ABNF.parser("S = SP").parse(text));
    }

    @Test
    void coreTestVCHAR() {
        var text = "!";
        Assertions.assertEquals(
                PT.create("S", PT.create("VCHAR", text)),
                ABNF.parser("S = VCHAR").parse(text));
    }

    @Test
    void coreTestWSP() {
        var text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("WSP", text)),
                ABNF.parser("S = WSP").parse(text));
    }
}
