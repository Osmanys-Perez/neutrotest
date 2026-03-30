package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;

public class FragileAssertionsDemoTest {

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
    void demonstrateBorderlineFail(ExtensionContext context) {
        // We want a BORDERLINE FAIL status.
        // Threshold = 0.5.
        // BORDERLINE FAIL is triggered when truth >= (threshold - 0.05).
        // So we need truth in [0.45, 0.5).

        // Using NumericEvaluator for exact control:
        // actual = 9.48, expected = 10.0, tolerance = 1.0
        // diff = 0.52
        // similarity = 1 - (0.52 / 1.0) = 0.48
        // Truth = similarity = 0.48
        // 0.48 is in [0.45, 0.5) -> BORDERLINE FAIL.

        System.out.println("Running Borderline Fail Demo...");
        NeutrosophicAssertions.assertThat(9.48, io.github.osmanys_perez.neutrotest.evaluator.NumericEvaluator.comparedTo(10.0).withTolerance(1.0), context)
                .isAccepted();
    }
}
