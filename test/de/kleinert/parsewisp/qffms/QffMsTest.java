package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.testutil.PT;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QffMsTest {
    @Test
    void test() {
        var g = """
                S = '\\u2626'{2,4} "\\u2626"
                """;
        var p = QffMs.parser(g);
        Assertions.assertTrue(p.parse("").isFailure());
        Assertions.assertTrue(p.parse("\u2626").isFailure());
        Assertions.assertTrue(p.parse("\u2626\u2626").isFailure());

        Assertions.assertEquals(
                PT.create("S", "\u2626", "\u2626", "\u2626"),
                p.parse("\u2626\u2626\u2626"));
        Assertions.assertEquals(
                PT.create("S", "\u2626", "\u2626", "\u2626", "\u2626"),
                p.parse("\u2626\u2626\u2626\u2626"));
        Assertions.assertEquals(
                PT.create("S", "\u2626", "\u2626", "\u2626", "\u2626", "\u2626"),
                p.parse("\u2626\u2626\u2626\u2626\u2626"));

        Assertions.assertTrue(p.parse("\u2626\u2626\u2626\u2626\u2626\u2626").isFailure());
    }

    @Test
    void testBigChar() {
        var g2 = """
                S = ('\\uD83D\\uDD46'..'\\uD83D\\uDD47')
                """;
        var p = QffMs.parser(g2);
        System.out.println(p.show());
        Assertions.assertEquals(PT.create("S", "\uD83D\uDD46"), p.parse("\uD83D\uDD46"));
        Assertions.assertEquals(PT.create("S", "\uD83D\uDD47"), p.parse("\uD83D\uDD47"));

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
