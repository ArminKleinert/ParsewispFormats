package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

public class PEG {
    public static @NotNull Parser parser(String grammar) {
        var options = ParserCreationOptions.getDefault();
        var res = Parsewisp.parser(baseGrammar(), options).parse(grammar);
        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
        return Parsewisp.parser(
                new Transformer().transform(res),
                ParserCreationOptions.getDefault());
    }

    public static @NotNull Grammar baseGrammar() {
        return new PEGGrammarBuilder().build();
    }

    private static class Transformer extends GrammarBuilder {
        @NotNull Grammar transform(ParseResult tree) {
            Function<List<Object>, Object> ignoreMe = (it) -> null;
            var m = new HashMap<Sym, Function<List<Object>, Object>>();
            m.put(Sym.sym("Grammar"), this::grammar);
            m.put(Sym.sym("Definition"), this::definition);
            m.put(Sym.sym("Expression"), this::expression);
            m.put(Sym.sym("Sequence"), this::sequence);
            m.put(Sym.sym("Prefix"), this::prefix);
            m.put(Sym.sym("Suffix"), this::suffix);
            m.put(Sym.sym("Primary"), this::primary);
            m.put(Sym.sym("Identifier"), this::identifier);
            m.put(Sym.sym("Literal"), this::literal);
            m.put(Sym.sym("StringLiteral"), this::stringLiteral);
            m.put(Sym.sym("RegexTerminal"), this::regexTerminal);
            m.put(Sym.sym("Dot"), this::dot);
            m.put(Sym.sym("Class"), this::classRule);
            m.put(Sym.sym("Range"), this::range);
            m.put(Sym.sym("Char"), this::charRule);
            m.put(Sym.sym("Spacing"), this::spacing);
            m.put(Sym.sym("Space"), this::space);
            return Transform.transform(tree, m, it -> this.build());
        }

        private Object grammar(List<Object> c) {
            for (Object r : c) {
                @SuppressWarnings("unchecked")
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }

        private Object definition(List<Object> c) {
            var lhs = (NonTerminal) c.get(0);
            var rhs = (Rule) c.get(2);
            return Map.entry(lhs, rhs);
        }

        private Object expression(List<Object> c) {
            return ordAlt(c.stream()
                    .filter(it -> it instanceof Rule)
                    .map(it -> (Rule) it)
                    .toList());
        }

        private Object sequence(List<Object> c) {
            //noinspection unchecked
            return cat((List<Rule>) ((Object) c));
        }

        private Object prefix(List<Object> c) {
            if (c.size() == 1)
                return c.get(0);
            return switch (((String) c.get(0)).charAt(0)) {
                case '&' -> look((Rule) c.get(1));
                case '!' -> neg((Rule) c.get(1));
                default -> throw new IllegalArgumentException("Invalid PEG prefix rule: " + c);
            };
        }

        private Object suffix(List<Object> c) {
            var r = (Rule) c.get(0);
            if (c.size() == 1)
                return r;
            //noinspection EnhancedSwitchMigration
            switch (((String) c.get(1)).charAt(0)) {
                case '?':
                    return opt(r);
                case '*':
                    return zeroOrMore(r);
                case '+':
                    return onceOrMore(r);
                default:
                    throw new IllegalArgumentException("Invalid PEG postfix: " + c);
            }
        }

        private Object primary(List<Object> c) {
            if (c.size() == 3) // Group rule: "(" expression ")"
                return c.get(1);
            return c.get(0);
        }

        private Object identifier(List<Object> c) {
            return nt(c.get(0).toString());
        }

        private Object literal(List<Object> c) {
            return c.get(0);
        }

        private final @NotNull StrParser strParser = new StrParser();

        private Object stringLiteral(List<Object> c) {
            return string(strParser.processString((String) c.get(0)));
        }

        private Object regexTerminal(List<Object> c) {
            return regex(strParser.processRegexp(c.get(0).toString()));
        }

        private Object dot(List<Object> c) {
            return regex(".");
        }

        private Object classRule(List<Object> c) {
            System.out.println("class: "+c);
            //noinspection unchecked
            return cat((List<Rule>) ((Object) c.subList(1, c.size() - 1)));
        }

        private Object range(List<Object> c) {
            System.out.println("range: "+c);
            if (c.size() == 3) return numVal((Integer) c.get(0), (Integer) c.get(2));
            return numVal((Integer) c.get(0));
        }

        private Object charRule(List<Object> c) {
            var s = (String) c.get(0);
            if (s.charAt(0) == '\\') {
                if (Character.isDigit(s.charAt(1)))
                    return Integer.parseInt(s.substring(1), 8);
                if (s.charAt(1) == 'u')
                    return Integer.parseInt(s.substring(2), 16);
                //noinspection EnhancedSwitchMigration
                switch (s.charAt(1)) {
                    case 'n':
                        return (int) '\n';
                    case 'r':
                        return (int) '\r';
                    case 't':
                        return (int) '\t';
                    case '´':
                        return (int) '´';
                    case '"':
                        return (int) '"';
                    case '[':
                        return (int) '[';
                    case ']':
                        return (int) ']';
                    case '\\':
                        return (int) '\\';
                }
            }
            return s.codePointAt(0);
        }

        private Object spacing(List<Object> c) {
            return null;
        }

        private Object space(List<Object> c) {
            return null;
        }
    }

