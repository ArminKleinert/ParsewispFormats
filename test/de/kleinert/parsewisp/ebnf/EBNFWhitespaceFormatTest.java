package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFWhitespaceFormatTest {
    @Test
    void noWhitespaceAtAll() {
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\";").parse(""));
    }

    @Test
    void spaceTabNewlineBeforeFirstRule() {
        Assertions.assertEquals(PT.create("S"), EBNF.parser("         S=\"\";").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("\t\tS=\"\";").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("\n\nS=\"\";").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("\r\nS=\"\";").parse(""));

        Assertions.assertEquals(PT.create("S"), EBNF.parser("\t    \n     S=\"\";").parse(""));
    }

    @Test
    void spaceTabNewlineAfterFirstRule() {
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\"         ;").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\"\t;\t").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\"\n;\n").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\"\r\n;").parse(""));
        Assertions.assertEquals(PT.create("S"), EBNF.parser("S=\"\";\t    \n     ").parse(""));
    }
}
