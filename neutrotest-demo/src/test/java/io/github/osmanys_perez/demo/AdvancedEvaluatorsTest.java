package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import io.github.osmanys_perez.neutrotest.evaluator.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.osmanys_perez.neutrotest.NeutrosophicAssert.assertThat;

public class AdvancedEvaluatorsTest {


    @Test
    void testCollectionContainsFuzzyMatch() {
        List<String> cities = List.of("New York", "Los Angeles", "San Francisco", "Chicago");

        assertThat(cities, CollectionContainsEvaluator.anElementThat(
                FuzzyStringEvaluator.comparedTo("San Franci")
        ), NeutrosophicContext.lenientContext()).isTrue(); // Use lenient context
    }

    @Test
    void testExceptionThrowing() {
        assertThat(() -> { throw new IllegalArgumentException("Invalid input parameter"); },
                ExceptionThrownEvaluator.throwsException(IllegalArgumentException.class)
                        .withMessageContaining("invalid input para"),
                NeutrosophicContext.lenientContext()).isTrue(); // Use lenient context
    }


    @Test
    void testPerformanceBound() {
        AtomicInteger counter = new AtomicInteger();

        // The lambda needs to return a value for the TimeBoundEvaluator
        assertThat(() -> {
                    // Simulate some work
                    for (int i = 0; i < 1000; i++) {
                        counter.incrementAndGet();
                    }
                    return null; // Added return statement
                }, TimeBoundEvaluator.completesWithin(Duration.ofMillis(10)),
                NeutrosophicContext.lenientContext()).isTrue();
    }

    @Test
    void testCombinedEvaluators() {
        List<Runnable> tasks = List.of(
                () -> { /* fast task */ },
                () -> {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Properly handle interrupt
                        throw new RuntimeException("Sleep interrupted", e);
                    }
                }
        );

        // Check that all tasks complete within time, and collection contains fast tasks
        assertThat(tasks, CollectionContainsEvaluator.anElementThat(
                task -> TimeBoundEvaluator.completesWithin(Duration.ofMillis(5)).evaluate(() -> {
                    task.run();
                    return null; // Return statement for the inner lambda
                })
        ), NeutrosophicContext.lenientContext()).isTrue();
    }
}