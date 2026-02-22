package io.github.osmanys_perez.neutrotest.junit5;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicAssert;
import io.github.osmanys_perez.neutrotest.NeutrosophicAssertion;
import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Entry point for JUnit 5 integrated Neutrosophic assertions.
 * <p>
 * Use this class in tests annotated with {@link NeutrosophicTest}.
 * It automatically retrieves the context from the JUnit {@link ExtensionContext}.
 */
public final class NeutrosophicAssertions {

    private NeutrosophicAssertions() {
        // Utility class
    }

    /**
     * The ONLY assertThat method. Returns the NeutrosophicAssertion for fluent chaining.
     * It is the caller's responsibility to call .isTrue(), .isFalse(), .isAccepted(), or .isRejected().
     */
    public static <T> NeutrosophicAssertion<T> assertThat(T actual, Evaluator<T> evaluator, ExtensionContext context) {
        NeutrosophicContext neutrosophicContext = NeutrosophicTestExtension.getContext(context);
        if (neutrosophicContext == null) {
            throw new IllegalStateException("No NeutrosophicContext found. Please add @NeutrosophicTest annotation.");
        }
        return NeutrosophicAssert.assertThat(actual, evaluator, neutrosophicContext);
    }
}