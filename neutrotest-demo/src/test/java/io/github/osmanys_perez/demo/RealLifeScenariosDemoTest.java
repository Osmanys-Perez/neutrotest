package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import io.qameta.allure.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;

import static io.github.osmanys_perez.neutrotest.evaluator.ExceptionThrownEvaluator.throwsException;
import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;
import static io.github.osmanys_perez.neutrotest.evaluator.NumericEvaluator.comparedTo;
import static io.github.osmanys_perez.neutrotest.evaluator.TemporalEvaluator.untilStable;
import static io.github.osmanys_perez.neutrotest.evaluator.TimeBoundEvaluator.completesWithin;

@Epic("Real-Life Scenarios")
@Feature("Neutrosophic Assertions in Practice")
public class RealLifeScenariosDemoTest {

    @Test
    @NeutrosophicTest(truthThreshold = 0.80)
    @DisplayName("API Response: Fragile Pass on Abbreviation Use")
    @Story("As a developer, I want to be warned when an API response uses an abbreviation but is still correctly understood")
    @Description("This test simulates an API returning 'S. Francisco' instead of 'San Francisco'. " +
            "With a threshold of 0.80, it results in a FRAGILE PASS because it's just above the requirement (similarity ~0.84).")
    void apiResponseFragilePass(ExtensionContext context) {
        // Simulation: API returns an abbreviation
        String apiResponse = "S. Francisco";
        String expected = "San Francisco";

        // FuzzyStringEvaluator similarity for "S. Francisco" vs "San Francisco" is approx 0.84
        // With threshold 0.80, 0.84 is a FRAGILE PASS (within 0.05 margin)
        NeutrosophicAssertions.assertThat(apiResponse, comparedTo(expected), context)
                .isAccepted();
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.95)
    @DisplayName("API Response: Borderline Fail on Minor Format Change")
    @Story("As a developer, I want to identify when an API response is starting to deviate significantly from expectations")
    @Description("This test simulates an API returning 'San Francisco.' (with a period) instead of 'San Francisco'. " +
            "With a strict threshold of 0.95, this results in a BORDERLINE FAIL because it's just below the requirement.")
    void apiResponseBorderlineFail(ExtensionContext context) {
        String apiResponse = "San Francisco.";
        String expected = "San Francisco";

        // Similarity is approx 0.928 (13/14 match)
        // With threshold 0.95, it's a BORDERLINE FAIL
        NeutrosophicAssertions.assertThat(apiResponse, comparedTo(expected).withPerfectMatchThreshold(1.0), context)
                .isAccepted();
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.72, indeterminacyThreshold = 0.20, falsityThreshold = 0.20)
    @DisplayName("Performance SLA: Fragile Pass on Minor Latency")
    @Story("As a DevOps engineer, I want to be warned about minor performance regressions")
    @Description("This test asserts that a process completes within 100ms. If it takes slightly longer (~105ms), it's a FRAGILE PASS, " +
            "indicating a minor regression that is still within the acceptable truth threshold.")
    @Tag("My-Tag")
    void performanceFragilePass(ExtensionContext context) {
        long targetMillis = 100;
        long actualMillis = 105; // Simulated regression

        // TimeBoundEvaluator logic (with variance 50ms):
        // overshootRatio = (105 - 100) / 50 = 0.1
        // truth = 0.8 - (0.1 * 0.3) = 0.77
        // (Actual T will be around 0.73-0.77 due to minor execution time beyond sleep)
        // With threshold 0.72, any T in [0.72, 0.771] is a FRAGILE PASS
        NeutrosophicAssertions.assertThat(
                () -> {
                    try {
                        Thread.sleep(actualMillis); // Sleep the actual simulated regression time
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return "Done";
                },
                completesWithin(Duration.ofMillis(targetMillis)).withVariance(Duration.ofMillis(50)),
                context
        ).isAccepted();
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.8)
    @DisplayName("Eventual Consistency: Strong Fail on Stability Timeout")
    @Story("As a tester, I want to distinguish between a broken service and an unstable/slow one")
    @Description("This test waits for a value to become 'Success' and stay stable. " +
            "If it never reaches the goal, it results in a FAILED status with high Indeterminacy.")
    void eventualConsistencyStrongFail(ExtensionContext context) {
        // Simulation: A service that stays 'Pending' and never reaches 'Success'
        NeutrosophicAssertions.assertThat(
                () -> "Pending",
                untilStable(comparedTo("Success"))
                        .withTimeout(Duration.ofMillis(200))
                        .withPollInterval(Duration.ofMillis(50)),
                context
        ).isAccepted();
    }

    @Test
    @NeutrosophicTest(context = "strict")
    @DisplayName("Dynamic Error: Strong Fail on Wrong Error Message")
    @Story("As a developer, I want my error assertions to be resilient to dynamic IDs but fail on wrong semantics")
    @Description("This test expects a 'User not found' error but receives a 'Database error' instead. " +
            "This is a STRONG FAIL because the semantic meaning is completely different.")
    void dynamicErrorStrongFail(ExtensionContext context) {
        String actualError = "Database error [ID: 550e8400-e29b-41d4-a716-446655440000]";
        String expectedFragment = "User not found";

        NeutrosophicAssertions.assertThat(
                () -> { throw new RuntimeException(actualError); },
                throwsException(RuntimeException.class).withMessageContaining(expectedFragment),
                context
        ).isAccepted();
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.95)
    @DisplayName("Financial Calculation: Borderline Fail on Precision Loss")
    @Story("As a financial analyst, I want to detect minor rounding errors in calculations")
    @Description("This test expects 100.00 but receives 99.94. With a 1.0 tolerance, similarity is 0.94. " +
            "Since threshold is 0.95, this is a BORDERLINE FAIL (within 5% margin).")
    void financialBorderlineFail(ExtensionContext context) {
        double actual = 99.94;
        double expected = 100.00;

        NeutrosophicAssertions.assertThat(actual, comparedTo(expected).withTolerance(1.0), context)
                .isAccepted();
    }
}
