package org.jl.nwn.twoDa.cellEditors;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class MappedTableCellRenderer extends DefaultTableCellRenderer {

    private Map<String, String> map;
	public Object unknownValue = "???";
	private Color defaultForeground = getForeground();

    public MappedTableCellRenderer(Map<String, String> map) {
		super();
		this.map = map;
	}

	public MappedTableCellRenderer( MappedCellEditor ed ){
		this( ed.map );
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
		Object o = map.get(value);
		setForeground( defaultForeground );
		if ( o == null ){
			o = value;
			setForeground( Color.RED );
		}
		return super.getTableCellRendererComponent( table, o, isSelected, hasFocus, row, column );
	}

	public MappedTableCellRenderer( Element e ){
		super();
		NodeList entries = e.getElementsByTagName( "entry" );
		String[] values = new String[ entries.getLength() ];
		String[] labels = new String[ entries.getLength() ];
		for ( int entryNum = 0; entryNum < entries.getLength(); entryNum++ ){
			Element entryNode = (Element) entries.item( entryNum );
			values[ entryNum ] = entryNode.getAttributes().getNamedItem( "value" ).getNodeValue();
			labels[ entryNum ] = entryNode.getAttributes().getNamedItem( "label" ).getNodeValue();
		}
		map = MappedCellEditor.buildMap( values, labels );
	}
}
