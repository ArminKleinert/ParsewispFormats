package de.kleinert.parsewisp.qffms;

import de.kleinert.parsewisp.Sym;
import de.kleinert.parsewisp.grammar.GrammarPrinter;
import de.kleinert.parsewisp.parsing.*;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class QffMsPrinter implements GrammarPrinter {

    @Override
    public @NotNull String productionToString(@NotNull Sym sym, @NotNull Rule rhs) {
        return sym.name() + " = " + this.ruleToString(rhs);
    }

    @Override
    public int precedence(final @NotNull Rule rule) {
        if (rule instanceof ValueRangeTerm)
            return 65;
        return GrammarPrinter.super.precedence(rule);
    }

    @Override
    public @NotNull String altToString(final @NotNull AlternationRule rule) {
        return rule.getRules().stream()
                .map(it -> parens(rule, it))
                .collect(Collectors.joining(" | "));
    }

    @Override
    public @NotNull String repToString(@NotNull VariableRepetitionRule rule) {
        return parens(rule, rule.getRule()) + "{" + rule.getMin() + "," + rule.getMax() + "}";
    }

    @Override
    public @NotNull String epsToString(final @NotNull EpsilonTerm rule) {
        return "\"\"";
    }

    @Override
    public @NotNull String valRangeToString(@NotNull ValueRangeTerm rule) {
        StringBuilder sb = new StringBuilder();
        valRangePartToString(rule.getLo(), sb);
        if (rule.getLo() != rule.getHi()) {
            sb.append("..");
            valRangePartToString(rule.getHi(), sb);
        }
        return sb.toString();
    }

    private void valRangePartToString(int codepoint, @NotNull StringBuilder sb) {
        sb.append('\'');

        if (codepoint < 256 && !Character.isISOControl(codepoint)) {
            sb.append((char) codepoint);
        } else if (codepoint == '\'' || codepoint == '\r' || codepoint == '\n' || codepoint == '\t') {
            sb.append(escape(String.valueOf((char) codepoint), (char) codepoint));
        } else if (Character.charCount(codepoint) == 1) {
            sb.append(String.format("\\u%04X", codepoint));
        } else {
            char[] parts = Character.toChars(codepoint);
            sb.append(String.format("\\u%04X\\u%04X", (int) parts[0], (int) parts[1]));
        }

        sb.append('\'');
    }

    @Override
    public @NotNull String regexpToString(@NotNull RegexTerm rule) {
        return "´" + this.escape(rule.getRegexp().pattern(), '´') + "´";
    }

    @Override
    public @NotNull String strTermToString(@NotNull StringTerm rule) {
        return "´" + this.escape(rule.getString(), '"') + "´";
    }
}
