package io.github.osmanys_perez.neutrotest;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a value in Neutrosophic logic, composed of three components:
 * <ul>
 *   <li>T: Degree of truth (membership)</li>
 *   <li>I: Degree of indeterminacy (unknown)</li>
 *   <li>F: Degree of falsehood (non-membership)</li>
 * </ul>
 * <p>
 * Each component is a value in the range [0.0, 1.0]. Standard neutrosophic logic
 * allows for their sum to be anything between 0.0 and 3.0, but for practical
 * application in testing, we often assume a normalized form where T + I + F <= 1.0.
 * This class enforces the normalized form by default, as it is most intuitive for
 * representing a single, coherent opinion about a proposition.
 * </p>
 * <p>
 * This class is immutable and thread-safe.
 * </p>
 */
public final class NeutrosophicValue {

    public static final NeutrosophicValue TRUE = new NeutrosophicValue(1.0, 0.0, 0.0);
    public static final NeutrosophicValue FALSE = new NeutrosophicValue(0.0, 0.0, 1.0);
    public static final NeutrosophicValue INDETERMINATE = new NeutrosophicValue(0.0, 1.0, 0.0);
    public static final NeutrosophicValue AMBIGUOUS = new NeutrosophicValue(0.33, 0.34, 0.33); // Example

    private final double truth;
    private final double indeterminacy;
    private final double falsity;

    /**
     * Constructs a new NeutrosophicValue.
     *
     * @param truth         the degree of truth (must be between 0.0 and 1.0 inclusive)
     * @param indeterminacy the degree of indeterminacy (must be between 0.0 and 1.0 inclusive)
     * @param falsity       the degree of falsehood (must be between 0.0 and 1.0 inclusive)
     * @throws IllegalArgumentException if any value is out of bounds or if the sum T + I + F > 1.0
     */
    public NeutrosophicValue(double truth, double indeterminacy, double falsity) {
        // Validation
        validateComponent(truth, "Truth");
        validateComponent(indeterminacy, "Indeterminacy");
        validateComponent(falsity, "Falsity");

        double sum = truth + indeterminacy + falsity;
        // Use a small epsilon for floating-point comparison
        double epsilon = 0.0000001;
        if (sum > 1.0 + epsilon) {
            throw new IllegalArgumentException(
                    String.format(Locale.US, "The sum of the components (T=%.2f, I=%.2f, F=%.2f = %.2f) must not exceed 1.0. " +
                                    "Consider normalizing your values or using a different neutrosophic model.",
                            truth, indeterminacy, falsity, sum));
        }

        this.truth = truth;
        this.indeterminacy = indeterminacy;
        this.falsity = falsity;
    }

    private void validateComponent(double value, String name) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException(name + " component must be between 0.0 and 1.0, but was " + value);
        }
    }

    // --- Getters ---
    public double truth() {
        return truth;
    }

    public double indeterminacy() {
        return indeterminacy;
    }

    public double falsity() {
        return falsity;
    }

    // --- Core Neutrosophic Operations ---
    // These operations allow for the logical combination of values,
    // which is essential for building complex assertions.

    /**
     * Performs a neutrosophic AND operation.
     * A common interpretation: T_and = min(T1, T2), I_and = max(I1, I2), F_and = max(F1, F2)
     * This is a conservative interpretation: the result is only as true as the least true operand,
     * and inherits the indeterminacy and falsehood of either.
     */
    public NeutrosophicValue and(NeutrosophicValue other) {
        double t = Math.min(this.truth, other.truth);
        double i = Math.max(this.indeterminacy, other.indeterminacy);
        double f = Math.max(this.falsity, other.falsity);
        return new NeutrosophicValue(t, i, f);
    }

    /**
     * Performs a neutrosophic OR operation.
     * A common interpretation: T_or = max(T1, T2), I_or = min(I1, I2), F_or = min(F1, F2)
     */
    public NeutrosophicValue or(NeutrosophicValue other) {
        double t = Math.max(this.truth, other.truth);
        double i = Math.min(this.indeterminacy, other.indeterminacy);
        double f = Math.min(this.falsity, other.falsity);
        return new NeutrosophicValue(t, i, f);
    }

    /**
     * Performs a neutrosophic NOT (complement) operation.
     * A straightforward interpretation: (T, I, F) -> (F, I, T)
     */
    public NeutrosophicValue not() {
        return new NeutrosophicValue(this.falsity, this.indeterminacy, this.truth);
    }

    // --- Utility Methods ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NeutrosophicValue that = (NeutrosophicValue) o;
        // Compare with a small epsilon to account for floating-point precision issues
        return Double.compare(that.truth, truth) == 0 &&
                Double.compare(that.indeterminacy, indeterminacy) == 0 &&
                Double.compare(that.falsity, falsity) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(truth, indeterminacy, falsity);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "(T=%.2f, I=%.2f, F=%.2f)", truth, indeterminacy, falsity);
    }
}