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

import java.util.List;

/**
 * A package rule is an immutable object which holds one naming rule used for
 * checking package names and its child {@link FileRule file rules}.
 *
 * @author Steff Lukas
 * @since 1.2
 */
public interface PackageRule
{
  /**
   * Tests whether the input {@code packageName} is compliant to this rule.
   *
   * @param packageName the name of the package to test, must not be null
   * @return true, if the package name is compliant to this rule, false
   *         otherwise
   * @throws NullPointerException if {@code packageName} is null
   */
  boolean testCompliance( final String packageName );

  /**
   * Gets the file rules which are defined inside this package rule.
   *
   * @return an immutable list of {@link FileRule file rules}, never null
   */
  List<FileRule> getFileRules();

  /**
   * Merges the file rules of {@code this} package rule and {@code other}
   * package rule and returns a newly created instance if both package rules
   * describe the same rule. The file rule merge is equivalent to this:
   *
   * <pre>
   * {@code new LinkedHashSet<>( this.getFileRules() ).addAll( other.getFileRules() )}
   * </pre>
   *
   * @param other the other package rule to merge with this rule
   * @return a new {@link PackageRule} instance comprising of
   * @throws NullPointerException if {@code other} is null
   * @throws IllegalArgumentException if {@code this} and {@code other} do not
   *           describe the same package rule
   */
  PackageRule merge( final PackageRule other );

}
