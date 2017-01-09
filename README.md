<!---
FileNamingWatchDog, a library for checking Java packages and source file
names for compliance to naming conventions.

Copyright (C) 2016++ Steff Lukas <steff.lukas@luossfi.org>

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Lesser General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
details.

You should have received a copy of the GNU Lesser General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
--->

# File Naming Watch Dog

**File Naming Watch Dog** is a library which checks if Java project sources are compliant to naming conventions
(both allowed characters and predefined naming patterns) by checking package names and the source file names against a set of rules.
It also allows the user to define *placeholders* which makes it possible to use the same rule definition for multiple projects,
if these projects use the same naming patterns with e.g. different suffixes.

## Example Definition File
```
# This is a simple package name with a placeholder for the project's name
package "org.luossfi.exception.%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* "Exception.java" # Exception classes should end with "Exception"
  file "ErrorMessageConstants.java"
}

# This more general definition is used as fallback
package "org.luossfi." ([a-z0-9]+ "." )* "%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* ".java"
}
```

## Getting File Naming Watch Dog
**File Naming Watch Dog** is available on [Bintray's jCenter repository](https://bintray.com/bintray/jcenter).

Maven GAV: [org.luossfi.tools:FileNamingWatchDog:1.0.1](https://bintray.com/luossfi/org.luossfi/FileNamingWatchDog/1.0.1)

## Further Documentation
Please see the [documentation](./doc/FileNamingWatchDog.md).

## Gradle Plugin
The Gradle Plugin can be found [here](https://github.com/luossfi/FileNamingWatchDogGradlePlugin). 

## Todos
- [ ] Write Unit Tests
