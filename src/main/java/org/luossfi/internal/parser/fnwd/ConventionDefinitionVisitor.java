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

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.luossfi.exception.fnwd.WatchDogException;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.AlternativesContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.CharGroupExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.CompositeExpressionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.ConventionDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.FileDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.GroupedExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.MultipliableExpressionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.PackageDefinitionContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.StringExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser.WildcardExprContext;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParserBaseVisitor;
import org.luossfi.internal.data.fnwd.FileRule;
import org.luossfi.internal.data.fnwd.PackageRule;

/**
 * The Visitor used to convert the parse tree into a Set of PackageRule objects.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class ConventionDefinitionVisitor extends ConventionDefinitionParserBaseVisitor<Set<PackageRule>>
{

  /** The placeholder values. */
  private final Map<String, String> placeholderValues;

  /** The error messages. */
  private final List<String>        errors;

  /**
   * Instantiates a new convention definition visitor.
   *
   * @param placeholderValues the placeholder values
   */
  public ConventionDefinitionVisitor( final Map<String, String> placeholderValues )
  {
    this.placeholderValues = placeholderValues;
    this.errors = new LinkedList<>();
  }

  /**
   * Gets the error messages.
   *
   * @return a list of error messages if any, or an empty list
   */
  public List<String> getErrors()
  {
    return errors;
  }

  /** {@inheritDoc} */
  @Override
  public Set<PackageRule> visitConventionDefinition( ConventionDefinitionContext ctx )
  {
    LinkedHashMap<String, PackageRule> patternToRule = new LinkedHashMap<>();

    StringVisitor stringVisitor = new StringVisitor();
    List<PackageDefinitionContext> packageDefinitions = ctx.packageDefinition();

    for ( PackageDefinitionContext packageDefinitionContext : packageDefinitions )
    {
      String pattern = stringVisitor.visitAlternatives( packageDefinitionContext.alternatives() );
      PackageRule packageRule = patternToRule.get( pattern );

      if ( packageRule == null )
      {
        packageRule = new PackageRule( pattern );
        patternToRule.put( pattern, packageRule );
      }

      List<FileDefinitionContext> fileDefinitions = packageDefinitionContext.fileDefinition();

      for ( FileDefinitionContext fileDefinitionContext : fileDefinitions )
      {
        String filePattern = stringVisitor.visitAlternatives( fileDefinitionContext.alternatives() );
        packageRule.addFileRule( new FileRule( filePattern ) );
      }

    }

    Set<PackageRule> packageRules = new LinkedHashSet<>();

    for ( PackageRule packageRule : patternToRule.values() )
    {
      packageRules.add( packageRule );
    }

    return packageRules;
  }

  private class StringVisitor extends ConventionDefinitionParserBaseVisitor<String>
  {
    /** {@inheritDoc} */
    @Override
    public String visitAlternatives( AlternativesContext ctx )
    {
      StringBuilder alternatives = new StringBuilder( ctx.getText().length() );
      List<CompositeExpressionContext> compositeExpressions = ctx.compositeExpression();
      for ( CompositeExpressionContext compositeExpressionContext : compositeExpressions )
      {
        if ( alternatives.length() > 0 )
        {
          alternatives.append( '|' );
        }
        alternatives.append( visitCompositeExpression( compositeExpressionContext ) );
      }
      return alternatives.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitCompositeExpression( CompositeExpressionContext ctx )
    {
      StringBuilder expression = new StringBuilder();

      for ( MultipliableExpressionContext multipliableExpressionContext : ctx.multipliableExpression() )
      {
        expression.append( visitMultipliableExpression( multipliableExpressionContext ) );
      }

      return expression.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitMultipliableExpression( MultipliableExpressionContext ctx )
    {
      StringBuilder expression = new StringBuilder( ctx.getText().length() );
      expression.append( visit( ctx.expression() ) );
      expression.append( ctx.MULTIPLIER() != null ? ctx.MULTIPLIER().getText() : "" );
      return expression.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitGroupedExpr( GroupedExprContext ctx )
    {
      StringBuilder groupExpr = new StringBuilder( ctx.getText().length() + 10 );
      groupExpr.append( "(?:" );
      groupExpr.append( visit( ctx.alternatives() ) );
      groupExpr.append( ')' );

      return groupExpr.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitStringExpr( StringExprContext ctx )
    {
      List<TerminalNode> stringLiterals = ctx.STRING_LITERAL();
      List<TerminalNode> placeholders = ctx.PLACEHOLDER();

      int literalIndex = 0;
      int placeholderIndex = 0;
      StringBuffer stringExpr = new StringBuffer();
      stringExpr.append( "\\Q" );

      while ( literalIndex < stringLiterals.size() || placeholderIndex < placeholders.size() )
      {
        if ( literalIndex == stringLiterals.size() )
        {
          String value = getPlaceholderValue( placeholders.get( placeholderIndex ) );
          stringExpr.append( value );
          placeholderIndex++;
        }
        else if ( placeholderIndex == placeholders.size() )
        {
          stringExpr.append( stringLiterals.get( literalIndex ).getText() );
          literalIndex++;
        }
        else
        {

          TerminalNode literalNode = stringLiterals.get( literalIndex );
          TerminalNode placeholderNode = placeholders.get( placeholderIndex );

          if ( literalNode.getSymbol().getTokenIndex() < placeholderNode.getSymbol().getTokenIndex() )
          {
            stringExpr.append( literalNode.getText() );
            literalIndex++;
          }
          else
          {
            String value = getPlaceholderValue( placeholderNode );
            stringExpr.append( value );
            placeholderIndex++;
          }
        }

      }

      stringExpr.append( "\\E" );

      return stringExpr.toString();
    }

    /** {@inheritDoc} */
    @Override
    public String visitWildcardExpr( WildcardExprContext ctx )
    {
      return ctx.getText();
    }

    /** {@inheritDoc} */
    @Override
    public String visitCharGroupExpr( CharGroupExprContext ctx )
    {
      return ctx.getText();
    }

    /**
     * Gets the placeholder value for the input placeholder node. If no value is
     * found then an exception is thrown.
     *
     * @param placeholderNode the placeholder node
     * @return the placeholder value
     * @throws WatchDogException if no placeholder value was found
     */
    private String getPlaceholderValue( TerminalNode placeholderNode )
    {
      String placeholder = placeholderNode.getText();
      placeholder = placeholder.substring( 1, placeholder.length() - 1 );
      String value = placeholderValues.get( placeholder );
      if ( value == null )
      {
        Token token = placeholderNode.getSymbol();
        errors.add( MessageFormat.format( "line {0}:{1} missing value for placeholder {2}", token.getLine(), token.getCharPositionInLine(),
            token.getText() ) );
      }
      return value;
    }

  }
}
