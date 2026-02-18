package io.github.osmanys_perez.neutrotest.evaluator;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;

/**
 * Evaluates strings based on fuzzy similarity to an expected value.
 */
public final class FuzzyStringEvaluator implements Evaluator<String> {

    private final String expected;
    // A threshold for what similarity score (0.0 to 1.0) is considered a definite match.
    // This influences how we map similarity to the (T, I, F) triplet.
    private final double perfectMatchThreshold;

    /**
     * Private constructor. Use {@link #comparedTo(String)} to create an instance.
     */
    private FuzzyStringEvaluator(String expected, double perfectMatchThreshold) {
        this.expected = expected;
        this.perfectMatchThreshold = perfectMatchThreshold;
    }

    /**
     * Creates a FuzzyStringEvaluator builder with the expected string to compare against.
     * @param expected the expected string
     * @return an instance of the evaluator's builder
     */
    public static FuzzyStringEvaluator comparedTo(String expected) {
        return new FuzzyStringEvaluator(expected, 0.85); // Default threshold
    }

    /**
     * Fluently sets a custom perfect match threshold.
     * @param threshold a value between 0.0 and 1.0. 0.85 means a similarity score of 0.85 is considered a perfect match (T=1.0).
     * @return this evaluator instance for method chaining
     */
    public FuzzyStringEvaluator withPerfectMatchThreshold(double threshold) {
        return new FuzzyStringEvaluator(this.expected, threshold);
    }

    @Override
    public NeutrosophicValue evaluate(String actual) {
        if (expected == null && actual == null) {
            return NeutrosophicValue.TRUE; // Both are null, perfect match.
        }
        if (expected == null || actual == null) {
            return new NeutrosophicValue(0.0, 0.1, 0.9); // Mostly false
        }
        if (expected.isEmpty() && actual.isEmpty()) {
            return NeutrosophicValue.TRUE; // Both are empty, perfect match.
        }

        double similarity = calculateSimilarity(expected, actual);

        // **Neutrosophic Interpretation of Similarity**:
        // We map the similarity score to the truth component.
        // Indeterminacy is highest when similarity is around 0.5.
        // Falsity is simply (1 - similarity), adjusted.

        // 1. Truth is directly based on similarity, scaled by the threshold.
        // If we are above the threshold, truth is high.
        double truth = Math.min(1.0, similarity / perfectMatchThreshold);

        // 2. Falsity is the opposite of truth.
        double falsity = 1.0 - similarity;

        // 3. Indeterminacy is the "unsure" part. It's highest when we are in the middle.
        // We model it as a Gaussian-like curve around 0.5, but scaled.
        // Avoid negative values with Math.max(0, ...)
        double peakIndeterminacy = 0.5; // The similarity value where we are most unsure
        double spread = 0.2; // How wide the peak of indeterminacy is
        double indeterminacy = 0.7 * Math.exp(-Math.pow((similarity - peakIndeterminacy) / spread, 2));

        // 4. NORMALIZE: Ensure T + I + F <= 1.
        // Our truth and falsity are already based on [0,1], but indeterminacy adds extra.
        // We need to scale them down proportionally if their sum exceeds 1.
        double total = truth + indeterminacy + falsity;
        if (total > 1.0) {
            truth = truth / total;
            indeterminacy = indeterminacy / total;
            falsity = falsity / total;
        }

        return new NeutrosophicValue(truth, indeterminacy, falsity);
    }

    /**
     * Calculates a normalized similarity score between 0.0 (completely different) and 1.0 (identical).
     * Uses Levenshtein distance.
     * @param s1 the first string
     * @param s2 the second string
     * @return the similarity score
     */
    private double calculateSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) {
            return 1.0; // Both strings are empty
        }
        int distance = calculateLevenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    private int calculateLevenshteinDistance(CharSequence s1, CharSequence s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(
                            dp[i - 1][j - 1] + costOfSubstitution(s1.charAt(i - 1), s2.charAt(j - 1)),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1
                    );
                }
            }
        }
        return dp[s1.length()][s2.length()];
    }

    private int costOfSubstitution(char a, char b) {
        return a == b ? 0 : 1;
    }

    private int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }
}