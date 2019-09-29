package org.jl.swing.undo;

import java.util.Collections;
import java.util.List;

import javax.swing.ListSelectionModel;

public class RowMutator<RowData> extends Mutator{
    RowMutable<RowData> model;
    ListSelectionModel lsl;

    public interface RowMutable<RowData>{
        public List<RowData> removeRows( int[] indices );
        public void insertRows( int startRow, List<RowData> rows );
    }

    public RowMutator( Mutator m, RowMutable<RowData> model, ListSelectionModel lsl ){
        super(m);
        this.model = model;
        this.lsl = lsl;
    }

    public RowMutator( RowMutable<RowData> model, ListSelectionModel lsl ){
        super();
        this.model = model;
        this.lsl = lsl;
    }

    public class InsertRowsEdit extends ModelEdit{
        int startRow;
        List<RowData> rows;
        public InsertRowsEdit(String pName, int startRow, List<RowData> newRows){
            super(pName);
            this.startRow = startRow;
            this.rows = newRows;
        }
        @Override protected Object performEdit(){
            //System.out.println("InsertRowsEdit");
            ((RowMutable)model).insertRows(startRow, rows);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((RowMutable)model).removeRows(makeIntIntervall(startRow, startRow+rows.size()-1));
            if (lsl != null)
                if (!isCompoundEdit)
                    lsl.setSelectionInterval(startRow, startRow);
                else
                    lsl.addSelectionInterval(startRow, startRow);
        }
        @Override public void redo(){
            super.redo();
            if (lsl != null)
                if (!isCompoundEdit)
                    lsl.setSelectionInterval(startRow, startRow + rows.size() - 1);
                else
                    lsl.addSelectionInterval(startRow, startRow + rows.size() - 1);
        }

    }

    public class RemoveRowsEdit extends ModelEdit{
        int[] indices;
        List<RowData> rows;
        public RemoveRowsEdit( String pName, int[] indices ){
            super(pName);
            this.indices = indices;
        }
        @Override public List<RowData> invoke(){
            return Collections.unmodifiableList((List<RowData>) super.invoke());
        }
        @Override protected List<RowData> performEdit(){
            //System.out.println("RemoveRowsEdit");
            rows = ((RowMutable<RowData>)model).removeRows(indices);
            return rows;
        }
        @Override public void undo(){
            super.undo();
            if (lsl != null && !isCompoundEdit)
                lsl.clearSelection();
            for ( int i = 0; i < indices.length; i++ ){
                ((RowMutable<RowData>)model).insertRows(indices[i], rows.subList(i,i+1));
                if (lsl != null)
                    lsl.addSelectionInterval(indices[i], indices[i]);
            }
        }
    }

    public class ReplaceRowsEdit extends ModelEdit{
        int[] indices;
        List<RowData> insertRows, replacedRows;
        public ReplaceRowsEdit(String pName, int startRow, int endRow, List<RowData> rows){
            super(pName);
            indices = makeIntIntervall(startRow, endRow);
            this.insertRows = rows;
        }
        @Override protected Object performEdit(){
            replacedRows = ((RowMutable<RowData>)model).removeRows(indices);
            ((RowMutable<RowData>)model).insertRows(indices[0], insertRows);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((RowMutable<RowData>)model).removeRows(makeIntIntervall(indices[0],indices[0]+insertRows.size()-1));
            ((RowMutable<RowData>)model).insertRows(indices[0], replacedRows);
        }
    }

    public List<RowData> removeRows( int[] indices ){
        return new RemoveRowsEdit("Remove Rows",indices).invoke();
    }

    public void insertRows( int startRow, List<RowData> rows ){
        new InsertRowsEdit("Insert Rows", startRow, rows).invoke();
    }

    public void replaceRows( int startRow, int endRow, List<RowData> replacement ){
        new ReplaceRowsEdit( "Replace Rows", startRow, endRow, replacement ).invoke();
    }

    protected int[] makeIntIntervall(int start, int end){
        int[] r = new int[end-start+1];
        for ( int i = 0; i < r.length; i++ )
            r[i] = start+i;
        return r;
    }

    public void setSelectionModel(ListSelectionModel lsl){
        this.lsl = lsl;
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
