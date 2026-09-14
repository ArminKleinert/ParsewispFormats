package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EBNFExampleTest {
    @Test
    void exampleTest() {
        var g = """
                sum        = product , ("+" | "-") , sum    | product ;
                product    = power , ("*" | "|") , product  | power ;
                power      = parenOrVal , "^" , power       | parenOrVal ;
                parenOrVal = "(" , sum , ")"                | number ;
                number     = ["+" | "-"] , digit , { digit } ;
                <digit>    = "0" | "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" ;
                """;
        var p = EBNF.parser(g);
        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("parenOrVal",
                                                PT.create("number", "1", "2", "3"))))),
                p.parse("123")
        );
        Assertions.assertEquals(
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("parenOrVal",
                                                PT.create("number", "1")))),
                        "+",
                        PT.create("sum",
                                PT.create("product",
                                        PT.create("power",
                                                PT.create("parenOrVal",
                                                        PT.create("number", "2")))))),
                p.parse("1+2")
        );
    }
}
