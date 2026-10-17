package de.kleinert.parsewisp.ebnf;

import de.kleinert.parsewisp.grammar.GrammarPrinter;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class EBNFPrinter implements GrammarPrinter {
    public EBNFPrinter() {
    }

    @Override
    public @NotNull String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" | "));
    }

    @Override
    public @NotNull String catToString(@NotNull ConcatRule rule) {
        return rule.getRules().stream().map((it) -> this.parens(rule, it)).collect(Collectors.joining(" , "));
    }

    @Override
    public @NotNull String optToString(final @NotNull OptionalRule rule) {
        return "[ " + ruleToString(rule.getRule()) + " ]";
    }

    @Override
    public @NotNull String strTermToString(final @NotNull StringTerm rule) {
        return '"' + escape(rule.getString(), '"') + '"';
    }

    @Override
    public @NotNull String zeroOrMoreToString(final @NotNull ZeroOrMoreRule rule) {
        return "{ " + rule.getRule() + " }";
    }

    @Override
    public @NotNull String epsToString(final @NotNull EpsilonTerm rule) {
        return "\"\"";
    }
}
