[Release 14.0.5] (2022-08-28)
recompiled jar to include updates made by shadguy to spells files

============================
[Release 14.0.4] (2019-10-07)
increased allowable size of CExoStrings to 128*1024

=============================

[Release 14.0.3] (2019-10-07)
increased allowable size of CExoStrings to 2*1024

=============================
[Release 14.0.2] (2019-10-07)
=============================
- added circleci build support (Thanks again Mingun)
- changed version.txt to CHANGELOG.md

[Release 14.0.1] (2019-10-06)
=============================
- Map escape to cancel action for 2da cels (Thanks Mingun {on github})
- Cleaned up and modernize java code (Mingun)
- Changed way tlkedit version number is displayed (now only found in version file)

[Release 14.0.1]: https://github.com/calgacus/TlkEdit-EE/commit/cf63cf2c3a9fa4218e8f20060709bdb99b291f64


[Release 14.0] (2019-10-02)
=========================
- Host project on github at https://github.com/calgacus/TlkEdit-EE
- Allow column resizing for 2DAs
- Fix a bug which prevent some column popups in some 2DAs
- Switch to a maven build (Mingun)
- Cleaned up and modernize java code (Mingun)
- Include build steps in README (Mingun)

[Release 14.0]: https://github.com/calgacus/TlkEdit-EE/commit/17ca29453f6345c72dbb36761c5fc2c6c358a9d7


Release 13c (2007-07-15)
========================
- Updated to a recent swingx build
- Now requires Java 6
- Replaced janel launcher with launch4j

TLK editor
----------
- Added filter function using regular expressions


Release 13b (2007-01-14)
========================
TLK/2DA editor
----------
- Copy & paste StrRefs of selected tlk entries into a 2da table

2DA editor
----------
- Loading of special cell editors & renderers is now based on NWN version


Release 13a (2006-12-13)
========================
- File version ( NWN1 or NWN2 ) can be selected in the open/save dialogs

ERF editor
----------
- Open & extract NWN2 mod & erf files, creating erf files is untested

GFF editor
----------
- All `.ifo` and `.bic` files should work now (error caused by `CExoLocSubstring`s with language id -1,
  added as language `GffToken`)
- Open `.ros` files

TLK editor
----------
- Display StrRefs as hexadecimal values


Release 13 (?)
==============
- (quick&dirty) fixes for NWN2 compatibility
- Support for 2DA V2.b files (KOTOR 1&2)
- Support for Vector type in gff files (KOTOR 1&2)


Release 12c (2006-10-22)
========================
- Fixed some memory issues

GFF editor
----------
- Empty lists were broken (bad code found by EPOlson)


Release 12b
===========
2DA editor
----------
- Fixed 'paste' action (broken in Release 12)


