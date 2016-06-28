package io.burt.jmespath.function;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;

import io.burt.jmespath.Adapter;
import io.burt.jmespath.jcf.JcfAdapter;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.containsString;

public class FunctionTest {
  private final Adapter<Object> adapter = new JcfAdapter();

  @Function(arity = 3)
  private static class BadName extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(name = "hello_world", arity = 3)
  private static class NameFromAnnotation extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(arity = 3)
  private static class NameFromClassNameFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(maxArity = 3)
  private static class NoMinArityFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(minArity = 3)
  private static class NoMaxArityFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(arity = 3)
  private static class FixedArityFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(minArity = 1, maxArity = Integer.MAX_VALUE)
  private static class VariadicFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Function(minArity = 1, maxArity = 2)
  private static class OptionalArgumentFunction extends JmesPathFunction {
    @Override
    protected <T> T internalCall(Adapter<T> adapter, List<ExpressionOrValue<T>> arguments) { return null; }
  }

  @Test
  public void nameMustEndWithFunction() {
    try {
      new BadName();
      fail("No exception thrown");
    } catch (FunctionConfigurationException fce) {
      assertThat(fce.getMessage(), containsString("must end with \"Function\""));
    }
  }

  @Test
  public void nameFromAnnotation() {
    assertThat(new NameFromAnnotation().name(), is("hello_world"));
  }

  @Test
  public void nameFromClassName() {
    assertThat(new NameFromClassNameFunction().name(), is("name_from_class_name"));
  }

  @Test
  public void arityOrMinArityIsRequired() {
    try {
      new NoMinArityFunction();
      fail("No exception thrown");
    } catch (FunctionConfigurationException fce) {
      assertThat(fce.getMessage(), containsString("must specify either minArity or arity"));
    }
  }

  @Test
  public void arityOrMaxArityIsRequired() {
    try {
      new NoMaxArityFunction();
      fail("No exception thrown");
    } catch (FunctionConfigurationException fce) {
      assertThat(fce.getMessage(), containsString("must specify either maxArity or arity"));
    }
  }

  @Test
  public void minArityCheck() {
    try {
      List<ExpressionOrValue<Object>> tooFewArguments = Arrays.asList(new ExpressionOrValue<Object>(1), new ExpressionOrValue<Object>(2));
      new FixedArityFunction().call(adapter, tooFewArguments);
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling fixed_arity: expected 3 but was 2"));
    }
  }

  @Test
  public void maxArityCheck() {
    try {
      List<ExpressionOrValue<Object>> tooManyArguments = Arrays.asList(new ExpressionOrValue<Object>(1), new ExpressionOrValue<Object>(2), new ExpressionOrValue<Object>(3), new ExpressionOrValue<Object>(4));
      new FixedArityFunction().call(adapter, tooManyArguments);
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling fixed_arity: expected 3 but was 4"));
    }
  }

  @Test
  public void arityCheckForVariadicFunction() {
    try {
      List<ExpressionOrValue<Object>> tooFewArguments = Collections.emptyList();
      new VariadicFunction().call(adapter, tooFewArguments);
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling variadic: expected at least 1 but was 0"));
    }
  }

  @Test
  public void arityCheckForOptionalArgument() {
    try {
      List<ExpressionOrValue<Object>> tooFewArguments = Collections.emptyList();
      new OptionalArgumentFunction().call(adapter, tooFewArguments);
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling optional_argument: expected at least 1 but was 0"));
    }
    try {
      List<ExpressionOrValue<Object>> tooManyArguments = Arrays.asList(new ExpressionOrValue<Object>(1), new ExpressionOrValue<Object>(2), new ExpressionOrValue<Object>(3), new ExpressionOrValue<Object>(4));
      new OptionalArgumentFunction().call(adapter, tooManyArguments);
    } catch (ArityException ae) {
      assertThat(ae.getMessage(), containsString("Wrong number of arguments calling optional_argument: expected at most 2 but was 4"));
    }
  }
}
