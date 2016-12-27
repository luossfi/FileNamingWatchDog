# File Naming Watch Dog Documentation

## Convention Definition File
The convention definition file is the place where the naming rules are defined.

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
| `|` | Separates alternatives from each other. |
| `*` | Greedy zero or more times multiplier |
| `?` | Greedy zero or one times multiplier |
| `+` | Greedy one or more times multiplier |
| `*?` | Lazy zero or more times multiplier |
| `??` | Lazy zero or one times multiplier |
| `+?` | Lazy one or more times multiplier |

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
This map is empty if no naming convention violations were found. If the map is not empty the following case occur:

* package name -&gt; empty set:

 The package does violate the naming conventions, thus no file names could be checked.
* package name -&gt; set containing file names:
 The package name does **not** violate the conventions, however it contains files which violate the file rules
 defined in the **first** package rule to which the packaged complied to.


