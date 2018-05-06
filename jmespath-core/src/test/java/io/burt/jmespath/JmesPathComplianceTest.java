package io.burt.jmespath;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Enumeration;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.net.URISyntaxException;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

import org.junit.runner.RunWith;
import org.hamcrest.Matcher;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.allOf;

@RunWith(ComplianceRunner.class)
public abstract class JmesPathComplianceTest<T> {
  private static final String TESTS_PATH = "jmespath.test/tests/";

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

    public ComplianceTest(Adapter<U> runtime, String featureName, String expression, U input, U expectedResult, String expectedError, String suiteComment, String testComment, String benchmark) {
      this.runtime = runtime;
      this.featureName = featureName;
      this.expression = expression;
      this.input = input;
      this.expectedResult = expectedResult;
      this.expectedError = expectedError;
      this.suiteComment = suiteComment;
      this.testComment = testComment;
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
          assertTrue(
            String.format("Expected <%s> to be <%s>, expression <%s> compiled expression <%s>", result, expectedResult, expression, compiledExpression),
            runtime.compare(expectedResult, result) == 0
          );
        } else {
          fail(String.format("Expected \"%s\" error", expectedError));
        }
      } catch (Exception e) {
        if (expectedError == null) {
          throw e;
        } else {
          String[] pieces = expectedError.split("-");
          Matcher<String> matcher = containsString(pieces[0]);
          for (int i = 1; i < pieces.length; i++) {
            matcher = allOf(matcher, containsString(pieces[i]));
          }
          assertThat(e.getMessage().toLowerCase(), matcher);
        }
      }
    }
  }

  protected T loadFeatureDescription(String featureName) {
    String path = String.format("/%s%s.json", TESTS_PATH, featureName);
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(JmesPathComplianceTest.class.getResourceAsStream(path), Charset.forName("UTF-8")))) {
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
      List<String> featureNames = new LinkedList<>();
      URI uri = JmesPathComplianceTest.class.getResource(String.format("/%s", TESTS_PATH)).toURI();
      if (uri.getScheme().equals("jar")) {
        String jarPath = uri.toString().substring("jar:file:".length(), uri.toString().indexOf("!"));
        try (JarFile jarFile = new JarFile(URLDecoder.decode(jarPath, "UTF-8"))) {
	        Enumeration<JarEntry> entries = jarFile.entries();
	        while (entries.hasMoreElements()) {
	          String fileName = entries.nextElement().getName();
	          if (fileName.startsWith(TESTS_PATH) && fileName.length() > TESTS_PATH.length()) {
              int lastSlashIndex = fileName.lastIndexOf("/");
              int lastPeriodIndex = fileName.lastIndexOf(".");
              String featureName = fileName.substring(lastSlashIndex + 1, lastPeriodIndex);
	            featureNames.add(featureName);
	          }
	        }
        }
      } else {
        File[] testFiles = new File(uri).listFiles();
        if (testFiles != null) {
          for (File testFile : testFiles) {
            String fileName = testFile.getName();
            featureNames.add(fileName.substring(0, fileName.length() - 5));
          }
        }
      }
      return featureNames;
    } catch (IOException ioe) {
      throw new RuntimeException("Could not load compliance feature names", ioe);
    } catch (URISyntaxException use) {
      throw new RuntimeException("Could not load compliance feature names", use);
    }
  }

  public Collection<ComplianceTest<T>> getTests(String featureName) {
    List<ComplianceTest<T>> tests = new LinkedList<>();
    T featureDescription = loadFeatureDescription(featureName);
    for (T suiteDescription : runtime().toList(featureDescription)) {
      String suiteComment = valueAsStringOrNull(suiteDescription, "comment");
      T input = runtime().getProperty(suiteDescription, runtime().createString("given"));
      T caseDescriptions = runtime().getProperty(suiteDescription, runtime().createString("cases"));
      for (T caseDescription : runtime().toList(caseDescriptions)) {
        String testComment = valueAsStringOrNull(caseDescription, "comment");
        if (testComment == null) {
          testComment = valueAsStringOrNull(caseDescription, "description");
        }
        String expression = valueAsStringOrNull(caseDescription, "expression");
        T expectedResult = runtime().getProperty(caseDescription, runtime().createString("result"));
        String expectedError = valueAsStringOrNull(caseDescription, "error");
        String benchmark = valueAsStringOrNull(caseDescription, "benchmark");
        if (runtime().typeOf(expectedResult) != JmesPathType.NULL || expectedError != null) {
          tests.add(new ComplianceTest<T>(runtime(), featureName, expression, input, expectedResult, expectedError, suiteComment, testComment, benchmark));
        }
      }
    }
    return tests;
  }

  private String valueAsStringOrNull(T object, String key) {
    T value = runtime().getProperty(object, runtime().createString(key));
    return runtime().typeOf(value) == JmesPathType.NULL ? null : runtime().toString(value);
  }

  public Collection<ComplianceTest<T>> getAllTests() {
    List<ComplianceTest<T>> tests = new LinkedList<>();
    for (String featureName : getFeatureNames()) {
      tests.addAll(getTests(featureName));
    }
    return tests;
  }
}
