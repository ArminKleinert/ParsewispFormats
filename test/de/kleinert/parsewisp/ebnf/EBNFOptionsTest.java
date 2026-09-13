package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFOptionsTest {
    @Test
    void doNotRequireSeparator() {
        var opts = new EBNF.EBNFOptions(null, null, true, false, false, null);
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("A", "b")),
                EBNF.parser("S = \"a\" A\nA=\"b\"", opts).parse("ab"));
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("A", "b")),
                EBNF.parser("S = \"a\" A A=\"b\"", opts).parse("ab"));
    }
    @Test
    void alternativeRepresentationOptional() {
        var opts = new EBNF.EBNFOptions(null, null, true, true, true, null);

        var pNormal = EBNF.parser("S = [ \"A\" ] ;");
        Assertions.assertEquals(PT.create("S"), pNormal.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), pNormal.parse("A"));

        var pAlternative = EBNF.parser("S = (/ \"A\" /) ;", opts);
        Assertions.assertEquals(PT.create("S"), pAlternative.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), pAlternative.parse("A"));
    }
    @Test
    void alternativeRepetitionTest() {
        var opts = new EBNF.EBNFOptions(null, null, true, true, true, null);

        var pNormal = EBNF.parser("S = { \"a\" } ;");
        Assertions.assertEquals(PT.create("S"), pNormal.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), pNormal.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), pNormal.parse("aa"));

        var pAlternative = EBNF.parser("S = (: \"a\" :) ;", opts);
        Assertions.assertEquals(PT.create("S"), pAlternative.parse(""));
        Assertions.assertEquals(PT.create("S", "a"), pAlternative.parse("a"));
        Assertions.assertEquals(PT.create("S", "a", "a"), pAlternative.parse("aa"));
    }
    @Test
    void alternativeAlternativeTest() {
        var opts = new EBNF.EBNFOptions(null, null, true, true, true, null);

        var pNormal = EBNF.parser("S = \"cat\" | \"dog\" ;");
        Assertions.assertEquals(PT.create("S", "cat"), pNormal.parse("cat"));
        Assertions.assertEquals(PT.create("S", "dog"), pNormal.parse("dog"));

        var pAlternative = EBNF.parser("S = \"cat\" ! \"dog\" ;", opts);
        Assertions.assertEquals(PT.create("S", "cat"), pAlternative.parse("cat"));
        Assertions.assertEquals(PT.create("S", "dog"), pAlternative.parse("dog"));
    }
}
