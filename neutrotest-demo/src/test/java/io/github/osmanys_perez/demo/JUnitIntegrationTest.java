package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.NeutrosophicValue;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.osmanys_perez.neutrotest.evaluator.CollectionContainsEvaluator.anElementThat;
import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;
import static io.github.osmanys_perez.neutrotest.evaluator.NumericEvaluator.comparedTo;
import static io.github.osmanys_perez.neutrotest.evaluator.TimeBoundEvaluator.completesWithin;
import static io.github.osmanys_perez.neutrotest.evaluator.ExceptionThrownEvaluator.throwsException;

public class JUnitIntegrationTest {

    // Test 1: Using predefined "lenient" context via annotation
    @Test
    @NeutrosophicTest(context = "lenient") // Much cleaner!
    void testFuzzyMatchingWithLenientContext(ExtensionContext context) {
        List<String> cities = List.of("New York", "Los Angeles", "San Francisco", "Chicago");

        NeutrosophicAssertions.assertThat(cities,
                anElementThat(comparedTo("San Franci")),
                context
        ).isTrue();
    }

    // Test 2: Using predefined "strict" context via annotation
    @Test
    @NeutrosophicTest(context = "strict")
    void testStrictFuzzyMatching(ExtensionContext context) {
        String preciseInput = "New York";

        NeutrosophicAssertions.assertThat(
                preciseInput,
                comparedTo("New York"),
                context
        ).isTrue();
    }

    // Test 3: Using individual thresholds (original behavior)
    @Test
    @NeutrosophicTest(truthThreshold = 0.7, indeterminacyThreshold = 0.3)
    void testWithCustomThresholds(ExtensionContext context) {
        NeutrosophicAssertions.assertThat(
                1.99998,
                comparedTo(2.0).withTolerance(0.01),
                context
        ).isAccepted();
    }

    // Test 4: Using the default context (no parameters needed)
    @Test
    @NeutrosophicTest
    void testWithDefaultContext(ExtensionContext context) {
        NeutrosophicAssertions.assertThat(
                "Hello",
                input -> new NeutrosophicValue(0.9, 0.05, 0.05),
                context
        ).isAccepted();
    }

    // Test 5: Exception testing with a lenient context
    @Test
    @NeutrosophicTest(context = "lenient")
    void testExceptionThrowing(ExtensionContext context) {
        NeutrosophicAssertions.assertThat(
                () -> { throw new IllegalArgumentException("Invalid input parameter"); },
                throwsException(IllegalArgumentException.class).withMessageContaining("invalid input param"),
                context
        ).isTrue();
    }

    // Test 6: Performance test with strict context
    @Test
    @NeutrosophicTest(context = "strict")
    void testPerformanceBound(ExtensionContext context) {
        AtomicInteger counter = new AtomicInteger();

        NeutrosophicAssertions.assertThat(
                () -> {
                    for (int i = 0; i < 1000; i++) counter.incrementAndGet();
                    return null;
                },
                completesWithin(Duration.ofMillis(10)),
                context
        ).isTrue();
    }
}