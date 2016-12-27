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

import java.util.regex.PatternSyntaxException;

/**
 * The Class FileRule is just a marker extension of the {@link Rule} class used
 * for storing file rules.
 *
 * @author Steff Lukas
 * @since 1.0
 * @see Rule
 */
public class FileRule extends Rule
{

  /**
   * Creates a new file rule object from the input rule regular expression.
   *
   * @param ruleRegex the rule regular expression
   * @throws PatternSyntaxException if the input rule regular expression cannot
   *           be compiled.
   */
  public FileRule( String ruleRegex )
  {
    super( ruleRegex );
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
    builder.append( "FileRule [" );
    builder.append( getRuleString() );
    builder.append( "]" );
    return builder.toString();
  }

}
