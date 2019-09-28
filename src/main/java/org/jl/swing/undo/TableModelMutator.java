package org.jl.swing.undo;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

public class TableModelMutator extends Mutator{
    protected TableModel model;
    protected ListSelectionModel lsl;

    public class SetValueAtEdit extends ModelEdit{
        int row, col;
        Object oldValue, newValue;
        public SetValueAtEdit( String pName, Object value, int row, int col ){
            super(pName);
            this.row = row;
            this.col = col;
            this.newValue = value;
        }
        @Override protected Object performEdit(){
            oldValue = model.getValueAt(row, col);
            //System.out.println("SetValueAtEdit " + newValue);
            model.setValueAt(newValue, row, col);
            return null;
        }
        @Override public void undo(){
            super.undo();
            model.setValueAt(oldValue, row, col);
            selectAffectedRow();
        }
        @Override public void redo(){
            super.redo();
            selectAffectedRow();
        }
        protected void selectAffectedRow(){
            if ( lsl != null && isSignificant() ){
                if ( !isCompoundEdit )
                    lsl.setSelectionInterval(row, row);
                else
                    lsl.addSelectionInterval(row, row);
            }
        }

        @Override public boolean isSignificant(){
            return (oldValue != null && !oldValue.equals(newValue))
            || (oldValue == null && newValue != null);
        }
    }

    public TableModelMutator( TableModel model, ListSelectionModel lsl ){
        super();
        this.model = model;
        this.lsl = lsl;
    }

    public TableModelMutator( Mutator m, TableModel model, ListSelectionModel lsl ){
        super(m);
        this.model = model;
        this.lsl = lsl;
    }

    public void setValueAt(Object value, int row, int column){
        new SetValueAtEdit("Edit Cell", value, row, column).invoke();
    }

    @Override public void compoundUndo(){
        if ( lsl != null )
            lsl.clearSelection();
    }

    @Override public void compoundRedo(){
        if ( lsl != null )
            lsl.clearSelection();
    }
}
