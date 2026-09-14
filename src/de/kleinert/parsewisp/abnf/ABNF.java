package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parser_options.RedefinitionOption;
import de.kleinert.parsewisp.parsing.*;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;

/// Provides support for ABNF grammars within the Parsewisp framework.
///
/// See [https://www.rfc-editor.org/info/rfc5234/](https://www.rfc-editor.org/info/rfc5234/)
/// and [https://www.rfc-editor.org/info/rfc7405/](https://www.rfc-editor.org/info/rfc7405/)
///
/// Any ABNF grammar provides the following core rules:
/// ```g
/// ALPHA          =  %x41-5A / %x61-7A   ; A-Z / a-z
/// BIT            =  "0" / "1"
/// CHAR           =  %x01-7F ; any 7-bit US-ASCII character, excluding NUL
/// CR             =  %x0D ; carriage return
/// CRLF           =  CR LF ; Internet standard newline
/// CTL            =  %x00-1F / %x7F ; controls
/// DIGIT          =  %x30-39 ; 0-9
/// DQUOTE         =  %x22 ; " (Double Quote)
/// HEXDIG         =  DIGIT / "A" / "B" / "C" / "D" / "E" / "F"
/// HTAB           =  %x09 ; horizontal tab
/// LF             =  %x0A ; linefeed
/// LWSP           =  *(WSP / CRLF WSP)
/// OCTET          =  %x00-FF ; 8 bits of data
/// SP             =  %x20
/// VCHAR          =  %x21-7E ; visible (printing) characters
/// WSP            =  SP / HTAB ; white space
/// ```
public final class ABNF {
    private ABNF() {
    }

    /**
     *
     */
    public static final class ABNFOptions extends ParserCreationOptions {
        boolean allowLookaheadAndNegations;

        /**
         *
         * @param whitespaceParser           See {@link ParserCreationOptions#getWhitespaceParser()}
         * @param startProduction            See {@link ParserCreationOptions#getStartProduction()}
         * @param allowLookaheadAndNegations If true, allow the usage of lookaheads and negative lookaheads.
         */
        public ABNFOptions(@Nullable Parser whitespaceParser,
                           @Nullable Sym startProduction,
                           boolean allowLookaheadAndNegations) {
            super(whitespaceParser, startProduction, RedefinitionOption.CHOICE, true);
            this.allowLookaheadAndNegations = allowLookaheadAndNegations;
        }

        /**
         * The default options for ABNF parsers.
         *
         * @return The default options for ABNF parsers.
         */
        public static @NotNull ABNFOptions getDefault() {
            return new ABNFOptions(null, null, false);
        }
    }

    /**
     * Constructs a {@link Parser} based on the grammar. Uses {@link ABNFOptions#getDefault()} as options.
     *
     * @param grammar The grammar.
     * @return A parser based on the provided grammar.
     * @see #parser(String, ABNFOptions)
     */
    public static @NotNull Parser parser(@NotNull String grammar) {
        return parser(grammar, ABNFOptions.getDefault());
    }

    /**
     * Constructs a {@link Parser} based on the grammar.
     * <p>
     * The `look` and `neg` rules are only added if {@link ABNFOptions#allowLookaheadAndNegations} is true for the options.
     * <p>
     * All options apply as described in {@link ParserCreationOptions}, except {@link ParserCreationOptions#getRedefinitionOption()} is set to {@link RedefinitionOption#CHOICE}.
     *
     * @param grammar The grammar.
     * @param options The options.
     * @return A parser based on the provided grammar.
     * @see #parser(String)
     */
    public static @NotNull Parser parser(@NotNull String grammar, @NotNull ABNFOptions options) {
        var abnfGrammarParser = Parsewisp.parser(
                baseGrammar(options),
                ParserCreationOptions.getDefault());
        var tree = abnfGrammarParser.parse(grammar);

        if (tree.isFailure()) {
            throw new ParserCreationFailure(tree.castToParseFailure().toString());
        }

        return Parsewisp.parser(new ABNF().transform(tree.castToParseSuccess()), null);
    }

