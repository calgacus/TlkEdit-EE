/*
 * Created on 26.10.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.twoDa;

import java.lang.ref.WeakReference;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jl.nwn.tlk.editor.TlkLookupPanel;

/**
 * @author ich
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class TwoDaTlkLookupLSListener implements ListSelectionListener {
    
    //private JTable table;
    private WeakReference<JTable> table;
    private TlkLookupPanel tlp;
    
    public TwoDaTlkLookupLSListener(TwoDaEdit ed, TlkLookupPanel tlp ){
        //this.table = ed.table;
        this.table = new WeakReference(ed.table);
        this.tlp = tlp;
    }
    
        /* (non-Javadoc)
         * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
         */
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
    
    public static void main(String[] args) {
    }
}
