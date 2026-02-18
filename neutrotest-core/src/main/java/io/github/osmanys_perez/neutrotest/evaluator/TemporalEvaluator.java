package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Evaluates a condition over time, polling until it stabilizes to an acceptable state
 * (as defined by a context) or a timeout is reached.
 * This is essential for testing asynchronous operations and eventual consistency.
 */
public final class TemporalEvaluator<T> implements Evaluator<Supplier<T>> {

    private final Evaluator<T> baseEvaluator;
    private final Duration timeout;
    private final Duration pollInterval;
    private final double stabilityThreshold; // Min truth value to consider stable

    private TemporalEvaluator(Evaluator<T> baseEvaluator, Duration timeout, Duration pollInterval, double stabilityThreshold) {
        this.baseEvaluator = baseEvaluator;
        this.timeout = timeout;
        this.pollInterval = pollInterval;
        this.stabilityThreshold = stabilityThreshold;
    }

    /**
     * Creates a TemporalEvaluator builder for a given base evaluator.
     * @param baseEvaluator the evaluator that checks the condition at a point in time
     * @return an instance of the TemporalEvaluator's builder
     */
    public static <T> TemporalEvaluator<T> untilStable(Evaluator<T> baseEvaluator) {
        return new TemporalEvaluator<>(
                baseEvaluator,
                Duration.ofSeconds(10), // Default timeout: 10 seconds
                Duration.ofMillis(500), // Default poll every 500ms
                0.9                    // Default stability threshold: 90% truth
        );
    }

    public TemporalEvaluator<T> withTimeout(Duration timeout) {
        return new TemporalEvaluator<>(this.baseEvaluator, timeout, this.pollInterval, this.stabilityThreshold);
    }

    public TemporalEvaluator<T> withPollInterval(Duration pollInterval) {
        return new TemporalEvaluator<>(this.baseEvaluator, this.timeout, pollInterval, this.stabilityThreshold);
    }

    public TemporalEvaluator<T> withStabilityThreshold(double stabilityThreshold) {
        return new TemporalEvaluator<>(this.baseEvaluator, this.timeout, this.pollInterval, stabilityThreshold);
    }

    @Override
    public NeutrosophicValue evaluate(Supplier<T> actualSupplier) {
        Instant startTime = Instant.now();
        Instant giveUpTime = startTime.plus(timeout);

        NeutrosophicValue lastResult = null;
        int stablePolls = 0;
        int pollsNeeded = 2; // Require 2 consecutive stable polls to consider it truly stable

        while (Instant.now().isBefore(giveUpTime)) {
            T currentActual = actualSupplier.get(); // Get the latest value
            lastResult = baseEvaluator.evaluate(currentActual);

            // Check if the result is stable enough to return
            if (lastResult.truth() >= stabilityThreshold) {
                stablePolls++;
                if (stablePolls >= pollsNeeded) {
                    // We've reached a stable, passing state! Return the good result.
                    return lastResult;
                }
            } else {
                // Result dipped, reset stability counter
                stablePolls = 0;
            }

            // Wait before polling again
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupt status
                // If we're interrupted, return a highly indeterminate state
                double lastTruth = lastResult != null ? lastResult.truth() : 0.0;
                double lastFalsity = lastResult != null ? lastResult.falsity() : 0.2;

                double indeterminacy = 0.5;
                double truth = lastTruth * 0.5;
                double falsity = lastFalsity * 0.5;

                // Ensure normalization
                double total = truth + indeterminacy + falsity;
                if (total > 1.0) {
                    truth = truth / total;
                    indeterminacy = indeterminacy / total;
                    falsity = falsity / total;
                }

                return new NeutrosophicValue(truth, indeterminacy, falsity);
            }
        }

        // If we get here, we timed out
        // The result is highly indeterminate - we don't know if it would have passed eventually
        double lastTruth = lastResult != null ? lastResult.truth() : 0.0;
        double lastFalsity = lastResult != null ? lastResult.falsity() : 0.2;

        // Create a highly indeterminate result but ensure it's normalized
        double indeterminacy = 0.8;
        // Scale truth and falsity down to make room for high indeterminacy
        double truth = lastTruth * 0.2;  // Reduce truth proportionally
        double falsity = lastFalsity * 0.2; // Reduce falsity proportionally

        // Normalize to ensure T + I + F <= 1.0
        double total = truth + indeterminacy + falsity;
        if (total > 1.0) {
            truth = truth / total;
            indeterminacy = indeterminacy / total;
            falsity = falsity / total;
        }

        return new NeutrosophicValue(truth, indeterminacy, falsity);
    }
}