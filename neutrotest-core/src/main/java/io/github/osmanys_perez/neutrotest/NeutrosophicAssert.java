package io.github.osmanys_perez.neutrotest;

/**
 * The main entry point for Neutrosophic assertions.
 */
public final class NeutrosophicAssert {

    // Private constructor to prevent instantiation.
    private NeutrosophicAssert() {}

    /**
     * Creates a neutrosophic assertion for a given value, using the provided evaluator and context.
     * This is the main entry point to the library.
     *
     * @param <T>       the type of the value under test
     * @param actual    the actual value to assert on
     * @param evaluator the evaluator that will generate a NeutrosophicValue from the actual value
     * @param context   the context that defines the pass/fail criteria for the NeutrosophicValue
     * @return a new NeutrosophicAssertion instance for the given value
     */
    public static <T> NeutrosophicAssertion<T> assertThat(T actual, Evaluator<T> evaluator, NeutrosophicContext context) {
        return new NeutrosophicAssertion<>(actual, evaluator, context);
    }

    /**
     * Creates a neutrosophic assertion using the default context.
     * A convenience method for common use cases.
     *
     * @param <T>       the type of the value under test
     * @param actual    the actual value to assert on
     * @param evaluator the evaluator that will generate a NeutrosophicValue from the actual value
     * @return a new NeutrosophicAssertion instance with the default context
     */
    public static <T> NeutrosophicAssertion<T> assertThat(T actual, Evaluator<T> evaluator) {
        return assertThat(actual, evaluator, NeutrosophicContext.defaultContext());
    }
}