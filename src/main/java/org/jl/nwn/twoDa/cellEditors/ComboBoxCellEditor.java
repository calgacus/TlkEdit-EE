package org.jl.nwn.twoDa.cellEditors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ComboBoxCellEditor extends DefaultCellEditor {

    public ComboBoxCellEditor() {
        this(new JComboBox<>());
    }

    public ComboBoxCellEditor(JComboBox<String> comboBox) {
        super(comboBox);
        comboBox.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put(
            KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel"
        );
        comboBox.getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { cancelCellEditing(); }
        });
    }

    public ComboBoxCellEditor(Element e) {
        this();
        final Node n = e.getAttributes().getNamedItem( "editable" );
        final boolean editable = n != null && Boolean.parseBoolean(n.getNodeValue());
        final NodeList entries = e.getElementsByTagName( "entry" );
        final String[] values = new String[ entries.getLength() ];
        for (int i = 0; i < values.length; ++i) {
            final Element entryNode = (Element) entries.item( i );
            values[i] = entryNode.getAttributes().getNamedItem( "value" ).getNodeValue();
        }
        final JComboBox<String> comboBox = (JComboBox<String>) super.editorComponent;
        comboBox.setModel( new DefaultComboBoxModel<>( values ) );
        comboBox.setEditable( editable );
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final Component retValue = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        final JComboBox<String> comboBox = (JComboBox<String>) super.editorComponent;
        comboBox.getEditor().selectAll();
        return retValue;
    }
}
