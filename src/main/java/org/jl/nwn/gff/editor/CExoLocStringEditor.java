package org.jl.nwn.gff.editor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.swing.table.StringTableCellEditor;

public class CExoLocStringEditor extends JPanel {
    private GffCExoLocString locString = new GffCExoLocString("new");

    public Box labelBox = new Box( BoxLayout.X_AXIS );
    Box substringBox = new Box(BoxLayout.Y_AXIS);
    JTextField strRefField = new JTextField(6);
    JTextField labelField = new JTextField(16);

    private AbstractTableModel model = new AbstractTableModel(){
        @Override
        public int getColumnCount(){
            return 3;
        }
        @Override
        public String getColumnName(int column){
            switch (column){
                case 0 : return "language";
                case 1 : return "gender";
                default : return "string";
            }
        }
        @Override
        public int getRowCount(){
            return locString.getSubstringCount();
        }
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex){
            return columnIndex == 2;
        }
        @Override
        public Object getValueAt( int row, int column ){
            CExoLocSubString s = locString.getSubstring( row );
            switch (column) {
                case 0 : return s.language;
                case 1 : return s.gender==0? "masc." : "fem.";
                default : return s.string;
            }
        }
        @Override
        public void setValueAt( Object o, int row, int column ){
            locString.getSubstring( row ).string = o.toString();
        }
    };
    JTable substringTable = new JTable(model);

    JDialog newSubStringDialog = new JDialog(){
        final JComboBox<String> cbGender = new JComboBox<>( new String[]{"masculine / neutral", "feminine"} );
        final JComboBox<NwnLanguage> cbLanguage = new JComboBox<>(NwnLanguage.LANGUAGES);
        ItemListener il = new ItemListener(){
            @Override
            public void itemStateChanged( ItemEvent e ){
                if ( e.getStateChange() == ItemEvent.SELECTED ){
                    actOK.setEnabled( locString.getSubstring( (NwnLanguage)cbLanguage.getSelectedItem(), cbGender.getSelectedIndex() )==null );
                }
            }
        };
        Action actOK = new AbstractAction("OK"){
            @Override
            public void actionPerformed( ActionEvent e ){
                CExoLocSubString sub = new CExoLocSubString("", (NwnLanguage)cbLanguage.getSelectedItem(), cbGender.getSelectedIndex());
                locString.addSubstring( sub );
                int row = locString.getChildIndex(sub);
                //model.fireTableRowsInserted(row, row);
                model.fireTableDataChanged();
                fireStateChanged();
                actOK.setEnabled( false );
                setVisible(false);
                dispose();
            }
        };
        Action actCancel = new AbstractAction("Cancel"){
            @Override
            public void actionPerformed( ActionEvent e ){
                setVisible(false);
                dispose();
            }
        };
        JButton btnOK = new JButton(actOK);
        JButton btnCancel = new JButton(actCancel);
        {
            setTitle( "new substring" );
            setModal( true );
            cbGender.addItemListener( il );
            cbLanguage.addItemListener( il );
            Box b = new Box( BoxLayout.Y_AXIS );
            b.add( cbLanguage );
            b.add( cbGender );
            b.add( btnOK );
            b.add( btnCancel );
            getContentPane().add(b);
            getRootPane().setDefaultButton(btnOK);
            KeyStroke ksEsc = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0);
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put( ksEsc, "escape" );
            getRootPane().getActionMap().put("escape", actCancel);
            //pack();
        }

        @Override
        public void setVisible(boolean b){
            if (b){
                actOK.setEnabled( locString.getSubstring( (NwnLanguage)cbLanguage.getSelectedItem(), cbGender.getSelectedIndex() )==null );
                pack();
                setLocation( strRefField.getLocationOnScreen() );
                //validate();
                super.setVisible(b);
            }
            else{
                super.setVisible(b);
                dispose();
            }
        }
    };

    public CExoLocStringEditor( GffCExoLocString s ){
        this();
        setCExoLocString( s );
    }

    public CExoLocStringEditor(){
        setLayout( new BorderLayout() );
        labelBox.add( new JLabel("label ") );
        labelBox.add( labelField );

        Box topBox = new Box( BoxLayout.X_AXIS );
        topBox.add(labelBox);
        topBox.add( new JLabel("StrRef ") );
        topBox.add( strRefField );
        JButton btnNew = new JButton( actNew );
        btnNew.setToolTipText( "add new substring" );
        btnNew.setMnemonic('n');
        topBox.add( btnNew );
        JButton btnRemove = new JButton( actRemove );
        btnRemove.setToolTipText( "remove selected substrings" );
        btnRemove.setMnemonic('d');
        topBox.add( btnRemove );

        StringTableCellEditor cellEditor =
                new StringTableCellEditor(){
            @Override
            public boolean stopCellEditing(){
                substringTable.setRowHeight( substringTable.getEditingRow(), substringTable.getRowHeight() );//default row height is 16
                super.stopCellEditing();
                substringTable.requestFocus();
                return true;
            }
            @Override
            public void cancelCellEditing() {
                substringTable.setRowHeight( substringTable.getEditingRow(), substringTable.getRowHeight() );//default row height is 16
                super.cancelCellEditing();
                substringTable.requestFocus();
            }
        };
        substringTable.getColumnModel().getColumn(2).setCellEditor( cellEditor );
        substringTable.setSurrendersFocusOnKeystroke(true);

        KeyStroke ksEnter = KeyStroke.getKeyStroke( KeyEvent.VK_ENTER, 0 );
        labelField.getInputMap( JComponent.WHEN_FOCUSED ).put( ksEnter, "setdata" );
        labelField.getActionMap().put("setdata", actEnter);
        strRefField.getInputMap( JComponent.WHEN_FOCUSED ).put( ksEnter, "setdata" );
        strRefField.getActionMap().put("setdata", actEnter);

        add( topBox, BorderLayout.NORTH );
        add( new JScrollPane(substringTable), BorderLayout.CENTER );

        setPreferredSize( new Dimension(500,120) );
        substringTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN );
        substringTable.getColumnModel().getColumn(0).setMinWidth(80);
        substringTable.getColumnModel().getColumn(0).setMaxWidth(80);
        substringTable.getColumnModel().getColumn(1).setMinWidth(60);
        substringTable.getColumnModel().getColumn(1).setMaxWidth(60);
        substringTable.getColumnModel().getColumn(1).setWidth(60);
        substringTable.getColumnModel().getColumn(2).setWidth(300);
    }

    Action actEnter = new AbstractAction("OK"){
        @Override
        public void actionPerformed( ActionEvent e ){
            try {
                locString.setStrRef( Integer.parseInt( strRefField.getText() ) );
            } catch ( NumberFormatException nfe ){
                strRefField.setText( Integer.toString( locString.getStrRef() ) );
            }

            locString.setLabel( labelField.getText() );
            fireStateChanged();
        }
    };

    Action actNew = new AbstractAction("new"){
        @Override
        public void actionPerformed( ActionEvent e ){
            newSubStringDialog.setVisible(true);
        }
    };

    Action actRemove = new AbstractAction("del"){
        @Override
        public void actionPerformed( ActionEvent e ){
            int[] selection = substringTable.getSelectedRows();
            for ( int i = selection.length - 1; i > -1; i-- )
                locString.removeSubstring( selection[i] );
            model.fireTableDataChanged();
            fireStateChanged();
        }
    };

    public void setCExoLocString( GffCExoLocString s ){
        locString = s;
        strRefField.setText( Integer.toString(s.getStrRef()) );
        labelField.setText( s.getLabel() );
        model.fireTableDataChanged();
        fireStateChanged();
    }

    protected List<ChangeListener> changeListeners = Collections.synchronizedList(new ArrayList<>());
    public void addChangeListener( ChangeListener cl ){
        if ( !changeListeners.contains( cl ) )
            changeListeners.add( cl );
    }

    public void removeChangeListener( ChangeListener cl ){
        changeListeners.remove( cl );
    }

    protected void fireStateChanged(){
        final ChangeEvent e = new ChangeEvent(locString);
        for (final ChangeListener l : changeListeners) {
            l.stateChanged(e);
        }
    }
}
