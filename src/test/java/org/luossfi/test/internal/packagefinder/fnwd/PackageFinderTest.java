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
package org.luossfi.test.internal.packagefinder.fnwd;

import static com.google.common.jimfs.Configuration.unix;
import static com.google.common.jimfs.Jimfs.newFileSystem;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.createFile;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.luossfi.internal.packagefinder.fnwd.PackageFinder.findPackages;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * @author Steff Lukas
 */
@DisplayName( "PackageFinder Tests" )
class PackageFinderTest
{
  private Path srcDir;

  @BeforeEach
  void createSrcDir() throws IOException
  {
    final FileSystem fs = newFileSystem( unix() );
    srcDir = fs.getPath( "/tmp/project" );
    createDirectories( srcDir );
  }

  @Test
  @DisplayName( "inputting null throws NullPointerException" )
  void inputtingNullThrowsNullPointerException() throws IOException
  {
    assertThatThrownBy( () -> findPackages( null ) ).isInstanceOf( NullPointerException.class );
  }

  @Test
  @DisplayName( "inputting a file throws IllegalArgumentException" )
  void inputtingFileThrowsIllegalArgumentException() throws IOException
  {
    final Path noDir = srcDir.resolve( "noDir" );
    createFile( noDir );

    assertThatThrownBy( () -> findPackages( noDir ) ).isInstanceOf( IllegalArgumentException.class );
  }

  @Nested
  @DisplayName( "find empty packages" )
  class EmptyPackages
  {

