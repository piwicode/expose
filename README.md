Expose
======

Creates a flat list of album suitable to be imported with iTunes, from
the documents stared in Picasa.

### Prerequisites:
 * It is recomanded to have a backup of your photos to avoid any data loss
 * This software requires Java runtime: (http://java.com/en/download)


### Command line:
  `java -jar expose.jar <source> <target>`

 * source: directory of the Picasa document collection
 * target: directory where stared document are copied. This directory 
           must be initially empty. The target must be in the same 
           filesystem as the source and the file system must support hardlinks.

**Warning:** The target is purged for the documents that are not seend stared in 
the source. It means the files and directory in the target can be deleted.


First the program scans recursively the directory hierarchy, looking for 
Picasa '.ini' files. Then the files are parsed in order to extract the list
of stared documents. Now a directory is created for every album that hold
at least one stared file. The album is populated with links to the original  
documents. Finally the files & directories of the target that no longer match 
a stared document of the input are removed.

