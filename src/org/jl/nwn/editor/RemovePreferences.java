/*
 * Created on 10.10.2003
 *
  */
package org.jl.nwn.editor;

import java.util.prefs.BackingStoreException;

import javax.swing.JOptionPane;

import org.jl.nwn.patcher.PatcherGUI;
import org.jl.nwn.tlk.editor.TlkEdit;

public class RemovePreferences {

	public static void main(String[] args) {
		boolean ok = true;
		if ( JOptionPane.showConfirmDialog( null, "remove all preferences stored by the Tlk/2da Editor & Patcher GUI ?", "remove preferences", JOptionPane.YES_NO_OPTION ) == JOptionPane.YES_OPTION ){
			try{
				PatcherGUI.removePreferences();
				EditorFrameX.removePreferences();
				TlkEdit.removePreferences();
			} catch ( BackingStoreException bse ){
				ok = false;
				JOptionPane.showMessageDialog( null, "not all preferences could be removed", "error", JOptionPane.ERROR_MESSAGE );
			}
			if ( ok )
				JOptionPane.showMessageDialog( null, "preferences have been removed" );
		}
		System.exit( 0 );
	}
}
