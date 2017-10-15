package org.luossfi.test.internal.parser.data.fnwd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.luossfi.internal.parser.data.fnwd.RegExpFileRuleImpl;

@SuppressWarnings( "static-method" )
@DisplayName( "A RegExpFileRuleImpl" )
class RegExpFileRuleImplTest
{
  RegExpFileRuleImpl rule;

  @SuppressWarnings( "unused" )
  @Test
  @DisplayName( "is instantiated with new RegExpFileRuleImpl( ruleRegExp )" )
  void isInstantiatedWithNew()
  {
    new RegExpFileRuleImpl( ".*" );
  }

  @Nested
  @DisplayName( "instantiation fails" )
  class Fails
  {
    @Test
    @DisplayName( "with NullPointerException if ruleRegExp is null" )
    void failsForNullInput()
    {
      assertThatThrownBy( () -> new RegExpFileRuleImpl( null ) ).isInstanceOf( NullPointerException.class );
    }

    @Test
    @DisplayName( "with PatternSyntaxException if ruleRegExp is an invalid regular expression" )
    void failsForIllegalRegEx()
    {
      assertThatThrownBy( () -> new RegExpFileRuleImpl( "[abc" ) ).isInstanceOf( PatternSyntaxException.class );
    }
  }

  @Nested
  @DisplayName( "when created" )
  class WhenCreated
  {
    @BeforeEach
    void createRule()
    {
      rule = new RegExpFileRuleImpl( "[a-z0-9]*\\.java" );
    }

    @Nested
    @DisplayName( "invoking testCompliance( fileName )" )
    class InvokingTestCompliance
    {
      @Test
      @DisplayName( "returns true if fileName is valid" )
      void returnsTrueForValidName()
      {
        assertThat( rule.testCompliance( "foo42.java" ) ).isTrue();
      }

      @Test
      @DisplayName( "returns false if fileName is invalid" )
      void returnsFalseForInvalidName()
      {
        assertThat( rule.testCompliance( "Foo42.java" ) ).isFalse();
      }

      @Test
      @DisplayName( "throws NullPointerException if fileName is null" )
      void failsForNullInput()
      {
        assertThatThrownBy( () -> rule.testCompliance( null ) ).isInstanceOf( NullPointerException.class );
      }
    }

    @Nested
    @DisplayName( "invoking equals( other )" )
    class InvokingEquals
    {
      @Test
      @DisplayName( "returns true if other is self" )
      void returnsTrueForSelf()
      {
        assertThat( rule.equals( rule ) ).isTrue();
      }

      @Test
      @DisplayName( "returns true if other bases on same regular expression" )
      void returnsTrueForSameRegExp()
      {
        final RegExpFileRuleImpl other = new RegExpFileRuleImpl( "[a-z0-9]*\\.java" );
        assumeTrue( rule != other );
        assertThat( rule.equals( other ) ).isTrue();
      }

      @Test
      @DisplayName( "returns false if other bases on different regular expression" )
      void returnsTrueForDifferentRegExp()
      {
        final RegExpFileRuleImpl other = new RegExpFileRuleImpl( "[A-Z0-9]*\\.java" );
        assumeTrue( rule != other );
        assertThat( rule.equals( other ) ).isFalse();
      }

      @Test
      @DisplayName( "returns false if other is null" )
      void returnsFalseForNullInput()
      {
        assertThat( rule.equals( null ) ).isFalse();
      }

      @SuppressWarnings( "unlikely-arg-type" )
      @Test
      @DisplayName( "returns false if other is of unrelated type" )
      void returnsFalseForUnrelated()
      {
        assertThat( rule.equals( "Foo" ) ).isFalse();
      }
    }

    @Nested
    @DisplayName( "invoking HashCode()" )
    class InvokingHashCode
    {
      @Test
      @DisplayName( "returns the same value for subsequent calls" )
      void returnSameValueOnSubsequentCalls()
      {
        final int hash1 = rule.hashCode();
        final int hash2 = rule.hashCode();

        assertThat( hash1 ).isEqualTo( hash2 );
      }

      @Test
      @DisplayName( "returns the same value for equal objects" )
      void returnSameValueForEqualObjects()
      {
        final RegExpFileRuleImpl other = new RegExpFileRuleImpl( "[a-z0-9]*\\.java" );

        assumeTrue( rule != other );
        assumeTrue( rule.equals( other ) );

        assertThat( rule.hashCode() ).isEqualTo( other.hashCode() );
      }

      @Test
      @DisplayName( "returns a different value for unequal objects" )
      void returnDifferentValueForEqualObjects()
      {
        final RegExpFileRuleImpl other = new RegExpFileRuleImpl( "[A-Z0-9]*\\.java" );

        assumeTrue( rule != other );
        assumeFalse( rule.equals( other ) );

        assertThat( rule.hashCode() ).isNotEqualTo( other.hashCode() );
      }
    }

    @Nested
    @DisplayName( "invoking toString()" )
    class InvokingToString
    {

      @Test
      @DisplayName( "returns non null, non blank, non default value" )
      void returnNonNullNonDefaultValue()
      {
        assertThat( rule.toString() ).isNotBlank().isNotEqualTo( rule.getClass().getName() + '@' + Integer.toHexString( rule.hashCode() ) );
      }
    }
  }
}
