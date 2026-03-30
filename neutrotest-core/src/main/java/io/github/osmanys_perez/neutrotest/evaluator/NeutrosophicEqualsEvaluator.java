package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

import java.util.Objects;

/**
 * Neutrosophic equality evaluator that acknowledges indeterminacy in equality comparisons.
 * Useful for philosophical consistency or when dealing with potentially ambiguous comparisons.
 */
public final class NeutrosophicEqualsEvaluator<T> implements Evaluator<T> {

    private final T expected;

    private NeutrosophicEqualsEvaluator(T expected) {
        this.expected = expected;
    }

    /**
     * Creates a {@link NeutrosophicEqualsEvaluator} for the expected value.
     *
     * @param <T>      the type of the value
     * @param expected the expected value to compare against
     * @return a new {@code NeutrosophicEqualsEvaluator}
     */
    public static <T> NeutrosophicEqualsEvaluator<T> comparedTo(T expected) {
        return new NeutrosophicEqualsEvaluator<>(expected);
    }

    @Override
    public NeutrosophicValue evaluate(T actual) {
        // Both null: Definite equality, but with philosophical doubt about "nothingness"
        if (expected == null && actual == null) {
            return new NeutrosophicValue(0.98, 0.01, 0.01);
        }

        // One null, one not: Mostly false, but uncertainty about what null represents
        if (expected == null || actual == null) {
            return new NeutrosophicValue(0.1, 0.3, 0.6);
        }

        // Both non-null: Use standard equals() but acknowledge potential for doubt
        boolean isEqual = Objects.equals(expected, actual);

        if (isEqual) {
            // High confidence in equality, but acknowledge possible edge cases or
            // implementation details that might create microscopic doubt
            return new NeutrosophicValue(0.95, 0.04, 0.01);
        } else {
            // Definitely not equal, but allow for some indeterminacy - perhaps they
            // could be considered equal under a different context or interpretation?
            return new NeutrosophicValue(0.05, 0.15, 0.80);
        }
    }

    @Override
    public String getExpectedValueDescription() {
        if (expected == null) return "null";
        if (expected instanceof CharSequence) return "\"" + expected + "\"";
        return expected.toString();
    }
}