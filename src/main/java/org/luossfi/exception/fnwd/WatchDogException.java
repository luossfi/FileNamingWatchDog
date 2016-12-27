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
 * The WatchDogException is used for all kinds of 'expected' exceptions in
 * FileNamingWatchDog library.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class WatchDogException extends Exception
{

  /** The Constant serialVersionUID. */
  private static final long serialVersionUID = 1L;

  /**
   * Instantiates a new watch dog exception.
   *
   * @param message the message to display
   * @param cause the original cause
   */
  public WatchDogException( String message, Exception cause )
  {
    super( message, cause );
  }

  /**
   * Instantiates a new watch dog exception.
   *
   * @param message the message to display
   */
  public WatchDogException( String message )
  {
    super( message );
  }

}
