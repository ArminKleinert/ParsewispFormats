package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.grammar.Grammar;
import de.kleinert.parsewisp.grammar.GrammarBuilder;
import de.kleinert.parsewisp.parser_options.ParserCreationOptions;

public class EBNF {

    public static Grammar baseGrammar() {
        return new EBNFGrammarBuilder().build();
    }

    private static class EBNFGrammarBuilder extends GrammarBuilder {
        EBNFGrammarBuilder() {
            super(ParserCreationOptions.getDefault());
        }

        @Override
        protected void make() {
            addProduction("syntax",
                    cat(nt("syntax_rule"), zeroOrMore(nt("syntax_rule"))));

            addProduction("syntax_rule",
                    cat(nt("meta_identifier"), string("="), nt("definitions_list"), string(";")));

            addProduction("definitions_list",
                    cat(nt("ordered_definitions_list"), zeroOrMore(cat(string("|"), nt("ordered_definitions_list")))));

            addProduction("ordered_definitions_list",
                    cat(nt("single_definition"), zeroOrMore(cat(string("/"), nt("single_definition")))));

            addProduction("single_definition",
                    cat(nt("term"), opt(cat(nt("term"), string("-"), nt("exception")))));

            addProduction("exception",
                    nt("factor"));

            addProduction("factor",
                    cat(opt(cat(nt("integer"), string("*"))), nt("primary")));

            addProduction("primary",
                    alt(nt("optional_sequence"), nt("repeated_sequence"), nt("special_sequence"), nt("grouped_sequence"), nt("meta_identifier"), nt("terminal"), nt("empty")));

            addProduction("empty",
                    eps());

            addProduction("optional_sequence",
                    alt(cat(string("["), nt("definitions_list"), string("]")),
                            cat(string("(/"), nt("definitions_list"), string("/)"))));

            addProduction("repeated_sequence",
                    alt(cat(string("{"), nt("definitions_list"), string("}")),
                            cat(string("(:"), nt("definitions_list"), string(":)"))));

            addProduction("grouped_sequence",
                    cat(string("("), nt("definitions_list"), string(")")));

            addProduction("terminal",
                    alt(nt("\"string_terminal\""), nt("regex_terminal")));

            addProduction("string_terminal",
                    alt(regex("\"[^\"\\\\]*(?:\\\\.[^\"\\\\]*)*\"(?x) # String"),
                            regex("'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # String")));

            addProduction("regex_terminal",
                    alt(regex("#'[^'\\\\]*(?:\\\\.[^'\\\\]*)*'(?x) # Regex"),
                            regex("#\\\"[^\\\"\\\\]*(?:\\\\.[^\\\"\\\\]*)*\\\"(?x) # Regex")));

            addProduction("meta_identifier",
                    regex("[a-zA-Z][a-zA-Z0-9\\_]*(?x) # NonTerminal"));

            addProduction("integer",
                    regex("[0-9]+"));

            addProduction("special_sequence",
                    cat(string("?"), regex("[^?]+"), string("?")));

            // TODO
            addProduction("comment",
                    cat(string("(*"), string("*)")));
        }
    }
}
