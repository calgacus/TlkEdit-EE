[![CircleCI](https://circleci.com/gh/calgacus/TlkEdit-EE.svg?style=svg)](https://circleci.com/gh/calgacus/TlkEdit-EE)

# TlkEdit-EE

This is a continuation of the TlkEdit project found at https://neverwintervault.org/project/nwn2/other/tool/2datlkgff-editor-aka-tlkedit2

Now on version 14.0.x

Any input is welcome, I hope to be able to expand on this tool.  I do not plan to support the launch4j part that creates the *.exe 32bit-java files.

My main foocus is supporting this project for NWN:EE but hopefully I will not have to break any support for the other games (NWN 1.69, NWN2, Witcher).

If you want spell-check for other languages there are some extra "myspell" format dictionary files at -> http://download.services.openoffice.org/contrib/dictionaries/
Download the desired ones to the /dict folder which should be in the same folder in which the tlkedit.jar file is present,
then update ./dict/dictionaries.properties to include the needed code like the others in the same file.

Run
-----
Install java, and if you want to compile from source maven and, if on windows, a unix style shell, eg git-bash.
In project root run ./tlkedit.sh or ./tlkedit2.sh for the NWN2 version.

Build
-----
Install java, maven and, if on windows, git-bash

First, install dependencies into local maven repository. This is temporal solution until
dependencies will be upgraded to one that exists in one of public maven repositories:

```bash
$ mvn install:install-file -Dfile=lib/jmyspell-1.0.0-beta1.jar -DgroupId=tlkedit -DartifactId=jmyspell -Dversion=1.0.0-beta1 -Dpackaging=jar -DlocalRepositoryPath=./lib
```

Then build project as usual:

```bash
mvn install
```

### Troubleshooting
If you got error:
```bash
$ mvn install
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------------------------------------------------------------
[INFO] Building TlkEdit-EE 1.0-SNAPSHOT
[INFO] ------------------------------------------------------------------------
[WARNING] The POM for tlkedit:jmyspell:jar:1.0.0-beta1 is missing, no dependency information available
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.392s
[INFO] Finished at: Mon Sep 30 19:08:08 YEKT 2019
[INFO] Final Memory: 4M/123M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal on project TlkEdit-EE: Could not resolve dependencies for project tlkedit:TlkEdit-EE:jar:1.0-SNAPSHOT: The following artifacts could not be resolved: tlkedit:jmyspell:jar:1.0.0-beta1: Failure to find tlkedit:jmyspell:jar:1.0.0-beta1 in file://D:\Projects\NWN\TlkEdit-EE/lib was cached in the local repository, resolution will not be reattempted until the update interval of local-jars has elapsed or updates are forced -> [Help 1]
[ERROR]
[ERROR] To see the full stack trace of the errors, re-run Maven with the -e switch.
[ERROR] Re-run Maven using the -X switch to enable full debug logging.
[ERROR]
[ERROR] For more information about the errors and possible solutions, please read the following articles:
[ERROR] [Help 1] http://cwiki.apache.org/confluence/display/MAVEN/DependencyResolutionException
$
```
it means, that you already tried to build project without success and maven cache
unsuccesfull state of dependency resolution. Just add switch `-U` to maven invocation
to force maven re-check dependencies:

```bash
$ mvn -U install
```

Credits
=======
TlkEdit wouldn't be the same without
* The [SwingX](http://swingx.dev.java.net/) library
* [JMySpell](http://jmyspell.javahispano.net/)

Other peoples' work included in this release:
* TlkEdit.exe is a [launch4j](http://launch4j.sourceforge.net/) native java launcher
* The icons are part of the 'Nuvola' icon set, created by David Vignoni for KDE. See http://www.icon-king.com/.
* Patch version of dlpcpakv01 by Garad Moonbeam (dragonlance races).

NWN1 vs NWN2
============
The tlk and gff format for NWN1 and NWN2 is the same except for the string encoding used.
That means that TlkEdit cannot distinguish between files for NWN1 or NWN2, but opening
a NWN2 tlk file as NWN tlk file will display lots of garbled text. When launched with
TlkEdit2.exe TlkEdit will use NWN2 mode by default. To open or save a file in NWN1 format
you need to select the **All Files - NWN 1** filter in the Open File / Save As dialog.
The mode in which a file will be written is also displayed in the title bar.
The default mode can be set with the `tlkedit.defaultNwnVersion` system property,
i.e. add `-Dtlkedit.defaultNwnVersion=NWN1` or `-Dtlkedit.defaultNwnVersion=NWN2`
to the tlkedit startup file (either `TlkEdit.l4j.ini/TlkEdit2.l4j.ini` or `tlkedit.sh`).

Running TlkEdit
===============
Windows
-------
TlkEdit.exe and TlkEdit2.exe are provided as .EXE launchers for TlkEdit, startup properties
can be found in `TlkEdit.l4j.ini`/`TlkEdit2.l4j.ini` (plain text file).

Linux / OS-X
-------------
See `tlkedit.sh`.

Server Mode
-----------
By default, TlkEdit will try to connect an already running instance so that there can only be
one open TlkEdit instance containing all opened files. This uses TCP/IP so your firewall
software might complain.

Memory Usage
------------
The maximum amount of memory java is allowed to use by default is 64MB. This can be overridden
using the `-Xmx` switch. Both `TlkEdit.lap` and `tlkedit.sh` use this to set the maximum amount
to 128 MB. Memory usage (heap space) is displayed in the TlkEdit status bar, so you can tell
when you are running out of space (some spell checking dictionaries are rather greedy - `de_DE_comb`
needs about 45 MB).

Spell Checking
--------------
Spell checking is provided by [JMySpell], a Java implementation of the OpenOffice spell checker MySpell.
Dictionaries can be downloaded from the [OpenOffice web site][oo_dict].
The site offers dictionaries in both myspell and (newer) hunspell format. JMySpell will only work
with myspell dictionaries. The dictionary zip file should be placed in the `dict` subdirectory
of the TlkEdit dir. Afterwards you need to edit the file `dict/dictionaries.properties` and set
the correct dictionary file for your language code. If you want to use `en_US.zip`
([download][en_US]) or `de_DE_comb.zip` ([download][ed_DE]), the properties are already set correctly.

[oo_dict]: http://wiki.services.openoffice.org/wiki/Dictionaries
[en_US]: http://ftp.services.openoffice.org/pub/OpenOffice.org/contrib/dictionaries/en_US.zip
[de_DE]: http://ftp.services.openoffice.org/pub/OpenOffice.org/contrib/dictionaries/de_DE_comb.zip

Editing
=======
Key bindings
------------
Key bindings (hot keys) can be altered by editing the file `keybinding.properties`
in the `settings` directory. TlkEdit needs to be restarted for the changes to take effect.
On OS-X TlkEdit should map 'control' to the 'command' key, but I couldn't test it.

Editing tlk files
-----------------
Cell editing is stopped by clicking somewhere outside the cell with the mouse or preferably by
pressing ALT+ENTER (you can input line breaks by pressing enter).

Unless you are editing a cell, cut/copy/paste work in entire table rows. This uses an internal
clipboard, so that you can copy tlk entries to other tlk files opened in the editor, but not to
other applications.

### Search & Replace
Pressing Enter or clicking the "find" button will hide the search dialog, unless the "keep dialog"
checkbox is selected. When "keep dialog" is checked you can step through the occurrences of
the search term with "find".

### The "flags" field
This field defines which of the 3 values (String, Sound ResRef and Sound Length) are actually used by NWN.
If you enter a string but set the flags to 0, the string will NOT be used in game. The flags field is set
automatically when you enter something in the "String", "Sound ResRef" or "Sound Length" field, so you can
safely ignore it.

### diff Menu
The tlk editor keeps track of changes to the tlk table. Modified lines are marked with `*`. This mark can be
set/removed manually with the "+*" / "-*" buttons (hotkeys Alt-m / Alt-u)

Menu items
- **"save diff"** : save all modified entries as diff file
- **"merge diff"** : load a diff file. Entries in the diff file will replace existing entries and will be marked as modified
- **"merge dtu"** : load a file created by DialogTLK
- **"discard diff info"** : set all lines to unmodified (this will NOT restore the original tlk entries)
- **"diff overview"** : display a list with the numbers of all modified entries. Selecting a number in the list will display the entry in the editor

(note : there is no default extension for these diff files, i use .tlu)

### tlk lookup
Whenever you edit 2da or Gff files and select a cell with an integer value or a `CExoLocString` node
TlkEdit will try to resolve that value as a StrRef and display the TLK String in the text area in the
TLK lookup dialog. Of course you need to have at least one tlk file opened for this.
If you have more than one tlk file opened in the editor you can select which one is used for tlk lookup
by activating the tab and selecting the appropriate item from the **'tlk lookup'** menu.
By default the first tlk file that's loaded will be used as default tlk table and the second will be used
as user tlk table (i.e. for StrRefs `>16777216`).

Editing 2da files
-----------------
Nothing much to say here ;) If you input strings that contain spaces the editor will try to add
quotation marks as needed. The cut/copy/paste functions use the system clipboard, which means
you can copy 2da lines into another application (like Excel) and vice versa.
Paste works only if the number of columns matches.

When entering StrRefs for custom content you might want to try the *"Edit->Toggle User Flag on StrRef"* menu item.
The **'alter table'** menu item pops up a dialog that will allow you to add, remove (drop) or rename a table column.

Editing gff files
-----------------
Cut/Copy/Paste works on entire subtrees, this uses an editor internal clipboard, so you can copy nodes to another gff file.
The **'find'** function will not find text inside `void` fields and **'replace'** can only be used on `CResRef`, `CExoStrings` and `CExoLocString` substrings.

By default, pressing the `TAB` key will expand / collapse a selected struct or list.

Editing erf files
-----------------
Should be straightforward enough :) Careful when editing ResRefs though: if you rename a resource
and another resource with the same name already exists it will be replaced!
Selecting **'edit resources'** from the **erf** menu will open selected 2da or gff files for editing.
The files will be extracted as temporary files and a new editor panel is added for each.
Everytime you save one of that editor panels the whole erf file will be rewritten.

Encodings (char sets)
=====================
NWN identifies a TLK file's language by looking at the value of byte number `0x08` in the tlk file
(the byte following the file type + version string `"TLK V3.0"`). This byte also determines
the encoding of the file, i.e. the way all those bytes are converted into readable text.
The language to encoding mappings used by TlkEdit are as follows:

|=Code|=Language          |=Encoding NWN 1|=Encoding NWN 2|
|    0|English            |CP1252         |UTF-8          |
|    1|French             |CP1252         |UTF-8          |
|    2|German             |CP1252         |UTF-8          |
|    3|Italian            |CP1252         |UTF-8          |
|    4|Spanish            |CP1252         |UTF-8          |
|    5|Polish             |CP1250         |UTF-8          |
|    6|Unknown            |Unused (CP1250)|UTF-8          |
|  128|Korean             |MS949          |UTF-8          |
|  129|Traditional Chinese|MS950          |UTF-8          |
|  130|Simplified Chinese |MS936          |UTF-8          |
|  131|Japanese           |MS932          |UTF-8          |

These values are used for tlk files and `CExoLocStrings` in gff files.
The mapping used for languages 0-4 is correct as far as I can tell.
However the encoding used can be modified with the `-Dtlkedit.charsetOverride`
command line switch. Values are of the form:
```
<NWNVersion>:<code>:<encoding>[;<code>:<encoding>]+
```
Launching TlkEdit with
```console
java -Dtlkedit.charsetOverride="NWN1:2:ISO-8859-1" -jar tlkedit.jar
```
would change the encoding used for German (2) NWN1 tlk files to ISO-8859-1.
For NWN2 use "NWN2" as version string. The override parameters can also be added
to the `tlkedit.lap` or `tlkedit2.lap` files which contain the startup configuration
for `TlkEdit.exe` and `TlkEdit2.exe`.

The list of encodings supported by Java2 can be found in the documentation under
"Guide to Features"->"Basic Features, Internationalization"->"Supported Encodings"
