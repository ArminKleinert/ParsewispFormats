package de.kleinert.parsewisp.qffms;

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
//        var p = ABNF.parser("""
//                sum          = product ("+" / "-") sum   / product
//                product      = power ("*" / "/") product / power
//                power        = paren-or-val "^" power    / paren-or-val
//                paren-or-val = "(" sum ")"               / number
//                number       = ["+" / "-"] digits
//                <digits>     = 1* %d48-57
//                """);
////        System.out.println(p.show());
//        System.out.println(new ABNFPrinter().toString(p.grammar()));
    }
}
