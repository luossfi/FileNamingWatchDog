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
## Further Documentation
Please see the [documentation](./doc/FileNamingWatchDog.md).

## Todos

- [ ] Create Gradle Plugin so the Watch Dog can be integrated into the build process
- [ ] Write Unit Tests
