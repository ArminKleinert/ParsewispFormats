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
    private EBNF() {
    }

    /**
     * Constructs a {@link Parser} based on the grammar. Uses {@link EBNF.EBNFOptions#getDefault()} as options.
     *
     * @param grammar The grammar.
     * @return A parser based on the provided grammar.
     * @see #parser(String)
     */
    public static @NotNull Parser parser(final @NotNull String grammar) {
        return parser(grammar, EBNFOptions.getDefault());
    }

    /**
     *
     * @param grammar The grammar.
     * @param options The options.
     * @return A parser based on the provided grammar.
     * @see #parser(String)
     */
    public static @NotNull Parser parser(final @NotNull String grammar, @Nullable EBNFOptions options) {
        options = options == null ? EBNFOptions.getDefault() : options;
        var res = Parsewisp.parser(baseGrammar(options), options).parse(grammar);
        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
        return Parsewisp.parser(
                new EBNFTransformer(options).transform(res.castToParseSuccess()),
                ParserCreationOptions.getDefault());
    }

    /// The base grammar for Parsewisp EBNF. The grammar can be expressed as follows:
    /// ```
    /// syntax = <cWsp> syntaxRule <cWsp> { (syntaxRule <cWsp>) }
    /// syntaxRule = (metaIdentifier | hideNt) <cWsp> "=" <cWsp> definitionsList <cWsp> (";" | ".")
    /// definitionsList = orderedDefinitionsList { (<cWsp> "|" <cWsp> orderedDefinitionsList) }
    /// orderedDefinitionsList = singleDefinition { (<cWsp> "/" <cWsp> singleDefinition) }
    /// singleDefinition = term { (<cWsp> "," <cWsp> term) }
    /// term = factor [ (<cWsp> "-" <cWsp> exception) ]
    /// exception = factor
    /// factor = [ (integer "*" <cWsp>) ] primary
    /// primary = optionalSequence | repeatedSequence | specialSequence | groupedSequence | metaIdentifier | terminal | empty | hideSeq
    /// look = "&" <cWsp> primary
    /// neg = "!" <cWsp> primary
    /// empty = ε
    /// optionalSequence = "[" <cWsp> definitionsList <cWsp> "]"
    /// repeatedSequence = "{" <cWsp> definitionsList <cWsp> "}"
    /// groupedSequence = "(" <cWsp> definitionsList <cWsp> ")"
    /// hideSeq = "<" <cWsp> definitionsList <cWsp> ">"
    /// terminal = stringTerminal | regexTerminal
    /// stringTerminal = #""[^"\\]*(?:\\.[^"\\]*)*"(?x) # String" | #"'[^'\\]*(?:\\.[^'\\]*)*'(?x) # String"
    /// regexTerminal = #"#'[^'\\]*(?:\\.[^'\\]*)*'(?x) # Regex" | #"#´[^'\\]*(?:\\.[^'\\]*)*´(?x) # Regex" | #"#\"[^\"\\]*(?:\\.[^\"\\]*)*\"(?x) # Regex"
    /// metaIdentifier = #"[a-zA-Z][a-zA-Z0-9\_]*(?x) # NonTerminal"
    /// hideNt = "<" #"[a-zA-Z][a-zA-Z0-9\_]*(?x) # NonTerminal" ">"
    /// integer = #"[0-9]+"
    /// specialSequence = "?" #"[^?]+" "?"
    /// comment = "(*" { (comment | #"(?s)(?:(?!\(\*|\*\)).)*(?x) # Comment text") } "*)"
    /// cWsp = <comment | #"\s*"+>
    /// ```
    ///
    /// @param options The options.
    /// @return The grammar which parses ABNF grammars.
    public static @NotNull Grammar baseGrammar(final @NotNull EBNFOptions options) {
        return new EBNFGrammarBuilder(options).build();
    }

    /**
     * Options for creating ABNF parsers.
     */
    public static final class EBNFOptions extends ParserCreationOptions {
        final boolean allowLookaheadAndNegations;
        final boolean useAlternativeRepresentation;
        final boolean requireCommasAndTerminators;
        final Map<String, @NotNull Function<@NotNull String, Optional<String>>> specialSequences;

        /**
         * Constructor.
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
            super(whitespaceParser, startProduction, RedefinitionOption.ERROR, true, new EBNFPrinter());
            this.allowLookaheadAndNegations = allowLookaheadAndNegations;
            this.useAlternativeRepresentation = useAlternativeRepresentation;
            this.requireCommasAndTerminators = requireCommasAndTerminators;
            this.specialSequences = specialSequences == null ? Map.of() : specialSequences;
        }

        /**
         * The default options for EBNF parsers.
         *
         * @return The default options for EBNF parsers.
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

        public Grammar transform(final @NotNull ParseTree tree) {
            Function<List<Object>, Object> ignoreMe = (it) -> null;
            var m = new HashMap<Sym, Function<List<Object>, Object>>();
            m.put(Sym.sym("syntax"), this::syntax);
            m.put(Sym.sym("syntaxRule"), this::syntaxRule);
            m.put(Sym.sym("definitionsList"), this::definitionsList);
            m.put(Sym.sym("orderedDefinitionsList"), this::orderedDefinitionsList);
            m.put(Sym.sym("singleDefinition"), this::singleDefinition);
            m.put(Sym.sym("term"), this::term);
            m.put(Sym.sym("exception"), this::exception);
            m.put(Sym.sym("factor"), this::factor);
            m.put(Sym.sym("look"), this::lookRule);
            m.put(Sym.sym("neg"), this::negRule);
            m.put(Sym.sym("primary"), this::primary);
            m.put(Sym.sym("empty"), this::empty);
            m.put(Sym.sym("optionalSequence"), this::optionalSequence);
            m.put(Sym.sym("repeatedSequence"), this::repeatedSequence);
            m.put(Sym.sym("groupedSequence"), this::groupedSequence);
            m.put(Sym.sym("hideSeq"), this::hideSeq);
            m.put(Sym.sym("terminal"), this::terminal);
            m.put(Sym.sym("stringTerminal"), this::stringTerminal);
            m.put(Sym.sym("regexTerminal"), this::regexTerminal);
            m.put(Sym.sym("metaIdentifier"), this::metaIdentifier);
            m.put(Sym.sym("hideNt"), this::hideNt);
            m.put(Sym.sym("integer"), this::integer);
            m.put(Sym.sym("specialSequence"), this::specialSequence);
            m.put(Sym.sym("comment"), ignoreMe);
            m.put(Sym.sym("cWsp"), ignoreMe);
            return Transform.transform(tree, m, ignore -> this.build());
        }

        private Object syntax(final @NotNull List<Object> c) {
            for (Object r : c) {
                //noinspection unchecked
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            return null;
        }

        private @NotNull Map.Entry<NonTerminal, Rule> syntaxRule(final @NotNull List<Object> c) {
            var lhs = (NonTerminal) c.get(0);
            var rhs = (Rule) c.get(2);
            return Map.entry(lhs, rhs);
        }

        private @NotNull Rule definitionsList(final @NotNull List<Object> c) {
            return alt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule orderedDefinitionsList(final @NotNull List<Object> c) {
            return ordAlt(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule singleDefinition(final @NotNull List<Object> c) {
            return cat(c.stream().filter(it -> it instanceof Rule).map(it -> (Rule) it).toList());
        }

        private @NotNull Rule term(final @NotNull List<Object> c) {
            if (c.size() == 1) return (Rule) c.get(0);
            return exclude((Rule) c.get(0), (Rule) c.get(2));
        }

        private @NotNull Rule exception(final @NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule factor(final @NotNull List<Object> c) {
            if (c.size() == 1) return (Rule) c.get(0);
            return rep((Rule) c.get(2), (Integer) c.get(0));
        }

        private @NotNull Rule lookRule(final @NotNull List<Object> c) {
            return look((Rule) c.get(1));
        }

        private @NotNull Rule negRule(final @NotNull List<Object> c) {
            return neg((Rule) c.get(1));
        }

        private @NotNull Rule primary(final @NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule empty(final @NotNull List<Object> c) {
            return eps();
        }

        private @NotNull Rule optionalSequence(final @NotNull List<Object> c) {
            return opt((Rule) c.get(1));
        }

        private @NotNull Rule repeatedSequence(final @NotNull List<Object> c) {
            return zeroOrMore((Rule) c.get(1));
        }

        private @NotNull Rule groupedSequence(final @NotNull List<Object> c) {
            return (Rule) c.get(1);
        }

        private @NotNull Rule hideSeq(final @NotNull List<Object> c) {
            return hide((Rule) c.get(1));
        }

        private @NotNull Rule terminal(final @NotNull List<Object> c) {
            return (Rule) c.get(0);
        }

        private @NotNull Rule stringTerminal(final @NotNull List<Object> c) {
            return string(strParser.processString((String) c.get(0)));
        }

        private @NotNull Rule regexTerminal(final @NotNull List<Object> c) {
            return regex(strParser.processRegexp(c.get(0).toString(), 2));
        }

        private @NotNull Rule metaIdentifier(final @NotNull List<Object> c) {
            return nt(c.get(0).toString());
        }

        private @NotNull Rule hideNt(final @NotNull List<Object> c) {
            return nt(c.get(1).toString()).enableHideTag();
        }

        private @NotNull Integer integer(final @NotNull List<Object> c) {
            return Integer.parseInt((String) c.get(0));
        }

        private @NotNull Rule specialSequence(final @NotNull List<Object> c) {
            var fn = options.specialSequences.get((String) c.get(1));
            if (fn == null) throw new ParserCreationFailure("Unknown special sequence: " + c.get(1));
            return specialSequence((String) c.get(1), fn);
        }

        protected void make() {
        }
    }

    private static class EBNFGrammarBuilder extends GrammarBuilder {
        final @NotNull EBNFOptions options;

        private EBNFGrammarBuilder(final @NotNull EBNFOptions options) {
            super(RedefinitionOption.ERROR);
            this.options = options;
        }

        @Override
        protected void make() {
            var cWsp = hide(nt("cWsp"));

            addProduction("syntax", cat(
                    List.of(cWsp,
                            nt("syntaxRule"), cWsp,
                            zeroOrMore(cat(nt("syntaxRule"), cWsp)))));

            var ruleRhsRule = options.requireCommasAndTerminators
                    ? cat(nt("definitionsList"), cWsp, alt(string(";"), string(".")))
                    : cat(nt("definitionsList"), opt(cat(cWsp, alt(string(";"), string(".")))));
            addProduction("syntaxRule", cat(
                    List.of(alt(nt("metaIdentifier"), nt("hideNt")), cWsp,
                            string("="), cWsp,
                            ruleRhsRule)));

            var dividerRule = options.useAlternativeRepresentation
                    ? string("!")
                    : string("|");
            addProduction("definitionsList", cat(
                    List.of(nt("orderedDefinitionsList"),
                            zeroOrMore(cat(cWsp, dividerRule, cWsp, nt("orderedDefinitionsList"))))));

            addProduction("orderedDefinitionsList", cat(
                    List.of(nt("singleDefinition"),
                            zeroOrMore(cat(cWsp, string("/"), cWsp, nt("singleDefinition"))))));

            var definitionTailRule = options.requireCommasAndTerminators
                    ? cat(cWsp, string(","), cWsp, nt("term"))
                    : cat(cWsp, opt(cat(string(","), cWsp)), nt("term"));
            addProduction("singleDefinition", cat(
                    List.of(nt("term"),
                            zeroOrMore(definitionTailRule))));

            addProduction("term", cat(
                    List.of(nt("factor"),
                            opt(cat(cWsp, string("-"), cWsp, nt("exception"))))));

            addProduction("exception",
                    nt("factor"));

            final List<Rule> factorRules = new ArrayList<>();
            factorRules.add(cat(
                    opt(cat(nt("integer"), string("*"), cWsp)),
                    nt("primary")));
            if (options.allowLookaheadAndNegations) {
                factorRules.addAll(List.of(nt("look"), nt("neg")));
            }
            addProduction("factor", alt(
                    factorRules));

            addProduction("primary", alt(
                    nt("optionalSequence"),
                    nt("repeatedSequence"),
                    nt("specialSequence"),
                    nt("groupedSequence"),
                    nt("metaIdentifier"),
                    nt("terminal"),
                    nt("empty"),
                    nt("hideSeq")));

            addProduction(
                    Sym.sym("look"),
                    cat(string("&"), cWsp, nt("primary")));

            addProduction(
                    Sym.sym("neg"),
                    cat(string("!"), cWsp, nt("primary")));

            addProduction("empty",
                    eps());

            var optionalSequenceRule = options.useAlternativeRepresentation
                    ? cat(string("(/"), cWsp, nt("definitionsList"), cWsp, string("/)"))
                    : cat(string("["), cWsp, nt("definitionsList"), cWsp, string("]"));
            addProduction("optionalSequence", optionalSequenceRule);

            var repeatedSequenceRule = options.useAlternativeRepresentation
                    ? cat(string("(:"), cWsp, nt("definitionsList"), cWsp, string(":)"))
                    : cat(string("{"), cWsp, nt("definitionsList"), cWsp, string("}"));
            addProduction("repeatedSequence", repeatedSequenceRule);

            addProduction("groupedSequence",
                    cat(string("("), cWsp, nt("definitionsList"), cWsp, string(")")));

            addProduction("hideSeq",
                    cat(string("<"), cWsp, nt("definitionsList"), cWsp, string(">")));

            addProduction("terminal", alt(
                    nt("stringTerminal"),
                    nt("regexTerminal")));

            addProduction("stringTerminal", alt(
                    regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String"),
                    regex("'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # String")));

            addProduction("regexTerminal", alt(
                    regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex"),
                    regex("#´[^'\\\\]*(?:\\\\.[^'\\\\]*)*´(?x) # Regex"),
                    regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) # Regex")));

            addProduction("metaIdentifier",
                    regex("[a-zA-Z][a-zA-Z0-9\\_]*(?x) # NonTerminal"));

            addProduction("hideNt",
                    cat(string("<"), regex("[a-zA-Z][a-zA-Z0-9\\_]*(?x) # NonTerminal"), string(">")));

            addProduction("integer",
                    regex("[0-9]+"));

            addProduction("specialSequence",
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
