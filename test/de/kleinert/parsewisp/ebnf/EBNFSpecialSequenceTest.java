package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

class EBNFSpecialSequenceTest {
    @Test
    void newlineAsSpecialSequenceTest() {
        var p = EBNF.parser("S = ?newline? ;",
                new EBNF.EBNFOptions(
                        null, null, false, false, true,
                        Map.of("newline", s -> s.startsWith("\n") ? Optional.of("\n") : Optional.empty())));
        Assertions.assertEquals(PT.create("S", "\n"), p.parse("\n"));
    }

    @Test
    void newlineAsSpecialSequenceInSequenceTest() {
        var p = EBNF.parser("S = 'line 1' , ?newline? , 'line 2' ;",
                new EBNF.EBNFOptions(
                        null, null, false, false, true,
                        Map.of("newline", s -> s.startsWith("\n") ? Optional.of("\n") : Optional.empty())));
        Assertions.assertEquals(
                PT.create("S", "line 1", "\n", "line 2"),
                p.parse("line 1\nline 2"));
    }
}
