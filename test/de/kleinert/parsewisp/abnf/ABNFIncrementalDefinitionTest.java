package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ABNFIncrementalDefinitionTest {
    @Test
    void incrementalExtensionTest() {
        var p = ABNF.parser("""
                S =  "a" S
                S =/ "b" S
                S =/ ""
                """);
        var epsTree = PT.create("S");
        Assertions.assertEquals(
                PT.create("S", "a", epsTree),
                p.parse("a")
        );
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("S", "b", epsTree)),
                p.parse("ab")
        );
        Assertions.assertEquals(
                PT.create("S", "a", PT.create("S", "a", epsTree)),
                p.parse("aa")
        );
    }
}
