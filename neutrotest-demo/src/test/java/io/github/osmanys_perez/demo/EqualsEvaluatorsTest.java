package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import io.github.osmanys_perez.neutrotest.evaluator.ExactEqualsEvaluator;
import io.github.osmanys_perez.neutrotest.evaluator.NeutrosophicEqualsEvaluator;
import org.junit.jupiter.api.Test;

import static io.github.osmanys_perez.neutrotest.NeutrosophicAssert.assertThat;

public class EqualsEvaluatorsTest {

    @Test
    void testExactEquals() {
        String value = "test";

        // Traditional binary equality - will return (T=1.0, I=0.0, F=0.0)
        assertThat(value, ExactEqualsEvaluator.comparedTo("test"))
                .isTrue();

        // Traditional binary inequality - will return (T=0.0, I=0.0, F=1.0)
        try {
            assertThat(value, ExactEqualsEvaluator.comparedTo("different"))
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("ExactEquals failure (expected):");
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testNeutrosophicEquals() {
        String value = "test";

        // Neutrosophic equality - will return something like (T=0.95, I=0.04, F=0.01)
        assertThat(value, NeutrosophicEqualsEvaluator.comparedTo("test"))
                .isTrue();

        // Neutrosophic inequality - will return something like (T=0.05, I=0.15, F=0.80)
        // This might still pass with a very lenient context!
        NeutrosophicContext lenientContext = NeutrosophicContext.builder()
                .withTruthThreshold(0.04) // Very lenient - only need 4% truth
                .withIndeterminacyThreshold(0.5)
                .build();

        try {
            assertThat(value, NeutrosophicEqualsEvaluator.comparedTo("different"), lenientContext)
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("NeutrosophicEquals failure:");
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testNullHandlingDifference() {
        String nullValue = null;

        // ExactEquals with null: returns FALSE (T=0.0, I=0.0, F=1.0)
        try {
            assertThat(nullValue, ExactEqualsEvaluator.comparedTo("test"))
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("ExactEquals null handling:");
            System.out.println(e.getMessage());
        }

        // NeutrosophicEquals with null: returns (T=0.1, I=0.3, F=0.6)
        // This acknowledges the uncertainty of comparing null to a value
        try {
            assertThat(nullValue, NeutrosophicEqualsEvaluator.comparedTo("test"))
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("NeutrosophicEquals null handling:");
            System.out.println(e.getMessage());
        }
    }
}