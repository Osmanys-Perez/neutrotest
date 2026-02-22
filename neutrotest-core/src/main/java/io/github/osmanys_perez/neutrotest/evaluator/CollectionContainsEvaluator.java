package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

import java.util.Collection;
import java.util.Optional;

/**
 * Evaluates whether a collection contains an element that matches a given evaluator's criteria.
 * Useful for testing collections with fuzzy or approximate matching.
 */
public final class CollectionContainsEvaluator<T> implements Evaluator<Collection<T>> {

    private final Evaluator<T> elementEvaluator;

    private CollectionContainsEvaluator(Evaluator<T> elementEvaluator) {
        this.elementEvaluator = elementEvaluator;
    }

    /**
     * Creates a {@link CollectionContainsEvaluator} that checks if the collection contains
     * an element matching the given evaluator's criteria.
     *
     * @param <T>              the type of elements in the collection
     * @param elementEvaluator the evaluator to apply to individual elements
     * @return a new {@code CollectionContainsEvaluator}
     */
    public static <T> CollectionContainsEvaluator<T> anElementThat(Evaluator<T> elementEvaluator) {
        return new CollectionContainsEvaluator<>(elementEvaluator);
    }

    @Override
    public NeutrosophicValue evaluate(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) {
            return new NeutrosophicValue(0.0, 0.1, 0.9);
        }

        Optional<NeutrosophicValue> bestMatch = collection.stream()
                .map(elementEvaluator::evaluate)
                .reduce((a, b) -> a.truth() > b.truth() ? a : b);

        if (bestMatch.isPresent()) {
            NeutrosophicValue bestValue = bestMatch.get();
            double newIndeterminacy = Math.min(bestValue.indeterminacy() + 0.1, 0.3);

            // Ensure normalization
            double total = bestValue.truth() + newIndeterminacy + bestValue.falsity();
            if (total > 1.0) {
                double scale = 1.0 / total;
                return new NeutrosophicValue(
                        bestValue.truth() * scale,
                        newIndeterminacy * scale,
                        bestValue.falsity() * scale
                );
            }

            return new NeutrosophicValue(bestValue.truth(), newIndeterminacy, bestValue.falsity());
        }

        return new NeutrosophicValue(0.0, 0.2, 0.8);
    }
}