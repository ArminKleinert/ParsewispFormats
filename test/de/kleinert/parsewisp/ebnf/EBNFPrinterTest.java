package de.kleinert.parsewisp.ebnf;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFPrinterTest {
    @Test
    void parens() {
        var p = EBNF.parser("S = \"a\" , \"b\" | \"c\" , \"d\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        Assertions.assertEquals(
                p.grammar(), EBNF.parser(p.show()).grammar());

        var p2 = EBNF.parser("S = (\"a\" | \"b\") , (\"c\" | \"d\") ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        Assertions.assertEquals(
                p2.grammar(), EBNF.parser(p2.show()).grammar());
    }

    @Test
    void altToString() {
        var p = EBNF.parser("S = \"a\" | \"b\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void catToString() {
        var p = EBNF.parser("S = \"a\" , \"b\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void epsToString() {
        var p = EBNF.parser("S = \"\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void lookToString() {
        var p = EBNF.parser("S = &\"a\" , { \"a\" } ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text,
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void negToString() {
        var p = EBNF.parser("S = !\"a\" , \"b\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text,
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void ntToString() {
        var p = EBNF.parser("S = A ;\nA = \"a\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void optToString() {
        var p = EBNF.parser("S = [ \"a\" ] ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void regexpToString() {
        var p = EBNF.parser("S = #\"a\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void literalToString() {
        var p = EBNF.parser("S = \"a\" ;",
                new EBNF.EBNFOptions(null, null, true, false, true, null));
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }

    @Test
    void zeroOrMoreToString() {
        var opts = new EBNF.EBNFOptions(null, null, true, false, true, null);
        var p = EBNF.parser("S = [ \"a\" ] ;", opts);
        var text = p.show();
        var p2 = EBNF.parser(text);
        Assertions.assertEquals(p.grammar(), p2.grammar());
    }
}