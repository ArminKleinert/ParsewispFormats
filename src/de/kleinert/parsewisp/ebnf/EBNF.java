package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Grammar from <a href="http://www.cl.cam.ac.uk/~mgk25/iso-14977.pdf">iso-14977 (1996)</a>
 */
public class EBNF {
    /**
     *Constructs a {@link Parser} based on the grammar. Uses {@link EBNF.EBNFOptions#getDefault()} as options.
     * @param grammar The grammar.
     * @return A parser based on the provided grammar.
     * @see #parser(String)
     */
    public static @NotNull Parser parser(@NotNull String grammar) {
        return parser(grammar, EBNFOptions.getDefault());
    }

    /**
     *
     * @param grammar The grammar.
     * @param options The options.
     * @return A parser based on the provided grammar.
     * @see #parser(String)
     */
    public static @NotNull Parser parser(@NotNull String grammar, @Nullable EBNFOptions options) {
        options = options == null ? EBNFOptions.getDefault() : options;
        var res = Parsewisp.parser(baseGrammar(options), options).parse(grammar);
        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
        return Parsewisp.parser(
                new EBNFTransformer(options).transform(res.castToParseSuccess()),
                ParserCreationOptions.getDefault());
    }

    /// The base grammar for Parsewisp EBNF. The grammar can be expressed as follows:
    /// ```
    /// syntax = <cWsp> syntax_rule <cWsp> { (syntax_rule <cWsp>) }
    /// syntax_rule = (meta_identifier | hide_nt) <cWsp> "=" <cWsp> definitions_list <cWsp> (";" | ".")
    /// definitions_list = ordered_definitions_list { (<cWsp> "|" <cWsp> ordered_definitions_list) }
    /// ordered_definitions_list = single_definition { (<cWsp> "/" <cWsp> single_definition) }
    /// single_definition = term { (<cWsp> "," <cWsp> term) }
    /// term = factor [ (<cWsp> "-" <cWsp> exception) ]
    /// exception = factor
    /// factor = [ (integer "*" <cWsp>) ] primary
    /// primary = optional_sequence | repeated_sequence | special_sequence | grouped_sequence | meta_identifier | terminal | empty | hide_seq
    /// look = "&" <cWsp> primary
    /// neg = "!" <cWsp> primary
    /// empty = ε
    /// optional_sequence = "[" <cWsp> definitions_list <cWsp> "]"
    /// repeated_sequence = "{" <cWsp> definitions_list <cWsp> "}"
    /// grouped_sequence = "(" <cWsp> definitions_list <cWsp> ")"
    /// hide_seq = "<" <cWsp> definitions_list <cWsp> ">"
    /// terminal = string_terminal | regex_terminal
    /// string_terminal = #""[^"\\]*(?:\\.[^"\\]*)*"(?x) # String" | #"'[^'\\]*(?:\\.[^'\\]*)*'(?x) # String"
    /// regex_terminal = #"#'[^'\\]*(?:\\.[^'\\]*)*'(?x) # Regex" | #"#´[^'\\]*(?:\\.[^'\\]*)*´(?x) # Regex" | #"#\"[^\"\\]*(?:\\.[^\"\\]*)*\"(?x) # Regex"
    /// meta_identifier = #"[a-zA-Z][a-zA-Z0-9\_]*(?x) # NonTerminal"
    /// hide_nt = "<" #"[a-zA-Z][a-zA-Z0-9\_]*(?x) # NonTerminal" ">"
    /// integer = #"[0-9]+"
    /// special_sequence = "?" #"[^?]+" "?"
    /// comment = "(*" { (comment | #"(?s)(?:(?!\(\*|\*\)).)*(?x) # Comment text") } "*)"
    /// cWsp = <comment | #"\s*"+>
    /// ```
    /// @param options The options.
    /// @return The grammar which parses ABNF grammars.
    public static @NotNull Grammar baseGrammar(@NotNull EBNFOptions options) {
        return new EBNFGrammarBuilder(options).build();
    }

