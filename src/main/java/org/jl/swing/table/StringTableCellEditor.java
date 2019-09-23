/*
 * Created on 04.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.swing.table;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.text.BadLocationException;
import javax.swing.undo.UndoManager;

import org.jl.swing.ActionListenerAction;

/**
 * Table cell editor for long strings. Uses a JScrollPane. Line breaks can be entered with <code>Enter</code>, editing is stopped with <code>Alt+Enter</code>.
 */
public class StringTableCellEditor
        extends AbstractCellEditor
        implements TableCellEditor, TextCellEditor{

    protected UndoManager undoManager = new UndoManager();
    protected JTextArea textArea = new JTextArea();
    protected JScrollPane scroll = new JScrollPane(textArea);

    public StringTableCellEditor(){
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.getDocument().addUndoableEditListener( undoManager );
        scroll.setHorizontalScrollBarPolicy(
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.addFocusListener( new FocusAdapter(){
            @Override
            public void focusGained(FocusEvent e){
                textArea.requestFocus();
            }
        } );
        InputMap im = textArea.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );

        Action aCancel = new ActionListenerAction(this, "cancelCellEditing", null);
        textArea.getActionMap().put( "cancel", aCancel );
        im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );

        Action aUndo = new ActionListenerAction(this, "undo", null);
        textArea.getActionMap().put( "undo", aUndo );
        im.put( KeyStroke.getKeyStroke( KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK ), "undo" );

        Action aRedo = new ActionListenerAction(this, "redo", null);
        textArea.getActionMap().put( "redo", aRedo );
        im.put( KeyStroke.getKeyStroke( KeyEvent.VK_Z, KeyEvent.SHIFT_DOWN_MASK|KeyEvent.CTRL_DOWN_MASK ), "redo" );

        Action aEndEdit = new ActionListenerAction( this, "stopCellEditing", null );
        textArea.getActionMap().put( "endEdit", aEndEdit );
        im.put( KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, KeyEvent.ALT_DOWN_MASK ), "endEdit" );
    }

    public void undo(){
        if ( undoManager.canUndo() )
            undoManager.undo();
    }

    public void redo(){
        if ( undoManager.canRedo() )
            undoManager.redo();
    }

    public JTextArea getTextArea(){
        return textArea;
    }

    @Override
    public java.awt.Component getTableCellEditorComponent(
            JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        try {
            textArea.getDocument().remove(0, textArea.getDocument().getLength());
            textArea.getDocument().insertString(0,value.toString(),null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
        //textArea.setText(value.toString());
        undoManager.discardAllEdits();
        /*
        Dimension d = new Dimension(
                scroll.getWidth(),
                Math.min( scroll.getHeight(), table.getHeight() ) );
         */
        //scroll.setMaximumSize(d);
        /*
        table.setRowHeight(
                row,
                Math.max(
                Math.min(
                table.getHeight() / 2,
                scroll.getPreferredSize().height),
                80));
        */
        //System.out.println(table.getVisibleRect().y);
        table.setRowHeight(
                row,
                (int) Math.min( 300,
                Math.max( 80, scroll.getPreferredSize().height )));
        scroll.scrollRectToVisible( scroll.getBounds() );
        return scroll;
    }

    @Override
    public Object getCellEditorValue() {
        return textArea.getText();
    }

    // copied from DefaultCellEditor, double click to start editing
    //this is sometimes called with a null argument ?!?
    @Override
    public boolean isCellEditable(java.util.EventObject anEvent) {
        if (anEvent instanceof MouseEvent) {
            return ((MouseEvent) anEvent).getClickCount() >= 2;
        }
        //System.out.println( "isCellEditable : " + anEvent );
        if (anEvent instanceof InputEvent) {
            //System.out.println( "InputEvent" );
            return 0
                    == (((InputEvent) anEvent).getModifiersEx()
                    & InputEvent.ALT_DOWN_MASK);
        }
        return true;
    }

    @Override
    public boolean stopCellEditing() {
        fireEditingStopped();
        return true;
    }

    @Override
    public JTextArea getTextComponent(){
        return textArea;
    }
}
