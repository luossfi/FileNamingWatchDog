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

package org.luossfi.watchdog.fnwd;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.luossfi.exception.fnwd.ErrorMessageConstants;
import org.luossfi.exception.fnwd.WatchDogException;
import org.luossfi.internal.data.fnwd.PackageRule;
import org.luossfi.internal.logic.fnwd.DefinitionFileParser;
import org.luossfi.internal.logic.fnwd.ErrorMessageTranslator;
import org.luossfi.internal.logic.fnwd.SourceDirectoryVisitor;

/**
 * <p>
 * The file naming watch dog is the worker class which should be used when using
 * this library.
 * </p>
 * <p>
 * It takes in a convention definition file and the placeholder values for
 * it.<br>
 * It parses the definition file and stores the result internally.
 * </p>
 * <p>
 * The method {@link FileNamingWatchDog#check(Path) check(Path)} takes in the
 * source files root directory and checks the whole subtree against the rule
 * set. Each finding is documented in a map which is returned by this method.
 * The map's content has the following meaning:
 * <ul>
 * <li>package name -&gt; empty set: The package name does not comply to the
 * defined rules</li>
 * <li>package name -&gt; set of file names: The package name is compliant but
 * the file names are not.</li>
 * </ul>
 * </p>
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class FileNamingWatchDog
{

  /** The definition file. */
  private Path                definitionFile;

  /** The placeholder values. */
  private Map<String, String> placeholderValues;

  /** The rule set parsed from the definition file. */
  private Set<PackageRule>    rules = null;

  /**
   * Instantiates a new file naming checker for the input definition file and no
   * placeholders.
   *
   * @param definitionFile the convention definition file
   */
  public FileNamingWatchDog( Path definitionFile )
  {
    this( definitionFile, Collections.emptyMap() );
  }

  /**
   * Instantiates a new file naming checker for the input definition file and
   * placeholder values.
   *
   * @param definitionFile the convention definition file
   * @param placeholderValues the placeholder values for this definition file
   * @throws IllegalArgumentException if the definition file is null or if the
   *           placeholder values is null
   */
  public FileNamingWatchDog( Path definitionFile, Map<String, String> placeholderValues )
  {
    if ( definitionFile == null )
    {
      throw new IllegalArgumentException( "The definition file must not be null!" );
    }

    if ( placeholderValues == null )
    {
      throw new IllegalArgumentException( "The placeholder values must not be null!" );
    }

    this.definitionFile = definitionFile;
    this.placeholderValues = placeholderValues;
  }

  /**
   * Checks all packages and their files for compliance against this checkers
   * rules.
   *
   * @param sourceRootDirectory the root directory of the source files. This is
   *          the same directory which is passed to javac when compiling the
   *          sources.
   * @return the found issues in a map (see {@link FileNamingWatchDog} for
   *         details) or an empty map if no issues were found
   * @throws WatchDogException if this checkers definition file could not be
   *           parsed or if either the definition file or the source root
   *           directory are not accessible by this method or the source root
   *           directory does not exist or is not a directory.
   * @throws IllegalArgumentException if the source root directory is null
   */
  public Map<String, Set<String>> check( Path sourceRootDirectory ) throws WatchDogException
  {
    if ( sourceRootDirectory == null )
    {
      throw new IllegalArgumentException( "The source root directory must not be null!" );
    }

    Path normalizedRoot = sourceRootDirectory.normalize();

    if ( !Files.exists( sourceRootDirectory ) || !Files.isDirectory( sourceRootDirectory ) )
    {
      String message = ErrorMessageTranslator.translate( ErrorMessageConstants.SCR_ROOT_DIR_NOT_EXISTING, normalizedRoot );
      throw new WatchDogException( message );
    }

    if ( rules == null )
    {
      DefinitionFileParser parser = new DefinitionFileParser( definitionFile, placeholderValues );
      rules = parser.parse();
    }

    SourceDirectoryVisitor visitor = new SourceDirectoryVisitor( normalizedRoot, rules );

    try
    {
      Files.walkFileTree( normalizedRoot, visitor );
    }
    catch ( IOException e )
    {
      String message = ErrorMessageTranslator.translate( ErrorMessageConstants.FILE_WALK_ERROR, normalizedRoot.toString(), e.getLocalizedMessage() );
      throw new WatchDogException( message, e );
    }

    return visitor.getComplianceIssues();
  }

}
