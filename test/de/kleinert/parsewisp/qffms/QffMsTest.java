package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QffMsTest {
    @Test
    void test() {
        var baseParser = QffMs.baseParser();
//        System.out.println(baseParser.show());

        var g = """
                S = ('a'..'\\u2626'){3,4} "\\u2626"
                """;
        System.out.println(QffMs.parser(g).show());
    }

    @Test
    void test1() {
        var p = QffMs.parser("""
                sum          = product ("+" | "-") sum   | product
                product      = power ("*" | "/") product | power
                power        = paren-or-val "^" power    | paren-or-val
                paren-or-val = "(" sum ")"               | number
                number       = ("+" | "-")? digits
                digits       = ('0'..'9')+
                """);
        var res = p.parse("1+2*3^4+-55");
        var pt = PT.create("sum",
                PT.create("product",
                        PT.create("power",
                                PT.create("paren-or-val",
                                        PT.create("number", PT.create("digits", "1"))))),
                "+",
                PT.create("sum",
                        PT.create("product",
                                PT.create("power",
                                        PT.create("paren-or-val",
                                                PT.create("number",
                                                        PT.create("digits", "2")))),
                                "*",
                                PT.create("product",
                                        PT.create("power",
                                                PT.create("paren-or-val",
                                                        PT.create("number",
                                                                PT.create("digits", "3"))),
                                                "^",
                                                PT.create("power",
                                                        PT.create("paren-or-val",
                                                                PT.create("number",
                                                                        PT.create("digits", "4"))))))),
                        "+",
                        PT.create("sum",
                                PT.create("product",
                                        PT.create("power",
                                                PT.create("paren-or-val",
                                                        PT.create("number",
                                                                "-",
                                                                PT.create("digits", "5", "5"))))))));
        //Viztool.dumpParseTree("parse-tree", res.castToParseSuccess());
        Assertions.assertEquals(pt, res);
    }
}
