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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.luossfi.internal.data.fnwd.ComplianceCheckStatus;
import org.luossfi.internal.data.fnwd.DirectoryInfo;
import org.luossfi.internal.data.fnwd.FileRule;
import org.luossfi.internal.data.fnwd.PackageRule;

/**
 * <p>
 * The SourceDirectoryVisitor collects all Java complianceIssues beginning from
 * a given root directory into a sorted map. This map contains the
 * complianceIssues mapped to a sorted set of all source file names (no
 * directories) contained in this package. If the package does not contain any
 * files then it is mapped to an empty set.<br>
 * The result is accessed by calling {@link SourceDirectoryVisitor#getPackages()
 * getPackages()} method.
 * </p>
 * <p>
 * If any {@link IOException} occurs during the collection process it is mapped
 * to the path which caused the error. The errors can be accessed by calling
 * {@link SourceDirectoryVisitor#getErrors() getErrors()}.
 * </p>
 * <p>
 * <b>Since this visitor does not lock any files, it is not advisable to add or
 * remove something inside the given root directory as long as the collection is
 * in progress! This will lead to unexpected behavior.</b>
 * </p>
 *
 * @see FileVisitor
 * @see Files#walkFileTree(Path, FileVisitor)
 * @author Steff Lukas
 * @since 1.0
 */
public class SourceDirectoryVisitor implements FileVisitor<Path>
{

  /** The map containing the package to file names mapping. */
  private final Map<String, Set<String>> complianceIssues = new TreeMap<>();

  /**
   * The directory stack used for easily accessing the current directory info.
   */
  private final Stack<DirectoryInfo>     directoryStack   = new Stack<>();

  /** The root directory. */
  private final Path                     rootDirectory;

  /** The file name separator. */
  private final String                   fileNameSeparator;

  /** The rule set to check against. */
  private final Set<PackageRule>         ruleSet;

  /**
   * Instantiates a new source directory visitor.
   *
   * @param rootDirectory the root directory
   */
  public SourceDirectoryVisitor( Path rootDirectory, Set<PackageRule> ruleSet )
  {
    this.rootDirectory = rootDirectory;
    this.fileNameSeparator = rootDirectory.getFileSystem().getSeparator();
    this.ruleSet = ruleSet;
  }

