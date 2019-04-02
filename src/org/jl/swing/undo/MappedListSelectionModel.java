/*
 * MappedListSelectionModel.java
 * 
 * Created on 26.08.2007, 13:41:45
 */

package org.jl.swing.undo;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import org.jdesktop.swingx.JXTable;

public abstract class MappedListSelectionModel implements ListSelectionModel{

    protected ListSelectionModel delegate;
    
    public MappedListSelectionModel(ListSelectionModel delegate){
        this.delegate = delegate;
    }
    
    abstract public int map(int i);
    
    public static MappedListSelectionModel createRowModelToViewMapper(final JXTable table){
        return new MappedListSelectionModel(table.getSelectionModel()){
        @Override public int map(int i){
                return table.convertRowIndexToView(i);
            }
        };
    }
    
    public static MappedListSelectionModel createColumnModelToViewMapper(final JXTable table){
        return new MappedListSelectionModel(table.getColumnModel().getSelectionModel()){
        @Override public int map(int i){
                return table.convertColumnIndexToView(i);
            }
        };
    }
    
    // mapped methods :

    @Override public void addSelectionInterval(int arg0, int arg1) {
        delegate.addSelectionInterval(map(arg0), map(arg1));
    }

    @Override public void insertIndexInterval(int arg0, int arg1, boolean arg2) {
        delegate.insertIndexInterval(map(arg0), map(arg1), arg2);
    }

    @Override public boolean isSelectedIndex(int arg0) {
        return delegate.isSelectedIndex(map(arg0));
    }

    @Override public void removeIndexInterval(int arg0, int arg1) {
        delegate.removeIndexInterval(map(arg0), map(arg1));
    }

    @Override public void removeSelectionInterval(int arg0, int arg1) {
        delegate.removeSelectionInterval(map(arg0), map(arg1));
    }

    @Override public void setAnchorSelectionIndex(int arg0) {
        delegate.setAnchorSelectionIndex(map(arg0));
    }

    @Override public void setLeadSelectionIndex(int arg0) {
        delegate.setLeadSelectionIndex(map(arg0));
    }

    @Override public void setSelectionInterval(int arg0, int arg1) {
        delegate.setSelectionInterval(map(arg0), map(arg1));
    }
    
    // purely delegated methods :
    
    @Override public void setValueIsAdjusting(boolean arg0) {
        delegate.setValueIsAdjusting(arg0);
    }

    @Override public void setSelectionMode(int arg0) {
        delegate.setSelectionMode(arg0);
    }

    @Override public void removeListSelectionListener(ListSelectionListener arg0) {
        delegate.removeListSelectionListener(arg0);
    }

    @Override public boolean isSelectionEmpty() {
        return delegate.isSelectionEmpty();
    }

    @Override public boolean getValueIsAdjusting() {
        return delegate.getValueIsAdjusting();
    }

    @Override public int getSelectionMode() {
        return delegate.getSelectionMode();
    }

    @Override public int getMinSelectionIndex() {
        return delegate.getMinSelectionIndex();
    }

    @Override public int getMaxSelectionIndex() {
        return delegate.getMaxSelectionIndex();
    }

    @Override public int getLeadSelectionIndex() {
        return delegate.getLeadSelectionIndex();
    }

    @Override public int getAnchorSelectionIndex() {
        return delegate.getAnchorSelectionIndex();
    }

    @Override public void clearSelection() {
        delegate.clearSelection();
    }

    @Override public void addListSelectionListener(ListSelectionListener arg0) {
        delegate.addListSelectionListener(arg0);
    }    
    
}
