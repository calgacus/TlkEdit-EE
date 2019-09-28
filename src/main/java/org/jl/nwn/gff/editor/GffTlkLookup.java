package org.jl.nwn.gff.editor;

import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.tlk.editor.TlkLookupPanel;

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
