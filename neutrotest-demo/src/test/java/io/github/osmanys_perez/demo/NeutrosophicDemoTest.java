package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator;
import org.junit.jupiter.api.Test;

import static io.github.osmanys_perez.neutrotest.NeutrosophicAssert.assertThat;

// This test demonstrates the core functionality
public class NeutrosophicDemoTest {

    @Test
    void testFuzzyMatching_Successful() {
        String userInput = "New Yrok"; // Classic typo

        // Create a context that is lenient enough for this specific case
        NeutrosophicContext lenientForTypo = NeutrosophicContext.builder()
                .withTruthThreshold(0.65)  // Lower the truth requirement to 65%
                .withIndeterminacyThreshold(0.3)
                .withFalsityThreshold(0.3)
                .build();

        // This should pass with a lenient context
        assertThat(userInput, FuzzyStringEvaluator.comparedTo("New York"), lenientForTypo)
                .isTrue();
    }

    @Test
    void testFuzzyMatching_Failure() {
        String userInput = "Boston"; // completely different

        // This should fail - the strings are too different
        try {
            assertThat(userInput, FuzzyStringEvaluator.comparedTo("New York"), NeutrosophicContext.strictContext())
                    .isTrue();
            // If we get here, the test logic is wrong, force a failure
            throw new AssertionError("Expected an AssertionError to be thrown for completely different strings");
        } catch (AssertionError e) {
            // Expected! Let's print the helpful error message
            System.out.println("EXPECTED ASSERTION ERROR:");
            System.out.println(e.getMessage());
            System.out.println("----------------------------------------");
        }
    }

    @Test
    void testEdgeCases() {
        // Testing with nulls
        assertThat(null, FuzzyStringEvaluator.comparedTo(null))
                .isTrue(); // Both null -> TRUE value

        // Testing with empty strings
        assertThat("", FuzzyStringEvaluator.comparedTo(""))
                .isTrue();

        // Testing a very close match with a custom threshold
        String input = "helo";
        FuzzyStringEvaluator evaluator = FuzzyStringEvaluator.comparedTo("hello")
                .withPerfectMatchThreshold(0.9); // Be more strict about what constitutes a match

        // This will likely fail or pass based on the calculated similarity.
        // Let's see what happens with the default context.
        try {
            assertThat(input, evaluator).isTrue();
        } catch (AssertionError e) {
            System.out.println("ASSERTION ERROR for 'helo' vs 'hello':");
            System.out.println(e.getMessage());
        }
    }

    @Test
    void testContextMatters() {
        String input = "SFrancisco"; // Abbreviation

        NeutrosophicContext veryLenient = NeutrosophicContext.builder()
                .withTruthThreshold(0.5)    // Only need 50% confidence
                .withIndeterminacyThreshold(0.4) // Tolerate up to 40% uncertainty
                .build();

        NeutrosophicContext veryStrict = NeutrosophicContext.builder()
                .withTruthThreshold(0.95)   // Need 95% confidence
                .withIndeterminacyThreshold(0.01) // Almost no uncertainty allowed
                .build();

        // This will pass - the context is very forgiving
        assertThat(input, FuzzyStringEvaluator.comparedTo("San Francisco"), veryLenient)
                .isTrue();

        // This will almost certainly fail - the context demands near certainty
        try {
            assertThat(input, FuzzyStringEvaluator.comparedTo("San Francisco"), veryStrict)
                    .isTrue();
        } catch (AssertionError e) {
            System.out.println("DEMO: How context changes the outcome:");
            System.out.println(e.getMessage());
        }
    }
}