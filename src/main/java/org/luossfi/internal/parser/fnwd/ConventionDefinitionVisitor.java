/*
 * FileNamingWatchDog, a library for checking Java packages and source file
 * names for compliance to naming conventions.
 *
 * Copyright (C) 2016++ Steff Lukas <steff.lukas@luossfi.org>
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.luossfi.internal.parser.fnwd;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toList;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.Token;
import org.luossfi.exception.fnwd.WatchDogException;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionLexer;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.AlternativesContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.CharGroupExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.CompositeExpressionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.ConventionDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.FileDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.GroupedExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.MultipliableExpressionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.PackageDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.StringContentContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.StringExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.WildcardExprContext;
import org.luossfi.internal.parser.data.fnwd.FileRule;
import org.luossfi.internal.parser.data.fnwd.PackageRule;
import org.luossfi.internal.parser.data.fnwd.RegExpFileRuleImpl;
import org.luossfi.internal.parser.data.fnwd.RegExpPackageRuleImpl;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParserBaseVisitor;

/**
 * The Visitor used to convert the parse tree into a Set of PackageRule objects.
 * This visitor can process multiple definition files. It will merge them
 * together into one set of {@link RegExpPackageRuleImpl package rules}. Please
 * be aware that the {@link #getErrors()} method should be called after each
 * call to {@link #visitConventionDefinition(ConventionDefinitionContext)}
 * method because the latter will reset the errors.
 *
 * @author Steff Lukas
 * @since 1.1
 */
public class ConventionDefinitionVisitor extends ConventionDefinitionParserBaseVisitor<Void>
{
  /** The error messages. */
  private final List<String>                  errors;

  /**
   * The collected rules. This is a map because it is easier to handle than a
   * set.
   */
  private final Map<PackageRule, PackageRule> rules;

  /** The package rule visitor. */
  final PackageRuleVisitor                    packageRuleVisitor;

  /**
   * Instantiates a new convention definition visitor.
   *
   * @param placeholderValues the placeholder values
   */
  public ConventionDefinitionVisitor( final Map<String, String> placeholderValues )
  {
    this.rules = new LinkedHashMap<>();
    this.errors = new LinkedList<>();

    final StringVisitor stringVisitor = new StringVisitor( placeholderValues != null ? placeholderValues : emptyMap(), errors );
    final FileRuleVisitor fileRuleVisitor = new FileRuleVisitor( stringVisitor );
    this.packageRuleVisitor = new PackageRuleVisitor( stringVisitor, fileRuleVisitor );
  }

  /**
   * Gets the error messages. Please be aware that each call to
   * {@link #visitConventionDefinition(ConventionDefinitionContext)} method will
   * reset the content of the returned list. If multiple convention definitions
   * are done with this parser then collect the errors after each round!
   *
   * @return a list of error messages if any, or an empty list
   */
  public List<String> getErrors()
  {
    return unmodifiableList( errors );
  }

  /**
   * Get the package rule set from this visitor. If the visitor has been used
   * for multiple inputs then the set will contain the merged rules from all the
   * inputs.
   *
   * @return the (merged) list of package rules
   */
  public List<PackageRule> getPackageRules()
  {
    // Use the values here, since the keys are not the merged values
    return rules.entrySet().stream().map( Entry::getValue ).collect( collectingAndThen( toList(), Collections::unmodifiableList ) );
  }

  /** {@inheritDoc} */
  @Override
  public Void visitConventionDefinition( final ConventionDefinitionContext ctx )
  {
    errors.clear();

    ctx.packageDefinition().stream().map( packageRuleVisitor::visit ).forEach( rule -> rules.merge( rule, rule, PackageRule::merge ) );

    return null;
  }

  private static class PackageRuleVisitor extends ConventionDefinitionParserBaseVisitor<PackageRule>
  {
    private final StringVisitor   stringVisitor;

    private final FileRuleVisitor fileRuleVisitor;

    private PackageRuleVisitor( final StringVisitor stringVisitor, final FileRuleVisitor fileRuleVisitor )
    {
      this.stringVisitor = stringVisitor;
      this.fileRuleVisitor = fileRuleVisitor;
    }

