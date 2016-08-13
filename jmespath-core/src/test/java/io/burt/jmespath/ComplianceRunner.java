package io.burt.jmespath;

import java.util.Collection;

import org.junit.runner.Runner;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

public class ComplianceRunner extends Runner {
  private final Class<?> testClass;

  public ComplianceRunner(Class<?> testClass) {
    this.testClass = testClass;
  }

  public Description getDescription() {
    Description description = Description.createSuiteDescription(testClass.getName(), testClass.getAnnotations());
    for (JmesPathComplianceTest.ComplianceTest complianceTest : getAllTests()) {
      description.addChild(createDescription(complianceTest));
    }
    return description;
  }

  private Description createDescription(JmesPathComplianceTest.ComplianceTest complianceTest) {
    return Description.createTestDescription(testClass, complianceTest.name());
  }

  @SuppressWarnings("unchecked")
  private Collection<JmesPathComplianceTest.ComplianceTest<?>> getAllTests() {
    try {
      return (Collection<JmesPathComplianceTest.ComplianceTest<?>>) ((JmesPathComplianceTest<?>) testClass.newInstance()).getAllTests();
    } catch (InstantiationException ie) {
      throw new RuntimeException("Could not instantiate runtime", ie);
    } catch (IllegalAccessException iae) {
      throw new RuntimeException("Could not instantiate runtime", iae);
    }
  }

  public void run(RunNotifier notifier) {
    for (JmesPathComplianceTest.ComplianceTest complianceTest : getAllTests()) {
      Description testDescription = createDescription(complianceTest);
      notifier.fireTestStarted(testDescription);
      try {
        complianceTest.run();
      } catch (AssertionError ae) {
        notifier.fireTestFailure(new Failure(testDescription, ae));
      } catch (Exception e) {
        notifier.fireTestFailure(new Failure(testDescription, e));
      } finally {
        notifier.fireTestFinished(testDescription);
      }
    }
  }
}
