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
$ mvn install:install-file -Dfile=lib/swingx.jar -DgroupId=tlkedit -DartifactId=swingx -Dversion=unknown -Dpackaging=jar -DlocalRepositoryPath=./lib
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
[WARNING] The POM for tlkedit:swingx:jar:unknown is missing, no dependency information available
[WARNING] The POM for tlkedit:jmyspell:jar:1.0.0-beta1 is missing, no dependency information available
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[INFO] Total time: 0.392s
[INFO] Finished at: Mon Sep 30 19:08:08 YEKT 2019
[INFO] Final Memory: 4M/123M
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal on project TlkEdit-EE: Could not resolve dependencies for project tlkedit:TlkEdit-EE:jar:1.0-SNAPSHOT: The following artifacts could not be resolved: tlkedit:swingx:jar:unknown, tlkedit:jmyspell:jar:1.0.0-beta1: Failure to find tlkedit:swingx:jar:unknown in file://D:\Projects\NWN\TlkEdit-EE/lib was cached in the local repository, resolution will not be reattempted until the update interval of local-jars has elapsed or updates are forced -> [Help 1]
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
