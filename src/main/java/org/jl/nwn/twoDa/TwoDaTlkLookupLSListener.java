package org.jl.nwn.twoDa;

import java.lang.ref.WeakReference;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jl.nwn.tlk.editor.TlkLookupPanel;

public class TwoDaTlkLookupLSListener implements ListSelectionListener {

    //private JTable table;
    private final WeakReference<JTable> table;
    private final TlkLookupPanel tlp;

    public TwoDaTlkLookupLSListener(TwoDaEdit ed, TlkLookupPanel tlp ){
        //this.table = ed.table;
        this.table = new WeakReference(ed.table);
        this.tlp = tlp;
    }

    @Override
    public void valueChanged(ListSelectionEvent e){
        if ( !e.getValueIsAdjusting() ){
            JTable table = this.table.get();
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();
            if ( row != -1 && col != -1 ){
                String value = table.getModel().getValueAt( row, table.convertColumnIndexToModel(col) ).toString();
                try{
                    tlp.lookup( value );
                } catch ( NumberFormatException nfe ){
                }
            }
        }
    }
}
