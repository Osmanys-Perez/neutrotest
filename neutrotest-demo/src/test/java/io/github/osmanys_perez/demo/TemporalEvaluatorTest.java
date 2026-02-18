package io.github.osmanys_perez.demo;


import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;
import io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator;
import io.github.osmanys_perez.neutrotest.evaluator.TemporalEvaluator;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.osmanys_perez.neutrotest.NeutrosophicAssert.assertThat;

public class TemporalEvaluatorTest {

    // Simulate a value that changes after a delay
    static class DelayedValueSimulator {
        private final AtomicReference<String> value = new AtomicReference<>("loading...");

        DelayedValueSimulator(String finalValue, long delayMs) {
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                value.set(finalValue);
            }).start();
        }

        public String getCurrentValue() {
            return value.get();
        }
    }

    @Test
    void testEventualConsistency() {
        // Simulate a database eventually getting the correct value
        DelayedValueSimulator simulator = new DelayedValueSimulator("New York", 2000); // Will change after 2 seconds

        // Create a supplier that gets the current value
        Supplier<String> valueSupplier = simulator::getCurrentValue;

        // We want to check if the value eventually becomes "New York"
        FuzzyStringEvaluator targetEvaluator = FuzzyStringEvaluator.comparedTo("New York");

        // Wrap it in a TemporalEvaluator
        TemporalEvaluator<String> temporalEvaluator = TemporalEvaluator
                .untilStable(targetEvaluator)
                .withTimeout(Duration.ofSeconds(5)) // Wait up to 5 seconds
                .withPollInterval(Duration.ofMillis(200)); // Check every 200ms

        // This assertion will wait and eventually pass once the value updates
        assertThat(valueSupplier, temporalEvaluator, NeutrosophicContext.lenientContext())
                .isTrue();
    }

    @Test
    void testTimeout() {
        // Simulate a value that never becomes correct
        AtomicReference<String> stuckValue = new AtomicReference<>("wrong data");
        Supplier<String> stuckSupplier = stuckValue::get;

        FuzzyStringEvaluator targetEvaluator = FuzzyStringEvaluator.comparedTo("correct data");

        TemporalEvaluator<String> temporalEvaluator = TemporalEvaluator
                .untilStable(targetEvaluator)
                .withTimeout(Duration.ofSeconds(2)) // Short timeout
                .withPollInterval(Duration.ofMillis(100));

        // This will fail due to timeout, resulting in a highly indeterminate value
        try {
            assertThat(stuckSupplier, temporalEvaluator, NeutrosophicContext.strictContext())
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("Expected timeout failure:");
            System.out.println(e.getMessage());
            // The error should show high indeterminacy, e.g., (T=0.xx, I=0.80, F=0.xx)
        }
    }

    @Test
    void testCountingOperation() {
        // Simulate a counter that increments slowly
        AtomicInteger counter = new AtomicInteger(0);
        new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(300);
                    counter.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        Supplier<Integer> counterSupplier = counter::get;

        // Create an evaluator that checks if the count is >= 5
        // We'll use a simple approach: create a custom evaluator
        Evaluator<Integer> atLeastFiveEvaluator = actual -> {
            if (actual >= 5) {
                return new NeutrosophicValue(0.95, 0.04, 0.01); // Mostly true
            } else {
                return new NeutrosophicValue(0.10, 0.20, 0.70); // Mostly false
            }
        };

        // Now this will work because both are typed for Integer
        TemporalEvaluator<Integer> temporalEvaluator = TemporalEvaluator
                .untilStable(atLeastFiveEvaluator)
                .withTimeout(Duration.ofSeconds(5));

        assertThat(counterSupplier, temporalEvaluator, NeutrosophicContext.lenientContext())
                .isTrue();
    }
}