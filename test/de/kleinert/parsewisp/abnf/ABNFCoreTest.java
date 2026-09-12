package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFCoreTest {
    @Test
    void coreTest() {
        Assertions.assertDoesNotThrow(() -> ABNF.parser(
                "S = ALPHA BIT CHAR CR CRLF CTL DIGIT DQUOTE HEXDIG HTAB LF LWSP OCTET SP VCHAR WSP"));

        var text = "a";
        Assertions.assertEquals(
                PT.create("S", PT.create("ALPHA", text)),
                ABNF.parser("S = ALPHA").parse(text));

        text = "0";
        Assertions.assertEquals(
                PT.create("S", PT.create("BIT", text)),
                ABNF.parser("S = BIT").parse(text));

        text = "b";
        Assertions.assertEquals(
                PT.create("S", PT.create("CHAR", text)),
                ABNF.parser("S = CHAR").parse(text));

        text = "\r";
        Assertions.assertEquals(
                PT.create("S", PT.create("CR", text)),
                ABNF.parser("S = CR").parse(text));

        text = "\r\n";
        Assertions.assertEquals(
                PT.create("S", PT.create("CRLF", text)),
                ABNF.parser("S = CRLF").parse(text));

        text = "\u0001";
        Assertions.assertEquals(
                PT.create("S", PT.create("CTL", text)),
                ABNF.parser("S = CTL").parse(text));

        text = "5";
        Assertions.assertEquals(
                PT.create("S", PT.create("DIGIT", text)),
                ABNF.parser("S = DIGIT").parse(text));

        text = "\"";
        Assertions.assertEquals(
                PT.create("S", PT.create("DQUOTE", text)),
                ABNF.parser("S = DQUOTE").parse(text));

        text = "F";
        Assertions.assertEquals(
                PT.create("S", PT.create("HEXDIG", text)),
                ABNF.parser("S = HEXDIG").parse(text));

        text = "\t";
        Assertions.assertEquals(
                PT.create("S", PT.create("HTAB", text)),
                ABNF.parser("S = HTAB").parse(text));

        text = "\n";
        Assertions.assertEquals(
                PT.create("S", PT.create("LF", text)),
                ABNF.parser("S = LF").parse(text));

        text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("LWSP", text)),
                ABNF.parser("S = LWSP").parse(text));

        text = "A";
        Assertions.assertEquals(
                PT.create("S", PT.create("OCTET", text)),
                ABNF.parser("S = OCTET").parse(text));

        text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("SP", text)),
                ABNF.parser("S = SP").parse(text));

        text = "!";
        Assertions.assertEquals(
                PT.create("S", PT.create("VCHAR", text)),
                ABNF.parser("S = VCHAR").parse(text));

        text = " ";
        Assertions.assertEquals(
                PT.create("S", PT.create("WSP", text)),
                ABNF.parser("S = WSP").parse(text));
    }
}
