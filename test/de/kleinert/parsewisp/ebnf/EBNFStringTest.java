package de.kleinert.parsewisp.ebnf;

import org.junit.jupiter.api.Test;

public class EBNFStringTest {
    @Test
    void test1() {
        var g = EBNF.baseGrammar();
        var p = EBNF.baseParser().parse("\nS = \"a\";");
        System.out.println(p);
    }
    @Test
    void test2() {
        var p = EBNF.parser("S = \"a\", \"b\";");
        System.out.println(p.show());
        System.out.println(p.parse("ab"));
    }
}