Release 12
==========
- TlkEdit now requires Java 1.5 (a.k.a Java 5)
- Uses `swingx` swing extensions (http://swingx.dev.java.net)
- Undo / Redo in TLK, GFF and 2DA editors
- Spell checking for TLK and GFF files
- Added menu items for most functions
- Editor toolbar is displayed in main window instead of editor panel
- Key bindings can be altered by editing the file `settings/keybindings.properties`
- Included TlkEdit.exe launcher for windows
- Drag&Drop : open files by dragging them onto the editor toolbar
- File browser tree has been removed
- TLK lookup is now an extra dialog window

GFF editor
----------
- Editor control is now a tree table which should make editing simpler
- Bugfix: data type `CHAR` was treated as unsigned value,
- Bugfix: `VOID` fields read & written incorrectly
- Bugfix: `.gui` files can now be opened

ERF editor
----------
- Columns can be sorted by clicking on the column header (sort by resource name, type or size)
- Description editor is only displayed for MOD an HAK files (not for ERFs)
- Type `sav` has been removed from the type selector (`.sav` and `.nwm` files have the erf type MOD)

TLK editor
----------
- Bugfix (Ornedan): position field accepts values in the user entry range


Release 11
==========
- Removed mnemonics for toolbar buttons with icons
- Menu shortcuts should work on Mac OS
- Improved readme.html ( marginally improved - wouldn't call it 'good' ;) )

TLK & 2DA editor
----------------
- Added search and replace dialog

TLK (NwnLanguage)
-----------------
- Added support for Chinese, Japanese and Korean tlk files

2DA editor
----------
- DEFAULT: field is now retained ( but there is no way to edit it the editor )

GFF (NwnLanguage)
-----------------
- Support for CExoLocStrings in Chinese, Japanese or Korean


Release 10a
===========
- Replaced icons with icons from the CrystalSVG icon set for KDE
- TlkEdit is now released under the GNU GPL

TLK editor
----------
- Added command line option for overriding character encoding.
  Character encoding used by tlkedit can be set with `-Dtlkedit.encoding=<encoding name>`
  command line option (hint: edit `tlkedit.bat`) e.g.
  ```console
  java  -Dtlkedit.encoding=cp1251 tlkedit.jar
  ```
  for a complete list of encodings supported by java 1.4.2 see
  http://java.sun.com/j2se/1.4.2/docs/guide/intl/encoding.doc.html


Release 10
==========
- TlkEdit now comes packaged as an executable jar file which will install the application.
  On Windows you should be able to run the jar file by double-clicking it or if double-clicking
  starts a zip utility, right-click the jar file and select "open with->javaw"
  in case that doesn't work either you need to launch it from the command line with:
  ```console
  java -jar TlkEditInstallerR10.jar
  ```
- Changed modifier key for most hotkeys to CTRL (so `CTRL+S` instead of `ALT+S` will save the file etc.),
  the `ALT+?` key bindings still remain though

TLK editor
----------
- Cells will be resized to display only one line after editing
- Cell editors now respond properly to ESC ( abort edit ) and F2 ( start edit ) (table lost focus when pressing ESC)

2DA editor
----------
- Behaviour of cell editors changed ( see tlk editor )
- Combobox editors are now editable ( i.e. can you can enter arbitrary values )

ERF editor
----------
- New !

Patcher
-------
- Command line options are obsolete
- Added 'user tlk' checkbox which will add 16777216 when computing new tlk references
- Key files (`chitin.key` etc.) are detected automatically
- Added readme: `ReadmePatcherGui.html`
- Info about updated 2da files is written to `out/patchinfo.txt`
- Updated for version 1.61


Release 9
=========
- TlkEdit now comes packaged as an executable jar file which will install the application.
  On Windows you should be able to run the jar file by double-clicking it, if that doesn't
  work you need to launch it from the command line which is:
  ```console
  java -jar TlkEditInstallerRx.jar
  ```

2DA editor
----------
- Added button for toggling the 'user' flag on StrRefs ( +-16777216 )
- WIP: cell editors for 2da tables configurable via xml file
    - bitflag editor ( for TargetType & MetaMagic )
    - combo box editors ( for selecting fixed values )
    - mapped editors for displaying/selecting labels instead of true values ( e.g. {yes, no, -} instead of {1, 0, ****} )
    - display of tooltips and description ( in html ) for selected column
    ( only editor mode is for spells.2da, see `meta/spells.meta.xml` (incomplete) )

TLK editor
----------
- Added button for switching between normal and user tlk line numbering
- 'Diff' menu is now displayed on the main menubar
- Can now choose a file to use for looking up user StrRefs in 2da files
    (per default 1st tlk file is used for normal lookup, 2nd one is used for user StrRefs)

GFF editor
----------
- Bugfix: `GffContent` barfed when reading `CExoLocString`s with 2 or more substrings
- Senseless use of shiny icons


Release 8
=========
- Replaced most button texts with icons
- Added 'uninstall' utility for removing stored preferences

GFF editor
----------
- It's new !

TLK editor
----------
- Added internationalization support for TlkEdit panel (only localization is en_GB)
- Can now choose which tlk file is used for tlk lookup
- Fixed some oddities with the cell editor, can now edit a cell without first double clicking it

2DA editor
----------
- Added 'pad' button for adding padding lines quickly

Patcher
-------
- Bugfix (linux only): patcher didn't compile scripts on linux
- Patcher GUI rewritten
- GUI now displays the standard output and error streams in a gui window


Release 7
=========
- Warns about unsaved files on exit
- Added patch: Dwarven Defender prestige class
- Added `bifextract.sh` shell script for extracting files from bifs

TLK editor
----------
- Diff feature fixed
- Removed the "diff->save patch" menu item and the "next/prev. modified lines" buttons
- TLK file memory leak fixed
- TLK editor now uses proper charset (windows-1252)
- "Flags" field is set automatically (can still be edited manually)

Patcher
-------
- Now displays compiler errors
- Fixed compiling scripts on windows (using `clcompile`)


Release 6
=========
TLK editor
----------
- Allows editing of "sound length" and "flags" fields (see TLK specification at http://nwn.bioware.com/developers/)
- Saving of files much faster now
- 'Find' and 'Find again' will now apropriately highlight text
- New "diff" feature for saving only the changes you made to the tlk file (see `readme.txt`)
    (comparable to DialogTLK, allows import of `.dtu` files created by that program)
- Bugfix: new memory leak (yay!) sorry about that, but tlk content doesn't get garbage collected (my fault).
    if you open more than 3 full size ( ca. 8MB ) tlk files the java vm will run out of memory.
    (if this troubles you, you can start java with `java -XmxYYYM ...` to allow the vm to use up to YYY MB of memory,  default is 64MB)

Patcher
-------
- Added support for tlk diff files created with "save diff..." in the tlk editor (files must be called `diff.tlu`)


Release 5
=========
Far far away long time ago...
