package de.kleinert.parsewisp.abnf;

import de.kleinert.parsewisp.grammar.GrammarPrinter;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class ABNFPrinter implements GrammarPrinter {
    public ABNFPrinter() {
    }

    public @NotNull String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" / "));
    }

    public @NotNull String onceOrMoreToString(final @NotNull OnceOrMoreRule rule) {
        return "1*" + parens(rule, rule.getRule());
    }

    public @NotNull String optToString(final @NotNull OptionalRule rule) {
        return "[" + ruleToString(rule.getRule()) + "]";
    }

    public @NotNull String literalToString(final @NotNull StringTerm rule) {
        if (rule.isCaseInsensitive()) return '"' + escape(rule.getString(), '"') + '"';
        return "%s\"" + escape(rule.getString(), '"') + '"';
    }

    public @NotNull String zeroOrMoreToString(final @NotNull ZeroOrMoreRule rule) {
        return "*" + parens(rule, rule.getRule());
    }

    public @NotNull String epsToString(final @NotNull EpsilonTerm rule) {
        return "\"\"";
    }
}
