/*
 * Created on 19.08.2004
 */
package org.jl.nwn.editor;

import java.io.File;
import java.util.prefs.Preferences;

/**
 */
public class SetPreferences {

	public static void main( String[] args ){
		if ( args.length > 0 ){
			Preferences prefs =
				Preferences.userNodeForPackage(EditorFrameX.class);
			File dir = new File( args[0] );
			prefs.put( "TlkEditHome", dir.getAbsolutePath() );
			System.out.println("tlk edit home : " + dir);
		}		
	}
	
}
