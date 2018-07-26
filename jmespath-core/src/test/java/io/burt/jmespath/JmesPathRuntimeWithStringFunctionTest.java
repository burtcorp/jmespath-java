package io.burt.jmespath;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.regex.PatternSyntaxException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import io.burt.jmespath.function.ArgumentTypeException;
import io.burt.jmespath.function.FunctionRegistry;
import io.burt.jmespath.parser.ParseException;

public abstract class JmesPathRuntimeWithStringFunctionTest<T> extends JmesPathRuntimeTest<T> {
  private Adapter<T> runtime = createRuntime(RuntimeConfiguration.builder()
          .withFunctionRegistry(FunctionRegistry.stringManipulationRegistry())
          .build());

  @Override
  protected Adapter<T> runtime() { return runtime; }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  private T dontCare, emptyObject, emptyList;

  @Before
  public void setUp() {
    emptyObject = parse("{}");
    emptyList = parse("[]");
    dontCare = emptyObject;
  }
  /**
   * The following test cases are inspired by the examples of the related functions from XPath Function Specification:
   * https://www.w3.org/TR/xpath-functions-31/
   * Those examples just illustrate the statements of specification rules and are considered as non-normative.
   */
  @Test
  public void concatJoinsTheParts() {
    T result1 = search("concat('un', 'grateful')", dontCare);
    T result2 = search("concat('Ingratitude, ', 'thou ', 'marble-hearted', ' fiend!')", dontCare);
    assertThat(result1, is(jsonString("ungrateful")));
    assertThat(result2, is(jsonString("Ingratitude, thou marble-hearted fiend!")));
  }

  @Test
  public void concatFiltersOutNullTypes() {
    T result1 = search("concat('Thy ', [], 'old ', `\"groans\"`, \"\", ' ring', ' yet', ' in', ' my', ' ancient',' ears.')", dontCare);
    T result2 = search("concat('Ciao!',[])", dontCare);
    assertThat(result1, is(jsonString("Thy old groans ring yet in my ancient ears.")));
    assertThat(result2, is(jsonString("Ciao!")));
  }

  @Test
  public void concatLiterals() {
    T result1 = search("concat(`1`, `2`, `3`, `4`, `true`)", parse("{}"));
    T result2 = search("concat(`1`, @)", parse("true"));
    assertThat(result1, is(jsonString("1234true")));
    assertThat(result2, is(jsonString("1true")));
  }

