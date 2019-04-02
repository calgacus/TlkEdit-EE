#!/bin/bash
if [ -z $TLKEDIT_HOME ]; then
  echo "TLKEDIT_HOME not set, using current directory : " `pwd`
  TLKEDIT_HOME=. ;
fi
java -cp "$TLKEDIT_HOME/tlkedit.jar" org.jl.nwn.patcher.PatcherGUI