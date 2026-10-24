package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.ebnf.EBNF;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsWhitespaceFormatTest {
    @Test
    void noWhitespaceAtAll() {
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"").parse(""));
    }

    @Test
    void spaceTabNewlineBeforeFirstRule() {
        Assertions.assertEquals(PT.create("S"), QffMs.parser("         S=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("\t\tS=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("\n\nS=\"\"").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("\r\nS=\"\"").parse(""));

        Assertions.assertEquals(PT.create("S"), QffMs.parser("\t    \n     S=\"\"").parse(""));
    }

    @Test
    void spaceTabNewlineAfterFirstRule() {
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"         ").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"\t\t").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"\n\n").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"\r\n").parse(""));
        Assertions.assertEquals(PT.create("S"), QffMs.parser("S=\"\"\t    \n     ").parse(""));
    }
}
