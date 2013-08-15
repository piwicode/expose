expose
======

Scans a directory hierarchy, identify the photos stared in Picasa and create 
a flat list of album suitable to be imported but iTunes.

This software requires Java runtime: http://java.com/en/download

java -jar expose.jar <source> <target>

 * source: full path of the root directory of the document collection
 * target: full path where stared document are copied. This directory 
           must be initially empty.

Warning: The target is purged for the documents that are not seend stared in 
the source. It means the files and directory in the target can be deleted.

First the process scans recursively the directory hierarchy, looking for 
picasa .ini files. Then the files are parsed in order to extract the list
of stared documents. Finaly an album is create foe every directory that holds
at least one stared file. The documents are not copied, but a hardlink is 
created instead. The files of the target directory that no longer match 
a stared document of the input are removed.