    @Test
    @DisplayName( "default package" )
    void defaultPackage() throws IOException
    {
      final Entry<Path, Set<Path>> emptyDefaultPkg = addPackage( srcDir, "" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( emptyDefaultPkg );
    }

    @Test
    @DisplayName( "single simple package" )
    void singleSimplePackage() throws IOException
    {
      final Entry<Path, Set<Path>> simpleEmptyPkg = addPackage( srcDir, "foo" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( simpleEmptyPkg );
    }

    @Test
    @DisplayName( "multiple simple packages" )
    void multiSimplePackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "single complex package" )
    void singleComplexPackage() throws IOException
    {
      final Entry<Path, Set<Path>> complexEmptyPackage = addPackage( srcDir, "foo/bar/baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( complexEmptyPackage );
    }

    @Test
    @DisplayName( "multiple complex packages" )
    void multiComplexPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "org/luossfi/foo" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "de/luossfi/bar" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "org/luossfi/baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "mixed packages" )
    void mixedPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo" );
      final Entry<Path, Set<Path>> luossfiPkg = addPackage( srcDir, "org/luossfi" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar" );
      final Entry<Path, Set<Path>> fnwdPkg = addPackage( srcDir, "de/luossfi/test/fnwd" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 4 ).contains( fooPkg, luossfiPkg, barPkg, fnwdPkg );
    }
  }

  @Nested
  @DisplayName( "find single files inside packages" )
  class PackagesWithSingleFiles
  {

    @Test
    @DisplayName( "default package" )
    void defaultPackage() throws IOException
    {
      final Entry<Path, Set<Path>> defaultPkg = addPackage( srcDir, "", "foo" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( defaultPkg );
    }

    @Test
    @DisplayName( "single simple package" )
    void singleSimplePackage() throws IOException
    {
      final Entry<Path, Set<Path>> simplePkg = addPackage( srcDir, "foo", "bar" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( simplePkg );
    }

    @Test
    @DisplayName( "multiple simple packages" )
    void multiSimplePackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo", "bar" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar", "baz" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "baz", "foo" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "single complex package" )
    void singleComplexPackage() throws IOException
    {
      final Entry<Path, Set<Path>> complexPackage = addPackage( srcDir, "foo/bar/baz", "fooBar" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( complexPackage );
    }

    @Test
    @DisplayName( "multiple complex packages" )
    void multiComplexPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "org/luossfi/foo", "bar.txt" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "de/luossfi/bar", "Baz.java" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "org/luossfi/baz", "foo" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "mixed packages" )
    void mixedPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo", "FooBar.java" );
      final Entry<Path, Set<Path>> luossfiPkg = addPackage( srcDir, "org/luossfi", "Baz.adoc" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar", "abcdef12" );
      final Entry<Path, Set<Path>> fnwdPkg = addPackage( srcDir, "de/luossfi/test/fnwd", "File_With_Unpleasant_Long_Name" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 4 ).contains( fooPkg, luossfiPkg, barPkg, fnwdPkg );
    }
  }

  @Nested
  @DisplayName( "find multiple files inside packages" )
  class PackagesWithMultipleFiles
  {

    @Test
    @DisplayName( "default package" )
    void defaultPackage() throws IOException
    {
      final Entry<Path, Set<Path>> defaultPkg = addPackage( srcDir, "", "foo", "bar", "baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( defaultPkg );
    }

    @Test
    @DisplayName( "single simple package" )
    void singleSimplePackage() throws IOException
    {
      final Entry<Path, Set<Path>> simplePkg = addPackage( srcDir, "foo", "bar", "baz", "test123" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( simplePkg );
    }

    @Test
    @DisplayName( "multiple simple packages" )
    void multiSimplePackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo", "bar", "Test.java", "baz" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar", "baz", "abcDEF.txt", "HelloWorld.java" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "baz", "foo", "bar", "baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "single complex package" )
    void singleComplexPackage() throws IOException
    {
      final Entry<Path, Set<Path>> complexPackage = addPackage( srcDir, "foo/bar/baz", "fooBar", "HelloWorld.java", "HelloWorld.adoc" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( complexPackage );
    }

    @Test
    @DisplayName( "multiple complex packages" )
    void multiComplexPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "org/luossfi/foo", "bar.txt", "fooBar", "Test.java" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "de/luossfi/bar", "Baz.java", "abcdef", "12_foo" );
      final Entry<Path, Set<Path>> bazPkg = addPackage( srcDir, "org/luossfi/baz", "foo", "bar", "HelloWorld.java" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 3 ).contains( fooPkg, barPkg, bazPkg );
    }

    @Test
    @DisplayName( "mixed packages" )
    void mixedPackages() throws IOException
    {
      final Entry<Path, Set<Path>> fooPkg = addPackage( srcDir, "foo", "FooBar.java", "abc.txt", "543Test.txt" );
      final Entry<Path, Set<Path>> luossfiPkg = addPackage( srcDir, "org/luossfi", "Baz.adoc", "HelloWorld.java", "FooBar.java" );
      final Entry<Path, Set<Path>> barPkg = addPackage( srcDir, "bar", "abcdef12", "HelloWorld.java", "Short_Name.java" );
      final Entry<Path, Set<Path>> fnwdPkg = addPackage( srcDir, "de/luossfi/test/fnwd", "File_With_Unpleasant_Long_Name", "bar", "baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 4 ).contains( fooPkg, luossfiPkg, barPkg, fnwdPkg );
    }
  }

  @Nested
  @DisplayName( "do not find hidden files and packages" )
  class HiddenStuff
  {
    @Test
    @DisplayName( "default package" )
    void defaultPackage() throws IOException
    {
      final Entry<Path, Set<Path>> defaultPkg = addPackage( srcDir, "", "notHidden", ".hidden", ".otherHidden.txt" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( defaultPkg );
    }

    @Test
    @DisplayName( "default package" )
    void simplePackage() throws IOException
    {
      final Entry<Path, Set<Path>> simplePkg = addPackage( srcDir, "foo", "notHidden", ".hidden", ".otherHidden.txt" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( simplePkg );
    }

    @Test
    @DisplayName( "default package" )
    void complexPackage() throws IOException
    {
      final Entry<Path, Set<Path>> complexPkg = addPackage( srcDir, "org/luossfi/test", "notHidden", ".hidden", ".otherHidden.txt" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).contains( complexPkg );
    }

    @Test
    @DisplayName( "hidden simple package" )
    void hiddenSimplePackage() throws IOException
    {
      addPackage( srcDir, ".hidden", ".hidden.txt", "notHidden.txt" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).containsEntry( srcDir.relativize( srcDir ), emptySet() );
    }

    @Test
    @DisplayName( "hidden complex package" )
    void hiddenComplexPackage() throws IOException
    {
      addPackage( srcDir, ".hidden/foo/bar", ".hidden.txt", "notHidden.txt" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).containsEntry( srcDir.relativize( srcDir ), emptySet() );
    }

    @Test
    @DisplayName( "single hidden simple non-empty package is not found" )
    void hiddenSingleSimplePackageWithContent() throws IOException
    {
      addPackage( srcDir, ".hidden", "foo", ".bar", "baz" );

      final Map<Path, Set<Path>> foundPackages = findPackages( srcDir );

      assertThat( foundPackages ).hasSize( 1 ).containsEntry( srcDir.relativize( srcDir ), emptySet() );
    }
  }

  /**
   * Creates a directory with path {@code pgk} as child(ren) to {srcDir} and
   * creates files identified by {@code contents}, if any.
   *
   * @param srcDir the src dir
   * @param pkg the pkg
   * @param contents the contents
   * @return an entry whose key is the {@link Path path} identified by
   *         {@code pkg} and the value is a set of all *non-hidden* {@link Path
   *         paths} inside {@code pkg}
   */
  private Entry<Path, Set<Path>> addPackage( final Path srcDir, final String pkg, final String... contents ) throws IOException
  {
    final FileSystem fs = srcDir.getFileSystem();
    final Path pkgPath = createDirectories( srcDir.resolve( pkg ) );
    final Set<Path> contentPaths = new HashSet<>();

    for ( final String content : contents )
    {
      final Path file = fs.getPath( content );
      createFile( pkgPath.resolve( file ) );
      if ( !Files.isHidden( file ) )
      {
        contentPaths.add( file );
      }
    }

    return new AbstractMap.SimpleImmutableEntry<>( srcDir.relativize( pkgPath ), contentPaths );
  }
}
