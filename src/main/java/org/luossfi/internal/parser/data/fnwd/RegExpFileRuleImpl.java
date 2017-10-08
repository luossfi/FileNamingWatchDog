/*
 * FileNamingWatchDog, a library for checking Java packages and source file
 * names for compliance to naming conventions.
 *
 * Copyright (C) 2017++ Steff Lukas <steff.lukas@luossfi.org>
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

package org.luossfi.internal.parser.data.fnwd;

import static java.util.Objects.requireNonNull;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * This class is an implementation of the {@link FileRule} interface which
 * provides a regular expression based rule.
 *
 * @author Steff Lukas
 * @since 1.2
 * @see FileRule
 */
public final class RegExpFileRuleImpl implements FileRule
{

  /** The rule's underlying regular expression. */
  private final Pattern ruleRegExp;

  /**
   * Creates a new regular expression based file rule object from the input rule
   * regular expression.
   *
   * @param ruleRegExp the regular expression to use by the created rule, must
   *          not be null
   * @throws NullPointerException if the input {@code ruleRegExp} is null
   * @throws PatternSyntaxException if the input {@code ruleRegExp} cannot be
   *           compiled.
   */
  public RegExpFileRuleImpl( final String ruleRegExp )
  {
    requireNonNull( ruleRegExp );
    this.ruleRegExp = Pattern.compile( ruleRegExp );
  }

  /** {@inheritDoc} */
  @Override
  public boolean testCompliance( final String fileName )
  {
    requireNonNull( fileName, "Input fileName must not be null!" );
    return ruleRegExp.matcher( fileName ).matches();
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append( "RegExpFileRuleImpl [ruleRegExp=" ).append( ruleRegExp ).append( ']' );
    return builder.toString();
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ruleRegExp.pattern().hashCode();
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals( final Object obj )
  {
    if ( this == obj )
    {
      return true;
    }
    if ( obj == null )
    {
      return false;
    }
    if ( getClass() != obj.getClass() )
    {
      return false;
    }
    final RegExpFileRuleImpl other = (RegExpFileRuleImpl) obj;

    if ( !ruleRegExp.pattern().equals( other.ruleRegExp.pattern() ) )
    {
      return false;
    }
    return true;
  }

}
