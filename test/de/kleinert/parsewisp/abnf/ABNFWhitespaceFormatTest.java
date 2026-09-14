package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFWhitespaceFormatTest {
    @Test
    void noWhitespaceAtAll() {
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"").parse(""));
    }

    @Test
    void spaceTabNewlineBeforeFirstRule() {
        Assertions.assertEquals(PT.create("S"), ABNF.parser("         S=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("\t\tS=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("\n\nS=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("\r\nS=\"\"").parse(""));

        Assertions.assertEquals(PT.create("S"), ABNF.parser("\t    \n     S=\"\"").parse(""));
    }

    @Test
    void spaceTabNewlineAfterFirstRule() {
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"         ").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"\t\t").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"\n\n").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"\r\n").parse(""));
        Assertions.assertEquals(PT.create("S"), ABNF.parser("S=\"\"\t    \n     ").parse(""));
    }
}
