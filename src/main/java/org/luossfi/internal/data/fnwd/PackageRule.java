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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * The Class PackageRule is an extension of the {@link Rule} class used for
 * storing package rules and their associated file rules.
 * </p>
 * <p>
 * <b>The file rules are not taken into account when calculating the hash value
 * of this class or when checking for equality!</b>
 * </p>
 *
 * @author Steff Lukas
 * @since 1.0
 * @see Rule
 */
public class PackageRule extends Rule
{

  private final Set<FileRule> fileRules = new LinkedHashSet<>();

  /**
   * Creates a new package rule object from the input rule regular expression.
   *
   * @param ruleRegex the rule regular expression
   * @throws PatternSyntaxException if the input rule regular expression cannot
   *           be compiled.
   */
  public PackageRule( String ruleRegex )
  {
    super( ruleRegex );
  }

  /**
   * Gets the file rules associated to this package rule.
   *
   * @return the file rules
   */
  public Set<FileRule> getFileRules()
  {
    return fileRules;
  }

  /**
   * Adds a new file rule to this package rule if this file rule was not yet
   * added.
   *
   * @param fileRule the file rule to add
   * @return true, if the file rule was not yet part of this package rule, false
   *         otherwise.
   * @see Set#add(Object)
   */
  public boolean addFileRule( FileRule fileRule )
  {
    return fileRules.add( fileRule );
  }

  /**
   * Returns the rule's underlying regular expression and number of file rules.
   *
   * @return the String representation of this rule's underlying regular
   *         expression and number of file rules.
   */
  @Override
  public String toString()
  {
    StringBuilder builder = new StringBuilder();
    builder.append( "PackageRule [rule=" );
    builder.append( getRuleString() );
    builder.append( ", fileRulesCount=" );
    builder.append( fileRules.size() );
    builder.append( "]" );
    return builder.toString();
  }
}