    /// The base grammar of ABNF itself. It is defined as follows:
    /// ```
    /// rulelist = { c-wsp } rule (rule | <{ c-wsp }>)+
    /// rule = (hide-nt | nonterm) <{ c-wsp }> ("=" | "=/") <{ c-wsp }> alternation { WSP } [ c-nl ]
    /// nonterm = #"[a-zA-Z][a-zA-Z0-9\-]*"
    /// hide-nt = "<" #"[a-zA-Z][a-zA-Z0-9\-]*" ">"
    /// c-wsp = #"\s+" | c-nl
    /// c-nl = comment | #"\r?\n"
    /// comment = ";" { (WSP | #"^\S+") } (#"\r?\n" | eof)
    /// alternation = concatenation { (<{ c-wsp }> "/" <{ c-wsp }> concatenation) }
    /// concatenation = repetition { (<{ c-wsp }> repetition) }
    /// repetition = [ #"[0-9]*(\*[0-9]*)?" ] <{ c-wsp }> element
    /// element = nonterm | hide | group | option | char-val | regexp | num-val
    /// group = "(" <{ c-wsp }> alternation <{ c-wsp }> ")"
    /// hide = "<" <{ c-wsp }> alternation <{ c-wsp }> ">"
    /// option = "[" <{ c-wsp }> alternation <{ c-wsp }> "]"
    /// look = "&" <{ c-wsp }> element
    /// neg = "!" <{ c-wsp }> element
    /// char-val = #"(%[is])?" #""[^"\\]*(?:\\.[^"\\]*)*""
    /// regexp = #"#'[^'\\]*(?:\\.[^'\\]*)*'" | #"#\"[^\"\\]*(?:\\.[^\"\\]*)*\""
    /// num-val = "%" (bin-val | dec-val | hex-val)
    /// bin-val = "b" #"[01]+([.01]*[01]|-[01]+)?"
    /// dec-val = "d" #"[0-9]+([.0-9]*[0-9]|-[0-9]+)?"
    /// hex-val = "x" #"[a-fA-F0-9]+([.a-fA-F0-9]*[a-fA-F0-9]|-[a-fA-F0-9]+)?"
    /// WSP = #"[\\u0020\\u0009]"
    /// ```
    ///
    /// The `look` and `neg` rules are only added if {@link ABNFOptions#allowLookaheadAndNegations} is true for the options.
    ///
    /// All options apply as described in {@link ParserCreationOptions}, except {@link ParserCreationOptions#getRedefinitionOption()} is set to {@link RedefinitionOption#CHOICE}.
    ///
    /// @param options The options.
    /// @return The grammar which parses ABNF grammars.
    public static @NotNull Grammar baseGrammar(final @NotNull ABNFOptions options) {
        return new AbnfGrammarParserGrammarBuilder(options).build();
    }

    private @NotNull Grammar transform(
            final @NotNull ParseTree parsedABNFGrammar) {
        return new Transformer().transform(parsedABNFGrammar);
    }

    private static final class Transformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();

        Transformer() {
            super(RedefinitionOption.CHOICE);
        }

        private Grammar transform(ParseTree tree) {
            Map<@NotNull Sym, @NotNull Function<@NotNull List<Object>, Object>> transformMap = new HashMap<>();
            transformMap.put(Sym.sym("rulelist"),
                    this::rulelist);
            transformMap.put(Sym.sym("rule"),
                    c -> Map.entry(c.get(0), c.get(2)));
            transformMap.put(Sym.sym("nonterm"),
                    c -> nt(c.get(0).toString()));
            transformMap.put(Sym.sym("hide-nt"),
                    c -> nt(c.get(1).toString()).enableHideTag());
            transformMap.put(Sym.sym("c-wsp"),
                    this::ignore);
            transformMap.put(Sym.sym("c-nl"),
                    this::ignore);
            transformMap.put(Sym.sym("comment"),
                    this::ignore);
            transformMap.put(Sym.sym("alternation"),
                    c -> altList(rulesNotNull(c)));
            transformMap.put(Sym.sym("concatenation"),
                    c -> cat(rulesNotNull(c)));
            transformMap.put(Sym.sym("repetition"),
                    c -> (c.size() == 1)
                            ? makeRepRule(null, c.get(0))
                            : makeRepRule(c.get(0).toString(), c.get(1)));
            transformMap.put(Sym.sym("element"),
                    c -> c.get(0));
            transformMap.put(Sym.sym("group"),
                    c -> c.get(1));
            transformMap.put(Sym.sym("hide"),
                    c -> ((Rule) c.get(1)).enableHideTag());
            transformMap.put(Sym.sym("option"),
                    c -> opt((Rule) c.get(1)));
            transformMap.put(Sym.sym("look"),
                    c -> look((Rule) c.get(1)));
            transformMap.put(Sym.sym("neg"),
                    c -> neg((Rule) c.get(1)));
            transformMap.put(Sym.sym("char-val"),
                    c -> string(
                            strParser.processString((String) c.get(1)),
                            ((String) c.get(0)).isEmpty() || ((String) c.get(0)).charAt(1) == 'i'));
            transformMap.put(Sym.sym("regexp"),
                    c -> regex(strParser.processRegexp(c.get(0).toString())));
            transformMap.put(Sym.sym("num-val"),
                    c -> c.get(1));
            transformMap.put(Sym.sym("bin-val"),
                    c -> numValHelper((String) c.get(1), 2));
            transformMap.put(Sym.sym("dec-val"),
                    c -> numValHelper((String) c.get(1), 10));
            transformMap.put(Sym.sym("hex-val"),
                    c -> numValHelper((String) c.get(1), 16));
            transformMap.put(Sym.sym("WSP"),
                    this::ignore);
            return Transform.transform(tree, transformMap, r -> (Grammar) r);
        }