    /**
     *
     */
    public static final class EBNFOptions extends ParserCreationOptions {
        final boolean allowLookaheadAndNegations;
        final boolean useAlternativeRepresentation;
        final boolean requireCommasAndTerminators;
        final Map<String, @NotNull Function<@NotNull String, Optional<String>>> specialSequences;

        /**
         *
         * @param whitespaceParser             See {@link ParserCreationOptions#getWhitespaceParser()}
         * @param startProduction              See {@link ParserCreationOptions#getStartProduction()}
         * @param allowLookaheadAndNegations   If true, allow the usage of lookaheads and negative lookaheads.
         * @param useAlternativeRepresentation
         * @param requireCommasAndTerminators
         * @param specialSequences
         */
        public EBNFOptions(@Nullable Parser whitespaceParser,
                           @Nullable Sym startProduction,
                           boolean allowLookaheadAndNegations,
                           boolean useAlternativeRepresentation,
                           boolean requireCommasAndTerminators,
                           @Nullable final Map<String, Function<String, Optional<String>>> specialSequences) {
            super(whitespaceParser, startProduction, RedefinitionOption.ERROR, true);
            this.allowLookaheadAndNegations = allowLookaheadAndNegations;
            this.useAlternativeRepresentation = useAlternativeRepresentation;
            this.requireCommasAndTerminators = requireCommasAndTerminators;
            this.specialSequences = specialSequences == null ? Map.of() : specialSequences;
        }

        /**
         * The default options for ABNF parsers.
         *
         * @return The default options for ABNF parsers.
         */
        public static @NotNull EBNFOptions getDefault() {
            return new EBNFOptions(null, null, false, false, true, Map.of());
        }
    }

    private static class EBNFTransformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();
        private final EBNFOptions options;

        private EBNFTransformer(final @NotNull EBNFOptions options) {
            super();
            this.options = options;
        }

        public Grammar transform(ParseTree tree) {
            Function<List<Object>, Object> ignoreMe = (it) -> null;
            var m = new HashMap<Sym, Function<List<Object>, Object>>();
            m.put(Sym.sym("syntax"), this::syntax);
            m.put(Sym.sym("syntax_rule"), this::syntax_rule);
            m.put(Sym.sym("definitions_list"), this::definitions_list);
            m.put(Sym.sym("ordered_definitions_list"), this::ordered_definitions_list);
            m.put(Sym.sym("single_definition"), this::single_definition);
            m.put(Sym.sym("term"), this::term);
            m.put(Sym.sym("exception"), this::exception);
            m.put(Sym.sym("factor"), this::factor);
            m.put(Sym.sym("look"), this::lookRule);
            m.put(Sym.sym("neg"), this::negRule);
            m.put(Sym.sym("primary"), this::primary);
            m.put(Sym.sym("empty"), this::empty);
            m.put(Sym.sym("optional_sequence"), this::optional_sequence);
            m.put(Sym.sym("repeated_sequence"), this::repeated_sequence);
            m.put(Sym.sym("grouped_sequence"), this::grouped_sequence);
            m.put(Sym.sym("hide_seq"), this::hide_seq);
            m.put(Sym.sym("terminal"), this::terminal);
            m.put(Sym.sym("string_terminal"), this::string_terminal);
            m.put(Sym.sym("regex_terminal"), this::regex_terminal);
            m.put(Sym.sym("meta_identifier"), this::meta_identifier);
            m.put(Sym.sym("hide_nt"), this::hide_nt);
            m.put(Sym.sym("integer"), this::integer);
            m.put(Sym.sym("special_sequence"), this::special_sequence);
            m.put(Sym.sym("comment"), ignoreMe);
            m.put(Sym.sym("cWsp"), ignoreMe);
            return Transform.transform(tree, m, ignore -> this.build());
        }

