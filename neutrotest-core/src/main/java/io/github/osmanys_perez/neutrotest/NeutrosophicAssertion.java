package io.github.osmanys_perez.neutrotest;

import java.util.Locale;

/**
 * Assertion class for neutrosophic values. The result of calling
 * {@link NeutrosophicAssert#assertThat(Object, Evaluator, NeutrosophicContext)}.
 * <p>
 * This class is responsible for executing the evaluation and context check,
 * and throwing a rich assertion error if the conditions are not met.
 * </p>
 *
 * @param <T> the type of the value under test
 */
public final class NeutrosophicAssertion<T> {

    private final T actual;
    private final Evaluator<T> evaluator;
    private final NeutrosophicContext context;

    // Package-private constructor. Use NeutrosophicAssert.assertThat().
    NeutrosophicAssertion(T actual, Evaluator<T> evaluator, NeutrosophicContext context) {
        this.actual = actual;
        this.evaluator = evaluator;
        this.context = context;
    }

    /**
     * Asserts that the evaluated neutrosophic value meets the success criteria
     * defined in the context (i.e., context.evaluate() returns true).
     *
     * @return the evaluated neutrosophic value
     * @throws AssertionError if the assertion fails
     */
    public NeutrosophicValue isTrue() {
        NeutrosophicValue result = evaluator.evaluate(actual);
        if (!context.evaluate(result)) {
            throw new AssertionError(buildFailureMessage(result));
        }
        return result;
    }

    /**
     * Asserts that the evaluated neutrosophic value does NOT meet the success criteria
     * defined in the context (i.e., context.evaluate() returns false).
     *
     * @return the evaluated neutrosophic value
     * @throws AssertionError if the assertion fails (i.e., if the value IS true)
     */
    public NeutrosophicValue isFalse() {
        NeutrosophicValue result = evaluator.evaluate(actual);
        if (context.evaluate(result)) {
            throw new AssertionError(buildFailureMessageForIsFalse(result));
        }
        return result;
    }

    /**
     * A more descriptive alias for {@link #isTrue()}.
     * Asserts that the proposition is accepted under the given context.
     *
     * @return the evaluated neutrosophic value
     */
    public NeutrosophicValue isAccepted() {
        return isTrue();
    }

    /**
     * A more descriptive alias for {@link #isFalse()}.
     * Asserts that the proposition is rejected under the given context.
     *
     * @return the evaluated neutrosophic value
     */
    public NeutrosophicValue isRejected() {
        return isFalse();
    }

    /**
     * Builds a detailed error message for a failed 'isTrue' assertion.
     * This message is crucial for debugging and understanding why the test failed.
     */
    private String buildFailureMessage(NeutrosophicValue result) {
        String expectedDesc = evaluator.getExpectedValueDescription();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%nNeutrosophic Assertion Failed:%n"));
        sb.append(String.format(Locale.US, "  Actual value.......: %s%n", formatActual()));
        if (expectedDesc != null) {
            sb.append(String.format(Locale.US, "  Expected value.....: %s%n", expectedDesc));
        }
        sb.append(String.format(Locale.US, "  Evaluated to.......: %s%n", result));
        sb.append(String.format(Locale.US, "  But context required: Truth >= %.2f, Indeterminacy < %.2f, Falsity < %.2f%n",
                context.truthThreshold(), context.indeterminacyThreshold(), context.falsityThreshold()));
        sb.append(String.format(Locale.US, "  Context (Tolerance): %.4f", context.tolerance()));

        return sb.toString();
    }

    private String buildFailureMessageForIsFalse(NeutrosophicValue result) {
        String expectedDesc = evaluator.getExpectedValueDescription();
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "%nNeutrosophic Assertion Failed:%n"));
        sb.append(String.format(Locale.US, "  Actual value.......: %s%n", formatActual()));
        if (expectedDesc != null) {
            sb.append(String.format(Locale.US, "  Expected value.....: %s%n", expectedDesc));
        }
        sb.append(String.format(Locale.US, "  Evaluated to.......: %s%n", result));
        sb.append(String.format(Locale.US, "  Context required this to be *false* (not accepted), but it was true.%n"));
        sb.append(String.format(Locale.US, "  Context thresholds.: Truth >= %.2f, Indeterminacy < %.2f, Falsity < %.2f",
                context.truthThreshold(), context.indeterminacyThreshold(), context.falsityThreshold()));

        return sb.toString();
    }

    private String formatActual() {
        if (actual == null) {
            return "null";
        }
        // Quote strings for better readability
        if (actual instanceof CharSequence) {
            return "\"" + actual + "\"";
        }
        // Format arrays nicely?
        return actual.toString();
    }
}