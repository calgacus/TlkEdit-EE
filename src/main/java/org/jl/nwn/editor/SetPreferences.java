package org.jl.nwn.editor;

import java.io.File;
import java.util.prefs.Preferences;

public class SetPreferences {
    public static void main( String[] args ){
        if ( args.length > 0 ){
            final Preferences prefs = Preferences.userNodeForPackage(EditorFrameX.class);
            final File dir = new File( args[0] );
            prefs.put( "TlkEditHome", dir.getAbsolutePath() );
            System.out.println("tlk edit home : " + dir);
        }
    }
}