    /** {@inheritDoc} */
    @Override
    public PackageRule visitPackageDefinition( final PackageDefinitionContext ctx )
    {
      final String ruleRegExp = stringVisitor.visit( ctx.alternatives() );
      final LinkedHashSet<FileRule> fileRules = ctx.fileDefinition().stream().map( fileRuleVisitor::visit ).collect(
          toCollection( LinkedHashSet::new ) );

      return new RegExpPackageRuleImpl( ruleRegExp, fileRules );
    }
  }

  private static class FileRuleVisitor extends ConventionDefinitionParserBaseVisitor<FileRule>
  {
    private final StringVisitor stringVisitor;

    private FileRuleVisitor( final StringVisitor stringVisitor )
    {
      this.stringVisitor = stringVisitor;
    }

    /** {@inheritDoc} */
    @Override
    public FileRule visitFileDefinition( final FileDefinitionContext ctx )
    {
      final String rule = stringVisitor.visitAlternatives( ctx.alternatives() );
      return new RegExpFileRuleImpl( rule );
    }
  }

  /**
   * Internal visitor implementation which returns String values.
   */
  private static class StringVisitor extends ConventionDefinitionParserBaseVisitor<String>
  {
    private final Map<String, String> placeholderValues;

    private final List<String>        errors;

    private StringVisitor( final Map<String, String> placeholderValues, final List<String> errors )
    {
      super();
      this.placeholderValues = placeholderValues;
      this.errors = errors;
    }

    /** {@inheritDoc} */
    @Override
    public String visitAlternatives( final AlternativesContext ctx )
    {
      return ctx.compositeExpression().stream().map( this::visitCompositeExpression ).collect( joining( "|" ) );
    }

    /** {@inheritDoc} */
    @Override
    public String visitCompositeExpression( final CompositeExpressionContext ctx )
    {
      return ctx.multipliableExpression().stream().map( this::visitMultipliableExpression ).collect( joining() );
    }

    /** {@inheritDoc} */
    @Override
    public String visitMultipliableExpression( final MultipliableExpressionContext ctx )
    {
      final StringBuilder expression = new StringBuilder( ctx.getText().length() );
      expression.append( visit( ctx.expression() ) );
      expression.append( ctx.MULTIPLIER() != null ? ctx.MULTIPLIER().getText() : "" );
      return expression.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitGroupedExpr( final GroupedExprContext ctx )
    {
      final StringBuilder groupExpr = new StringBuilder( ctx.getText().length() + 10 );
      groupExpr.append( "(?:" );
      groupExpr.append( visit( ctx.alternatives() ) );
      groupExpr.append( ')' );

      return groupExpr.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitStringExpr( final StringExprContext ctx )
    {
      return ctx.stringContent().stream().map( this::visit ).collect( joining( "", "\\Q", "\\E" ) );
    }

    /** {@inheritDoc} */
    @Override
    public String visitWildcardExpr( final WildcardExprContext ctx )
    {
      return ctx.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String visitCharGroupExpr( final CharGroupExprContext ctx )
    {
      return ctx.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String visitStringContent( final StringContentContext ctx )
    {
      // Only one token possible here
      final Token token = ctx.getStart();
      String value;
      if ( token.getType() == ConventionDefinitionLexer.PLACEHOLDER )
      {
        value = getPlaceholderValue( token );
      }
      else
      {
        value = token.getText();
      }

      return value;
    }

    /**
     * Gets the placeholder value for the input placeholder node. If no value is
     * found then an exception is thrown.
     *
     * @param placeholderNode the placeholder node
     * @return the placeholder value
     * @throws WatchDogException if no placeholder value was found
     */
    private String getPlaceholderValue( final Token token )
    {
      String placeholder = token.getText();
      placeholder = placeholder.substring( 1, placeholder.length() - 1 );
      final String value = placeholderValues.get( placeholder );

      if ( value == null )
      {
        errors.add( format( "line {0}:{1} missing value for placeholder {2}", Integer.valueOf(token.getLine()), Integer.valueOf( token.getCharPositionInLine()), token.getText() ) );
      }
      return value;
    }

  }
}
