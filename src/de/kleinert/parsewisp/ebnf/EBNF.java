package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.Parsewisp;
import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.error.ParserCreationFailure;
import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser.Parser;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;
import de.kleinert.parsewisp.parsing.NonTerminal;
import de.kleinert.parsewisp.parsing.Rule;
import de.kleinert.parsewisp.result.ParseTree;
import de.kleinert.parsewisp.util.StrParser;
import de.kleinert.parsewisp.util.Transform;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Grammar from <a href="http://www.cl.cam.ac.uk/~mgk25/iso-14977.pdf">iso-14977 (1996)</a>
 */
public class EBNF {

    public static @NotNull Parser parser(String grammar) {
        var res = baseParser().parse(grammar);
        if (res.isFailure()) throw new ParserCreationFailure(res.castToParseFailure().toString());
        return Parsewisp.parser(new EBNFTransformer().transform(res.castToParseSuccess()), ParserCreationOptions.getDefault());
    }

    public static @NotNull Parser baseParser() {
        return Parsewisp.parser(baseGrammar(), ParserCreationOptions.getDefault());
    }

    public static @NotNull Grammar baseGrammar() {
        return new EBNFGrammarBuilder().build();
    }

    private static class EBNFTransformer extends GrammarBuilder {
        private final @NotNull StrParser strParser = new StrParser();

        private EBNFTransformer() {
            super(ParserCreationOptions.getDefault());
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
            m.put(Sym.sym("WSP"), ignoreMe);
            m.put(Sym.sym("cwsp"), ignoreMe);
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
            throw new UnsupportedOperationException("TODO");
        }

        protected void make() {
        }
    }

    private static class EBNFGrammarBuilder extends GrammarBuilder {
        private EBNFGrammarBuilder() {
            super(ParserCreationOptions.getDefault());
        }

        @Override
        protected void make() {
            var cwsp = hide(nt("cwsp"));

            addProduction("syntax",
                    cat(cwsp, nt("syntax_rule"), cwsp, zeroOrMore(cat(nt("syntax_rule"), cwsp))));

            addProduction("syntax_rule",
                    cat(alt(nt("meta_identifier"), nt("hide_nt")), cwsp, string("="), cwsp, nt("definitions_list"), cwsp, string(";")));

            addProduction("definitions_list",
                    cat(nt("ordered_definitions_list"), zeroOrMore(cat(cwsp, string("|"), cwsp, nt("ordered_definitions_list")))));

            addProduction("ordered_definitions_list",
                    cat(nt("single_definition"), zeroOrMore(cat(cwsp, string("/"), cwsp, nt("single_definition")))));

            addProduction("single_definition",
                    cat(nt("term"), zeroOrMore(cat(cwsp, string(","), cwsp, nt("term")))));

            addProduction("term",
                    cat(nt("factor"), opt(cat(cwsp, string("-"), cwsp, nt("exception")))));

            addProduction("exception",
                    nt("factor"));

            addProduction("factor",
                    cat(opt(cat(nt("integer"), string("*"), cwsp)), nt("primary")));

            addProduction("primary", alt(
                    nt("optional_sequence"),
                    nt("repeated_sequence"),
                    nt("special_sequence"),
                    nt("grouped_sequence"),
                    nt("meta_identifier"),
                    nt("terminal"),
                    nt("empty"),
                    nt("hide_seq")));

            addProduction("empty",
                    eps());

            addProduction("optional_sequence", alt(
                    cat(string("["), cwsp, nt("definitions_list"), cwsp, string("]")),
                    cat(string("(/"), cwsp, nt("definitions_list"), cwsp, string("/)"))));

            addProduction("repeated_sequence", alt(
                    cat(string("{"), cwsp, nt("definitions_list"), cwsp, string("}")),
                    cat(string("(:"), cwsp, nt("definitions_list"), cwsp, string(":)"))));

            addProduction("grouped_sequence",
                    cat(string("("), cwsp, nt("definitions_list"), cwsp, string(")")));

            addProduction("hide_seq",
                    cat(string("<"), cwsp, nt("definitions_list"), cwsp, string(">")));

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

            addProduction("comment",
                    cat(string("(*"), regex("^(?!\\*\\)).*"), string("*)")));

            addProduction("WSP",
                    regex(Pattern.compile("\\s*")));

            addProduction("cwsp", hide(alt(
                    nt("comment"),
                    nt("WSP"))));
        }
    }
}
