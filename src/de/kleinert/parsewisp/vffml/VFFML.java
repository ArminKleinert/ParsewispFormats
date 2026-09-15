package de.kleinert.parsewisp.vffml;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * "Very Free-Form Meta-language"
 */
public class VFFML {

    public static Parser parser(String grammar) {
        var p = baseParser();
        return Parsewisp.parser(new Transformer().transform(p.parse(grammar)), ParserCreationOptions.getDefault());
    }

    public static Parser baseParser() {
        String g;
        try {
            g = Files.readString(Path.of("vffml.g"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Parsewisp.parser(g);
    }

    private static class Transformer extends GrammarBuilder {
        public Grammar transform(ParseResult tree) {
            Map<Sym, Function<List<Object>, Object>> m = new HashMap<>();
            m.put(Sym.sym("syntax"), this::syntax);
            m.put(Sym.sym("rule"), this::rule);
            m.put(Sym.sym("alternation"), this::alternation);
            m.put(Sym.sym("concatenation"), this::concatenation);
            m.put(Sym.sym("suffix"), this::suffix);
            m.put(Sym.sym("primary"), this::primary);
            m.put(Sym.sym("nt"), this::nt);
            m.put(Sym.sym("terminal"), this::terminal);
            m.put(Sym.sym("stringTerminal"), this::stringTerminal);
            m.put(Sym.sym("regexTerminal"), this::regexTerminal);
            m.put(Sym.sym("quasiRegexTerminal"), this::quasiRegexTerminal);
            m.put(Sym.sym("comment"), this::comment);
            m.put(Sym.sym("WS"), this::WS);
            return Transform.transform(tree, m, ignore -> this.build());
        }

        private Object syntax(List<Object> c) {
            for (Object r : c) {
                //noinspection unchecked
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }

        private Object rule(List<Object> c) {
            var lhs = c.get(0);
            var rhs = c.get(2);
            return Map.entry(lhs, rhs);
        }

        private Object alternation(List<Object> c) {
            return alt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private Object concatenation(List<Object> c) {
            //noinspection unchecked
            return cat((List<Rule>) ((Object) c));
        }

        private Object suffix(List<Object> c) {
            if (c.size() == 1)
                return c.get(0);
            switch (((String) c.get(1)).charAt(0)) {
                case '?':
                    opt((Rule) c.get(0));
                case '*':
                    zeroOrMore((Rule) c.get(0));
                case '+':
                    onceOrMore((Rule) c.get(0));
                default:
                    throw new IllegalArgumentException();
            }
        }

        private Object primary(List<Object> c) {
            var first = c.get(0);
            if (first instanceof String) {
                switch (((String) first).charAt(0)) {
                    case '[':
                        return opt((Rule) c.get(1));
                    case '{':
                        return zeroOrMore((Rule) c.get(1));
                    case '(':
                        return c.get(1);
                }
            }
            return first;
        }

        private Object nt(List<Object> c) {
            return nt(c.get(0).toString());
        }

        private Object terminal(List<Object> c) {
            return c.get(0);
        }

        private final @NotNull StrParser strParser = new StrParser();

        private Object stringTerminal(List<Object> c) {
            return string(strParser.processString("\"" + c.get(1) + "\""));
        }

        private Object regexTerminal(List<Object> c) {
            return regex(strParser.processString(c.get(0).toString()));
        }

        private Object quasiRegexTerminal(List<Object> c) {
            return regex(strParser.processString(" " + c.stream().map(it->(String)it).collect(Collectors.joining())+" "));
        }

        private Object comment(List<Object> c) {
            return null;
        }

        private Object WS(List<Object> c) {
            return null;
        }
    }
}
