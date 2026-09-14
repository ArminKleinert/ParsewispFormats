package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGCommentTest {
    @Test
    void commentBeforeFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- \"A\"").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- ´A´").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- ´B´ / ´A´").parse("A"));
    }

    @Test
    void commentAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, PEG.parser("S <- \"A\"\n#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("S <- ´A´\n#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("S <- ´B´ / ´A´\n#Comment").parse("A"));
    }

    @Test
    void commentAfterRuleInSameLine() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, PEG.parser("S <- \"A\"#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("S <- ´A´#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("S <- ´B´ / ´A´#Comment").parse("A"));
    }

    @Test
    void commentBeforeAndAfterFirstRule() {
        var tree = PT.create("S", "A");
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- \"A\"\n#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- ´A´\n#Comment").parse("A"));
        Assertions.assertEquals(tree, PEG.parser("# Comment\nS <- ´B´ / ´A´\n#Comment").parse("A"));
    }

    @Test
    void commentBetweenRules() {
        var tree = PT.create("S", PT.create("A", "A"));
        Assertions.assertEquals(tree,
                PEG.parser("# Comment\nS <- A\n# Comment\nA <- \"A\"\n# Comment").parse("A"));
        Assertions.assertEquals(tree,
                PEG.parser("# Comment\nS <- A\n# Comment\nA <- ´A´\n# Comment").parse("A"));
        Assertions.assertEquals(tree,
                PEG.parser("# Comment\nS <- A\n# Comment\nA <- ´B´ / ´A´\n# Comment").parse("A"));
    }

    @Test
    void commentInComment() {
        var p = PEG.parser("""
                # Comment # Nested comment
                S <- "A"
                """);
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
    }

    @Test
    void commentInRule() {
        Assertions.assertEquals(
                PT.create("S", "A"),
                PEG.parser("""
                        S <- ´A´ # Comment
                          /  ´B´
                        """).parse("A"));
        Assertions.assertEquals(
                PT.create("S", "A"),
                PEG.parser("""
                        # Comment
                        S <- ´A´ # Comment
                          /  ´B´ # Comment
                        """).parse("A"));
    }
}
