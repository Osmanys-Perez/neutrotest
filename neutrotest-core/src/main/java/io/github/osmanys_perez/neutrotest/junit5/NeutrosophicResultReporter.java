package io.github.osmanys_perez.neutrotest.junit5;

import io.github.osmanys_perez.neutrotest.NeutrosophicValue;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * Interface for reporting Neutrosophic test results to various reporting frameworks.
 */
public interface NeutrosophicResultReporter {
    /**
     * Reports the result of a Neutrosophic assertion.
     *
     * @param context   the JUnit extension context
     * @param status    the interpreted status (PASSED, FRAGILE PASS, BORDERLINE FAIL, FAILED)
     * @param value     the actual Neutrosophic value
     * @param threshold the truth threshold used for evaluation
     */
    void report(ExtensionContext context, String status, NeutrosophicValue value, double threshold);
}
