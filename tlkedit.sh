#!/bin/bash
if [ -z $TLKEDIT_HOME ]; then
  echo "TLKEDIT_HOME not set !"
  fullpath=`which $0`
  TLKEDIT_HOME=`dirname $fullpath`
  echo "using TLKEDIT_HOME="$TLKEDIT_HOME
fi

#tlkedit.charsetOverride sample :
#-Dtlkedit.charsetOverride=NWN1:2:ASCII;NWN2:0:UTF-16

# set Metal (Ocean) L&F :
#-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel \

# set tlkedit.serverport=-1 to disable server socket

#TlkEdit-EE-1.0-SNAPSHOT.jar
if [ ! -f "$TLKEDIT_HOME/tlkedit.jar" ]; then
	cp  "$TLKEDIT_HOME/target/TlkEdit-EE-1.0-SNAPSHOT.jar"  "$TLKEDIT_HOME/tlkedit.jar" 
fi

java -Xmx128M -Dswing.aatext=true \
  -Dcom.apple.macos.useScreenMenuBar=true \
  -Dtlkedit.serverport=4712 \
  -Dtlkedit.defaultNwnVersion=NWN1 \
  -jar "$TLKEDIT_HOME/tlkedit.jar" "$@" 1>/dev/null &

