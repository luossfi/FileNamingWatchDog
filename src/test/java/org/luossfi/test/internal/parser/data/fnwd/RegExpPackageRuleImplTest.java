package org.luossfi.test.internal.parser.data.fnwd;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.luossfi.internal.parser.data.fnwd.FileRule;
import org.luossfi.internal.parser.data.fnwd.PackageRule;
import org.luossfi.internal.parser.data.fnwd.RegExpFileRuleImpl;
import org.luossfi.internal.parser.data.fnwd.RegExpPackageRuleImpl;

@SuppressWarnings( "static-method" )
@DisplayName( "A RegExpPackageRuleImpl" )
class RegExpPackageRuleImplTest
{
  static final List<FileRule> FILE_RULES = asList( new RegExpFileRuleImpl( "[a-z0-9].java" ), new RegExpFileRuleImpl( "package-info.java" ) );

  RegExpPackageRuleImpl       rule;

  @SuppressWarnings( "unused" )
  @Test
  @DisplayName( "is instantiated with new RegExpPackageRuleImpl( ruleRegExp, fileRules )" )
  void isInstantiatedWithNew()
  {
    new RegExpPackageRuleImpl( ".*", FILE_RULES );
  }

  @Nested
  @DisplayName( "instantiation fails" )
  class Fails
  {
    @Test
    @DisplayName( "with NullPointerException if ruleRegExp is null" )
    void failsForNullRegExp()
    {
      assertThatThrownBy( () -> new RegExpPackageRuleImpl( null, FILE_RULES ) ).isInstanceOf( NullPointerException.class );
    }

    @Test
    @DisplayName( "with PatternSyntaxException if ruleRegExp is an invalid regular expression" )
    void failsForIllegalRegEx()
    {
      assertThatThrownBy( () -> new RegExpPackageRuleImpl( "[abc", FILE_RULES ) ).isInstanceOf( PatternSyntaxException.class );
    }

    @Test
    @DisplayName( "with NullPointerException if fileRules is null" )
    void failsForNullFileRules()
    {
      assertThatThrownBy( () -> new RegExpPackageRuleImpl( ".*", null ) ).isInstanceOf( NullPointerException.class );
    }

    @Test
    @DisplayName( "with IllegalArgumentException if fileRules is empty" )
    void failsForEmptyFileRules()
    {
      assertThatThrownBy( () -> new RegExpPackageRuleImpl( ".*", emptyList() ) ).isInstanceOf( IllegalArgumentException.class );
    }
  }

  @Nested
  @DisplayName( "when created" )
  class WhenCreated
  {
    static final String RULE_REG_EXP       = "org\\.luossfi\\.[a-z]*";
    static final String OTHER_RULE_REG_EXP = "org\\.luossfi\\.foo\\.[0-9]+";

    @BeforeEach
    void createRule()
    {
      rule = new RegExpPackageRuleImpl( RULE_REG_EXP, FILE_RULES );
    }

    @Nested
    @DisplayName( "invoking testCompliance( packageName )" )
    class InvokingTestCompliance
    {
      @Test
      @DisplayName( "returns true if packageName isvalid" )
      void returnsTrueForValidName()
      {
        assertThat( rule.testCompliance( "org.luossfi.test" ) ).isTrue();
      }

      @Test
      @DisplayName( "returns false if packageName is invalid" )
      void returnsFalseForInvalidName()
      {
        assertThat( rule.testCompliance( "org.luossfi.42" ) ).isFalse();
      }

      @Test
      @DisplayName( "throws NullPointerException if packageName is null" )
      void failsForNullInput()
      {
        assertThatThrownBy( () -> rule.testCompliance( null ) ).isInstanceOf( NullPointerException.class );
      }
    }

    @Nested
    @DisplayName( "invoking getFileRules()" )
    class InvokingGetFileRules
    {
      @Test
      @DisplayName( "returns same content as constructor input" )
      void returnsContentEqualToOriginalInput()
      {
        assertThat( rule.getFileRules() ).containsExactlyElementsOf( FILE_RULES );
      }

      @Nested
      @DisplayName( "returns list" )
      class ReturnedList
      {
        List<FileRule> list;

        @BeforeEach
        void getList()
        {
          list = rule.getFileRules();
        }

        @Test
        @DisplayName( "fails on invoking add( fileRule )" )
        void failOnAdd()
        {
          assertThatThrownBy( () -> list.add( new RegExpFileRuleImpl( "abc" ) ) ).isInstanceOf( UnsupportedOperationException.class );
        }

        @Test
        @DisplayName( "fails on invoking set( index, fileRule )" )
        void failOnSet()
        {
          assertThatThrownBy( () -> list.set( 0, new RegExpFileRuleImpl( "abc" ) ) ).isInstanceOf( UnsupportedOperationException.class );
        }

        @Test
        @DisplayName( "fails on invoking remove( index )" )
        void failOnRemove()
        {
          assertThatThrownBy( () -> list.remove( 0 ) ).isInstanceOf( UnsupportedOperationException.class );
        }

        @Test
        @DisplayName( "fails on invoking addAll( fileRules )" )
        void failOnAddAll()
        {
          assertThatThrownBy( () -> list.addAll( FILE_RULES ) ).isInstanceOf( UnsupportedOperationException.class );
        }

