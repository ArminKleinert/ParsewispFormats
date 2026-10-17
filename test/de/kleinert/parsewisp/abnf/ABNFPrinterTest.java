package de.kleinert.parsewisp.abnf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFPrinterTest {
    @Test
    void parens() {
        var p = ABNF.parser("S = \"a\" \"b\" / \"c\" \"d\"",
                new ABNF.ABNFOptions(null, null, true));
        Assertions.assertEquals(
                p.grammar(), ABNF.parser(p.show()).grammar());

        var p2 = ABNF.parser("S = (\"a\" / \"b\") (\"c\" / \"d\")",
                new ABNF.ABNFOptions(null, null, true));
        Assertions.assertEquals(
                p2.grammar(), ABNF.parser(p2.show()).grammar());
    }

    @Test
    void altToString() {
        var p = ABNF.parser("S = \"a\" / \"b\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void catToString() {
        var p = ABNF.parser("S = \"a\" \"b\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void epsToString() {
        var p = ABNF.parser("S = \"\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void lookToString() {
        var p = ABNF.parser("S = &\"a\" *\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text,
                new ABNF.ABNFOptions(null, null, true));
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void negToString() {
        var p = ABNF.parser("S = !\"a\" \"b\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text,
                new ABNF.ABNFOptions(null, null, true));
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void ntToString() {
        var p = ABNF.parser("S = A\nA = \"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void onceOrMoreToString() {
        var p = ABNF.parser("S = 1*\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void optToString() {
        var p = ABNF.parser("S = [ \"a\" ]",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void ordToString() {
        var p = ABNF.parser("S = \"a\" / \"b\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void regexpToString() {
        var p = ABNF.parser("S = #\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void literalToString() {
        var p = ABNF.parser("S = \"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void valRangeToString() {
        var p = ABNF.parser("S = %x41-42 %d65",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void repToString() {
        var p = ABNF.parser("S = 2*\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void rep2ToString() {
        var p = ABNF.parser("S = 2*4\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void rep3ToString() {
        var p = ABNF.parser("S = *4\"a\"",
                new ABNF.ABNFOptions(null, null, true));
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void zeroOrMoreToString() {
        var opts = new ABNF.ABNFOptions(null, null, true);
        var p = ABNF.parser("S = *\"a\"", opts);
        var text = p.show();
        var p2 = ABNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }
}