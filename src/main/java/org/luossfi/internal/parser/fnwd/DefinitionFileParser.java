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

import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static org.luossfi.exception.fnwd.ErrorMessageConstants.DEF_FILE_NOT_EXISTING;
import static org.luossfi.exception.fnwd.ErrorMessageConstants.DEF_FILE_READ_FAILED;
import static org.luossfi.exception.fnwd.ErrorMessageConstants.PARSING_ERRORS;
import static org.luossfi.exception.fnwd.ErrorMessageTranslator.translate;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.luossfi.exception.fnwd.WatchDogException;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionLexer;
import org.luossfi.gen.antlr.fnwd.ConventionDefinitionParser;
import org.luossfi.internal.parser.data.fnwd.PackageRule;
import org.luossfi.internal.parser.data.fnwd.RegExpFileRuleImpl;
import org.luossfi.internal.parser.data.fnwd.RegExpPackageRuleImpl;

/**
 * <p>
 * The definition file parser takes in one or more paths to definition files and
 * a map of placeholder values. The rules defined in the files are put to the
 * resulting Set of {@link RegExpPackageRuleImpl PackageRules} in the same
 * ordering as they first appeared. Equal rules are merged. The same thing
 * applies for the {@link RegExpFileRuleImpl FileRules} which are part of each
 * {@link RegExpPackageRuleImpl}.
 * </p>
 * <p>
 * The placeholders are replaced during the parse process, so the resulting
 * rules do not contain any placeholders. The parser throws an exception if a
 * definition contains a placeholder which is not defined in the placeholders
 * map.
 * </p>
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class DefinitionFileParser
{

  /** The definition files. */
  private final List<Path>          definitionFiles;

  /** The placeholder values. */
  private final Map<String, String> placeholderValues;

  /**
   * Instantiates a new definition file parser for one definition file without
   * any placeholders.
   *
   * @param definitionFile the <b>UTF-8</b> encoded definition file
   * @throws WatchDogException if the input definition file does not exist
   * @throws IllegalArgumentException if the input definition file is null
   */
  public DefinitionFileParser( final Path definitionFile ) throws WatchDogException
  {
    this( definitionFile, emptyMap() );
  }

  /**
   * Instantiates a new definition file parser for one definition file.
   *
   * @param definitionFile the <b>UTF-8</b> encoded definition file
   * @param placeholderValues the placeholder values
   * @throws WatchDogException if the input definition file does not exist
   * @throws IllegalArgumentException if the input definition file is null
   */
  public DefinitionFileParser( final Path definitionFile, final Map<String, String> placeholderValues ) throws WatchDogException
  {
    this( singletonList( definitionFile ), placeholderValues );
  }

  /**
   * Instantiates a new definition file parser for multiple definition files.
   *
   * @param definitionFiles the <b>UTF-8</b> encoded definition files
   * @param placeholderValues the placeholder values
   * @throws WatchDogException if the input definition file does not exist
   * @throws IllegalArgumentException if the input definition file is null
   * @since 1.1
   */
  public DefinitionFileParser( final List<Path> definitionFiles, final Map<String, String> placeholderValues ) throws WatchDogException
  {
    checkAndNormalizePathList( definitionFiles );
    this.definitionFiles = definitionFiles;
    this.placeholderValues = placeholderValues;
  }

  /**
   * Parse the definition files.
   *
   * @return the set of package rules in the same ordering as they where written
   *         in the definition file.
   * @throws WatchDogException the watch dog exception if one of the definition
   *           files cannot be accessed by the parser or if the parser found
   *           syntax problems.
   */
  public List<PackageRule> parse() throws WatchDogException
  {

    final ConventionDefinitionVisitor visitor = new ConventionDefinitionVisitor( placeholderValues );

    for ( final Path definitionFile : definitionFiles )
    {
      try ( final InputStreamReader reader = new InputStreamReader( Files.newInputStream( definitionFile ), "UTF-8" ) )
      {
        final CharStream antlrInputStream = CharStreams.fromReader( reader, definitionFile.getFileName().toString() );

        final ErrorCollectingListener listener = new ErrorCollectingListener();

        final ConventionDefinitionLexer lexer = new ConventionDefinitionLexer( antlrInputStream );
        lexer.removeErrorListeners();
        lexer.addErrorListener( listener );

        final ConventionDefinitionParser parser = new ConventionDefinitionParser( new CommonTokenStream( lexer ) );
        parser.removeErrorListeners();
        parser.addErrorListener( listener );

        visitor.visitConventionDefinition( parser.conventionDefinition() );

        checkForErrors( listener, visitor, definitionFile );
      }
      catch ( final IOException e )
      {
        throw new WatchDogException( translate( DEF_FILE_READ_FAILED, definitionFile.normalize().toString() ).orElse( DEF_FILE_READ_FAILED ), e );
      }
    }

    return visitor.getPackageRules();
  }

  /**
   * Checks if the input listener and/or visitor contains any error messages and
   * throws a WatchDogException if so.
   *
   * @param listener the listener which might contain error messages
   * @param visitor the visitor which might contain error messages
   * @param definitionFile the definition file's path (used if an error occur to
   *          throw a user friendlier exception)
   * @throws WatchDogException if errors occurred during the parsing of
   *           <b>one</b> definition file
   * @since 1.1
   */
  private static void checkForErrors( final ErrorCollectingListener listener, final ConventionDefinitionVisitor visitor, final Path definitionFile )
      throws WatchDogException
  {
    final List<String> parseErrors = listener.getErrors();
    final List<String> visitorErrors = visitor.getErrors();

    if ( !parseErrors.isEmpty() || !visitorErrors.isEmpty() )
    {
      final String errors = concat( parseErrors.stream(), visitorErrors.stream() ).collect( joining( lineSeparator() ) );
      throw new WatchDogException( translate( PARSING_ERRORS, definitionFile, errors ).orElse( PARSING_ERRORS ) );
    }
  }

  /**
   * Checks the input <code>definitionFiles</code> and normalizes the contained
   * {@link Path paths}.
   *
   * @param definitionFiles the list of definition files to check and normalize
   * @return the normalized definition file paths
   * @throws WatchDogException if one of the definition files does not exist on
   *           the file system or is no regular file
   * @throws IllegalArgumentException if the input <code>definitionFiles</code>
   *           list is null or empty or contains a null value
   * @since 1.1
   */
  private static List<Path> checkAndNormalizePathList( final List<Path> definitionFiles ) throws WatchDogException
  {
    if ( definitionFiles == null || definitionFiles.isEmpty() )
    {
      throw new IllegalArgumentException( "The list containing the naming convention definition files must not be null or empty!" );
    }

    final List<Path> normalizedPaths = new ArrayList<>( definitionFiles.size() );

    for ( final Path definitionFile : definitionFiles )
    {
      checkInputPath( definitionFile );
      normalizedPaths.add( definitionFile.normalize() );
    }

    return normalizedPaths;
  }

  /**
   * Checks the input path and throws exceptions if something is wrong with it.
   *
   * @param definitionFile the definition file to check
   * @throws WatchDogException if the definition file does not exist on the file
   *           system or is no regular file
   * @throws IllegalArgumentException if the input definition file is null
   */
  private static void checkInputPath( final Path definitionFile ) throws WatchDogException
  {
    if ( definitionFile == null )
    {
      throw new IllegalArgumentException( "The naming convention definition file must not be null!" );
    }

    if ( !Files.exists( definitionFile ) || !Files.isRegularFile( definitionFile ) )
    {
      throw new WatchDogException( translate( DEF_FILE_NOT_EXISTING, definitionFile.normalize() ).orElse( DEF_FILE_NOT_EXISTING ) );
    }
  }
}
