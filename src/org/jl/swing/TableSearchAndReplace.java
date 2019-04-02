/*
 * Created on 17.04.2004
 */
package org.jl.swing;

import java.awt.Color;
import java.text.MessageFormat;
import javax.swing.CellEditor;
import javax.swing.JFrame;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import org.jl.swing.table.TextCellEditor;

public class TableSearchAndReplace extends SearchAndReplaceDialog {
    
    protected int row = 0;
    protected int col = 0;
    protected int startRow, startCol;
    
    protected JTable table;
    
    public TableSearchAndReplace( JFrame owner, JTable aTable ){
        super(owner);
        this.table = aTable;
        table.getModel().addTableModelListener(
                new TableModelListener(){
            public void tableChanged( TableModelEvent e ){
                TableSearchAndReplace.this.invalidate();
            }
        }
        );
        // invalidate matcher when the user changes the row selection
        table.getSelectionModel().addListSelectionListener( new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent lse){
                if ( !lse.getValueIsAdjusting() ){
                    TableSearchAndReplace.this.invalidate();
                }
            }
        });
        I18nUtil.setText( rbSearchSelection, "&Selected Rows" );
        bottomPanel.validate();
        
        DocumentListener docChangeListener = new DocumentListener(){
            public void changedUpdate( DocumentEvent de ){
                //invalidate();
            }
            public void removeUpdate( DocumentEvent de ){
                invalidate();
            }
            public void insertUpdate( DocumentEvent de ){
                invalidate();
            }
        };
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++ ){
            TableColumn tc = table.getColumnModel().getColumn(i);
            if ( tc.getCellEditor() instanceof TextCellEditor ){
                System.out.println("adding DocumentListener");
                ((TextCellEditor)tc.getCellEditor()).getTextComponent().getDocument().addDocumentListener(docChangeListener);
            }
        }
        pack();
    }
    
    @Override public void setVisible(boolean v){
        super.setVisible(v);
        if ( v && table.getSelectedRowCount() > 1 )
            rbSearchSelection.setSelected(true);
    }
    
    public void init(){
        //System.out.println( "TableSearchAndReplace.init()" );
        if ( table.getSelectedRow() != -1 ) row = table.getSelectedRow();
        if ( table.getSelectedColumn() !=-1 )
            col = table.getSelectedColumn();
        startRow = row;
        startCol = col;
        super.init();
        //System.out.println( row + ", " + col );
    }
    
        /* (non-Javadoc)
         * @see org.jl.swingutil.SearchAndReplaceDialog#selectText(int, int)
         */
    protected void selectText(int start, int end) {
        table.scrollRectToVisible( table.getCellRect( row, col, true ) );
        JTextComponent text = getTextComponent();
        try{
            table.setEnabled(false);
            if (text != null){
                text.getCaret().setDot(start);
                text.getCaret().moveDot(end);
                text.getCaret().setSelectionVisible(true);
                table.getSelectionModel().setSelectionInterval( row, row );
            } else{
                if ( table.getCellEditor() != null )
                    table.getCellEditor().stopCellEditing();
                table.getSelectionModel().setSelectionInterval( row, row );
                table.getColumnModel().getSelectionModel().setSelectionInterval( col, col );
            }
        } finally{
            table.setEnabled(true);
            //SwingUtilities.invokeLater(EnableTable);
        }
    }
    /*
    protected Runnable EnableTable = new Runnable() {
        public void run() {
            table.setEnabled(true);
        }
    };
    */
        /* (non-Javadoc)
         * @see org.jl.swingutil.SearchAndReplaceDialog#getString()
         */
    public String getString() {
        if ( table.getEditingColumn() == col && table.getEditingRow() == row )
            if ( (table.getCellEditor()) instanceof TextCellEditor )
                return ((TextCellEditor)table.getCellEditor()).getTextComponent().getText();
        Object o =  table.getValueAt( row, col );
        return o==null? "":modelObject2String(o, row, col);
    }
    
    public String modelObject2String(Object value, int row, int col){
        return value.toString();
    }
    
    public Object string2ModelObject(String s, int row, int col){
        return s;
    }
    
    protected JTextComponent getTextComponent(){
        CellEditor ed;
        if ( table.getEditingColumn() == col && table.getEditingRow() == row ){
            if ( (ed = table.getCellEditor()) instanceof TextCellEditor )
                return ((TextCellEditor)ed).getTextComponent();
            else{
                ed.stopCellEditing();
                return null;
            }
        }
        if ( (ed = table.getCellEditor(row, col)) instanceof TextCellEditor ){
            table.editCellAt(row, col);
            return ((TextCellEditor)ed).getTextComponent();
        } else
            return null;
    }
    
        /* (non-Javadoc)
         * @see org.jl.swingutil.SearchAndReplaceDialog#updateString(java.lang.String)
         */
    public void updateString(String s){
        if ( table.getEditingRow() == row && table.getEditingColumn() == col){
            if (table.getCellEditor() instanceof TextCellEditor){
                ((TextCellEditor)table.getCellEditor()).getTextComponent().setText(s);
                this.invalidState = false;
                return;
            } else
                table.getCellEditor().stopCellEditing();
        }
        table.setValueAt( string2ModelObject(s, row, col), row, col );
    }
    
    public boolean search(){
        if ( invalidState )
            init();
        if ( row >= table.getRowCount() ) return false;
        boolean match = super.search();
        boolean fullCircle = false;
        //while ( !match && nextCell() ){
        while ( !match && !fullCircle ){
            fullCircle = !nextCell() | fullCircle;
            match = super.search();
        }
        if ( fullCircle ){
            startRow = row;
            startCol = col;
        }
        if ( match ){
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText(MessageFormat.format("Match at ({0}, {1}), position {2}", row, table.getColumnName(col), matcher.start()));
        }
        return match;
    }
    
    public void replaceAll(){
        if ( table.getCellEditor() != null )
            if (!table.getCellEditor().stopCellEditing())
                table.getCellEditor().cancelCellEditing();
        row = 0;
        int lastRow = Integer.MAX_VALUE-1;
        if ( rbSearchSelection.isSelected() ){
            row = table.getSelectedRow();
            int[] selection = table.getSelectedRows();
            if ( selection.length == 0 )
                return;
            row = selection[0];
            lastRow = selection[selection.length-1];
        }
        col = 0;
        startRow = row;
        startCol = col;
        super.init();
        int count = 0;
        do{
            if ( !rbSearchSelection.isSelected() || table.isRowSelected(row) ){
                if ( table.getValueAt( row, col ) != null && matcher.find() ){
                    count++;
                    String s = matcher.replaceAll( getReplacement() );
                    table.setValueAt( string2ModelObject(s, row, col),
                            row, col );
                }
            }
        } while ( nextCell() && row < lastRow+1 );
        invalidState = true;
    }
    
    protected boolean matchIsInSelection() throws IllegalStateException{
        if ( rbSearchSelection.isEnabled() && rbSearchSelection.isSelected() ){
            return table.isRowSelected( row );
        } else
            return true;
    }
    
    /**
     * iterates through the table cells, returns false if the cell at (startRow, startCol)
     * has been reached again, else returns true.
     */
    protected boolean nextCell(){
        row += col==table.getColumnCount()-1?1:0;
        if ( row == table.getRowCount() ){
            row = 0;
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("search wrapped");
        }
        col = ++col % table.getColumnCount();
        if ( !(row == startRow && col == startCol) ){
            super.init();
            return true;
        } else {
            super.init();
            return false;
        }
        
    }
}
