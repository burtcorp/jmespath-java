package io.burt.jmespath;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.net.URISyntaxException;

import org.junit.runner.RunWith;

import io.burt.jmespath.parser.ParseException;
import io.burt.jmespath.function.ArgumentTypeException;
import io.burt.jmespath.function.ArityException;
import io.burt.jmespath.function.FunctionCallException;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;

@RunWith(ComplianceRunner.class)
public abstract class JmesPathComplianceTest<T> {
  private static final String TESTS_PATH = "/jmespath.test/tests/";

  protected abstract Adapter<T> runtime();

  public static class ComplianceTest<U> {
    private final Adapter<U> runtime;
    private final String featureName;
    private final String expression;
    private final U input;
    private final U expectedResult;
    private final String expectedError;
    private final String suiteComment;
    private final String testComment;
    private final String benchmark;

    public ComplianceTest(Adapter<U> runtime, String featureName, String expression, U input, U expectedResult, String expectedError, String suiteComment, String testComment, String benchmark) {
      this.runtime = runtime;
      this.featureName = featureName;
      this.expression = expression;
      this.input = input;
      this.expectedResult = expectedResult;
      this.expectedError = expectedError;
      this.suiteComment = suiteComment;
      this.testComment = testComment;
      this.benchmark = benchmark;
    }

    public String name() {
      StringBuilder name = new StringBuilder();
      name.append(featureName);
      name.append(": ");
      if (suiteComment != null) {
        name.append(suiteComment);
        name.append(" ");
      }
      if (testComment != null) {
        name.append(testComment);
      } else {
        name.append(expression);
      }
      return name.toString();
    }

    public void run() {
      try {
        Expression<U> compiledExpression = runtime.compile(expression);
        U result = compiledExpression.search(input);
        if (expectedError == null) {
          assertThat(result, is(expectedResult));
        } else if ("syntax".equals(expectedError)) {
          fail("Expected ParseException to have been raised");
        } else if ("invalid-type".equals(expectedError)) {
          fail("Expected ArgumentTypeException to have been raised");
        } else if ("invalid-arity".equals(expectedError)) {
          fail("Expected ArityException to have been raised");
        } else if ("unknown-function".equals(expectedError)) {
          fail("Expected FunctionCallException to have been raised");
        }
      } catch (ParseException pe) {
        if (!"syntax".equals(expectedError)) {
          throw pe;
        }
      } catch (ArgumentTypeException ate) {
        if (!"invalid-type".equals(expectedError)) {
          throw ate;
        }
      } catch (ArityException ae) {
        if (!"invalid-arity".equals(expectedError)) {
          throw ae;
        }
      } catch (FunctionCallException fce) {
        if (!"unknown-function".equals(expectedError)) {
          throw fce;
        }
      }
    }
  }

  protected T loadFeatureDescription(String featureName) {
    String path = String.format("%s%s.json", TESTS_PATH, featureName);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(path)))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line);
      }
      return runtime().parseString(buffer.toString());
    } catch (IOException ioe) {
      throw new RuntimeException(String.format("Failed parsing %s", path), ioe);
    }
  }

  public Iterable<String> getFeatureNames() {
    try {
      File testsDir = new File(getClass().getResource(TESTS_PATH).toURI());
      List<String> featureNames = new LinkedList<>();
      for (File testFile : testsDir.listFiles()) {
        String fileName = testFile.getName();
        featureNames.add(fileName.substring(0, fileName.length() - 5));
      }
      return featureNames;
    } catch (URISyntaxException use) {
      throw new RuntimeException("Could not load compliance feature names", use);
    }
  }

  public Collection<ComplianceTest<T>> getTests(String featureName) {
    List<ComplianceTest<T>> tests = new LinkedList<>();
    T featureDescription = loadFeatureDescription(featureName);
    for (T suiteDescription : runtime().toList(featureDescription)) {
      String suiteComment = valueAsStringOrNull(suiteDescription, "comment");
      T input = runtime().getProperty(suiteDescription, "given");
      T caseDescriptions = runtime().getProperty(suiteDescription, "cases");
      for (T caseDescription : runtime().toList(caseDescriptions)) {
        String testComment = valueAsStringOrNull(caseDescription, "comment");
        if (testComment == null) {
          testComment = valueAsStringOrNull(caseDescription, "description");
        }
        String expression = valueAsStringOrNull(caseDescription, "expression");
        T expectedResult = runtime().getProperty(caseDescription, "result");
        String expectedError = valueAsStringOrNull(caseDescription, "error");
        String benchmark = valueAsStringOrNull(caseDescription, "benchmark");
        if (expectedResult != null) {
          tests.add(new ComplianceTest<T>(runtime(), featureName, expression, input, expectedResult, expectedError, suiteComment, testComment, benchmark));
        }
      }
    }
    return tests;
  }

  private String valueAsStringOrNull(T object, String key) {
    T value = runtime().getProperty(object, key);
    return value == null ? null : runtime().toString(value);
  }

  public Collection<ComplianceTest<T>> getAllTests() {
    List<ComplianceTest<T>> tests = new LinkedList<>();
    for (String featureName : getFeatureNames()) {
      tests.addAll(getTests(featureName));
    }
    return tests;
  }
}
