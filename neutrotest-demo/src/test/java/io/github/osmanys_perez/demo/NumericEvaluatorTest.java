package io.github.osmanys_perez.demo;


import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import io.github.osmanys_perez.neutrotest.evaluator.NumericEvaluator;
import org.junit.jupiter.api.Test;

import static io.github.osmanys_perez.neutrotest.NeutrosophicAssert.assertThat;

public class NumericEvaluatorTest {

    @Test
    void testApproximateEquality_Success() {
        double calculatedValue = 1.999998; // e.g., from a complex calculation
        double expectedValue = 2.0;

        // This will pass. The default tolerance (0.01) is more than enough.
        assertThat(calculatedValue, NumericEvaluator.comparedTo(expectedValue))
                .isTrue();

        // 2. Use a context that is strict, but not ultra-strict, for this very close match.
        NeutrosophicContext veryAccurateContext = NeutrosophicContext.builder()
                .withTruthThreshold(0.9) // Less strict than 0.95, but stricter than default 0.8
                .withIndeterminacyThreshold(0.05)
                .withFalsityThreshold(0.20)
                .build();

        assertThat(calculatedValue, NumericEvaluator.comparedTo(expectedValue).withTolerance(0.0001), veryAccurateContext)
                .isTrue();
    }

    @Test
    void testApproximateEquality_Failure() {
        double measuredTime = 105.0; // milliseconds
        double sla = 100.0; // milliseconds

        // This will fail. 105 is outside the default tolerance (0.01) of 100.
        try {
            assertThat(measuredTime, NumericEvaluator.comparedTo(sla))
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("Expected failure for performance SLA:");
            System.out.println(e.getMessage());
        }

        // But we can define a context that understands "performance degradation"
        // instead of "complete failure". We lower the truth requirement but are strict on indeterminacy.
        NeutrosophicContext performanceContext = NeutrosophicContext.builder()
                .withTruthThreshold(0.4) // We accept a weaker truth signal
                .withIndeterminacyThreshold(0.1) // But we want to be fairly certain
                .withFalsityThreshold(0.6) // And we have a higher tolerance for falsity
                .build();

        // This might pass, depending on the calculated NeutrosophicValue.
        // It evaluates the *degree* of failure, not just pass/fail.
        assertThat(measuredTime, NumericEvaluator.comparedTo(sla).withTolerance(10.0), performanceContext)
                .isTrue();
    }

    @Test
    void testWithDifferentNumberTypes() {
        // The evaluator works with any Number type
        Integer actualInt = 10;
        Long expectedLong = 10L;

        // This should pass
        assertThat(actualInt, NumericEvaluator.comparedTo(expectedLong))
                .isTrue();

        Float actualFloat = 3.1415f;
        Double expectedDouble = 3.14;

        // This should pass with a reasonable tolerance
        assertThat(actualFloat, NumericEvaluator.comparedTo(expectedDouble).withTolerance(0.01))
                .isTrue();
    }
}