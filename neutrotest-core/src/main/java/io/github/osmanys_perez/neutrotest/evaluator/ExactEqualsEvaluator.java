package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

import java.util.Objects;

/**
 * Traditional exact equality evaluator that returns binary results (TRUE or FALSE)
 * with zero indeterminacy. Use this for classic, deterministic equality checks.
 */
public final class ExactEqualsEvaluator<T> implements Evaluator<T> {

    private final T expected;

    private ExactEqualsEvaluator(T expected) {
        this.expected = expected;
    }

    /**
     * Creates an ExactEqualsEvaluator for the expected value.
     * @param expected the expected value to compare against
     * @return an instance of the evaluator
     */
    public static <T> ExactEqualsEvaluator<T> comparedTo(T expected) {
        return new ExactEqualsEvaluator<>(expected);
    }

    @Override
    public NeutrosophicValue evaluate(T actual) {
        if (Objects.equals(expected, actual)) {
            return NeutrosophicValue.TRUE; // (T=1.0, I=0.0, F=0.0)
        } else {
            return NeutrosophicValue.FALSE; // (T=0.0, I=0.0, F=1.0)
        }
    }
}