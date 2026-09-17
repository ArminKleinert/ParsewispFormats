package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.abnf.ABNF;
import de.kleinert.parsewisp.abnf.ABNFPrinter;
import org.junit.jupiter.api.Test;

public class QffMsTest {
    @Test
    void test() {
        var baseParser = QffMs.baseParser();
//        System.out.println(baseParser.show());

        var g = """
                S = 'a'..'z'
                """;
//        System.out.println(QffMs.parser(g));
    }

    @Test
    void test1() {
        var p = ABNF.parser("""
                sum          = product ("+" / "-") sum   / product
                product      = power ("*" / "/") product / power
                power        = paren-or-val "^" power    / paren-or-val
                paren-or-val = "(" sum ")"               / number
                number       = ["+" / "-"] digits
                <digits>     = 1* %d48-57
                """);
//        System.out.println(p.show());
        System.out.println(new ABNFPrinter().toString(p.grammar()));
    }
}
