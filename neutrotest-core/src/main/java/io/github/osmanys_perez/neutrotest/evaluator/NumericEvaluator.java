package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

/**
 * Evaluates numbers based on approximate equality to an expected value within a given tolerance.
 * Useful for floating-point comparisons and performance testing.
 */
public final class NumericEvaluator implements Evaluator<Number> {

    private final Number expected;
    private final double tolerance;

    private NumericEvaluator(Number expected, double tolerance) {
        this.expected = expected;
        this.tolerance = Math.abs(tolerance); // Ensure tolerance is positive
    }

    /**
     * Creates an NumericEvaluator builder with the expected number to compare against.
     * @param expected the expected number
     * @return an instance of the evaluator's builder
     */
    public static NumericEvaluator comparedTo(Number expected) {
        return new NumericEvaluator(expected, 0.01); // Default tolerance of 0.01
    }

    /**
     * Fluently sets a custom tolerance for the comparison.
     * @param tolerance the acceptable difference between the actual and expected values
     * @return this evaluator instance for method chaining
     */
    public NumericEvaluator withTolerance(double tolerance) {
        return new NumericEvaluator(this.expected, tolerance);
    }

    @Override
    public NeutrosophicValue evaluate(Number actual) {
        if (expected == null && actual == null) {
            return NeutrosophicValue.TRUE;
        }
        if (expected == null || actual == null) {
            return new NeutrosophicValue(0.0, 0.1, 0.9); // One is null, mostly false
        }

        double expectedDouble = expected.doubleValue();
        double actualDouble = actual.doubleValue();
        double difference = Math.abs(actualDouble - expectedDouble);

        // Calculate a "similarity" score between 0.0 and 1.0
        // 1.0 = perfect match, 0.0 = completely different
        double similarity;
        if (difference <= tolerance) {
            // Within tolerance: Map to a high similarity (1.0 when difference is 0)
            similarity = 1.0 - (difference / tolerance);
        } else {
            // Outside tolerance: similarity decays based on how far outside it is
            // We add 1 to the denominator to avoid division by zero and control the decay rate
            similarity = 1.0 / (1.0 + (difference - tolerance));
        }

        // Map the similarity score to a NeutrosophicValue
        // High similarity -> High Truth, Low Indeterminacy and Falsity
        double truth = similarity;
        // Indeterminacy is low for numeric comparisons; we're usually quite certain.
        // It could be higher if the tolerance is very large and the meaning is fuzzy.
        double indeterminacy = 0.05 * (1 - similarity);
        double falsity = 1.0 - similarity - indeterminacy;

        // Ensure normalization (T+I+F <= 1)
        double total = truth + indeterminacy + falsity;
        if (total > 1.0) {
            truth = truth / total;
            indeterminacy = indeterminacy / total;
            falsity = falsity / total;
        }

        return new NeutrosophicValue(truth, indeterminacy, falsity);
    }
}