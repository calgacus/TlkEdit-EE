/*
 * Created on 16.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.twoDa.cellEditors;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author ich
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class MappedTableCellRenderer extends DefaultTableCellRenderer {

	private Map map;
	public Object unknownValue = "???";
	private Color defaultForeground = getForeground();
	
	public MappedTableCellRenderer( Map map ){
		super();
		this.map = map;
	}
	
	public MappedTableCellRenderer( MappedCellEditor ed ){
		this( ed.map );
	}
	
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
