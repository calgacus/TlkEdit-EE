package org.jl.swing.table;

import java.util.List;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import org.jl.swing.undo.Mutator;
import org.jl.swing.undo.RowMutator;
import org.jl.swing.undo.RowMutator.InsertRowsEdit;
import org.jl.swing.undo.RowMutator.ReplaceRowsEdit;
import org.jl.swing.undo.TableModelMutator;

public class TableMutator<RowData, ColumnData> extends TableModelMutator{
    
    public interface ColumnMutable<ColumnData>{
        public ColumnData dropColumn( int index );
        public void insertColumn(int position, String name, Class cClass, ColumnData data);
        public void setColumnName( int index, String name );
    }
    
    protected RowMutator rowMutator;
    
    /** Creates a new instance of TableModelMutator */
    public TableMutator( TableModel model, ListSelectionModel lsl ){
        super(model, lsl);
        init();
    }
    
    public TableMutator( Mutator m, TableModel model, ListSelectionModel lsl ){
        super(m, model, lsl);
        init();
    }
    
    private void init(){
        rowMutator = new RowMutator(this,(RowMutator.RowMutable)model, lsl);
    }
    
    public class SetColumnNameEdit extends ModelEdit{
        String newName, oldName;
        int pos;
        public SetColumnNameEdit(String pName, int pos, String cName){
            super(pName);
            this.pos = pos;
            this.newName = cName;
        }
        @Override protected Object performEdit(){
            //System.out.println("RemoveRowsEdit");
            oldName = model.getColumnName(pos);
            ((ColumnMutable)model).setColumnName(pos, newName);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((ColumnMutable)model).setColumnName(pos, oldName);
        }
    }
    
    public class DropColumnEdit extends ModelEdit{
        Class cClass;
        String name;
        int pos;
        ColumnData data;
        public DropColumnEdit(String pName, int pos){
            super(pName);
            this.pos = pos;
        }
        @Override protected Object performEdit(){
            cClass = model.getColumnClass( pos );
            name = model.getColumnName( pos );
            data = ((ColumnMutable<ColumnData>)model).dropColumn(pos);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((ColumnMutable)model).insertColumn(pos, name, cClass, data);
        }
    }
    
    public class InsertColumnEdit extends ModelEdit{
        Class cClass;
        String name;
        int pos;
        ColumnData data;
        public InsertColumnEdit(String pName, int pos, String cName, Class cClass, ColumnData data){
            super(pName);
            this.pos = pos;
            this.name = cName;
            this.data = data;
            this.cClass = cClass;
        }
        @Override protected Object performEdit(){
            ((ColumnMutable)model).insertColumn(pos, name, cClass, data);
            return null;
        }
        @Override public void undo(){
            super.undo();
            ((ColumnMutable)model).dropColumn(pos);
        }
    }
    
    public List<RowData> removeRows( int[] indices ){
        return rowMutator.removeRows(indices);
    }
    
    public void insertRows( int startRow, List<RowData> rows ){
        rowMutator.insertRows(startRow, rows);
    }
    
    public void replaceRows( int startRow, int endRow, List<RowData> replacement ){
        rowMutator.replaceRows( startRow, endRow, replacement );
    }
    
}
