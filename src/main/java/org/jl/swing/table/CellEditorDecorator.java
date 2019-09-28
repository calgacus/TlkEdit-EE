package org.jl.swing.table;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;

public class CellEditorDecorator implements TableCellEditor {

    private final TableCellEditor delegate;

    public CellEditorDecorator(TableCellEditor delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    @Override
    public boolean isCellEditable(EventObject anEvent) {
        return delegate.isCellEditable(anEvent);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        delegate.removeCellEditorListener(l);
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        delegate.addCellEditorListener(l);
    }

    @Override
    public boolean stopCellEditing() {
        return delegate.stopCellEditing();
    }

    @Override
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    @Override
    public void cancelCellEditing() {
        delegate.cancelCellEditing();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        return delegate.getTableCellEditorComponent(table,value,isSelected,row,column);
    }
}
