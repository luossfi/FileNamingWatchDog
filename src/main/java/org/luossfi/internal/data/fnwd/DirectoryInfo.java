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

package org.luossfi.internal.data.fnwd;

import java.nio.file.Path;

/**
 * The Class DirectoryInfo is used to store various information during the file
 * tree visiting.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public class DirectoryInfo
{

  /** The directory represented by this object. */
  private final Path            path;

  /** Content flag */
  private boolean               hasContent  = false;

  /** The compliance check status. */
  private ComplianceCheckStatus status      = ComplianceCheckStatus.PENDING;

  /** The package rule this package complied to. */
  private PackageRule           packageRule = null;

  /** The package name of this directory. */
  private String                packageName = null;

  /**
   * Instantiates a new directory info.
   *
   * @param path the path which is represented by this directory info object
   */
  public DirectoryInfo( Path path )
  {
    this.path = path;
  }

  /**
   * Gets the path.
   *
   * @return the path
   */
  public Path getPath()
  {
    return path;
  }

  /**
   * Checks the content flag
   *
   * @return true, if this directory was flagged having content, false otherwise
   */
  public boolean hasContent()
  {
    return hasContent;
  }

  /**
   * Sets the hasContent flag
   *
   * @param hasContent true if this directory has content, false otherwise
   */
  public void setHasContent( boolean hasContent )
  {
    this.hasContent = hasContent;
  }

  /**
   * Gets the compliance status.
   *
   * @return the compliance status
   */
  public ComplianceCheckStatus getStatus()
  {
    return status;
  }

  /**
   * Sets the compliance status.
   *
   * @param status the new compliance status
   */
  public void setStatus( ComplianceCheckStatus status )
  {
    this.status = status;
  }

  /**
   * Gets the package rule which this package complies to.
   *
   * @return the package rule or null if no package rule is set
   */
  public PackageRule getPackageRule()
  {
    return packageRule;
  }

  /**
   * Sets the package rule.
   *
   * @param packageRule the new package rule
   */
  public void setPackageRule( PackageRule packageRule )
  {
    this.packageRule = packageRule;
  }

  /**
   * Gets the package name.
   *
   * @return the package name
   */
  public String getPackageName()
  {
    return packageName;
  }

  /**
   * Sets the package name.
   *
   * @param packageName the new package name
   */
  public void setPackageName( String packageName )
  {
    this.packageName = packageName;
  }

}
