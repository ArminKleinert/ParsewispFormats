package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFCommentTest {
    @Test
    void commentBeforeFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = \"A\"").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = %d65").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = %d66 / %d65").parse("A"));
    }

    @Test
    void commentAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, ABNF.parser("S = \"A\"\n;Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("S = %d65\n;Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("S = %d66 / %d65\n;Comment").parse("A"));
    }

    @Test
    void commentAfterRuleInSameLine() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, ABNF.parser("S = \"A\";Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("S = %d65;Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("S = %d66 / %d65;Comment").parse("A"));
    }

    @Test
    void commentBeforeAndAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = \"A\"\n;Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = %d65\n;Comment").parse("A"));
        Assertions.assertEquals(tree, ABNF.parser("; Comment\nS = %d66 / %d65\n;Comment").parse("A"));
    }

    @Test
    void commentBetweenRules() {
        var tree = PT.create("S", PT.create("A", "A"));
        Assertions.assertEquals(tree,
                ABNF.parser("; Comment\nS = A\n; Comment\nA = \"A\"\n; Comment").parse("A"));
        Assertions.assertEquals(tree,
                ABNF.parser("; Comment\nS = A\n; Comment\nA = %d65\n; Comment").parse("A"));
        Assertions.assertEquals(tree,
                ABNF.parser("; Comment\nS = A\n; Comment\nA = %d66 / %d65\n; Comment").parse("A"));
    }

    @Test
    void commentInComment() {
        var p = ABNF.parser("""
                ; Comment ; Nested comment
                S = "A"
                """);
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
    }

    @Test
    void commentInRule() {
        Assertions.assertEquals(
                PT.create("S", "A"),
                ABNF.parser("""
                        S = %d65 ; Comment
                          / %d66
                        """).parse("A"));
        Assertions.assertEquals(
                PT.create("S", "A"),
                ABNF.parser("""
                        ; Comment
                        S =  %d65 ; Comment
                        S =/ %d66 ; Comment
                        """).parse("A"));
    }
}
