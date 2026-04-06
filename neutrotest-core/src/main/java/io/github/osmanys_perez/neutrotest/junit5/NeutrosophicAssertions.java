package io.github.osmanys_perez.neutrotest.junit5;

import io.github.osmanys_perez.neutrotest.Evaluator;
import io.github.osmanys_perez.neutrotest.NeutrosophicAssert;
import io.github.osmanys_perez.neutrotest.NeutrosophicAssertion;
import io.github.osmanys_perez.neutrotest.NeutrosophicValue;
import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entry point for JUnit 5 integrated Neutrosophic assertions.
 * <p>
 * Use this class in tests annotated with {@link NeutrosophicTest}.
 * It automatically retrieves the context from the JUnit {@link ExtensionContext}.
 */
public final class NeutrosophicAssertions {

    private static final List<NeutrosophicResultReporter> REPORTERS = new ArrayList<>();

    static {
        // Register default JUnit 5 reporter
        REPORTERS.add((context, status, value, threshold) ->
            context.publishReportEntry(Map.of(
                "Neutrosophic Status", status,
                "Truth", format(value.truth()),
                "Indeterminacy", format(value.indeterminacy()),
                "Falsity", format(value.falsity()),
                "Threshold", format(threshold)
            ))
        );

        // Safely try to register Allure reporter
        try {
            Class.forName("io.qameta.allure.Allure");
            REPORTERS.add(new AllureReporter());
        } catch (ClassNotFoundException e) {
            // Allure not on classpath
        }
    }

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private NeutrosophicAssertions() {
        // Utility class
    }

    /**
     * Entry point for assertThat in JUnit 5. Returns a decorated assertion that
     * automatically reports results to JUnit's ExtensionContext.
     */
    public static <T> ReportedNeutrosophicAssertion<T> assertThat(T actual, Evaluator<T> evaluator, ExtensionContext context) {
        NeutrosophicContext neutrosophicContext = NeutrosophicTestExtension.getContext(context);
        if (neutrosophicContext == null) {
            throw new IllegalStateException("No NeutrosophicContext found. Please add @NeutrosophicTest annotation.");
        }
        return new ReportedNeutrosophicAssertion<>(actual, evaluator, neutrosophicContext, context);
    }

    /**
     * Adds a custom reporter to the assertion library.
     *
     * @param reporter the reporter to add
     */
    public static void registerReporter(NeutrosophicResultReporter reporter) {
        REPORTERS.add(reporter);
    }

    /**
     * A decorator for NeutrosophicAssertion that publishes results to reporting frameworks.
     */
    public static final class ReportedNeutrosophicAssertion<T> {
        private final T actual;
        private final Evaluator<T> evaluator;
        private final NeutrosophicAssertion<T> delegate;
        private final ExtensionContext extensionContext;
        private final NeutrosophicContext neutrosophicContext;

        private ReportedNeutrosophicAssertion(T actual, Evaluator<T> evaluator, NeutrosophicContext neutrosophicContext, ExtensionContext extensionContext) {
            this.actual = actual;
            this.evaluator = evaluator;
            this.delegate = NeutrosophicAssert.assertThat(actual, evaluator, neutrosophicContext);
            this.extensionContext = extensionContext;
            this.neutrosophicContext = neutrosophicContext;
        }

        public NeutrosophicValue isTrue() {
            return report(delegate::isTrue);
        }

        public NeutrosophicValue isFalse() {
            return report(delegate::isFalse);
        }

        public NeutrosophicValue isAccepted() {
            return isTrue();
        }

        public NeutrosophicValue isRejected() {
            return isFalse();
        }

        private NeutrosophicValue report(java.util.function.Supplier<NeutrosophicValue> assertion) {
            try {
                NeutrosophicValue result = assertion.get();
                publishResult(result, true);
                return result;
            } catch (AssertionError e) {
                // If it fails, we evaluate it ourselves to get the result for the report
                NeutrosophicValue result = evaluator.evaluate(actual);
                String status = determineStatus(result, false);
                publishResult(result, false);
                throw new AssertionError(status + ": " + e.getMessage());
            }
        }

        private void publishResult(NeutrosophicValue value, boolean passed) {
            String status = determineStatus(value, passed);

            // Notify all registered reporters
            for (NeutrosophicResultReporter reporter : REPORTERS) {
                try {
                    reporter.report(extensionContext, status, value, neutrosophicContext.truthThreshold());
                } catch (Exception e) {
                    // Fail-safe: don't let a reporter crash the test
                    System.err.println("Error in Neutrosophic reporter: " + e.getMessage());
                }
            }
        }

        private String determineStatus(NeutrosophicValue value, boolean passed) {
            double truth = value.truth();
            double threshold = neutrosophicContext.truthThreshold();

            if (passed) {
                if (truth < 1.0 && (truth - threshold) < 0.051) {
                    return "FRAGILE PASS";
                } else {
                    return "PASSED";
                }
            } else {
                if (truth >= threshold - 0.051) {
                    return "BORDERLINE FAIL";
                } else {
                    return "FAILED";
                }
            }
        }
    }

    /**
     * Implementation of {@link NeutrosophicResultReporter} for Allure.
     * Separated to avoid direct dependency issues if Allure is not present.
     */
    private static class AllureReporter implements NeutrosophicResultReporter {
        @Override
        public void report(ExtensionContext context, String status, NeutrosophicValue value, double threshold) {
            io.qameta.allure.Allure.parameter("Neutrosophic Status", status);
            io.qameta.allure.Allure.parameter("Truth", format(value.truth()));
            io.qameta.allure.Allure.parameter("Indeterminacy", format(value.indeterminacy()));
            io.qameta.allure.Allure.parameter("Falsity", format(value.falsity()));
            io.qameta.allure.Allure.parameter("Threshold", format(threshold));

            // Inject status into Allure StatusDetails so messageRegex can catch it (passed tests only)
            if ("FRAGILE PASS".equals(status)) {
                io.qameta.allure.Allure.getLifecycle().updateTestCase(t -> 
                    t.setStatusDetails(new io.qameta.allure.model.StatusDetails().setMessage("FRAGILE PASS"))
                );
            }

            // Apply Status Overlay via Tags
            if ("FRAGILE PASS".equals(status)) {
                io.qameta.allure.Allure.label("tag", "FRAGILE-PASS");
            } else if ("BORDERLINE FAIL".equals(status)) {
                io.qameta.allure.Allure.label("tag", "BORDERLINE-FAIL");
            }
        }
    }
}