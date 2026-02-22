package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;

/**
 * Evaluates whether an operation completes within a specified time bound.
 * Accounts for system variability and measurement uncertainty.
 */
public final class TimeBoundEvaluator<T> implements Evaluator<Supplier<T>> {

    private final Duration maxDuration;
    private final Duration acceptableVariance;

    private TimeBoundEvaluator(Duration maxDuration, Duration acceptableVariance) {
        this.maxDuration = maxDuration;
        this.acceptableVariance = acceptableVariance;
    }

    /**
     * Creates a {@link TimeBoundEvaluator} for an operation that is expected to complete within a given duration.
     *
     * @param <T>      the return type of the operation
     * @param duration the maximum expected duration
     * @return a new {@code TimeBoundEvaluator}
     */
    public static <T> TimeBoundEvaluator<T> completesWithin(Duration duration) {
        return new TimeBoundEvaluator<>(duration, Duration.ofMillis(50));
    }

    /**
     * Configures the acceptable variance (buffer) beyond the maximum duration.
     *
     * @param variance the acceptable variance duration
     * @return a new {@code TimeBoundEvaluator} instance with the updated variance
     */
    public TimeBoundEvaluator<T> withVariance(Duration variance) {
        return new TimeBoundEvaluator<>(this.maxDuration, variance);
    }

    @Override
    public NeutrosophicValue evaluate(Supplier<T> operation) {
        Instant start = Instant.now();

        try {
            operation.get(); // Execute the operation
        } catch (Exception e) {
            return new NeutrosophicValue(0.0, 0.3, 0.7); // Operation failed
        }

        Duration actualDuration = Duration.between(start, Instant.now());
        long actualMillis = actualDuration.toMillis();
        long maxMillis = maxDuration.toMillis();
        long varianceMillis = acceptableVariance.toMillis();

        if (actualMillis <= maxMillis) {
            // Well within bounds: high truth, low indeterminacy
            return new NeutrosophicValue(0.95, 0.04, 0.01);
        }
        else if (actualMillis <= maxMillis + varianceMillis) {
            // Within acceptable variance: moderate truth, higher indeterminacy
            double overshootRatio = (double) (actualMillis - maxMillis) / varianceMillis;
            double truth = 0.8 - (overshootRatio * 0.3);
            double indeterminacy = 0.1 + (overshootRatio * 0.2);
            return new NeutrosophicValue(truth, indeterminacy, 1 - truth - indeterminacy);
        }
        else {
            // Significantly over time: low truth, moderate indeterminacy
            long overshoot = actualMillis - maxMillis - varianceMillis;
            double severity = Math.min(overshoot / 1000.0, 1.0); // Cap at 1.0
            double truth = 0.3 * (1 - severity);
            double indeterminacy = 0.2;
            return new NeutrosophicValue(truth, indeterminacy, 1 - truth - indeterminacy);
        }
    }
}