        private @NotNull Grammar rulelist(List<Object> content) {
            for (Object r : content) {
                if (r == null) continue;
                @SuppressWarnings("unchecked")
                var prod = (Map.Entry<NonTerminal, Rule>) r;
                var lhs = prod.getKey().isHidden() ? prod.getValue().hideTag() : prod.getValue();
                addProduction(prod.getKey().getKeyword(), lhs);
            }
            var CRLF = string("\r\n", false);
            var WSP = regex(Pattern.compile("[\\u0020\\u0009]"));
            addProduction(Sym.sym("ALPHA"), regex(Pattern.compile("[a-zA-Z]")));
            addProduction(Sym.sym("BIT"), regex(Pattern.compile("[01]")));
            addProduction(Sym.sym("CHAR"), regex(Pattern.compile("[\\u0001-\\u007F]")));
            addProduction(Sym.sym("CR"), string("\r", false));
            addProduction(Sym.sym("CRLF"), CRLF);
            addProduction(Sym.sym("CTL"), regex(Pattern.compile("[\\u0000-\\u001F|\\u007F]")));
            addProduction(Sym.sym("DIGIT"), regex(Pattern.compile("[0-9]")));
            addProduction(Sym.sym("DQUOTE"), string("\"", false));
            addProduction(Sym.sym("HEXDIG"), regex(Pattern.compile("[0-9a-fA-F]")));
            addProduction(Sym.sym("HTAB"), regex(Pattern.compile("\t")));
            addProduction(Sym.sym("LF"), regex(Pattern.compile("\n")));
            addProduction(Sym.sym("LWSP"), zeroOrMore(alt(WSP, cat(CRLF, WSP))));
            addProduction(Sym.sym("OCTET"), regex(Pattern.compile("[\\u0000-\\u00FF]")));
            addProduction(Sym.sym("SP"), string(" ", false));
            addProduction(Sym.sym("VCHAR"), regex(Pattern.compile("[\\u0021-\\u007E]")));
            addProduction(Sym.sym("WSP"), WSP);
            return build();
        }

        private @NotNull List<Rule> rulesNotNull(@NotNull List<Object> content) {
            return content.stream().filter(Objects::nonNull).map(this::of).toList();
        }

        private @Nullable Object ignore(List<Object> content) {
            return null;
        }

        private Rule numValHelper(@NotNull String digitStr, int radix) {
            var minusIndex = digitStr.indexOf('-');
            if (minusIndex < 0) {
                var sb = new StringBuilder();
                for (String part : digitStr.split("\\.")) {
                    sb.appendCodePoint(Integer.parseInt(part, radix));
                }
                return string(sb.toString());
            }

            var parts = digitStr.split("-");
            var min = Integer.parseInt(parts[0], radix);
            var max = Integer.parseInt(parts[1], radix);
            return numVal(min, max);
        }

