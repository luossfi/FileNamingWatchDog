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

import static java.text.MessageFormat.format;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * <p>
 * This class is an implementation of the {@link PackageRule} interface which
 * provides a regular expression based package rule.
 * </p>
 * <p>
 * <b>The file rules are not taken into account when calculating the hash value
 * of this class or when checking for equality!</b>
 * </p>
 *
 * @author Steff Lukas
 * @since 1.2
 * @see Rule
 */
public class RegExpPackageRuleImpl implements PackageRule
{

  /** This rule's underlying regular expression. */
  private final Pattern        ruleRegExp;

  /** The unmodifiable list of fileRules. */
  private final List<FileRule> fileRules;

  /**
   * Creates a new regular expression based package rule object from the input
   * rule regular expression and child file rules.<br>
   * This constructor does not allow to use an empty {@code fileRules}
   * collection since this would effectively fail the checks every time.
   *
   * @param ruleRegExp the rule's regular expression, must not be null
   * @param fileRules the collection of file rules, must not be null
   * @throws NullPointerException if either {@code ruleRegExp} or
   *           {@code fileRules} is null
   * @throws IllegalArgumentException if {@code fileRules} is empty
   * @throws PatternSyntaxException if the input rule regular expression cannot
   *           be compiled.
   */
  public RegExpPackageRuleImpl( final String ruleRegExp, final Collection<FileRule> fileRules )
  {
    requireNonNull( ruleRegExp, "The input ruleRegExp must not be null!" );
    requireNonNull( fileRules, "The input fileRules must not be null!" );
    if ( fileRules.isEmpty() )
    {
      throw new IllegalArgumentException( "The input fileRules must not be empty!" );
    }

    this.ruleRegExp = Pattern.compile( ruleRegExp );
    this.fileRules = unmodifiableList( new ArrayList<>( fileRules ) );
  }

  /**
   * Private constructor used by {@link #merge(PackageRule)} method.
   *
   * @param ruleRegExp the rule's regular expression
   * @param fileRules the file rules
   */
  private RegExpPackageRuleImpl( final Pattern ruleRegExp, final Collection<FileRule> fileRules )
  {
    this.ruleRegExp = ruleRegExp;
    this.fileRules = unmodifiableList( new ArrayList<>( fileRules ) );
  }

  /** {@inheritDoc} */
  @Override
  public boolean testCompliance( final String packageName )
  {
    requireNonNull( packageName, "The input packageName must not be null!" );
    return ruleRegExp.matcher( packageName ).matches();
  }

  /** {@inheritDoc} */
  @Override
  public List<FileRule> getFileRules()
  {
    return fileRules;
  }

  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    final StringBuilder builder = new StringBuilder();
    builder.append( "RegExpPackageRuleImpl [ruleRegExp=" ).append( ruleRegExp ).append( ", fileRules=" ).append( fileRules ).append( ']' );
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
    if ( !( obj instanceof RegExpPackageRuleImpl ) )
    {
      return false;
    }
    final RegExpPackageRuleImpl other = (RegExpPackageRuleImpl) obj;
    if ( !ruleRegExp.pattern().equals( other.ruleRegExp.pattern() ) )
    {
      return false;
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public PackageRule merge( final PackageRule other )
  {
    requireNonNull( other, "Cannot merge with a null value!" );
    if ( !equals( other ) )
    {
      throw new IllegalArgumentException( format( "Cannot merge this rule \"{0}\" with other rule \"{1}\"!", this, other ) );
    }

    final Set<FileRule> mergedFileRules = new LinkedHashSet<>( fileRules );
    mergedFileRules.addAll( other.getFileRules() );

    return new RegExpPackageRuleImpl( ruleRegExp, mergedFileRules );
  }

}
