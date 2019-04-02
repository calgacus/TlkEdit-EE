/*
 * Created on 28.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.twoDa.cellEditors;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ComboBoxCellEditor extends DefaultCellEditor {
	
	JComboBox comboBox;
	
	public ComboBoxCellEditor(){
		super( new JComboBox() );
		comboBox = (JComboBox) super.editorComponent;
		comboBox.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );
		comboBox.getActionMap().put( "cancel", new AbstractAction(){ public void actionPerformed( ActionEvent e ){ cancelCellEditing(); } } );
	}
	
	public ComboBoxCellEditor( JComboBox b ){
		super( b );
	}        

	public ComboBoxCellEditor( Element e ){
		this();
		boolean editable = false;
		Node n = e.getAttributes().getNamedItem( "editable" );
		if ( n!=null )
			editable = Boolean.valueOf( n.getNodeValue() ).booleanValue();
		NodeList entries = e.getElementsByTagName( "entry" );
		String[] values = new String[ entries.getLength() ];
		for ( int entryNum = 0; entryNum < entries.getLength(); entryNum++ ){
			Element entryNode = (Element) entries.item( entryNum );
			values[ entryNum ] = entryNode.getAttributes().getNamedItem( "value" ).getNodeValue();
		}
		comboBox.setModel( new DefaultComboBoxModel( values ) );
		comboBox.setEditable( editable );
	}

	public static void main(String[] args) {
	}

    @Override public java.awt.Component getTableCellEditorComponent(javax.swing.JTable table, Object value, boolean isSelected, int row, int column) {
        java.awt.Component retValue;
        retValue = super.getTableCellEditorComponent(table, value, isSelected, row, column);
        comboBox.getEditor().selectAll();
        return retValue;
    }
}
