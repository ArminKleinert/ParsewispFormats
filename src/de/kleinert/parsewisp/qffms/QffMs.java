package de.kleinert.parsewisp.qffms;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * "Quite free-form metasyntax"
 */
public class QffMs {private QffMs(){}

    public static Parser baseParser() {
    var g = """
            syntax = WS ( definition WS )+
            definition = identifier WS "=" WS alternation
            identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
            alternation = concatenation ( WS "/" WS concatenation )*
            concatenation = prefix
            prefix = ("!" | "&")? suffix
            suffix = primary ("?" | "*" | "+")?
            primary = group | identifier | literal | regex | characterRange | character
            group = "(" WS alternation WS ")"
            regex = #'´([^´\\\\]|\\\\(.|\\\\n))*´'
            literal = #'"([^"\\\\]|\\\\(.|\\\\n))*"'
            
            characterRange = character ".." character
            character = #"'\\\\u[a-zA-F0-9][a-zA-F0-9][a-zA-F0-9][a-zA-F0-9]'"
                      | "'\\\\''"
                      | #"'.'"
            
            <WS> = < (space | comment)* >
            space = #"[\\s+]"
            comment = "#" #"[\\u0020\\u0009\\S]+" ("\\n" | EOF)
            """;
        return Parsewisp.parser(g);
    }

    public static Parser parser(String grammar) {
    return Parsewisp.parser(
            new QffMsTransformer().transform(baseParser().parse(grammar)),
            ParserCreationOptions.getDefault());
    }
    private static class QffMsTransformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();
    Grammar transform(ParseResult tree) {
        Map<Sym, Function<List<Object>,Object>> m = new HashMap<>();

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

        return Transform.transform(tree,m,ignore->this.build());
    }
        private Object       syntax    (List<Object>c){
            for (Object r : c) {
                //noinspection unchecked
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }
        private Object      definition     (List<Object>c){return Map.entry(c.get(0), c.get(2));
        }
        private Object        identifier   (List<Object>c){return nt((String) c.get(0));
        }
        private Object       alternation    (List<Object>c){//noinspection unchecked
            return alt((List<Rule>) ((Object)c));
        }
        private Object     concatenation      (List<Object>c){//noinspection unchecked
        return cat((List<Rule>) ((Object)c));
        }
        private Object    prefix       (List<Object>c){
        if (c.size() == 1) return c.get(0);
        var rule = (Rule)c.get(1);
        switch(((String)c.get(0)).charAt(0)) {
            case'!':neg(rule);
            case'&':look(rule);
            default:throw new IllegalArgumentException(c.toString());
        }
        }
        private Object      suffix     (List<Object>c){
        var rule = (Rule)c.get(0);
        if (c.size() == 1) return rule;
        return switch (((String) c.get(1)).charAt(0)) {
                case '+' -> onceOrMore(rule);
                case '*' -> zeroOrMore(rule);
                case '?' -> opt(rule);
                default -> throw new IllegalArgumentException(c.toString());
            };
        }
        private Object     primary      (List<Object>c){return c.get(0);
        }
        private Object       group    (List<Object>c){return c.get(1);
        }
        private Object       regex    (List<Object>c){return regex(strParser.processRegexp(c.get(0).toString(), 1, 1));
        }
        private Object    literal       (List<Object>c){return strParser.processString((String) c.get(0));
        }
        private Object        characterRange   (List<Object>c){var start = (Integer)c.get(0);
        var end = (Integer)c.get(2);
        return numVal(start, end);
        }
        private Object       character    (List<Object>c){return strParser.processString((String)c.get(0)).codePointAt(0);
        }
    }

//    public static @NotNull Parser parser(final @NotNull String grammar) {
//        return parser(grammar, QffMsOptions.getDefault());
//    }
//    public static @NotNull Parser parser(final @NotNull String grammar, @Nullable QffMsOptions options) {
//        options = options == null ? QffMsOptions.getDefault() : options;
//        var res = Parsewisp.parser(baseGrammar(options), options).parse(grammar);
//        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
//        return Parsewisp.parser(
//                new QffMsTransformer(options).transform(res.castToParseSuccess()),
//                ParserCreationOptions.getDefault());
//    }
//    public static @NotNull Grammar baseGrammar(final @NotNull EBNF.EBNFOptions options) {
//        return new QffMsGrammarBuilder(options).build();
//    }
//
//    /**
//     * Options for creating QffMs parsers.
//     */
//    public static final class QffMsOptions extends ParserCreationOptions {
//        /**
//         * Constructor.
//         *
//         * @param whitespaceParser             See {@link ParserCreationOptions#getWhitespaceParser()}
//         * @param startProduction              See {@link ParserCreationOptions#getStartProduction()}
//         */
//        public QffMsOptions(@Nullable Parser whitespaceParser,
//                           @Nullable Sym startProduction) {
//            super(whitespaceParser, startProduction, RedefinitionOption.ERROR, true);
//        }
//
//        /**
//         * The default options for EBNF parsers.
//         *
//         * @return The default options for EBNF parsers.
//         */
//        public static @NotNull QffMsOptions getDefault() {
//            return new QffMsOptions(null, null);
//        }
//    }
}
