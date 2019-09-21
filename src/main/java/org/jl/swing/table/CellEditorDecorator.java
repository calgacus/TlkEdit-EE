/*
 * CellEditorDecorator.java
 *
 * Created on 7. Oktober 2006, 14:30
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jl.swing.table;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

/**
 *
 * @author ich
 */
public class CellEditorDecorator implements TableCellEditor{
    
    private TableCellEditor delegate;
    
    /** Creates a new instance of CellEditorDecorator */
    public CellEditorDecorator(TableCellEditor delegate) {
        this.delegate = delegate;
    }

    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    public boolean isCellEditable(EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    public void removeCellEditorListener(CellEditorListener l) {
        delegate.removeCellEditorListener(l);
    }

    public void addCellEditorListener(CellEditorListener l) {
        delegate.addCellEditorListener(l);
    }

    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return delegate.getTableCellEditorComponent(table,value,isSelected,row,column);
    }
    
}
