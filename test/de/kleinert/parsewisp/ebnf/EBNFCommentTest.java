package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EBNFCommentTest {
    @Test
    void commentBeforeFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, EBNF.parser("(* Comment *) S = \"A\" ;").parse("A"));
        Assertions.assertEquals(tree, EBNF.parser("(* Comment *)\nS = \"A\" ;").parse("A"));
        Assertions.assertEquals(tree, EBNF.parser("(* Comment *) \nS = 'A' ;").parse("A"));
    }

    @Test
    void commentAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, EBNF.parser("S = \"A\" ; (*Comment*)").parse("A"));
        Assertions.assertEquals(tree, EBNF.parser("S = \"A\" ;\n(*Comment*)").parse("A"));
        Assertions.assertEquals(tree, EBNF.parser("S = 'A' ; (*Comment*)").parse("A"));
    }

    @Test
    void commentBeforeAndAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, EBNF.parser("(*Comment*)\nS = \"A\";\n(*Comment*)").parse("A"));
        Assertions.assertEquals(tree, EBNF.parser("(*Comment*)S = \"A\";(*Comment*)").parse("A"));
    }

    @Test
    void commentBetweenRules() {
        var tree = PT.create("S", PT.create("A", "A"));
        Assertions.assertEquals(tree,
                EBNF.parser("(*Comment*)\nS = A;\n(*Comment*)\nA = \"A\";\n").parse("A"));
    }

    @Test
    void commentInComment() {
        var p =  EBNF.parser("""
                (*(* tra la la (*(*Comment*) bla bla*) bulb*)*)
                S = "A" ;
                """);
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
    }

    @Test
    void commentInRule() {
        Assertions.assertEquals(
                PT.create("S", "A"),
                EBNF.parser("""
                        S = "A" (*Comment*)
                          | "B" ;
                        """).parse("A"));
    }
}
