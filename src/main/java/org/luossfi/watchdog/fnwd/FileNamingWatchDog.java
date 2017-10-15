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

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.luossfi.exception.fnwd.ErrorMessageConstants.FILE_WALK_ERROR;
import static org.luossfi.exception.fnwd.ErrorMessageConstants.SCR_ROOT_DIR_NOT_EXISTING;
import static org.luossfi.exception.fnwd.ErrorMessageTranslator.translate;
import static org.luossfi.internal.packagefinder.fnwd.PackageFinder.findPackages;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import org.luossfi.exception.fnwd.WatchDogException;
import org.luossfi.internal.parser.data.fnwd.FileRule;
import org.luossfi.internal.parser.data.fnwd.PackageRule;
import org.luossfi.internal.parser.fnwd.DefinitionFileParser;

/**
 * <p>
 * The file naming watch dog is the worker class which should be used when using
 * this library.
 * </p>
 * <p>
 * It takes in one or multiple convention definition files and the placeholder
 * values for them.<br>
 * It parses the definition files and stores the result internally.
 * </p>
 * <p>
 * The method {@link #check(Path)} takes in the source files root directory and
 * checks the whole subtree against the rule set. Each finding is documented in
 * a map which is returned by this method. The map's content has the following
 * meaning:
 * </p>
 * <ul>
 * <li>package name -&gt; empty set: The package name does not comply to the
 * defined rules</li>
 * <li>package name -&gt; set of file names: The package name is compliant but
 * the file names are not.</li>
 * </ul>
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class FileNamingWatchDog
{

  /** The definition files. */
  private final List<Path>          definitionFiles;

  /** The placeholder values. */
  private final Map<String, String> placeholderValues;

  /** The rule set parsed from the definition file. */
  private List<PackageRule>         rules = null;

  /**
   * Instantiates a new file naming watch dog for the input (single) definition
   * file and no placeholders.
   *
   * @param definitionFile the convention definition file
   */
  public FileNamingWatchDog( final Path definitionFile )
  {
    this( definitionFile, emptyMap() );
  }

  /**
   * Instantiates a new file naming watch dog for the input (single) definition
   * file and placeholder values.
   *
   * @param definitionFile the convention definition file
   * @param placeholderValues the placeholder values for this definition file
   * @throws IllegalArgumentException if the definition file is null or if the
   *           placeholder values is null
   */
  public FileNamingWatchDog( final Path definitionFile, final Map<String, String> placeholderValues )
  {
    this( definitionFile != null ? singletonList( definitionFile ) : null, placeholderValues );
  }

  /**
   * Instantiates a new file naming watch dog for the input list of definition
   * files and without any placeholders.
   *
   * @param definitionFiles list of definition files, must neither be null nor
   *          empty
   * @throws IllegalArgumentException if the input <code>definitionFiles</code>
   *           is null or empty
   * @since 1.1
   */
  public FileNamingWatchDog( final List<Path> definitionFiles )
  {
    this( definitionFiles, emptyMap() );
  }

  /**
   * Instantiates a new file naming watch dog for the input list of definition
   * files and placeholder values.
   *
   * @param definitionFiles list of definition files, must neither be null nor
   *          empty
   * @param placeholderValues the placeholder values
   * @throws IllegalArgumentException if the input <code>definitionFiles</code>
   *           is null or empty or if the input <code>placeholderValues</code>
   *           is null
   * @since 1.1
   */
  public FileNamingWatchDog( final List<Path> definitionFiles, final Map<String, String> placeholderValues )
  {
    if ( definitionFiles == null || definitionFiles.isEmpty() )
    {
      throw new IllegalArgumentException( "The definition files must not be null or empty!" );
    }

    if ( placeholderValues == null )
    {
      throw new IllegalArgumentException( "The placeholder values must not be null!" );
    }

    this.definitionFiles = definitionFiles;
    this.placeholderValues = placeholderValues;
  }

  /**
   * Checks all packages and their files for compliance against this watch dog's
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
  public Map<String, Set<String>> check( final Path sourceRootDirectory ) throws WatchDogException
  {
    requireNonNull( sourceRootDirectory, "The source root directory must not be null!" );

    final Path normalizedRoot = sourceRootDirectory.normalize();

    if ( !Files.exists( sourceRootDirectory ) || !Files.isDirectory( sourceRootDirectory ) )
    {
      throw new WatchDogException( translate( SCR_ROOT_DIR_NOT_EXISTING, normalizedRoot ).orElse( SCR_ROOT_DIR_NOT_EXISTING ) );
    }

    if ( rules == null )
    {
      final DefinitionFileParser parser = new DefinitionFileParser( definitionFiles, placeholderValues );
      rules = parser.parse();
    }

    Map<Path, Set<Path>> packages;
    try
    {
      packages = findPackages( normalizedRoot );
    }
    catch ( final IOException e )
    {
      final String message = translate( FILE_WALK_ERROR, normalizedRoot.toString(), e.getLocalizedMessage() ).orElse( FILE_WALK_ERROR );
      throw new WatchDogException( message, e );
    }

    final Map<String, Set<String>> complianceIssues = findComplianceIssues( packages );

    return complianceIssues;
  }

  private Map<String, Set<String>> findComplianceIssues( final Map<Path, Set<Path>> packages )
  {
    final Map<String, Set<String>> complianceIssues = new TreeMap<>();

    for ( final Entry<Path, Set<Path>> packageEntry : packages.entrySet() )
    {
      final String packageName = pathToPackage( packageEntry.getKey() );
      final Optional<PackageRule> matchingRule = rules.stream().filter( rule -> rule.testCompliance( packageName ) ).findFirst();
      if ( matchingRule.isPresent() )
      {
        final Set<String> nonCompliantFiles = checkFiles( packageEntry.getValue(), matchingRule.get().getFileRules() );
        if ( !nonCompliantFiles.isEmpty() )
        {
          complianceIssues.put( packageName, nonCompliantFiles );
        }
      }
      else
      {
        complianceIssues.put( packageName, emptySet() );
      }

    }
    return complianceIssues;
  }

  private static String pathToPackage( final Path toConvert )
  {
    return stream( toConvert.spliterator(), false ).map( Path::toString ).collect( joining( "." ) );
  }

  /**
   * Checks the input {@code files} for compliance against the input
   * {@code fileRules}.
   *
   * @param files the files to check
   * @param fileRules the file rules to check against
   * @return the set of non-compliant file names
   */
  private static Set<String> checkFiles( final Collection<Path> files, final Collection<FileRule> fileRules )
  {
    return files.stream().map( Path::toString ).filter( file -> fileRules.stream().noneMatch( rule -> rule.testCompliance( file ) ) ).collect(
        toSet() );
  }

}