        @Test
        @DisplayName( "fails on invoking addAll( fileRules )" )
        void failOnReplaceAll()
        {
          assertThatThrownBy( () -> list.replaceAll( rule -> rule ) ).isInstanceOf( UnsupportedOperationException.class );
        }

        @Test
        @DisplayName( "fails on invoking addAll( fileRules )" )
        void failOnSort()
        {
          assertThatThrownBy( () -> list.sort( ( rule1, rule2 ) -> Integer.compare( rule1.hashCode(), rule2.hashCode() ) ) )
              .isInstanceOf( UnsupportedOperationException.class );
        }

      }
    }

    @Nested
    @DisplayName( "invoking merge( other )" )
    class InvokingMerge
    {
      final List<FileRule> OTHER_FILE_RULES               = asList( new RegExpFileRuleImpl( "[a-z].java" ), new RegExpFileRuleImpl( "foo.java" ),
          new RegExpFileRuleImpl( "bar.xml" ) );

      final List<FileRule> PARTIALLY_DIFFERENT_FILE_RULES = asList( new RegExpFileRuleImpl( "[a-z].java" ), new RegExpFileRuleImpl( "foo.java" ),
          new RegExpFileRuleImpl( "package-info.java" ) );

      @Test
      @DisplayName( "throws NullPointerException if other is null" )
      void failOnNullInput()
      {
        assertThatThrownBy( () -> rule.merge( null ) ).isInstanceOf( NullPointerException.class );
      }

      @Test
      @DisplayName( "throws IllegalArgumentException if other is not equal" )
      void failsOnUnequalOther()
      {
        final PackageRule other = new RegExpPackageRuleImpl( OTHER_RULE_REG_EXP, FILE_RULES );
        assumeFalse( rule.equals( other ) );
        assertThatThrownBy( () -> rule.merge( other ) ).isInstanceOf( IllegalArgumentException.class );
      }

      @Test
      @DisplayName( "returns new instace" )
      void returnNewInstance()
      {
        final PackageRule merged = rule.merge( rule );

        assertThat( merged ).isNotSameAs( rule );
      }

      @Test
      @DisplayName( "returns package rule with same file rules if merged with self" )
      void returnSameContentForSelf()
      {
        final PackageRule merged = rule.merge( rule );

        assumeTrue( merged != rule );
        assertThat( merged.getFileRules() ).containsExactlyElementsOf( FILE_RULES );
      }

      @Test
      @DisplayName( "returns package rule with file rules from rule and other" )
      void returnCombinedContentForOther()
      {
        final PackageRule disjunct = new RegExpPackageRuleImpl( RULE_REG_EXP, OTHER_FILE_RULES );
        final PackageRule merged = rule.merge( disjunct );

        assumeTrue( rule.equals( disjunct ) );
        assumeTrue( merged != rule );
        assumeTrue( merged != disjunct );

        assertThat( merged.getFileRules() ).containsExactly( FILE_RULES.get( 0 ), FILE_RULES.get( 1 ), OTHER_FILE_RULES.get( 0 ),
            OTHER_FILE_RULES.get( 1 ), OTHER_FILE_RULES.get( 2 ) );
      }

      @Test
      @DisplayName( "returns package rule with distinct file rules from rule and partially other" )
      void returnCombinedContentForPartiallyOther()
      {
        final PackageRule partially = new RegExpPackageRuleImpl( RULE_REG_EXP, PARTIALLY_DIFFERENT_FILE_RULES );
        final PackageRule merged = rule.merge( partially );

        assumeTrue( rule.equals( partially ) );
        assumeTrue( merged != rule );
        assumeTrue( merged != partially );

        assertThat( merged.getFileRules() ).containsExactly( FILE_RULES.get( 0 ), FILE_RULES.get( 1 ), OTHER_FILE_RULES.get( 0 ),
            OTHER_FILE_RULES.get( 1 ) );
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
        final RegExpPackageRuleImpl other = new RegExpPackageRuleImpl( RULE_REG_EXP, FILE_RULES );
        assumeTrue( rule != other );
        assertThat( rule.equals( other ) ).isTrue();
      }

      @Test
      @DisplayName( "returns false if other bases on different regular expression" )
      void returnsTrueForDifferentRegExp()
      {
        final RegExpPackageRuleImpl other = new RegExpPackageRuleImpl( OTHER_RULE_REG_EXP, FILE_RULES );
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
    @DisplayName( "invoking hashCode()" )
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
        final RegExpPackageRuleImpl other = new RegExpPackageRuleImpl( RULE_REG_EXP, FILE_RULES );

        assumeTrue( rule != other );
        assumeTrue( rule.equals( other ) );

        assertThat( rule.hashCode() ).isEqualTo( other.hashCode() );
      }

      @Test
      @DisplayName( "returns a different value for unequal objects" )
      void returnDifferentValueForEqualObjects()
      {
        final RegExpPackageRuleImpl other = new RegExpPackageRuleImpl( OTHER_RULE_REG_EXP, FILE_RULES );

        assumeTrue( rule != other );
        assumeFalse( rule.equals( other ) );

        assertThat( rule.hashCode() ).isNotEqualTo( other.hashCode() );
      }
    }

    @Nested
    @DisplayName( "invoking toString()" )
    class invokingToString
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
