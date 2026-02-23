# Neutrotest: Neutrosophic Assertion Library

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**Neutrotest** is a Java-based assertion library that brings the power of **Neutrosophic Logic** to software testing. Unlike traditional binary assertions (true/false), Neutrotest allows for nuanced, "fuzzy" evaluations by incorporating degrees of truth, indeterminacy, and falsehood.

## 🚀 Why Neutrotest?

In the real world, things aren't always black and white. Traditional testing tools often struggle with:
- **Floating-point comparisons**: Where "almost equal" is often "good enough."
- **Fuzzy string matching**: Dealing with typos, abbreviations, or partial matches.
- **Performance/Timing**: Asserting that an operation completes "within a reasonable time."
- **Uncertain environments**: Testing systems where some degree of uncertainty is inherent.

Neutrotest provides a formal, mathematical way to express these "gray area" assertions.

## 📊 Rich Reporting & Allure Integration

Neutrotest doesn't just pass or fail; it provides detailed insights into *why* a test resulted in a specific outcome.

- **Nuanced Statuses**: Beyond `PASSED` and `FAILED`, Neutrotest identifies:
    - **`FRAGILE PASS`**: The test passed, but it was close to the failure threshold.
    - **`BORDERLINE FAIL`**: The test failed, but it was almost a pass.
- **Allure Integration**: Neutrosophic values (T, I, F) and statuses are automatically published to **Allure Reports** as **test parameters**. This makes them immediately visible in the report's "Parameters" section for every assertion.
- **JUnit 5 Compatibility**: For other reporters (like Surefire or IDE consoles), the data is published via standard JUnit `publishReportEntry`, ensuring it's captured across different testing ecosystems.
- **Metadata Visibility**: You can see the exact (T, I, F) triplet for every assertion, allowing for deep analysis of why a "fuzzy" result was accepted or rejected.

### 📊 Viewing the Report
After running your tests, you can view the rich Allure report by pointing the Allure CLI to the results directory within the `neutrotest-demo` module:

```bash
allure serve neutrotest-demo/target/allure-results
```

## 🧠 Core Concepts: The (T, I, F) Triple

Every evaluation in Neutrotest results in a `NeutrosophicValue`, which consists of three components in the range [0.0, 1.0]:
- **Truth (T)**: The degree to which the condition is true.
- **Indeterminacy (I)**: The degree of uncertainty or "don't know."
- **Falsity (F)**: The degree to which the condition is false.

The core constraint is `T + I + F <= 1.0`.

## 🛠️ Quick Start

### 1. Add to your project
*(Note: Not yet available on Maven Central. For now, you can clone and install locally.)*

```bash
git clone https://github.com/osmanys-perez/neutrotest.git
cd neutrotest
mvn install
```

### 2. Write a Neutrosophic Test

```java
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicTest;
import io.github.osmanys_perez.neutrotest.junit5.NeutrosophicAssertions;
import static io.github.osmanys_perez.neutrotest.evaluator.FuzzyStringEvaluator.comparedTo;

public class MyFuzzyTest {

    @Test
    @NeutrosophicTest(context = "lenient") // Uses a predefined 'lenient' context
    void testPartialMatch(ExtensionContext context) {
        String actual = "Neutrosophy is great";
        
        // This will pass even if it's not a 100% exact match
        NeutrosophicAssertions.assertThat(actual, 
            comparedTo("Neutrosophy"), 
            context
        ).isAccepted();
    }
}
```

## 🏗️ Built-in Evaluators

Neutrotest comes with several ready-to-use evaluators:
- **`NumericEvaluator`**: Approximate numeric equality with tolerance.
- **`FuzzyStringEvaluator`**: Partial string matching and similarity.
- **`CollectionContainsEvaluator`**: Find fuzzy matches within collections.
- **`ExceptionThrownEvaluator`**: Assertions on exceptions and their messages.
- **`TimeBoundEvaluator`**: Performance and execution time assertions.

## ⚙️ Customizing the Context

The `NeutrosophicContext` defines the "pass" criteria for your tests. You can use predefined contexts or create your own:

```java
@NeutrosophicTest(
    truthThreshold = 0.9, 
    indeterminacyThreshold = 0.1, 
    falsityThreshold = 0.1
)
void myCustomStrictTest(ExtensionContext context) { ... }
```

## 🤝 Contributing

Contributions are welcome! Please feel free to:
- Open an Issue to report a bug or suggest a feature.
- Submit a Pull Request with improvements.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
