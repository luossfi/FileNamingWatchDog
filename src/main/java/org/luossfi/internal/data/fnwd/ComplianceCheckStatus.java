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

/**
 * The Enum ComplianceCheckStatus defines the different possible status for the
 * compliance check.
 *
 * @author Steff Lukas
 * @since 1.0
 */
public enum ComplianceCheckStatus
{

  /** The compliance check is not yet done. */
  PENDING,

  /** The compliance check succeeded. */
  SUCCESS,

  /** The compliance check failed. */
  FAILED

}