    private static class PEGGrammarBuilder extends GrammarBuilder {
        @Override
        protected void make() {
            var spacing = hide(nt("Spacing"));

            addProduction("Grammar",
                    cat(spacing, onceOrMore(nt("Definition")), eof()));

            addProduction("Definition",
                    cat(nt("Identifier"), spacing, opt(cat(string("<-"), spacing)), nt("Expression")));

            addProduction("Expression",
                    cat(nt("Sequence"), zeroOrMore(cat(string("/"), spacing, nt("Sequence")))));

            addProduction("Sequence",
                    zeroOrMore(nt("Prefix")));

            addProduction("Prefix",
                    cat(alt(string("&"), string("!"), eps()), nt("Suffix")));

            addProduction("Suffix",
                    cat(nt("Primary"), spacing, alt(string("?"), string("*"), string("+"), eps())));

            addProduction("Primary", alt(
                    nt("Identifier"),
                    cat(string("("), spacing, nt("Expression"), spacing, string(")")),
                    nt("Literal"),
                    nt("Class"),
                    nt("Dot")));

            addProduction("Identifier",
                    regex("[a-zA-Z_][a-zA-Z_0-9]*"));

            addProduction("Literal", alt(
                    nt("StringLiteral"),
                    nt("RegexTerminal")));

            addProduction("StringLiteral", alt(
                    regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String"),
                    regex("´[^´\\\\]*(?:\\\\.[^´\\\\]*)*´(?x) # String")));

            addProduction("RegexTerminal", alt(
                    regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex")));

            addProduction("Dot",
                    string("."));

            addProduction("Class",
                    cat(string("["), zeroOrMore(nt("Range")), string("]")));

            addProduction("Range",
                    cat(nt("Char"), opt(cat(string("-"), nt("Char")))));

            // \\([nrt´"\[\]\\]|[0-2][0-7]{2}|[0-7][0-7]?|u[0-9a-fA-F]{4})|[^\\\s\-]
            // Matches any of the following:
            //   \n \r \t \´ \" \[ \] \\
            //   \ followed by [0-2][0-7][0-7]
            //   \ followed by [0-7][0-7]
            //   \ followed by [0-7]
            //   Any char that is not \, whitespace, or minus
            addProduction("Char",
                    regex(Pattern.compile("\\\\([nrt´\"\\[\\]\\\\]|[0-2][0-7]{2}|[0-7][0-7]?|u[0-9a-fA-F]{4})|[^\\\\\\s\\-](?x) # PEG char")));

            addProduction("Spacing", zeroOrMore(alt(
                    nt("Space"),
                    cat(string(";"), regex("[\\u0020\\u0009\\S]+(?x) # Comment until newline/eof"), alt(string("\n"), eof())))));

            addProduction("Space",
                    regex("[\\u0020\\u0009\\r\\n]+"));
        }
    }
}
