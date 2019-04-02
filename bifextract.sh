#!/bin/bash
# modify -Dnwn.home to match your nwn install !
# optional : use -Dnwn.bifkeys=<space separated list of .key files>
#   to specify which key files to use
# files to extract / list are specified with a regular expression
# usage example : 'bifextract.sh -l ".*2da"' lists all 2da files
# usage example : 'bifextract.sh -x ".*2da" 2das' extract all 2da files to dir "2das"
# usage example : 'bifextract.sh -gui' show gui
# nwn_home=/usr/local/neverwinter
if [ -z $TLKEDIT_HOME ]; then
  echo "TLKEDIT_HOME not set, using current directory : " `pwd`
  TLKEDIT_HOME=. ;
fi
if [ -z ${nwn_home} ]; then
  echo "nwn_home not set !";
else
  java -Dnwn.home=$nwn_home -cp "$TLKEDIT_HOME/tlkedit.jar" org.jl.nwn.bif.BifRepository "$@";
fi