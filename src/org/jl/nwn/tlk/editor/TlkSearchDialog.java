/*
 * Created on 03.04.2004
 */
package org.jl.nwn.tlk.editor;

import javax.swing.JFrame;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.jl.swing.TableSearchAndReplace;

/**
 */
class TlkSearchDialog extends TableSearchAndReplace {
    private TlkEdit edit;
    
    public TlkSearchDialog( JFrame owner, TlkEdit ed ){
        super(owner, ed.tlkTable);
        //setModal(true);
        this.edit = ed;
    }
    
    @Override public void replaceAll(){
        edit.mutator.beginUpdate();
        super.replaceAll();
        edit.mutator.endUpdate();
    }
    
    @Override public Object string2ModelObject(String s, int row, int col){
        if ( table.convertColumnIndexToModel(col) == 3 )
            return Float.parseFloat(s);
        if ( table.convertColumnIndexToModel(col) == 4 )
            return Byte.parseByte(s);
        return super.string2ModelObject(s,row,col);
    }
}
