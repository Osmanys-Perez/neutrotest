package io.github.osmanys_perez.demo;

import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;

import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;
import static io.github.osmanys_perez.neutrotest.evaluator.NumericEvaluator.comparedTo;

@Epic("Reporting Integration")
@Feature("Allure Reporting")
public class AllureReportingDemoTest {

    @Test
    @NeutrosophicTest(truthThreshold = 0.55, indeterminacyThreshold = 0.25, falsityThreshold = 0.25)
    @DisplayName("Demonstrate Neutrosophic Metadata in Allure")
    @Story("As a tester, I want to see T, I, F values in my Allure report")
    @Description("This test performs a fuzzy string match. Allure captures the published report entries " +
                 "and displays them as first-class 'Parameters' in the report.")
    void demonstrateAllureMetadata(ExtensionContext context) {
        String actual = "Neutrosophic Logic";
        String expected = "Neutrosophic Logic Systems";

        // This will publish Truth, Indeterminacy, Falsity, and Status to Allure
        NeutrosophicAssertions.assertThat(actual, comparedTo(expected), context)
                .isAccepted();
    }

    @Test
    @NeutrosophicTest(truthThreshold = 0.95)
    @DisplayName("Demonstrate Borderline Fail in Allure Reports")
    @Story("As a tester, I want to identify borderline failures in reports")
    @Description("This test is a 'Borderline Fail'. It fails, but the truth value is very close to the threshold. " +
                 "The status 'BORDERLINE FAIL' will be clearly visible in Allure's 'Parameters' section.")
    void demonstrateBorderlineFailInAllure(ExtensionContext context) {
        double actual = 94.5;
        double expected = 100.0;

        try {
            // NumericEvaluator logic:
            // diff = 5.5, tolerance = 100.0 -> similarity = 1.0 - 5.5/100 = 0.945
            // With threshold 0.95, 0.945 is within the 0.05 margin (0.90 to 0.95) -> BORDERLINE FAIL

            NeutrosophicAssertions.assertThat(actual, comparedTo(expected).withTolerance(100.0), context)
                    .isAccepted();
        } catch (AssertionError e) {
            System.out.println("Expected failure for Allure demo: " + e.getMessage());
        }
    }
}
