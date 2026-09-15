package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.ebnf.EBNF;
import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PEGExampleTest {
    @Test
    void test() {
        var p = EBNF.parser("""
                S = number , ("+" / "-") , S / number ;
                number = [ "+" / "-" ] , digits ;
                <digits> = #'[0-9]' , { #'[0-9]' } ;
                """);
        System.out.println(p.parse("+123"));
    }

    @Test
    void exampleTest() {
        var g = """
                sum          <- product ("+" / "-") sum   / product
                product      <- power ("*" / "/") product / power
                power        <- paren_or_val "^" power     / paren_or_val
                paren_or_val <- "(" sum ")"                / number
                number       <- ("+" / "-")? digits
                <digits>     <- ("1" / "2" / "3")+
                """;
        var p = PEG.parser(g);

        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("paren_or_val",
                                                PT.create("number", "1", "2", "3"))))),
                p.parse("123")
        );
        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("paren_or_val",
                                                PT.create("number", "1")))),
                        "+",
                        PT.create("sum",
                                PT.create("product",
                                        PT.create("power",
                                                PT.create("paren_or_val",
                                                        PT.create("number", "2")))))),
                p.parse("1+2")
        );
    }
}