        private Object syntax(@NotNull List<Object> c) {
            for (Object r : c) {
                @SuppressWarnings("unchecked")
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }

        private @NotNull Map.Entry<NonTerminal, Rule> syntax_rule(@NotNull List<Object> c) {
            var lhs = (NonTerminal) c.get(0);
            var rhs = (Rule) c.get(2);
            return Map.entry(lhs, rhs);
        }

        private @NotNull Rule definitions_list(@NotNull List<Object> c) {
            return alt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule ordered_definitions_list(@NotNull List<Object> c) {
            return ordAlt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule single_definition(@NotNull List<Object> c) {
            return cat(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule term(@NotNull List<Object> c) {
            if (c.size() == 1) return (Rule) c.get(0);
            return exclude((Rule) c.get(0), (Rule) c.get(2));
        }

        private @NotNull Rule exception(@NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule factor(@NotNull List<Object> c) {
            if (c.size() == 1) return (Rule) c.get(0);
            return rep((Rule) c.get(2), (Integer) c.get(0));
        }

        private @NotNull Rule lookRule(@NotNull List<Object> c) {
            return look((Rule) c.get(1));
        }

        private @NotNull Rule negRule(@NotNull List<Object> c) {
            return neg((Rule) c.get(1));
        }

        private @NotNull Rule primary(@NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule empty(@NotNull List<Object> c) {
            return eps();
        }

        private @NotNull Rule optional_sequence(@NotNull List<Object> c) {
            return opt((Rule) c.get(1));
        }

        private @NotNull Rule repeated_sequence(@NotNull List<Object> c) {
            return zeroOrMore((Rule) c.get(1));
        }

        private @NotNull Rule grouped_sequence(@NotNull List<Object> c) {
            return (Rule) c.get(1);
        }

        private @NotNull Rule hide_seq(@NotNull List<Object> c) {
            return hide((Rule) c.get(1));
        }

        private @NotNull Rule terminal(@NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule string_terminal(@NotNull List<Object> c) {
            return string(strParser.processString((String) c.get(0)));
        }

        private @NotNull Rule regex_terminal(@NotNull List<Object> c) {
            return regex(strParser.processRegexp(c.get(0).toString()));
        }

        private @NotNull Rule meta_identifier(@NotNull List<Object> c) {
            return nt(c.get(0).toString());
        }

        private @NotNull Rule hide_nt(@NotNull List<Object> c) {
            return nt(c.get(1).toString()).enableHideTag();
        }

        private @NotNull Integer integer(@NotNull List<Object> c) {
            return Integer.parseInt((String) c.get(0));
        }

        private @NotNull Rule special_sequence(@NotNull List<Object> c) {
            var fn = options.specialSequences.get((String) c.get(1));
            if (fn == null) throw new ParserCreationFailure("Unknown special sequence: " + c.get(1));
            return specialSequence((String) c.get(1), fn);
        }

        protected void make() {
        }
    }

    private static class EBNFGrammarBuilder extends GrammarBuilder {
        EBNFOptions options;

        private EBNFGrammarBuilder(EBNFOptions options) {
            super(RedefinitionOption.ERROR);
            this.options = options;
        }

        @Override
        protected void make() {
            var cWsp = hide(nt("cWsp"));

            addProduction("syntax", cat(
                    List.of(cWsp,
                            nt("syntax_rule"), cWsp,
                            zeroOrMore(cat(nt("syntax_rule"), cWsp)))));

            var ruleRhsRule = options.requireCommasAndTerminators
                    ? cat(nt("definitions_list"), cWsp, alt(string(";"), string(".")))
                    : cat(nt("definitions_list"), opt(cat(cWsp, alt(string(";"), string(".")))));
            addProduction("syntax_rule", cat(
                    List.of(alt(nt("meta_identifier"), nt("hide_nt")), cWsp,
                            string("="), cWsp,
                            ruleRhsRule)));

            var dividerRule = options.useAlternativeRepresentation
                    ? string("!")
                    : string("|");
            addProduction("definitions_list", cat(
                    List.of(nt("ordered_definitions_list"),
                            zeroOrMore(cat(cWsp, dividerRule, cWsp, nt("ordered_definitions_list"))))));

            addProduction("ordered_definitions_list", cat(
                    List.of(nt("single_definition"),
                            zeroOrMore(cat(cWsp, string("/"), cWsp, nt("single_definition"))))));

            var definitionTailRule = options.requireCommasAndTerminators
                    ? cat(cWsp, string(","), cWsp, nt("term"))
                    : cat(cWsp, opt(cat(string(","), cWsp)), nt("term"));
            addProduction("single_definition", cat(
                    List.of(nt("term"),
                            zeroOrMore(definitionTailRule))));

            addProduction("term", cat(
                    List.of(nt("factor"),
                            opt(cat(cWsp, string("-"), cWsp, nt("exception"))))));

            addProduction("exception",
                    nt("factor"));

            final List<Rule> factorRules = new ArrayList<>();
factorRules.add(                  cat(
                    opt(cat(nt("integer"), string("*"), cWsp)),
                    nt("primary")));
            if (options.allowLookaheadAndNegations) {
                factorRules.addAll(List.of(nt("look"), nt("neg")));
            }
            addProduction("factor", altList(
                    factorRules));

            addProduction("primary", alt(
                    nt("optional_sequence"),
                    nt("repeated_sequence"),
                    nt("special_sequence"),
                    nt("grouped_sequence"),
                    nt("meta_identifier"),
                    nt("terminal"),
                    nt("empty"),
                    nt("hide_seq")));

            addProduction(
                    Sym.sym("look"),
                    cat(string("&"), cWsp, nt("primary")));

            addProduction(
                    Sym.sym("neg"),
                    cat(string("!"), cWsp, nt("primary")));

            addProduction("empty",
                    eps());

            var optionalSequenceRule = options.useAlternativeRepresentation
                    ? cat(string("(/"), cWsp, nt("definitions_list"), cWsp, string("/)"))
                    : cat(string("["), cWsp, nt("definitions_list"), cWsp, string("]"));
            addProduction("optional_sequence", optionalSequenceRule);

            var repeatedSequenceRule = options.useAlternativeRepresentation
                    ? cat(string("(:"), cWsp, nt("definitions_list"), cWsp, string(":)"))
                    : cat(string("{"), cWsp, nt("definitions_list"), cWsp, string("}"));
            addProduction("repeated_sequence", repeatedSequenceRule);

            addProduction("grouped_sequence",
                    cat(string("("), cWsp, nt("definitions_list"), cWsp, string(")")));

            addProduction("hide_seq",
                    cat(string("<"), cWsp, nt("definitions_list"), cWsp, string(">")));

            addProduction("terminal", alt(
                    nt("string_terminal"),
                    nt("regex_terminal")));

            addProduction("string_terminal", alt(
                    regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String"),
                    regex("'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # String")));

            addProduction("regex_terminal", alt(
                    regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex"),
                    regex("#´[^'\\\\]*(?:\\\\.[^'\\\\]*)*´(?x) # Regex"),
                    regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) # Regex")));

            addProduction("meta_identifier",
                    regex("[a-zA-Z][a-zA-Z0-9\\_]*(?x) # NonTerminal"));

            addProduction("hide_nt",
                    cat(string("<"), regex("[a-zA-Z][a-zA-Z0-9\\_]*(?x) # NonTerminal"), string(">")));

            addProduction("integer",
                    regex("[0-9]+"));

            addProduction("special_sequence",
                    cat(string("?"), regex("[^?]+"), string("?")));

            final var insideComment = regex(
                    Pattern.compile("(?s)(?:(?!\\(\\*|\\*\\)).)*(?x) # Comment text"));
            addProduction("comment", cat(
                    List.of(string("(*"),
                            zeroOrMore(alt(nt("comment"), insideComment)),
                            string("*)"))));

            addProduction("cWsp", hide(onceOrMore(alt(
                    nt("comment"),
                    regex(Pattern.compile("\\s*"))))));
        }
    }
}
