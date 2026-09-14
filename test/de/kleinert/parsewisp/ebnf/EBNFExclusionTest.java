package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFExclusionTest {
    @Test
    void basicTest4() {
        // Allow 1, 11 and 111, but not 11..
        var p = EBNF.parser("S = (\"1\" | \"11\" | \"111\") - \"11\" ;");

        Assertions.assertEquals(PT.create("S", "1"), p.parse("1"));
        Assertions.assertTrue(p.parse("11").isFailure());
        Assertions.assertEquals(PT.create("S", "111"), p.parse("111"));
    }

    @Test
    void basicTest5() {
        // Allow 1, 11 and 111, but not 11. The number is followed by a single "a".
        var p = EBNF.parser("S = (\"1\" | \"11\" | \"111\") - \"11\" , \"a\" ;");

        Assertions.assertEquals(PT.create("S", "1", "a"), p.parse("1a"));
        Assertions.assertTrue(p.parse("11a").isFailure());
        Assertions.assertEquals(PT.create("S", "111", "a"), p.parse("111a"));
    }

    @Test
    void identifierButNotKeyword() {
        // Allow 1, 11 and 111, but not 11. The number is followed by a single "a".
        var p = EBNF.parser("""
                S = Identifier - Keyword ;
                Identifier = #"_*[a-zA-Z][a-zA-Z0-9_]*" ;
                Keyword = "int" | "char" | "void" ;
                """);
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "a")), p.parse("a"));
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "int1")), p.parse("int1"));
        Assertions.assertEquals(PT.create("S", PT.create("Identifier", "myint")), p.parse("myint"));
        Assertions.assertTrue(p.parse("int").isFailure());
        Assertions.assertTrue(p.parse("char").isFailure());
        Assertions.assertTrue(p.parse("void").isFailure());
    }
}
