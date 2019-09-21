/*
 * Created on 26.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.gff.editor;

import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import org.jl.nwn.gff.Gff;

import org.jl.nwn.tlk.editor.TlkLookupPanel;
;

/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class GffTlkLookup implements TreeSelectionListener {
	
	private TlkLookupPanel tlp = null;
	
	public GffTlkLookup( TlkLookupPanel tlp ){
		this.tlp = tlp; 
	}
    
    public void registerWith( GffEditX ed ){
        ed.treeTable.addTreeSelectionListener(this);
    }
    
    public void deregisterWith( GffEditX ed ){
        ed.treeTable.removeTreeSelectionListener(this);
    }

	public static void main(String[] args) {
	}
	/* (non-Javadoc)
	 * @see javax.swing.event.TreeSelectionListener#valueChanged(javax.swing.event.TreeSelectionEvent)
	 */
	@Override
	public void valueChanged(TreeSelectionEvent e) {		
		GffField field = (GffField) e.getPath().getLastPathComponent();
		if ( field.getType() == Gff.CEXOLOCSTRING ){
			GffCExoLocString s = ( GffCExoLocString ) field;
			if ( s.getStrRef() != -1 )
				tlp.lookup( s.getStrRef() );
		}
	}

}