        private Rule makeRepRule(@Nullable String s, Object rule) {
            if (s == null || s.isEmpty()) {
                return (Rule) rule;
            }

            var parts = s.split("\\*");
            final int min, max;

            if (parts.length == 1) {
                // Format at this point is [0-9]+\\* or \\*[0-9]+ or [0-9]+

                if (s.charAt(0) == '*') {
                    // Only maximum given
                    min = 0;
                    max = Integer.parseInt(parts[0]);
                } else if (s.charAt(s.length() - 1) == '*') {
                    // Only minimum given
                    min = Integer.parseInt(parts[0]);
                    max = Integer.MAX_VALUE;
                } else {
                    // Exact number given
                    min = Integer.parseInt(parts[0]);
                    max = min;
                }
            } else if (parts.length == 0) {
                // No minimum, no maximum
                return zeroOrMore((Rule) rule);
            } else {
                min = parts[0].isEmpty() ? 0 : Integer.parseInt(parts[0]);
                max = Integer.parseInt(parts[1]);
            }
            try {
                return rep((Rule) rule, min, max);
            } catch (IllegalArgumentException iae) {
                throw new ParserCreationFailure(iae);
            }
        }

        @Override
        protected void make() {
        }
    }

    private static final class AbnfGrammarParserGrammarBuilder extends GrammarBuilder {
        private final ABNFOptions abnfOptions;

        private AbnfGrammarParserGrammarBuilder(
                @NotNull ABNFOptions abnfOptions) {
            super();
            this.abnfOptions = abnfOptions;
        }

        NonTerminal alternation = nt("alternation");
        NonTerminal binVal = nt("bin-val");
        NonTerminal cNl = nt("c-nl");
        NonTerminal cWsp = nt("c-wsp");
        NonTerminal charVal = nt("char-val");
        NonTerminal comment = nt("comment");
        NonTerminal concatenation = nt("concatenation");
        NonTerminal decVal = nt("dec-val");
        NonTerminal element = nt("element");
        NonTerminal group = nt("group");
        NonTerminal hexVal = nt("hex-val");
        NonTerminal hide = nt("hide");
        NonTerminal hideNt = nt("hide-nt");
        NonTerminal nonterm = nt("nonterm");
        NonTerminal numVal = nt("num-val");
        NonTerminal option = nt("option");
        NonTerminal regexp = nt("regexp");
        NonTerminal repetition = nt("repetition");
        NonTerminal rule = nt("rule");
        NonTerminal rulelist = nt("rulelist");
        NonTerminal WSP = nt("WSP");

        Rule cWspRepeat = zeroOrMore(cWsp).enableHideTag();
        Rule newline = regex(Pattern.compile("\\r?\\n"));

        @Override
        protected void make() {
            // rulelist       =  1*( rule / (*WSP c-nl) )
            addProduction(
                    rulelist.getKeyword(),
                    cat(zeroOrMore(cWspRepeat), rule,
                            onceOrMore(alt(rule, cWspRepeat))));

            // rule           =  (nonterm / hide-nt) defined-as elements c-nl
            // defined-as     =  *c-wsp ("=" / "=/") *c-wsp
            // elements       =  alternation *WSP
            addProduction(
                    rule.getKeyword(),
                    cat(alt(hideNt, nonterm),
                            cWspRepeat,
                            alt(string("="), string("=/")),
                            cWspRepeat,
                            alternation,
                            zeroOrMore(WSP),
                            opt(cNl)));

            // nonterm       =  ALPHA *(ALPHA / DIGIT / "-")
            addProduction(
                    nonterm.getKeyword(),
                    regex("[a-zA-Z][a-zA-Z0-9\\-]*(?x) # NonTerminal"));
            addProduction(
                    hideNt.getKeyword(),
                    cat(string("<"), regex("[a-zA-Z][a-zA-Z0-9\\-]*(?x) # Non-terminal"), string(">")));

            // c-wsp          =  WSP / (c-nl WSP)
            addProduction(
                    cWsp.getKeyword(),
                    alt(regex(Pattern.compile("\\s+")), cNl));

            // c-nl           =  comment / CRLF ; comment or newline
            addProduction(
                    cNl.getKeyword(),
                    alt(comment, newline));

            // comment        =  ";" *(WSP / VCHAR) CRLF
            addProduction(
                    comment.getKeyword(),
                    cat(string(";"), zeroOrMore(alt(WSP, regex("^\\S+(?x) # Comment until newline/eof"))), alt(newline, eof())));

            // alternation    =  concatenation *(*c-wsp "/" *c-wsp concatenation)
            addProduction(
                    alternation.getKeyword(),
                    cat(concatenation, zeroOrMore(cat(cWspRepeat, string("/"), cWspRepeat, concatenation))));

            // concatenation  =  repetition *(1*c-wsp repetition)
            addProduction(
                    concatenation.getKeyword(),
                    cat(repetition, zeroOrMore(cat(cWspRepeat, repetition))));

            // repetition     =  [repeat] element
            addProduction(
                    repetition.getKeyword(),
                    cat(opt(regex("[0-9]*(\\*[0-9]*)?")), cWspRepeat, element));

            // element        =  nonterm / hide / group / option / char-val / num-val
            var elementAlternatives = new ArrayList<Rule>(List.of(
                    nonterm, hide, group, option, charVal, regexp, numVal));
            if (abnfOptions.allowLookaheadAndNegations) {
                elementAlternatives.addAll(List.of(nt("look"), nt("neg")));
            }
            addProduction(
                    element.getKeyword(),
                    altList(elementAlternatives));

            // group          =  "(" *c-wsp alternation *c-wsp ")"
            addProduction(
                    group.getKeyword(),
                    cat(string("("), cWspRepeat, alternation, cWspRepeat, string(")")));

            // hide          =  "<" *c-wsp alternation *c-wsp ">"
            addProduction(
                    hide.getKeyword(),
                    cat(string("<"), cWspRepeat, alternation, cWspRepeat, string(">")));

            // option         =  "[" *c-wsp alternation *c-wsp "]"
            addProduction(
                    option.getKeyword(),
                    cat(string("["), cWspRepeat, alternation, cWspRepeat, string("]")));

            // look = <'&' opt-whitespace> element;
            addProduction(
                    Sym.sym("look"),
                    cat(string("&"), cWspRepeat, element));

            // neg = <'!' opt-whitespace> element;
            addProduction(
                    Sym.sym("neg"),
                    cat(string("!"), cWspRepeat, element));

            // char-val       =  [ "%i" / "%s" ] DQUOTE *(%x20-21 / %x23-7E) DQUOTE ; quoted string of SP and VCHAR without DQUOTE
            // char-val       =  #"(%[is])?\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"";
            addProduction(
                    charVal.getKeyword(),
                    concatNoEpsilonMoreThan1(List.of(
                            regex("(%[is])?(?x) # String prefix"),
                            regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String"))));

            // regexp         = #'[^'\\\\]*(?:\\\\.[^'\\\\]*)*' / #\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"
            final @NotNull Rule rulesRule =
                    alternationGuaranteeDistinctAndNotEmpty(
                            List.of(regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex"),
                                    regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) # Regex")));
            addProduction(
                    regexp.getKeyword(),
                    rulesRule);

            // num-val        =  "%" (bin-val / dec-val / hex-val)
            addProduction(
                    numVal.getKeyword(),
                    cat(string("%"), alt(binVal, decVal, hexVal)));

            // bin-val        =  "b" 1*BIT [ 1*("." 1*BIT) / ("-" 1*BIT) ] ; series of concatenated bit values or single ONEOF range
            // b [0-1]+
            // b [0-1]+ ( "." [0-1]+ )+
            // b [0-1]+ "-" [0-1]+
            addProduction(
                    binVal.getKeyword(),
                    cat(string("b"), regex("[01]+([.01]*[01]|-[01]+)?(?x) # Binary num-val")));

            // dec-val        =  "d" 1*DIGIT [ 1*("." 1*DIGIT) / ("-" 1*DIGIT) ]
            addProduction(
                    decVal.getKeyword(),
                    cat(string("d"), regex("[0-9]+([.0-9]*[0-9]|-[0-9]+)?(?x) # Decimal num-val")));

            // hex-val        =  "x" 1*HEXDIG [ 1*("." 1*HEXDIG) / ("-" 1*HEXDIG) ]
            addProduction(
                    hexVal.getKeyword(),
                    cat(string("x"), regex("[a-fA-F0-9]+([.a-fA-F0-9]*[a-fA-F0-9]|-[a-fA-F0-9]+)?(?x) # Hexadecimal num-val")));

            addProduction(WSP.getKeyword(), regex("[\\u0020\\u0009](?x) # Whitespace"));
        }
    }
}
