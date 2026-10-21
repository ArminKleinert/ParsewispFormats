package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.parsing.ValueRangeTerm;
import de.kleinert.parsewisp.result.ParseResult;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * "Quite free-form metasyntax"
 */
public class QffMs {
    private QffMs() {
    }

    public static Parser baseParser() {
        var g = """
                syntax         = WS ( definition WS )+ EOF
                definition     = identifier WS "=" WS alternation
                identifier     = #'[a-zA-Z_\\-][a-zA-Z0-9_\\-]*'
                alternation    = concatenation ( WS "|" WS concatenation )*
                concatenation  = ( (characterRange | prefix) WS )+
                prefix         = ("!" | "&")? suffix
                suffix         = primary ("?" | "*" | "+" | numRep)?
                primary        = group | identifier | literal | regex | character
                group          = "(" WS alternation WS ")"
                regex          = #'´([^´\\\\]|\\\\(.|\\\\n))*´'
                literal        = #'"([^"\\\\]|\\\\(.|\\\\n))*"'
                
                characterRange = character ".." character
                character      = #"'\\\\u[a-zA-F0-9][a-zA-F0-9][a-zA-F0-9][a-zA-F0-9]'"
                               | #"'\\\\u[a-zA-F0-9][a-zA-F0-9][a-zA-F0-9][a-zA-F0-9]\\\\u[a-zA-F0-9][a-zA-F0-9][a-zA-F0-9][a-zA-F0-9]'"
                               | "'\\\\''"
                               | #"'\\S'"
                
                numRep         = "{" #"[0-9]*" [ "," #"[0-9]*" ] "}"
                
                <WS>           = < (space | comment)* >
                space          = #"\\s+"
                comment        = "#" #"[\\u0020\\u0009\\S]+" ("\\n" | EOF)
                """;
        return Parsewisp.parser(g);
    }

    public static Parser parser(String grammar) {
        return Parsewisp.parser(
                new QffMsTransformer().transform(baseParser().parse(grammar)),
                QffMsOptions.getDefault());
    }

    /**
     * Options for creating ABNF parsers.
     */
    public static final class QffMsOptions extends ParserCreationOptions {
        /**
         * Constructor.
         *
         * @param whitespaceParser See {@link ParserCreationOptions#getWhitespaceParser()}
         * @param startProduction  See {@link ParserCreationOptions#getStartProduction()}
         */
        public QffMsOptions(@Nullable Parser whitespaceParser,
                            @Nullable Sym startProduction) {
            super(whitespaceParser, startProduction, RedefinitionOption.ERROR, true, new QffMsPrinter());
        }

        /**
         * The default options for EBNF parsers.
         *
         * @return The default options for EBNF parsers.
         */
        public static @NotNull QffMsOptions getDefault() {
            return new QffMsOptions(null, null);
        }
    }

    private static class QffMsTransformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();

        Grammar transform(ParseResult tree) {
            Map<Sym, Function<List<Object>, Object>> m = new HashMap<>();

            m.put(Sym.sym("syntax"), this::syntax);
            m.put(Sym.sym("definition"), this::definition);
            m.put(Sym.sym("identifier"), this::identifier);
            m.put(Sym.sym("alternation"), this::alternation);
            m.put(Sym.sym("concatenation"), this::concatenation);
            m.put(Sym.sym("prefix"), this::prefix);
            m.put(Sym.sym("suffix"), this::suffix);
            m.put(Sym.sym("primary"), this::primary);
            m.put(Sym.sym("group"), this::group);
            m.put(Sym.sym("regex"), this::regex);
            m.put(Sym.sym("literal"), this::literal);
            m.put(Sym.sym("characterRange"), this::characterRange);
            m.put(Sym.sym("character"), this::character);
            m.put(Sym.sym("numRep"), this::numRep);

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

        private Object definition(List<Object> c) {
            return Map.entry(c.get(0), c.get(2));
        }

        private Object identifier(List<Object> c) {
            return nt((String) c.get(0));
        }

        private Object alternation(List<Object> c) {
            // Remove strings from the tree, keep only rules and make an alternation.
            return alt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private Object concatenation(List<Object> c) {
            //noinspection unchecked
            return cat((List<Rule>) ((Object) c));
        }

        private Object prefix(List<Object> c) {
            if (c.size() == 1) return c.get(0);
            var rule = (Rule) c.get(1);
            switch (((String) c.get(0)).charAt(0)) {
                case '!':
                    neg(rule);
                case '&':
                    look(rule);
                default:
                    throw new IllegalArgumentException(c.toString());
            }
        }

        private Object suffix(List<Object> c) {
            var rule = (Rule) c.get(0);
            if (c.size() == 1) return rule;
            if (c.get(1) instanceof int[]) {
                //noinspection PatternVariableCanBeUsed
                var arr = (int[]) c.get(1);
                return rep(rule, arr[0], arr[1]);
            }
            return switch (((String) c.get(1)).charAt(0)) {
                case '+' -> onceOrMore(rule);
                case '*' -> zeroOrMore(rule);
                case '?' -> opt(rule);
                default -> throw new IllegalArgumentException(c.toString());
            };
        }

        private int[] numRep(List<Object> c) {
            var c0 = (String) c.get(1);
            var min = c0.isEmpty() ? 0 : Integer.parseInt(c0);
            int max;
            if (c.size() == 3) {
                // Format: numRep = "{" digits "}"
                max = min;
            } else {
                // Format: numRep = "{" digits "," digits "}"
                var c3 = (String) c.get(3);
                max = c3.isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(c3);
            }
            return new int[]{min, max};
        }

        private Object primary(List<Object> c) {
            return c.get(0);
        }

        private Object group(List<Object> c) {
            return c.get(1);
        }

        private Object regex(List<Object> c) {
            return regex(strParser.processRegexp(c.get(0).toString(), 1, 1));
        }

        private Object literal(List<Object> c) {
            return string(strParser.processString((String) c.get(0)));
        }

        private Object characterRange(List<Object> c) {
            var start = (Integer) ((ValueRangeTerm) c.get(0)).getLo();
            var end = (Integer) ((ValueRangeTerm) c.get(2)).getLo();
            return numVal(start, end);
        }

        private Object character(List<Object> c) {
            return numVal(strParser.processString((String) c.get(0)).codePointAt(0));
        }
    }
}
