# File Naming Watch Dog Documentation

## Convention Definition File
The convention definition file is the place where the naming rules are defined. The 
Watch Dog assumes that this file is encoded using **UTF-8**.

The rules have to be defined from special to general as the first rule matching
a package will define which rules are used to check the packages files. If the
same package rule is defined twice its file rules will be added to the first
occurrence of the package rule.
### General Syntax
```
package <package naming rule>
{
  file <file naming rule>
  file <other file naming rule>
}

package <other package naming rule>
{
  file <another file naming rule>
}
```
As shown in the example above the definition file consists of multiple **package** rules which
contain multiple **file** rules.

**Note:**   
It is not allowed to nest a package rule inside another package rule.

### Rule Definition Elements

| Element | Explanation |
| -------:| ----------- |
| `" "` | A string which may contain anything but the "-char itself and single %-chars (see placeholder). |
| `% %` | A placeholder inside a string. The placeholder's name may be anything but the %-char itself. |
| `[ ]` | A character group. This defines a set of allowed characters for one single char. Ranges are defined by putting the '-'-char between the start and the end. If the '-'-char itself is part of the group then it must appear as the very first char in the group. |
| `( )` | Groups together multiple rule elements |
| <code>&#124;</code> | Separates alternatives from each other. |
| `*` | Greedy zero or more times multiplier |
| `?` | Greedy zero or one times multiplier |
| `+` | Greedy one or more times multiplier |
| `*?` | Lazy zero or more times multiplier |
| `??` | Lazy zero or one times multiplier |
| `+?` | Lazy one or more times multiplier |
| `.` | Wildcard |

**Warning:**   
Characters surrounded by `" "` are **NOT** automatically grouped, the `" "` should only improve the readability
of the definition file (so escaping special characters is not necessary). If a sequence of characters
(surrounded by `" "`) are supposed to be a group then it still is required to surround 
them with `( )` to e.g. apply a multiplier to the whole sequence and not only to its last character.

Example:
```
# matches packages org.luossfi.fo.bar and org.luossfi.foo.bar
# but NOT org.luossfi.bar
package "org.luossfi" ".foo"? ".bar" { ... }

# matches packages org.luossfi.bar and org.luossfi.foo.bar
package "org.luossfi" ( ".foo" )? ".bar" { ... }
```
### Comments
Comments are started with a \#-char and everything followed by this in the same line will be ignored.

## Using the File Naming Watch Dog
The usage of the watch dog is rather simple as only one class needs to be instantiated (only interesting parts of class added):

```java
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import org.luossfi.watchdog.fnwd.FileNamingWatchDog;

// More imports of course

// Static example-code
public class MyWatchDog
{
  public static void myChecking( Path definitionFile, Map<String, String> placeholderValues, Path... sourceDirsToCheck )
  {
    FileNamingWatchDog watchDog = new FileNamingWatchDog( definitionFile, placeholderValues );
    
    for( Path currentDir : sourceDirsToCheck )
    {
      Map<String, Set<String>> checkResult = watchDog.check( currentDir );
      
      //TODO: Do something with the returned checkResult
    }
  }
}

```

### The File Naming Watch Dog's Result Map Explained
Each call to `org.luossfi.watchdog.fnwd.FileNamingWatchDog.check(Path)` returns a `java.util.Map`.
This map is empty if no naming convention violations were found. If the map is not empty the following cases occur:

* package name -&gt; empty set:

 The package does violate the naming conventions, thus no file names could be checked.
* package name -&gt; set containing file names:
 The package name does **not** violate the conventions, however it contains files which violate the file rules
 defined in the **first** package rule to which the packaged complied to.

### Using Multiple Definition Files
From version 1.1 on it is possible to have multiple definition files merged together before checking. This is useful
if e.g. a general definition file is used for many projects but some of them require some additional rules. 

**Note:**  
This feature does not provide any means of rewriting any rules, it simply adds new stuff at the end.

Example
```
# general config file

# Only Java source files allowed here which must be suffixed with 'Test'.
package "org.luossfi.test.%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* "Test.java"
}

# This more general definition is used as fallback.
package "org.luossfi." ([a-z0-9]+ "." )* "%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* ".java"
}
```

```
# Special config file for 'foo' project

# In test package also 'TestFoo' is allowed as suffix.
package "org.luossfi.test.foo"
{
  file [A-Z][a-zA-Z0-9]* "TestFoo.java"
}
```


Merge results:  
Case `%PROJECT%` has the value `foo`
```
package "org.luossfi.test.foo"
{
  file [A-Z][a-zA-Z0-9]* "Test.java"
  file [A-Z][a-zA-Z0-9]* "TestFoo.java" # Additional rule added here
}

# This more general definition is used as fallback.
package "org.luossfi." ([a-z0-9]+ "." )* "%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* ".java"
}
```
Case `%PROJECT%` has the value `bar`
```
package "org.luossfi.test.bar"
{
  file [A-Z][a-zA-Z0-9]* "Test.java"
}

# This more general definition is used as fallback.
package "org.luossfi." ([a-z0-9]+ "." )* "%PROJECT%"
{
  file [A-Z][a-zA-Z0-9]* ".java"
}

# Additional rule added here
package "org.luossfi.test.foo"
{
  file [A-Z][a-zA-Z0-9]* "TestFoo.java"
}
```

Usage of multiple definition files in the code (only interesting parts of class added):

```java
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.luossfi.watchdog.fnwd.FileNamingWatchDog;

// More imports of course

// Static example-code
public class MyWatchDog
{
  public static void myChecking( List<Path> definitionFiles, Map<String, String> placeholderValues, Path... sourceDirsToCheck )
  {
    FileNamingWatchDog watchDog = new FileNamingWatchDog( definitionFiles, placeholderValues );
    
    for( Path currentDir : sourceDirsToCheck )
    {
      Map<String, Set<String>> checkResult = watchDog.check( currentDir );
      
      //TODO: Do something with the returned checkResult
    }
  }
}

```
