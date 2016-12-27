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

import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * This extension of ANTLR's {@link BaseErrorListener} collects all error
 * messages into a list which can be obtained by calling
 * {@link ErrorCollectingListener#getErrors() getErrors()} method after the
 * parser finished.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class ErrorCollectingListener extends BaseErrorListener
{
  /** The list containing all collected errors. */
  private final List<String> errors = new LinkedList<>();

  /**
   * Gets the list of error messages.
   *
   * @return a list of error messages or an empty list, is no error occurred
   */
  public List<String> getErrors()
  {
    return errors;
  }

  /** {@inheritDoc} */
  @Override
  public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e )
  {
    StringBuilder message = new StringBuilder( msg.length() + 16 );
    message.append( "line " );
    message.append( line );
    message.append( ':' );
    message.append( charPositionInLine );
    message.append( ' ' );
    message.append( msg );
    errors.add( message.toString() );
  }

}
