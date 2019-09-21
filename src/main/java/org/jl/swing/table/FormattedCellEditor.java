package org.jl.swing.table;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.beans.EventHandler;
import java.text.Format;
import java.text.ParseException;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.text.Caret;

/**
 * cell editor that uses a JFormattedText field and can optionally perform bounds checking
 */
public class FormattedCellEditor extends AbstractCellEditor implements TableCellEditor{
    
    //protected Format format;
    protected JFormattedTextField text;
    protected Comparable min;
    protected Comparable max;
    
    /** Creates a new instance of FormattedCellEditor. If min and/or max are not null,
     * the edited value will only be accepted if it is within the specified bounds.
     * min/max must be of the same type as the objects returned by format.parseObject(...)
     */
    public FormattedCellEditor( Format format, Comparable min, Comparable max ){
        super();
        //this.format = format;
        this.min = min;
        this.max = max;
        text = new JFormattedTextField(format);
        text.setFocusLostBehavior(JFormattedTextField.PERSIST);
        Action aStopEditing = EventHandler.create(Action.class, this, "stopCellEditing");
        text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "stopCellEditing");
        text.getActionMap().put("stopCellEditing", aStopEditing);
    }
    
    public FormattedCellEditor( Format format ){
        this(format, null, null);
    }
    
    public FormattedCellEditor( JFormattedTextField text ){
        this.text = text;
        text.setFocusLostBehavior(JFormattedTextField.PERSIST);
        Action aStopEditing = EventHandler.create(Action.class, this, "stopCellEditing");
        text.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), "stopCellEditing");
        text.getActionMap().put("stopCellEditing", aStopEditing);
    }
    
    protected boolean isValid( Object value ){
        return ( max == null || max.compareTo(value) > -1 ) &&
               ( min == null || min.compareTo(value) < 1 );
    }
    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        text.setValue(value);
        int length = text.getDocument().getLength();
        Caret c = text.getCaret();
        c.setSelectionVisible(true);
        c.setDot(0);
        c.moveDot( length );
        return text;
    }
    
    @Override
    public Object getCellEditorValue(){
        return text.getValue();
    }
        
    @Override
    public boolean stopCellEditing(){
        try{
            text.commitEdit();
        } catch ( ParseException pe ){
            
        }
        if ( text.isEditValid() && isValid(text.getValue())) {
            //System.out.println(text.getValue());
            fireEditingStopped();
            return true;
        } else
            return false;
    }
    
    public void setBounds(Comparable min, Comparable max){
        this.min = min;
        this.max = max;
    }
}
