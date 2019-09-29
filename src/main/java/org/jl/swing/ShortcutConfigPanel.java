package org.jl.swing;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;

public class ShortcutConfigPanel extends JPanel {

	public ShortcutConfigPanel( String[] keys, KeyStroke[] keystrokes ){
		super();
		test = keys;
		for ( int i = 0; i < keys.length; i++ )
			map.put( keys[i], keystrokes[i] );
		table.setModel(model);
		table.addKeyListener( kl );


		setLayout( new BorderLayout() );
		add( table, BorderLayout.CENTER );
	}

	String[] test = new String[]{
			"TlkEdit.copy_buttonLabel",
			"TlkEdit.cut_buttonLabel",
			"TlkEdit.paste_buttonLabel" };

    TreeMap<String, KeyStroke> map = new TreeMap<>();

	JTable table = new JTable();

	KeyListener kl = new KeyAdapter(){
		@Override
		public void keyPressed(KeyEvent e){
			System.out.println(e);
			int row = table.getSelectedRow();
			map.put( test[row], KeyStroke.getKeyStrokeForEvent(e) );
			model.fireTableCellUpdated( row, 1 );
		}
	};

	AbstractTableModel model = new AbstractTableModel(){
		@Override
		public Object getValueAt( int row, int col ){
			if (col == 0)
				return test[row];
			else {
                final KeyStroke ks = map.get(test[row]);
				return getKeyStrokeText(ks);
			}
		}

		@Override
		public int getColumnCount(){
			return 2;
		}
		@Override
		public int getRowCount(){
			return test.length;
		}
	};

	protected String getKeyStrokeText( KeyStroke ks ){
		String s = ks.getModifiers()!=0 ? KeyEvent.getKeyModifiersText(ks.getModifiers())+"-" : "";
		s += ks.getKeyCode()!=KeyEvent.VK_UNDEFINED?KeyEvent.getKeyText(ks.getKeyCode()) : ""+ks.getKeyChar();
		return s;
	}

	public static void main( String[] args ){
		JFrame f = new JFrame( "test" );
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		KeyStroke[] ks = new KeyStroke[]{
			KeyStroke.getKeyStroke( "control pressed C" ),
			KeyStroke.getKeyStroke( "control pressed X" ),
			KeyStroke.getKeyStroke( "control pressed V" )
		};
		f.getContentPane().add( new ShortcutConfigPanel( new String[]{
				"TlkEdit.copy_buttonLabel",
				"TlkEdit.cut_buttonLabel",
				"TlkEdit.paste_buttonLabel" }, ks ) );
		f.pack();
		f.setVisible(true);
	}
}
