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
package org.luossfi.internal.logic.fnwd;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * The error message translator is a helper class which can be used to translate
 * input error keys into the JVM's language. It also replaces any place holders
 * inside the translated message with the input arguments.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class ErrorMessageTranslator
{

  /** The error message bundle's name. */
  private static final String ERROR_MESSAGE_BUNDLE = "ErrorMessages";

  /**
   * Translate the input message key and replace the placeholders inside the
   * translation with the values from arguments.
   *
   * @param messageKey the message key
   * @param arguments the arguments
   * @return the translated and replaced message or the messageKey if no
   *         translation could be found or the message bundle could not be
   *         instanciated.
   */
  public static String translate( String messageKey, Object... arguments )
  {
    String translation;
    try
    {
      ResourceBundle bundle = ResourceBundle.getBundle( ERROR_MESSAGE_BUNDLE );
      translation = bundle.getString( messageKey );

      if ( arguments.length > 0 )
      {
        translation = MessageFormat.format( translation, arguments );
      }
    }
    catch ( MissingResourceException e )
    {
      translation = messageKey;
    }

    return translation;
  }
}
