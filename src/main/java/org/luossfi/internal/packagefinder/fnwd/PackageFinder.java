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
package org.luossfi.internal.packagefinder.fnwd;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.Files.isHidden;
import static java.nio.file.Files.walkFileTree;
import static java.text.MessageFormat.format;
import static java.util.Collections.emptySet;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Steff Lukas
 */
public final class PackageFinder
{
  /**
   * Finds all packages contained in the input {@code sourceDirectory}. A
   * package is considered to be a path which either contains at least one file
   * or is completely empty. The package is a {@link Path} relative to the input
   * {@code sourceDirectory}.
   *
   * @param sourceDirectory the source directory containing the packages to find
   * @return the package to contained files mapping
   * @throws IOException if the input {@code sourceDirectory} cannot be walked
   *           successfully
   * @throws NullPointerException if the input {@code sourceDirectory} is null
   * @throws IllegalArgumentException if the input {@code sourceDirectory} is
   *           not an directory
   */
  public static Map<Path, Set<Path>> findPackages( final Path sourceDirectory ) throws IOException
  {
    requireNonNull( sourceDirectory, "The source directory must not be null!" );
    if ( !Files.isDirectory( sourceDirectory, LinkOption.NOFOLLOW_LINKS ) )
    {
      throw new IllegalArgumentException( format( "The path \"{0}\" is not a directory!", sourceDirectory ) );
    }
    final PackageFinderVisitor visitor = new PackageFinderVisitor( sourceDirectory );
    walkFileTree( sourceDirectory, visitor );
    return visitor.directories;
  }

  /**
   * <p>
   * The PackageFinderVisitor collects all packages which either contain at
   * least one file or nothing at all by calling its {@link #findPackages(Path)}
   * method.<br>
   * The resulting multi-valued map contains the found package in the form of a
   * {@link Path} which is relative to the input {@code sourceDirectory} as its
   * keys. The value is a set containing all files which are part of the given
   * package or the set is empty if the package is empty (i.e. does neither
   * contain any file nor sub-directory).
   * </p>
   * <p>
   * <b>Since this visitor does not lock any files, it is not advisable to add
   * or remove something inside the given root directory as long as the
   * collection is in progress! This will lead to unexpected behavior.</b>
   * </p>
   *
   * @author Steff Lukas
   * @since 1.0
   */
  private final static class PackageFinderVisitor implements FileVisitor<Path>
  {

    /** The source directory to which the found packages are relative. */
    private final Path                 sourceDirectory;

    /** The map containing the package to file names mapping. */
    private final Map<Path, Set<Path>> directories      = new HashMap<>();

    /** Set holding possibly empty directories. */
    private final Set<Path>            emptyDirectories = new HashSet<>();

    /**
     * hidden constructor to keep control over visitor creation.
     *
     * @param sourceDirectory the source directory, not null
     */
    private PackageFinderVisitor( final Path sourceDirectory )
    {
      super();
      this.sourceDirectory = sourceDirectory;
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult preVisitDirectory( final Path directory, final BasicFileAttributes attributes ) throws IOException
    {
      FileVisitResult result;

      if ( !isHidden( directory ) )
      {
        // Remove parent directory, it obviously is not empty.
        emptyDirectories.remove( directory.getParent() );

        /*
         * Add this directory, it might be empty (not yet known, possible
         * children have yet to be visited).
         */
        emptyDirectories.add( directory );

        result = CONTINUE;
      }
      else
      {
        result = SKIP_SUBTREE;
      }

      return result;
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult visitFile( final Path file, final BasicFileAttributes attributes ) throws IOException
    {
      if ( !isHidden( file ) )
      {
        final Path enclosingDir = file.getParent();

        // Remove parent directory, it obviously is not empty.
        emptyDirectories.remove( enclosingDir );

        // Parent directory is a package and this file is one if its contents.
        directories.computeIfAbsent( sourceDirectory.relativize( enclosingDir ), key -> new HashSet<>() ).add( file.getFileName() );
      }

      return CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult visitFileFailed( final Path file, final IOException ioException ) throws IOException
    {
      if ( !isHidden( file ) )
      {
        throw ioException;
      }

      return CONTINUE;
    }

    /** {@inheritDoc} */
    @Override
    public FileVisitResult postVisitDirectory( final Path directory, final IOException ioException ) throws IOException
    {
      /*
       * If this directory is still in emptyDirectories this means it does
       * neither contain a file nor another directory. So it is assumed to be
       * empty.
       */
      if ( emptyDirectories.remove( directory ) )
      {
        directories.put( sourceDirectory.relativize( directory ), emptySet() );
      }

      return CONTINUE;
    }
  }
}
