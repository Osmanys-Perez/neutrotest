package io.github.osmanys_perez.neutrotest.junit5;

import io.github.osmanys_perez.neutrotest.NeutrosophicContext;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Method;

/**
 * JUnit 5 extension that manages the {@link NeutrosophicContext} for tests annotated with {@link NeutrosophicTest}.
 * <p>
 * This extension is responsible for:
 * <ul>
 *   <li>Extracting configuration from the {@code @NeutrosophicTest} annotation.</li>
 *   <li>Initializing and storing the appropriate {@code NeutrosophicContext} in the JUnit store.</li>
 *   <li>Providing the {@code ExtensionContext} as a parameter to test methods if requested.</li>
 * </ul>
 */
public class NeutrosophicTestExtension implements BeforeEachCallback, ParameterResolver {

    @Override
    public void beforeEach(ExtensionContext context) {
        Method testMethod = context.getRequiredTestMethod();
        NeutrosophicTest annotation = testMethod.getAnnotation(NeutrosophicTest.class);

        if (annotation != null) {
            NeutrosophicContext neutrosophicContext;

            // Check if using predefined context
            if (!annotation.context().isEmpty()) {
                neutrosophicContext = getPredefinedContext(annotation.context());
            } else {
                // Use individual thresholds (original simple logic)
                neutrosophicContext = NeutrosophicContext.builder()
                        .withTruthThreshold(annotation.truthThreshold())
                        .withIndeterminacyThreshold(annotation.indeterminacyThreshold())
                        .withFalsityThreshold(annotation.falsityThreshold())
                        .withTolerance(annotation.tolerance())
                        .build();
            }

            context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                    .put("neutrosophicContext", neutrosophicContext);
        }
    }

    private NeutrosophicContext getPredefinedContext(String contextName) {
        switch (contextName.toLowerCase()) {
            case "lenient":
                return NeutrosophicContext.lenientContext();
            case "strict":
                return NeutrosophicContext.strictContext();
            case "default":
            default:
                return NeutrosophicContext.defaultContext();
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return parameterContext.getParameter().getType().equals(ExtensionContext.class);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return extensionContext;
    }

    /**
     * Retrieves the {@link NeutrosophicContext} from the given JUnit {@link ExtensionContext}.
     *
     * @param context the JUnit extension context
     * @return the neutrosophic context, or {@code null} if not found
     */
    public static NeutrosophicContext getContext(ExtensionContext context) {
        return context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL)
                .get("neutrosophicContext", NeutrosophicContext.class);
    }
}