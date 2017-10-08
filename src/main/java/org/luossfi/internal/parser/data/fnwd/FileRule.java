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

/**
 * A file rule is an immutable object which holds one naming rule used for
 * checking file names.
 *
 * @author Steff Lukas
 * @since 1.2
 */
public interface FileRule
{

  /**
   * Tests whether the input {@code fileName} is compliant to this rule.
   *
   * @param fileName the name of the file to test, must not be null
   * @return true, if the file name is compliant to this rule, false otherwise
   * @throws NullPointerException if {@code fileName} is null
   */
  boolean testCompliance( final String fileName );
}
