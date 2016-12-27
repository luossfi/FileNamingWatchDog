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
package org.luossfi.exception.fnwd;

/**
 * This constants class is used to define the error message constants.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public final class ErrorMessageConstants
{

  /**
   * The definition-file "{0}" does not exist on the file system or is not a
   * regular file!
   */
  public static final String DEF_FILE_NOT_EXISTING     = "fnwd.error.01";

  /** The definition-file "{0}" could not be opened/read! */
  public static final String DEF_FILE_READ_FAILED      = "fnwd.error.02";

  /** Errors occurred when parsing file "{0}":{1} */
  public static final String PARSING_ERRORS            = "fnwd.error.03";

  /** An error occurred while analyzing the sources root directory "{0}": {1} */
  public static final String FILE_WALK_ERROR           = "fnwd.error.04";

  /**
   * The source root directory "{0}" does not exist on the file system or is not
   * a directory!
   */
  public static final String SCR_ROOT_DIR_NOT_EXISTING = "fnwd.error.05";

  /**
   * Private constructor, this is a constants only class
   */
  private ErrorMessageConstants()
  {
    super();
  }

}
