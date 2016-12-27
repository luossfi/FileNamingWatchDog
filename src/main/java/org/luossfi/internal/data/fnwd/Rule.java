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

package org.luossfi.internal.data.fnwd;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The Class Rule is used to describe a general naming rule. This class
 * basically wraps a {@link Pattern} object but it makes sure that every regular
 * expression is inserted into a Hash* collection only once.
 *
 * @author Steff Lukas
 * @since 1.0
 * @see Pattern
 */
public abstract class Rule
{

  /**
   * The string representing the pattern used to compile the content of field
   * rule. Used mainly in hasCode() and equals() methods, so this object can
   * safely be used in hashed collections.
   */
  private final String  ruleString;

  /** The compiled rule. */
  private final Pattern rule;

  /**
   * Creates a new rule object from the input rule regular expression.
   *
   * @param ruleRegex the rule regular expression
   * @throws PatternSyntaxException if the input rule regular expression cannot
   *           be compiled.
   */
  public Rule( String ruleRegex )
  {
    ruleString = ruleRegex;
    rule = Pattern.compile( ruleString );
  }

  /**
   * Gets this rule's {@link Pattern}.
   *
   * @return the rule
   */
  public Pattern getRule()
  {
    return rule;
  }

  /**
   * Gets the rule's regular expression's string representation.
   *
   * @return the regular expression
   */
  protected String getRuleString()
  {
    return ruleString;
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ( ( ruleString == null ) ? 0 : ruleString.hashCode() );
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals( Object obj )
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
    Rule other = (Rule) obj;
    if ( ruleString == null )
    {
      if ( other.ruleString != null )
      {
        return false;
      }
    }
    else if ( !ruleString.equals( other.ruleString ) )
    {
      return false;
    }
    return true;
  }

  /**
   * Returns the rule's underlying regular expression.
   *
   * @return the String representation of this rule's underlying regular
   *         expression.
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( "Rule [" );
    builder.append( ruleString );
    builder.append( "]" );
    return builder.toString();
  }
}
