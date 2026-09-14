package de.kleinert.parsewisp.peg;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/// [https://bford.info/pub/lang/peg.pdf](https://bford.info/pub/lang/peg.pdf)
public class PEG {
    public static @NotNull Parser parser(final @NotNull String grammar) {
        return parser(grammar, PEGOptions.getDefault());
    }

    public static @NotNull Parser parser(final @NotNull String grammar, final @NotNull PEGOptions options) {
        var res = Parsewisp.parser(baseGrammar(), ParserCreationOptions.getDefault()).parse(grammar);
        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
        return Parsewisp.parser(
                new Transformer(options).transform(res),
                options);
    }

    public static @NotNull Grammar baseGrammar() {
        return new PEGGrammarBuilder().build();
    }

    public static final class PEGOptions extends ParserCreationOptions {
        final boolean tryTurnCharClassesIntoPatterns;

        public PEGOptions(final @Nullable Parser whitespaceParser, final @Nullable Sym startProduction, final boolean tryTurnCharClassesIntoPatterns) {
            super(whitespaceParser, startProduction, RedefinitionOption.defaultOption, true);
            this.tryTurnCharClassesIntoPatterns = tryTurnCharClassesIntoPatterns;
        }

        public static @NotNull PEGOptions getDefault() {
            return new PEGOptions(null, null, false);
        }
    }

    private static final class Transformer extends GrammarBuilder {
        private final @NotNull PEGOptions options;

        public Transformer(final @NotNull PEGOptions options) {
            this.options = options;
        }

        @NotNull Grammar transform(final @NotNull ParseResult tree) {
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
            m.put(Sym.sym("HideNt"), this::hideNt);
            m.put(Sym.sym("Hide"), this::hide);
            m.put(Sym.sym("Literal"), this::literal);
            m.put(Sym.sym("StringLiteral"), this::stringLiteral);
            m.put(Sym.sym("RegexTerminal"), this::regexTerminal);
            m.put(Sym.sym("Dot"), this::dot);
            m.put(Sym.sym("Class"), this::classRule);
            m.put(Sym.sym("Range"), this::range);
            m.put(Sym.sym("Char"), this::charRule);
            m.put(Sym.sym("Spacing"), ignoreMe);
            m.put(Sym.sym("Space"), ignoreMe);
            return Transform.transform(tree, m, it -> this.build());
        }

