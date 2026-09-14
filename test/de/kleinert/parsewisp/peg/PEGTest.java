package de.kleinert.parsewisp.peg;

import org.junit.jupiter.api.Test;

class PEGTest {
    @Test
    void basic() {
        var p = PEG.parser("S <- A\nA <- \"B\"");
    }
}