  /**
   * Gets the compliance check result. The resulting map's key is always a
   * package name. If the package is mapped to an empty set, then the package
   * does not comply. If the set contains values then the package name is
   * compliant, but the source files contained in it are not.
   *
   * @return the compliance issues
   */
  public Map<String, Set<String>> getComplianceIssues()
  {
    return complianceIssues;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult preVisitDirectory( Path dir, BasicFileAttributes attrs ) throws IOException
  {
    if ( !directoryStack.empty() )
    {
      DirectoryInfo parent = directoryStack.peek();
      parent.setHasContent( true );
    }

    directoryStack.push( new DirectoryInfo( dir ) );

    return FileVisitResult.CONTINUE;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult visitFile( Path file, BasicFileAttributes attrs ) throws IOException
  {
    if ( attrs.isRegularFile() )
    {
      DirectoryInfo directoryInfo = directoryStack.peek();
      directoryInfo.setHasContent( true );

      if ( ComplianceCheckStatus.PENDING == directoryInfo.getStatus() )
      {
        checkPackageCompliance( directoryInfo );
      }

      if ( ComplianceCheckStatus.SUCCESS == directoryInfo.getStatus() )
      {
        CheckFileCompliance( file, directoryInfo );
      }
    }
    return FileVisitResult.CONTINUE;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult visitFileFailed( Path file, IOException exc ) throws IOException
  {
    throw exc;
  }

  /** {@inheritDoc} */
  @Override
  public FileVisitResult postVisitDirectory( Path dir, IOException exc ) throws IOException
  {
    if ( exc != null )
    {
      throw exc;
    }

    DirectoryInfo directoryInfo = directoryStack.pop();

    // This directory info contains an empty package
    if ( !directoryInfo.hasContent() )
    {
      String packageName = createPackageName( directoryInfo.getPath() );
      directoryInfo.setPackageName( packageName );

      PackageRule applicableRule = findPackageRule( packageName );
      directoryInfo.setPackageRule( applicableRule );

      ComplianceCheckStatus newStatus = applicableRule != null ? ComplianceCheckStatus.SUCCESS : ComplianceCheckStatus.FAILED;
      directoryInfo.setStatus( newStatus );

      if ( ComplianceCheckStatus.FAILED == newStatus )
      {
        complianceIssues.put( directoryInfo.getPackageName(), Collections.emptySet() );
      }
    }

    return FileVisitResult.CONTINUE;
  }

  /**
   * Converts the input directory info's path into a Java package name by
   * replacing the default file separator with a "." and by relativizing it
   * against this visitors root directory.
   *
   * @param directory the directory's path
   * @return the package name
   */
  private String createPackageName( Path directory )
  {
    Path relativePath = rootDirectory.relativize( directory ).normalize();
    String packageName = relativePath.toString();
    return packageName.replace( fileNameSeparator, "." );
  }

  /**
   * Find a package rule from the rule set to which the input package name
   * complies.
   *
   * @param packageName the package name to check
   * @return the package rule if the package complied to one, null otherwise
   */
  private PackageRule findPackageRule( String packageName )
  {
    PackageRule applicableRule = null;
    Iterator<PackageRule> ruleIterator = ruleSet.iterator();

    // Iterate over rule set as long as no applicable rule was found
    while ( ruleIterator.hasNext() && applicableRule == null )
    {
      PackageRule packageRule = ruleIterator.next();
      Pattern rule = packageRule.getRule();

      if ( rule.matcher( packageName ).matches() )
      {
        applicableRule = packageRule;
      }
    }

    return applicableRule;
  }

  /**
   * Check the directory info's package compliance. The package name is created
   * and set, the PackageRule is set if one applied and the status is changed to
   * {@link ComplianceCheckStatus#SUCCESS SUCCESS} or
   * {@link ComplianceCheckStatus#FAILED FAILED} depending on the outcome. It
   * also adds the package to the compliance issues if no applicable rule could
   * be found.
   *
   * @param directoryInfo the directory info to check
   */
  private void checkPackageCompliance( DirectoryInfo directoryInfo )
  {
    String packageName = createPackageName( directoryInfo.getPath() );
    directoryInfo.setPackageName( packageName );

    PackageRule applicableRule = findPackageRule( packageName );
    directoryInfo.setPackageRule( applicableRule );

    ComplianceCheckStatus newStatus = applicableRule != null ? ComplianceCheckStatus.SUCCESS : ComplianceCheckStatus.FAILED;
    directoryInfo.setStatus( newStatus );

    if ( ComplianceCheckStatus.FAILED == newStatus )
    {
      complianceIssues.put( directoryInfo.getPackageName(), Collections.emptySet() );
    }
  }

  /**
   * If the input parent directory has a package rule and if so it checks the
   * file's file name against the contained file rules. If the file name does
   * not match any rule it is added to the compliance errors.
   *
   * @param file the file to check
   * @param parentDirectory the parent directory info
   */
  private void CheckFileCompliance( Path file, DirectoryInfo parentDirectory )
  {
    String fileName = file.getFileName().toString();
    PackageRule packageRule = parentDirectory.getPackageRule();

    // Just in case to avoid a NPE
    if ( packageRule != null )
    {
      Iterator<FileRule> fileRulesIterator = packageRule.getFileRules().iterator();
      boolean fileNameCompliant = false;

      while ( fileRulesIterator.hasNext() && !fileNameCompliant )
      {
        FileRule fileRule = fileRulesIterator.next();

        if ( fileRule.getRule().matcher( fileName ).matches() )
        {
          fileNameCompliant = true;
        }

      }

      if ( !fileNameCompliant )
      {
        String packageName = parentDirectory.getPackageName();
        Set<String> fileIssues = complianceIssues.get( packageName );

        if ( fileIssues == null )
        {
          fileIssues = new TreeSet<>();
          complianceIssues.put( packageName, fileIssues );
        }

        fileIssues.add( fileName );
      }
    }

  }

}
