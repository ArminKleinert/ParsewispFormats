package de.kleinert.parsewisp.vffml;

import de.kleinert.parsewisp.Parsewisp;
import org.junit.jupiter.api.Test;

class VFFMLTest {
    @Test void test1 () {
        var p = VFFML.baseParser();
        System.out.println(p.show());
        System.out.println(p.parse("S = \"abc\""));
        System.out.println(p.parse("S = abc"));
        System.out.println(p.parse("S = [\\u0041-\\u005A]+[a]?"));
    }
    @Test void test2 () {
        var p = VFFML.parser("S = [a-zA-Z0-9]*");
        System.out.println(p.show());
        System.out.println(p.parse(""));
        System.out.println(p.parse("fF31"));
    }
    @Test void test3 () {
        var p = VFFML.parser("S = ´[a-zA-Z0-9]+´");
        System.out.println(p.show());
        System.out.println(p.parse("fF31"));
    }
}