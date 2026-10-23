package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class QffMsOptionalTest {
    @Test
    void basicOptionTest() {
        var p = QffMs.parser("S = \"A\"?");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
    }

    @Test
    void optionInCatTest() {
        var p = QffMs.parser("S = \"A\" \"B\"? \"C\"");
        Assertions.assertEquals(PT.create("S", "A", "C"), p.parse("AC"));
        Assertions.assertEquals(PT.create("S", "A", "B", "C"), p.parse("ABC"));
    }

    @Test
    void optionAroundRepetitionTest() {
        var p = QffMs.parser("S = ( \"A\" \"A\"* )?");
        Assertions.assertEquals(PT.create("S"), p.parse(""));
        Assertions.assertEquals(PT.create("S", "A"), p.parse("A"));
        Assertions.assertEquals(PT.create("S", "A", "A", "A"), p.parse("AAA"));
    }
}
