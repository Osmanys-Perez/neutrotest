package io.github.osmanys_perez.neutrotest;

/**
 * A function that evaluates a target object or condition and returns a {@link NeutrosophicValue}
 * representing its truth, indeterminacy, and falsehood.
 *
 * @param <T> the type of the input to the evaluator
 */
@FunctionalInterface
public interface Evaluator<T> {
    /**
     * Evaluates the given input and returns a neutrosophic value.
     *
     * @param input the input to evaluate
     * @return the neutrosophic result of the evaluation
     */
    NeutrosophicValue evaluate(T input);
}