        private @Nullable Object grammar(final @NotNull List<Object> c) {
            for (Object r : c) {
                @SuppressWarnings("unchecked")
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }

        private @NotNull Map.Entry<NonTerminal, Rule> definition(final @NotNull List<Object> c) {
            var lhs = (NonTerminal) c.get(0);
            var rhs = (Rule) c.get(2);
            return Map.entry(lhs, rhs);
        }

        private @NotNull Rule expression(final @NotNull List<Object> c) {
            return ordAlt(c.stream()
                    .filter(it -> it instanceof Rule)
                    .map(it -> (Rule) it)
                    .toList());
        }

        private @NotNull Rule sequence(final @NotNull List<Object> c) {
            //noinspection unchecked
            return cat((List<Rule>) ((Object) c));
        }

        private @NotNull Rule prefix(final @NotNull List<Object> c) {
            if (c.size() == 1)
                return (Rule) c.get(0);
            return switch (((String) c.get(0)).charAt(0)) {
                case '&' -> look((Rule) c.get(1));
                case '!' -> neg((Rule) c.get(1));
                default -> throw new IllegalArgumentException("Invalid PEG prefix rule: " + c);
            };
        }

        private @NotNull Rule suffix(final @NotNull List<Object> c) {
            var r = (Rule) c.get(0);
            if (c.size() == 1)
                return r;

            var operator = ((String) c.get(1)).charAt(0);

            if (options.tryTurnCharClassesIntoPatterns) {
                try {
                    if (r instanceof RegexTerm)
                        return regex(((RegexTerm) r).getRegexp().pattern() + operator);
                    if (r instanceof ValueRangeTerm) {
                        //noinspection PatternVariableCanBeUsed
                        var cr = (ValueRangeTerm) r;
                        return regex("[\\u" + cr.getLo() + "-\\u" + cr.getHi() + "]" + operator);
                    }
                } catch (PatternSyntaxException ignored) {
                    // Some regexes, like "[a]+?+" are illegal because the "+" at the end cannot be added.
                    // In such a case, the transformer simply decides to make a normal repetition/optional rule as below.
                }
            }

            //noinspection EnhancedSwitchMigration
            switch (operator) {
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

        private @NotNull Rule primary(final @NotNull List<Object> c) {
            if (c.size() == 3) // Group rule: "(" expression ")"
                return (Rule) c.get(1);
            return (Rule) c.get(0);
        }

        private @NotNull NonTerminal identifier(final @NotNull List<Object> c) {
            return nt(c.get(0).toString());
        }

        private @NotNull Rule literal(final @NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule hideNt(final @NotNull List<Object>c) {return ((Rule)c.get(1)).enableHideTag();}

        private @NotNull Rule hide(final @NotNull List<Object>c) {return ((Rule)c.get(1)).enableHideTag();}

        private final @NotNull StrParser strParser = new StrParser();

        private @NotNull Rule stringLiteral(final @NotNull List<Object> c) {
            return string(strParser.processString((String) c.get(0)));
        }

        private @NotNull Rule regexTerminal(final @NotNull List<Object> c) {
            return regex(strParser.processRegexp(c.get(0).toString()));
        }

        private @NotNull Rule dot(final @NotNull List<Object> c) {
            return regex(".");
        }

        private @NotNull Rule classRule(final @NotNull List<Object> c) {
            //noinspection unchecked
            var rules = (List<Rule>) ((Object) c.subList(1, c.size() - 1));

            if (options.tryTurnCharClassesIntoPatterns) {
                var sb = new StringBuilder("[");
                for (Rule ruleUncast : rules) {
                    var rule = (ValueRangeTerm) ruleUncast;

                    if (rule.getLo() == rule.getHi()) sb.append(String.format("\\u%04X", rule.getLo()));
                    else sb.append(String.format("\\u%04X-\\u%04X", rule.getLo(), rule.getHi()));
                }
                sb.append(']');
                return regex(Pattern.compile(sb.toString()));
            }

            return alt(rules);
        }

        private @NotNull ValueRangeTerm range(final @NotNull List<Object> c) {
            return (ValueRangeTerm) (c.size() == 3
                    ? numVal((Integer) c.get(0), (Integer) c.get(2))
                    : numVal((Integer) c.get(0)));
        }

        private Integer charRule(final @NotNull List<Object> c) {
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
                    default:
                        break;
                }
            }
            return s.codePointAt(0);
        }
    }

    private static final class PEGGrammarBuilder extends GrammarBuilder {
        @Override
        protected void make() {
            var spacing = hide(nt("Spacing"));

            addProduction("Grammar",
                    cat(spacing, onceOrMore(nt("Definition")), eof()));

            addProduction("Definition",
                    cat(alt(nt("Identifier"), nt("HideNt")), spacing, string("<-"), spacing, nt("Expression")));

            addProduction("Expression",
                    cat(nt("Sequence"), spacing, zeroOrMore(cat(string("/"), spacing, nt("Sequence")))));

            addProduction("Sequence",
                    zeroOrMore(nt("Prefix")));

            addProduction("Prefix",
                    cat(alt(string("&"), string("!"), eps()), spacing, nt("Suffix")));

            addProduction("Suffix",
                    cat(nt("Primary"), spacing, opt(cat(alt(string("?"), string("*"), string("+")), spacing))));

            addProduction("Primary", alt(
                    nt("Identifier"),
                    cat(string("("), spacing, nt("Expression"), spacing, string(")")),
                    nt("Literal"),
                    nt("Class"),
                    nt("Dot"),
                    nt("Hide")));

            addProduction("Identifier",
                    regex("[a-zA-Z_][a-zA-Z_0-9]*"));

            addProduction("HideNt",
                    cat(string("<"), nt("Identifier"), string(">")));

            addProduction("Hide",
                    cat(string("<"), spacing, nt("Expression"), spacing, string(">")));

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
                    cat(string("#"), regex("[\\u0020\\u0009\\S]+(?x) # Comment until newline/eof"), alt(string("\n"), eof())))));

            addProduction("Space",
                    regex("[\\u0020\\u0009\\r\\n]+"));
        }
    }
}
