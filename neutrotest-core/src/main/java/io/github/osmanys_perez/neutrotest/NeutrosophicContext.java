package io.github.osmanys_perez.neutrotest;

/**
 * Configures the interpretation of a {@link NeutrosophicValue} for making an assertion.
 * It defines the thresholds for truth, indeterminacy, and falsehood that determine
 * the outcome of a test.
 * <p>
 * This class uses the Builder pattern for flexible and readable configuration.
 * </p>
 * <p>
 * A standard context might require truth >= 0.8 and indeterminacy < 0.2 to pass.
 * </p>
 */
public final class NeutrosophicContext {

    private final double truthThreshold;
    private final double indeterminacyThreshold;
    private final double falsityThreshold;
    private final double tolerance; // Useful for numerical comparisons

    private NeutrosophicContext(Builder builder) {
        this.truthThreshold = builder.truthThreshold;
        this.indeterminacyThreshold = builder.indeterminacyThreshold;
        this.falsityThreshold = builder.falsityThreshold;
        this.tolerance = builder.tolerance;
    }

    public double truthThreshold() {
        return truthThreshold;
    }

    public double indeterminacyThreshold() {
        return indeterminacyThreshold;
    }

    public double falsityThreshold() {
        return falsityThreshold;
    }

    public double tolerance() {
        return tolerance;
    }

    /**
     * The core evaluation function. Evaluates the given NeutrosophicValue against this context.
     * @param value the value to evaluate
     * @return true if the value meets the criteria for success defined by this context.
     */
    public boolean evaluate(NeutrosophicValue value) {
        // The assertion passes if:
        // 1. Truth is high enough AND
        // 2. Indeterminacy is low enough AND
        // 3. Falsehood is low enough (optional, often implied by truth being high)
        return (value.truth() >= truthThreshold) &&
                (value.indeterminacy() < indeterminacyThreshold) &&
                (value.falsity() < falsityThreshold); // Often falsityThreshold is (1 - truthThreshold)
    }

    /**
     * Creates a new Builder instance with default values.
     * @return a new Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * The Builder for NeutrosophicContext.
     */
    public static final class Builder {
        private double truthThreshold = 0.8;
        private double indeterminacyThreshold = 0.2;
        private double falsityThreshold = 0.2;
        private double tolerance = 0.01;

        private Builder() {}

        public Builder withTruthThreshold(double truthThreshold) {
            this.truthThreshold = truthThreshold;
            return this;
        }

        public Builder withIndeterminacyThreshold(double indeterminacyThreshold) {
            this.indeterminacyThreshold = indeterminacyThreshold;
            return this;
        }

        public Builder withFalsityThreshold(double falsityThreshold) {
            this.falsityThreshold = falsityThreshold;
            return this;
        }

        public Builder withTolerance(double tolerance) {
            this.tolerance = tolerance;
            return this;
        }

        /**
         * Builds and validates the NeutrosophicContext.
         * @return a new, immutable NeutrosophicContext instance.
         */
        public NeutrosophicContext build() {
            // Validate the thresholds make sense
            validateComponent(truthThreshold, "Truth Threshold");
            validateComponent(indeterminacyThreshold, "Indeterminacy Threshold");
            validateComponent(falsityThreshold, "Falsity Threshold");
            if (tolerance < 0.0) {
                throw new IllegalArgumentException("Tolerance must be a non-negative number.");
            }
            // You might add more complex validation, e.g., if (truthThreshold + falsityThreshold > 1.0) { warn? }
            return new NeutrosophicContext(this);
        }

        private void validateComponent(double value, String name) {
            if (value < 0.0 || value > 1.0) {
                throw new IllegalArgumentException(name + " must be between 0.0 and 1.0, but was " + value);
            }
        }
    }

    /**
     * Returns a default context with standard thresholds.
     * This is a convenience method for the most common case.
     * @return the default context
     */
    public static NeutrosophicContext defaultContext() {
        return builder().build();
    }

    /**
     * Returns a strict context that requires near-certainty.
     * @return a strict context
     */
    public static NeutrosophicContext strictContext() {
        return builder()
                .withTruthThreshold(0.95)
                .withIndeterminacyThreshold(0.05)
                .withFalsityThreshold(0.05)
                .build();
    }

    /**
     * Returns a lenient context that is more tolerant of indeterminacy.
     * Useful for heuristic or fuzzy checks.
     * @return a lenient context
     */
    public static NeutrosophicContext lenientContext() {
        return builder()
                .withTruthThreshold(0.6)
                .withIndeterminacyThreshold(0.4)
                .withFalsityThreshold(0.4)
                .build();
    }
}