  @Test
  public void concatRequiresAtLeastTwoArguments() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"concat\" (expected at least 2 but was 1)"));
    search("concat(@)", parse("{}"));
  }

  @Test
  public void lowerCaseExampleFromXPathSpec() {
    T result = search("lower_case('ABc!D')", dontCare);
    assertThat(result, is(jsonString("abc!d")));
  }

  @Test
  public void lowerCaseRequiresASingleArgument() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"lower_case\" (expected 1 but was 2)"));
    search("lower_case(@, @)", dontCare);
  }

  @Test
  public void lowerCaseRequiresAStringAsArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was array"));
    search("lower_case(@)", parse("[3]"));
  }

  @Test
  public void upperCaseExamplesFromXPathSpec() {
    T result = search("upper_case('abCd0')", dontCare);
    assertThat(result, is(jsonString("ABCD0")));
  }

  @Test
  public void upperCaseRequiresASingleArgument() {
    thrown.expect( ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"upper_case\" (expected 1 but was 2)"));
    search("upper_case(@, @)", dontCare);
  }

  @Test
  public void upperCaseRequiresAStringAsArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was object"));
    search("upper_case(@)", dontCare);
  }

  @Test
  public void normalizeSpaceExamplesFromXPathSpec() {
    T result = search("normalize_space(@)", parse("\" The    wealthy curled darlings\\n" +
            "                             of    our    nation. \""));
    assertThat(result, is(jsonString("The wealthy curled darlings of our nation.")));
  }

  @Test
  public void normalizeSpaceRemovesLeadingWhitespaces() {
    T result = search("normalize_space(str)", parse("{ \"str\" : \"\\n\\t \\tend\"}"));
    assertThat(result, is(jsonString("end")));
  }

  @Test
  public void normalizeSpaceRemovesTrailingWhitespaces() {
    T result = search("normalize_space(str)", parse("{ \"str\" : \"begin\\n \\t \\t\"}"));
    assertThat(result, is(jsonString("begin")));
  }

  @Test
  public void normalizeSpaceCollapseInnerWhitespaces() {
    T result = search("normalize_space(str)", parse("{ \"str\" : \"begin\\n\\t \\tend\"}"));
    assertThat(result, is(jsonString("begin end")));
  }

  @Test
  public void normalizeSpaceRequiresASingleArgument() {
    thrown.expect( ParseException.class );
    thrown.expectMessage(containsString("invalid arity calling \"normalize_space\" (expected 1 but was 2)"));
    search("normalize_space(@, @)", dontCare);
  }

  @Test
  public void normalizeSpaceRequiresAStringAsArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was object"));
    search("normalize_space(@)", dontCare);
  }

  @Test
  public void translateExamplesFromXpathSpec() {
    T result1 = search("translate('bar','abc','ABC')", dontCare);
    T result2 = search("translate('--aaa--', 'abc-', 'ABC')", dontCare);
    T result3 = search("translate('abcdabc', 'abc', 'AB')", dontCare);
    assertThat(result1, is(jsonString("BAr")));
    assertThat(result2, is(jsonString("AAA")));
    assertThat(result3, is(jsonString("ABdAB")));
  }

  @Test
  public void translateRequiresAStringAsFirstArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was object"));
    search("translate(@, 'foo', 'bar')", dontCare);
  }

  @Test
  public void translateRequiresAStringAsSecondArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was object"));
    search("translate('foo', @, 'bar')", dontCare);
  }

  @Test
  public void translateRequiresAStringAsThirdArgument() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was object"));
    search("translate('foo', 'bar', @)", dontCare);
  }

  @Test
  public void translateRequiresThreeArgumentsInsteadOfTwo() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"translate\" (expected 3 but was 2)"));
    search("translate('foo', 'bar')", dontCare);
  }

  @Test
  public void translateRequiresThreeArgumentsInsteadOfFour() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"translate\" (expected 3 but was 4)"));
    search("translate('foo', 'bar', 'baz', 'woo')", dontCare);

  }

  @Test
  public void translateRequiresAValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("translate(&foo, 'bar', 'baz')", dontCare);
  }

  @Test
  public void translateRequiresAValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("translate(&foo, 'bar', 'baz')", dontCare);
  }

  @Test
  public void translateRequiresAValue3() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("translate('foo', 'bar', &foo)", dontCare);
  }

  @Test
  public void substringAfterExamplesFromXpathSpec() {
    T result1 = search("substring_after('tattoo','tat')", dontCare);
    T result2 = search("substring_after('tattoo', 'tattoo')", dontCare);
    T result3 = search("substring_after(@, @)", emptyObject);
    T result4 = search("substring_after(@, @)", emptyList);
    assertThat(result1, is(jsonString("too")));
    assertThat(result2, is(jsonString("")));
    assertThat(result3, is(jsonString("")));
    assertThat(result4, is(jsonString("")));
  }

  @Test
  public void substringAfterDoesNotSupportCollation_deviationFromXPathSpec() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"substring_after\" (expected 2 but was 3)"));
    search("substring_after('abcdefgi','--d-e-', 'http://www.w3.org/2013/collation/UCA?lang=en;alternate=blanked;strength=primary')", dontCare);
  }

  @Test
  public void substringAfterRequiresAValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected any value but was expression"));
    search("substring_after(&foo, 'bar')", dontCare);
  }

  @Test
  public void substringAfterRequiresAValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected any value but was expression"));
    search("substring_after('foo', &foo)", dontCare);
  }

  @Test
  public void substringBeforeExamplesFromXpathSpec() {
    T result1 = search("substring_before('tattoo','attoo')", dontCare);
    T result2 = search("substring_before('tattoo', 'tatto')", dontCare);
    T result3 = search("substring_before(@, @)", emptyObject);
    T result4 = search("substring_before(@, @)", emptyList);
    assertThat(result1, is(jsonString("t")));
    assertThat(result2, is(jsonString("")));
    assertThat(result3, is(jsonString("")));
    assertThat(result4, is(jsonString("")));
  }

  @Test
  public void substringBeforeDoesNotSupportCollation_deviationFromXPathSpec() {
    thrown.expect(ParseException.class);
    thrown.expectMessage(containsString("invalid arity calling \"substring_before\" (expected 2 but was 3)"));
    search("substring_before('abcdefgi','--d-e-', 'http://www.w3.org/2013/collation/UCA?lang=en;alternate=blanked;strength=primary')", dontCare);
  }

  @Test
  public void substringBeforeRequiresAValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected any value but was expression"));
    search("substring_before(&foo, 'bar')", dontCare);
  }

  @Test
  public void substringBeforeRequiresAValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected any value but was expression"));
    search("substring_before('foo', &foo)", dontCare);
  }

  @Test
  public void matchesExamplesFromXPathSpec() {
    T result1 = search("matches('abracadabra', 'bra')", dontCare);
    T result2 = search("matches('abracadabra', '^a.*a$')", dontCare);
    T result3 = search("matches('abracadabra', '^bra')", dontCare);
    assertThat(result1, is(jsonBoolean(true)));
    assertThat(result2, is(jsonBoolean(true)));
    assertThat(result3, is(jsonBoolean(false)));
  }

  @Test
  public void matchesReturnsTrueCaseInsensitiveWithIFlag() {
    T withFlagI = search("matches('A', 'a', 'i')", dontCare);
    T otherwise = search("matches('A', 'a')", dontCare);
    assertThat(withFlagI, is(jsonBoolean(true)));
    assertThat(otherwise, is(jsonBoolean(false)));
  }

  @Test
  public void matchesReturnsTrueIfLiterallyMatchesWithQFlag() {
    T withFlagQ = search("matches('b^az', '^a', 'q')", dontCare);
    T otherwise = search("matches('b^az', '^a')", dontCare);
    assertThat(withFlagQ, is(jsonBoolean(true)));
    assertThat(otherwise, is(jsonBoolean(false)));
  }

  @Test
  public void matchesReturnsTrueMultilineWithMFlag() {
    T withFlagM = search("matches('a\nb\nc', '^b$', 'm')", dontCare);
    T otherwise = search("matches('a\nb\nc', '^b$', '')", dontCare);
    assertThat(withFlagM, is(jsonBoolean(true)));
    assertThat(otherwise, is(jsonBoolean(false)));
  }

  @Test
  public void matchesMatchesNewLineWithSFlag() {
    T withFlagS = search("matches('a\nb\nc', '.b.', 's')", dontCare);
    T otherwise = search("matches('a\nb\nc', '.b.')", dontCare);
    assertThat(withFlagS, is(jsonBoolean(true)));
    assertThat(otherwise, is(jsonBoolean(false)));
  }

  @Test
  public void matchesFlagsCanBeCombined() {
    T withFlagQ = search("matches('b^az', '^A', 'qi')", dontCare);
    T otherwise = search("matches('b^az', '^a')", dontCare);
    assertThat(withFlagQ, is(jsonBoolean(true)));
    assertThat(otherwise, is(jsonBoolean(false)));
  }

  @Test
  public void matchesThrowsPatternSyntaxExceptionOnInvalidPattern() {
    thrown.expect(PatternSyntaxException.class);
    search("matches('abba', '?')", dontCare);
  }

  @Test
  public void matchesThrowsPatternSyntaxExceptionOnZeroMatchingPattern() {
    thrown.expect(PatternSyntaxException.class);
    thrown.expectMessage("pattern matches zero-length string");
    search("matches('abba', '.?')", dontCare);
  }

  @Test
  public void matchesRequiresAStringValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("matches(&foo, 'bar', 'baz')", emptyObject);
  }

  @Test
  public void matchesRequiresAStringValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("matches('foo', &bar, 'baz')", emptyObject);
  }

  @Test
  public void matchesRequiresAStringValue3() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("matches('foo', 'bar', &baz)", emptyObject);
  }

  @Test
  public void replaceExamplesFromXpathSpec() {
    T result1 = search("replace('abracadabra', 'bra', '*')", dontCare);
    T result2 = search("replace('abracadabra', 'a.*a', '*')", dontCare);
    T result3 = search("replace('abracadabra', 'a.*?a', '*') ", dontCare);
    T result4 = search("replace('abracadabra', 'a', '')", dontCare);
    T result5 = search("replace('abracadabra', 'a(.)', 'a$1$1')", dontCare);
    T result6 = search("replace('AAAA', 'A+', 'b')", dontCare);
    T result7 = search("replace('AAAA', 'A+?', 'b')", dontCare);
    T result8 = search("replace('darted', '^(.*?)d(.*)$', '$1c$2')", dontCare);
    assertThat(result1, is(jsonString("a*cada*")));
    assertThat(result2, is(jsonString("*")));
    assertThat(result3, is(jsonString("*c*bra")));
    assertThat(result4, is(jsonString("brcdbr")));
    assertThat(result5, is(jsonString("abbraccaddabbra")));
    assertThat(result6, is(jsonString("b")));
    assertThat(result7, is(jsonString("bbbb")));
    assertThat(result8, is(jsonString("carted")));
  }

  @Test
  public void replaceMatchesCaseInsensitiveWithIFlag() {
    T withFlagI = search("replace('*A-b-C*', '[a-z]', '', 'i')", dontCare);
    T otherwise = search("replace('*A-b-C*', '[a-z]', '')", dontCare);
    assertThat(withFlagI, is(jsonString("*--*")));
    assertThat(otherwise, is(jsonString("*A--C*")));
  }

  @Test
  public void replaceMatchesLiterallyWithQFlag() {
    T withFlagQ = search("replace('a.b.c.d', '.', '', 'q')", dontCare);
    T otherwise = search("replace('a.b.c.d', '.', '')", dontCare);
    assertThat(withFlagQ, is(jsonString("abcd")));
    assertThat(otherwise, is(jsonString("")));
  }

  @Test
  public void replaceMatchesMultilineWithMFlag() {
    T withFlagM = search("replace('a\nb\nc', '^\\w$', '', 'm')", dontCare);
    T otherwise = search("replace('a\nb\nc', '^\\w$', '')", dontCare);
    assertThat(withFlagM, is(jsonString("\n\n")));
    assertThat(otherwise, is(jsonString("a\nb\nc")));
  }

  @Test
  public void replaceMatchesNewLineWithSFlag() {
    T withFlagS = search("replace('a\nb\nc', '.b.', '-B-', 's')", dontCare);
    T otherwise = search("replace('a\nb\nc', '.b.', '-B-')", dontCare);
    assertThat(withFlagS, is(jsonString("a-B-c")));
    assertThat(otherwise, is(jsonString("a\nb\nc")));
  }

  @Test
  public void replaceFlagsCanBeCombined() {
    T result = search("replace('a\nB\nc', '.b.', '-B-', 'si')", dontCare);
    assertThat(result, is(jsonString("a-B-c")));
  }

  @Test
  public void replaceThrowsPatternSyntaxExceptionOnInvalidPattern() {
    thrown.expect(PatternSyntaxException.class);
    search("replace('abba', '?', '')", dontCare);
  }

  @Test
  public void replaceThrowsPatternSyntaxExceptionOnZeroMatchingPattern() {
    thrown.expect(PatternSyntaxException.class);
    thrown.expectMessage("pattern matches zero-length string");
    search("replace('abracadabra', '.*?', '$1')", dontCare);
  }

  @Test
  public void replaceRequiresAStringValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("replace(&foo, 'bar', 'baz')", emptyObject);
  }

  @Test
  public void replaceRequiresAStringValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("replace('foo', &bar, 'baz')", emptyObject);
  }

  @Test
  public void replaceRequiresAStringValue3() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("replace('foo', 'bar', &baz)", emptyObject);
  }

  @Test
  public void replaceRequiresAStringValue4() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("replace('foo', 'bar', 'baz', &woo)", emptyObject);
  }
  
  @Test
  public void tokenizeWithoutPatternRemovesSurroundingWhitespacesToo_FirstExampleFromXpathSpec() {
    T result1 = search("tokenize(' red green blue ')", dontCare);
    assertThat(result1, is(jsonArrayOfStrings("red", "green", "blue")));
  }

  @Test
  public void tokenizeWithWhitespacePatternMayProduceEmptyParts_ThirdExampleFromXpathSpec() {
    T result3 = search("tokenize(' red green blue ', '\\s+')", dontCare);
    assertThat(result3, is(jsonArrayOfStrings("", "red", "green", "blue", "")));
  }

  @Test
  public void tokenizeWithWhitespacePatternSplitsIntoWords_SecondExampleFromXPathSpec() {
    T result2 = search("tokenize('The cat sat on the mat', '\\s+')", dontCare);
    assertThat(result2, is(jsonArrayOfStrings("The", "cat", "sat", "on", "the", "mat")));
  }

  @Test
  public void tokenizeRemainingExamplesFromXPathSpec() {
    T result4 = search("tokenize('1, 15, 24, 50', ',\\s*')", dontCare);
    T result5 = search("tokenize('1,15,,24,50,', ',')", dontCare);
    T result6 = search("tokenize('Some unparsed <br> HTML <BR> text', '\\s*<br>\\s*', 'i')", dontCare);
    T result7 = search("tokenize('abracadabra', '(ab)|(a)')", dontCare);
    assertThat(result4, is(jsonArrayOfStrings("1", "15", "24", "50")));
    assertThat(result5, is(jsonArrayOfStrings("1", "15", "", "24", "50", "")));
    assertThat(result6, is(jsonArrayOfStrings("Some unparsed", "HTML", "text")));
    assertThat(result7, is(jsonArrayOfStrings("", "r", "c", "d", "r", "")));
  }

  @Test
  public void tokenizeMatchesCaseInsensitiveWithIFlag() {
    T withFlagI = search("tokenize('*A-b-C*', '[a-z]', 'i')", dontCare);
    T otherwise = search("tokenize('*A-b-C*', '[a-z]')", dontCare);
    assertThat(withFlagI, is(jsonArrayOfStrings("*", "-", "-", "*")));
    assertThat(otherwise, is(jsonArrayOfStrings("*A-", "-C*")));
  }

  @Test
  public void tokenizeMatchesLiterallyWithQFlag() {
    T withFlagQ = search("tokenize('a.b.c.d', '.', 'q')", dontCare);
    T otherwise = search("tokenize('a.b.c.d', '.')", dontCare);
    assertThat(withFlagQ, is(jsonArrayOfStrings("a", "b", "c", "d")));
    assertThat(otherwise, is(jsonArrayOfStrings("", "", "", "", "", "", "", "")));
  }

  @Test
  public void tokenizeMatchesMultilineWithMFlag() {
    T withFlagM = search("tokenize('a\nb\nc', '^\\w$', 'm')", dontCare);
    T otherwise = search("tokenize('a\nb\nc', '^\\w$')", dontCare);
    assertThat(withFlagM, is(jsonArrayOfStrings("", "\n", "\n", "")));
    assertThat(otherwise, is(jsonArrayOfStrings("a\nb\nc")));
  }

  @Test
  public void tokenizeMatchesNewLineWithSFlag() {
    T withFlagS = search("tokenize('a\nb\nc', '.b.', 's')", dontCare);
    T otherwise = search("tokenize('a\nb\nc', '.b.')", dontCare);
    assertThat(withFlagS, is(jsonArrayOfStrings("a", "c")));
    assertThat(otherwise, is(jsonArrayOfStrings("a\nb\nc")));
  }

  @Test
  public void tokenizeFlagsCanBeCombined() {
    T withFlags = search("tokenize('a\nb\nc', '.B.', 'sim')", dontCare);
    T otherwise = search("tokenize('a\nb\nc', '.B.')", dontCare);
    assertThat(withFlags, is(jsonArrayOfStrings("a", "c")));
    assertThat(otherwise, is(jsonArrayOfStrings("a\nb\nc")));
  }

  @Test
  public void tokenizeThrowsPatternSyntaxExceptionOnInvalidPattern() {
    thrown.expect(PatternSyntaxException.class);
    search("tokenize('abba', '?')", dontCare);
  }

  @Test
  public void tokenizeThrowsPatternSyntaxExceptionOnZeroMatchingPattern() {
    thrown.expect(PatternSyntaxException.class);
    thrown.expectMessage("pattern matches zero-length string");
    search("tokenize('abba', '.?')", dontCare);
  }

  @Test
  public void tokenizeRequiresAStringValue1() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("tokenize(&foo, 'bar', 'baz')", emptyObject);
  }

  @Test
  public void tokenizeRequiresAStringValue2() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("tokenize('foo', &bar, 'baz')", emptyObject);
  }

  @Test
  public void tokenizeRequiresAStringValue3() {
    thrown.expect(ArgumentTypeException.class);
    thrown.expectMessage(containsString("expected string but was expression"));
    search("tokenize('foo', 'bar', &baz)", emptyObject);
  }
}
