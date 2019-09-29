package org.jl.nwn.twoDa.cellEditors;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MappedCellEditor extends DefaultCellEditor {

    protected Map<String, String> map;
    private Map<String, String> invMap;
    private JComboBox<?> comboBox;

	private Action aAbort = new AbstractAction(){
		@Override
		public void actionPerformed( ActionEvent e ){
			cancelCellEditing();
		}
	};

	public MappedCellEditor(){
        super( new JComboBox<>() );
		comboBox = ( JComboBox ) editorComponent;
		comboBox.setEditable( true );
		comboBox.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );
		comboBox.getInputMap( JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ).put( KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), "cancel" );
		comboBox.getActionMap().put( "cancel", aAbort );
	}

	public MappedCellEditor( String[] values, String[] labels ){
		this();
		setup( values, labels );
	}

	private void setup( String[] values, String[] labels ){
		map = buildMap( values, labels );
		invMap = buildMap( labels, values );
		comboBox.setModel( new DefaultComboBoxModel( labels ) );
	}

    public static Map<String, String> buildMap(String[] values, String[] labels) {
        final TreeMap<String, String> map = new TreeMap<>( String.CASE_INSENSITIVE_ORDER );
		for ( int i = 0; i < values.length; i++ )
			map.put( values[i], labels[i] );
        return map;
	}

	@Override
	public Object getCellEditorValue(){
        final String o = invMap.get( super.getCellEditorValue() );
		return o==null? comboBox.getSelectedItem() : o;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
        final String o = map.get( value );
		//comboBox.setSelectedItem( o==null? value : o );
		//return comboBox;
		Component r = super.getTableCellEditorComponent( table, o==null?value:o, isSelected, row, column );
                comboBox.getEditor().selectAll();
                return r;
	}

	public MappedCellEditor( Element e ){
		this();
		boolean editable = false;
		Node n = e.getAttributes().getNamedItem( "editable" );
		if ( n!=null )
			editable = Boolean.valueOf( n.getNodeValue() ).booleanValue();
		NodeList entries = e.getElementsByTagName( "entry" );
		String[] values = new String[ entries.getLength() ];
		String[] labels = new String[ entries.getLength() ];
		for ( int entryNum = 0; entryNum < entries.getLength(); entryNum++ ){
			Element entryNode = (Element) entries.item( entryNum );
			values[ entryNum ] = entryNode.getAttributes().getNamedItem( "value" ).getNodeValue();
			labels[ entryNum ] = entryNode.getAttributes().getNamedItem( "label" ).getNodeValue();
		}
		setup( values, labels );
		comboBox.setEditable( editable );
	}
}
