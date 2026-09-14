package de.kleinert.parsewisp.peg;

import org.junit.jupiter.api.Test;

public class PEGTest {
    @Test
    void basic() {
        var p = PEG.parser("S <- [a-z]");
        System.out.println(p.parse("g"));
    }
}
