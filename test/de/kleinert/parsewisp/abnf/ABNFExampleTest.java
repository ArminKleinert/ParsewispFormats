package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ABNFExampleTest {
    @Test
    void exampleTest() {
        var g = """
                sum          = product ("+" / "-") sum   / product
                product      = power ("*" / "/") product / power
                power        = paren-or-val "^" power    / paren-or-val
                paren-or-val = "(" sum ")"               / number
                number       = ["+" / "-"] 1* %d48-57
                """;
        var p = ABNF.parser(g);
        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("paren-or-val",
                                                PT.create("number", "1", "2", "3"))))),
                p.parse("123")
        );
        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("paren-or-val",
                                                PT.create("number", "1")))),
                        "+",
                        PT.create("sum",
                                PT.create("product",
                                        PT.create("power",
                                                PT.create("paren-or-val",
                                                        PT.create("number", "2")))))),
                p.parse("1+2")
        );
    }
}
