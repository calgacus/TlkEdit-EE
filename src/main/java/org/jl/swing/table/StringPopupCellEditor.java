package org.jl.swing.table;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.undo.UndoManager;

import org.jl.swing.ActionListenerAction;

/**
 * Table cell editor that uses a popup dialog to display/edit long text.
 */
public class StringPopupCellEditor extends AbstractCellEditor implements TableCellEditor, TextCellEditor{
    JDialog popup;
    JLabel dummy = new JLabel(){
        @Override
        public void requestFocus(){
            textArea.requestFocus();
        }
    };
    final protected JTextArea textArea = new JTextArea();
    protected UndoManager undoManager = new UndoManager();

    public StringPopupCellEditor(){
        textArea.setEditable(true);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        dummy.setLabelFor(textArea);
        textArea.getDocument().addUndoableEditListener( undoManager );

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

    private void init( JTable table ){
        popup = new JDialog((JFrame) SwingUtilities.getWindowAncestor(table));
        popup.setUndecorated(true);
        popup.getContentPane().add(new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
        //popup.pack();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        if (popup == null)
            init(table);
        textArea.setText( value.toString() );
        undoManager.discardAllEdits();
        Rectangle r = table.getCellRect(row, column, false);
        Point p = table.getLocationOnScreen();
        popup.setLocation(p.x + r.x, p.y + r.y);
        int height = Math.max( 100, textArea.getPreferredSize().height );
        popup.pack();
        textArea.setSize(r.width, height);
        popup.setSize(r.width, height);
        textArea.requestFocusInWindow();
        popup.setVisible( true );
        return dummy;
    }

    @Override
    public boolean stopCellEditing(){
        popup.setVisible( false );
        popup.dispose();
        fireEditingStopped();
        return true;
    }

    @Override
    public void cancelCellEditing(){
        popup.setVisible( false );
        popup.dispose();
        super.cancelCellEditing();
    }

    @Override
    public Object getCellEditorValue(){
        return textArea.getText();
    }

    @Override
    public JTextArea getTextComponent(){
        return textArea;
    }

    @Override protected void finalize() throws Throwable{
        if ( popup != null ) popup.dispose();
    }
}
