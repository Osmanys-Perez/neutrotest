package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;

public class FragileAssertionsDemo {

    @Test
    @NeutrosophicTest(truthThreshold = 0.4, indeterminacyThreshold = 0.5, falsityThreshold = 0.5)
    void demonstrateFragilePass(ExtensionContext context) {
        // This input is close to the threshold. 
        String input = "San Fran"; 
        
        // Let's use a very strict context or a lower match threshold in evaluator
        FuzzyStringEvaluator evaluator = comparedTo("San Francisco").withPerfectMatchThreshold(0.95);
        
        System.out.println("Running Fragile Pass Demo...");
        // Truth will be 0.42, threshold is 0.4. (Fragile Pass!)
        NeutrosophicAssertions.assertThat(input, evaluator, context)
                .isAccepted();
        
        System.out.println("Test passed. Check the report for 'FRAGILE PASS' status!");
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.9)
    void demonstrateStrongPass(ExtensionContext context) {
        String input = "San Francisco";
        
        System.out.println("Running Strong Pass Demo...");
        NeutrosophicAssertions.assertThat(input, comparedTo("San Francisco"), context)
                .isAccepted();
        
        System.out.println("Test passed. Check the report for 'PASSED' status!");
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.5)
    void demonstrateNearMiss(ExtensionContext context) {
        // "San Fran" vs "San Francisco" with threshold 0.5.
        // Truth is 0.42. (Near Miss!)
        String input = "San Fran";
        
        System.out.println("Running Near Miss Demo...");
        try {
            NeutrosophicAssertions.assertThat(input, comparedTo("San Francisco").withPerfectMatchThreshold(0.95), context)
                    .isAccepted();
        } catch (AssertionError e) {
            System.out.println("Test failed as expected. Check the report for 'NEAR MISS' status!");
            return;
        }
        throw new AssertionError("Expected test to fail but it passed!");
    }
}
