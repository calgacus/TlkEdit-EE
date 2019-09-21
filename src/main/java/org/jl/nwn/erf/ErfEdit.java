/*
 * Created on 30.12.2003
 */
package org.jl.nwn.erf;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import org.jdesktop.swingx.JXTable;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

import org.jl.nwn.editor.SimpleFileEditorPanel;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.editor.CExoLocStringEditor;
import org.jl.swing.FileDropHandler;
import org.jl.swing.UIDefaultsX;
import org.jl.nwn.resource.ResRefUtil;
import org.jl.nwn.resource.ResourceID;
import org.jl.swing.Actions;
import org.jl.swing.I18nUtil;
import org.jl.swing.table.FormattedCellEditor;

/**
 */
public class ErfEdit extends SimpleFileEditorPanel{

    private ErfFile erf;

    private DefaultListModel model = new DefaultListModel();
    private JToolBar toolbar = new JToolBar();
    private JMenuBar mbar = new JMenuBar();
    private JMenu menuFile = new JMenu("File");
    private JMenu menuErf = new JMenu();
    private JSplitPane sPane;

    private JComboBox cbTypeSelector = new JComboBox(ErfFile.ERFTYPES.toArray());
    private CExoLocStringEditor descEditor = new CExoLocStringEditor();
    private Box descriptionBox = new Box(BoxLayout.Y_AXIS);

    private JFileChooser fChooser = new JFileChooser( new File(".") );

    private static final UIDefaultsX uid = new UIDefaultsX();

    private static FileFilter fFilterErf = new FileFilter(){
        public boolean accept( File f ){
            String s = f.getName().toLowerCase();
            return ( !f.isFile() || s.endsWith(".mod") || s.endsWith(".hak") || s.endsWith(".sav") || s.endsWith(".erf") || s.endsWith(".nwm") );
        }
        public String getDescription(){
            return "erf files";
        }
    };

    static{
        uid.addResourceBundle("org.jl.nwn.erf.uidefaults");
        uid.addResourceBundle("settings.keybindings");
    }

    private AbstractTableModel tableModel = new AbstractTableModel(){

        public String getColumnName(int column){
            switch (column){
                case 0 : return "name";
                case 1 : return "type";
                default : return "size";
            }
        }

        public int getColumnCount(){
            return 3;
        }
        public int getRowCount(){
            return model.size();
        }
        public Object getValueAt( int row, int column ){
            ResourceID id = (ResourceID) model.get(row);
            switch (column){
                case 0 : return id.getName();
                case 1 : {
                    String ext = ResourceID.getExtensionForType( id.getType() );
                    return ext == null ? toHex(id.getType()) : ext;
                }
                case 2 : return erf.getResourceSize( id );
            }
            return null;
        }

        private String toHex(short s){
            String hex = Integer.toHexString(s);
            return "0x0000".substring(0, 6-hex.length())+hex;
        }

        public boolean isCellEditable(int rowIndex, int columnIndex){
            return columnIndex == 0;
        }

        public void setValueAt(Object aValue, int rowIndex, int columnIndex){
            if ( columnIndex == 0 ){
                if ( !getValueAt(rowIndex, columnIndex ).equals(aValue.toString()) ){
                    try{
                        String name =
                                ResRefUtil.instance(erf.getVersion())
                                .parseString( aValue.toString() );
                        ResourceID id = erf.renameResource( (ResourceID) model.get(rowIndex), name );
                        redoList();
                        int s = model.indexOf(id);
                        tableModel.fireTableDataChanged();
                        table.setRowSelectionInterval(s,s);
                    } catch (ParseException pex){
                        // should not happen since ResRef editor is used
                        pex.printStackTrace();
                    }
                }
            }
        }

        public void fireTableChanged( TableModelEvent e ){
            super.fireTableChanged(e);
            setIsModified(true);
        }
    };
    private JXTable table = new JXTable( tableModel );

    {
        toolbar.setFloatable(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        table.getColumnModel().getColumn(0).setMinWidth(200);
        cbTypeSelector.addItemListener( new ItemListener(){
            public void itemStateChanged( ItemEvent e ){
                if ( e.getStateChange() == ItemEvent.SELECTED ){
                    ErfFile.ErfType type = (ErfFile.ErfType) cbTypeSelector.getSelectedItem();
                    erf.setType( type );
                    descriptionBox.setVisible( type != ErfFile.ERF );
                    sPane.setDividerLocation(sPane.getDividerLocation());
                    //sPane.setEnabled( type != ErfFile.ERF );
                }
            }
        });
        table.setDefaultRenderer( Object.class, new DefaultTableCellRenderer(){
            Color defaultColor = getBackground();
            public Component getTableCellRendererComponent(JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column){
                setBackground( defaultColor );
                Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
                if ( erf.isFileResource((ResourceID)model.get(((JXTable)table).convertRowIndexToModel(row))))
                    c.setBackground( isSelected? Color.ORANGE:Color.YELLOW );
                setHorizontalAlignment( column==2?JLabel.TRAILING:JLabel.LEADING );
                return c;
            }
        });
        descEditor.addChangeListener( new ChangeListener(){
            public void stateChanged( ChangeEvent e ){
                setIsModified(true);
            }
        } );
    }

    public ErfEdit( Version nwnVersion ){
        super();
        GffCExoLocString desc = new GffCExoLocString( "erf_desc" );
        descEditor.setCExoLocString( desc );
        CExoLocSubString s = new CExoLocSubString(
                "<HAK NAME>\n<URL>\n<DESCRIPTION>",
                NwnLanguage.ENGLISH,
                Gff.GENDER_MALE
                );
        desc.addSubstring(s);
        erf = new ErfFile( new File("new hak"), ErfFile.HAK, desc, nwnVersion );
        setLayout( new BorderLayout() );
        //add( new JScrollPane(list), BorderLayout.CENTER );
        sPane = new JSplitPane( JSplitPane.VERTICAL_SPLIT );
        sPane.setOneTouchExpandable(true);
        //add( new JScrollPane(table), BorderLayout.CENTER );
        //add( descEditor, BorderLayout.SOUTH );
        sPane.add( new JScrollPane( table ) );
        JLabel descLabel = new JLabel();
        I18nUtil.setText(descLabel, "MOD / HAK Description" );
        descLabel.setLabelFor(descEditor);
        descriptionBox.add( descLabel );
        descriptionBox.add( descEditor );
        sPane.add( descriptionBox );
        sPane.setResizeWeight( 0.8 );
        add( sPane, BorderLayout.CENTER );
        descEditor.labelBox.setVisible(false);
        cbTypeSelector.setSelectedItem( erf.getType() );
        actSave.setEnabled(false);

        //toolbar.add( actSave );
        Actions.configureActionUI(actAddFiles,uid,"ErfEdit.add");
        Actions.configureActionUI(actRemove,uid,"ErfEdit.remove");
        Actions.configureActionUI(actExtractSelected,uid,"ErfEdit.extract");
        JButton addButton = toolbar.add( actAddFiles );
        JButton rmButton = toolbar.add( actRemove );
        toolbar.add( new JToolBar.Separator() );
        JButton exButton = toolbar.add( actExtractSelected );
        addButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        rmButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        exButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        I18nUtil.setText(menuErf, uid.getString("ErfEdit.erfMenuTitle"));
        menuErf.add(actAddFiles);
        menuErf.add(actRemove);
        menuErf.addSeparator();
        menuErf.add(actExtractSelected);

        toolbar.add( new JToolBar.Separator() );
        JLabel typeSelectorLabel = new JLabel();
        I18nUtil.setText( typeSelectorLabel, "Erf &Type" );
        typeSelectorLabel.setLabelFor( cbTypeSelector );
        toolbar.add( typeSelectorLabel );
        toolbar.add(cbTypeSelector);
        cbTypeSelector.setAlignmentX( JComponent.RIGHT_ALIGNMENT );
        toolbar.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        toolbar.add( Box.createHorizontalGlue() );

        mbar.add(menuFile);
        mbar.setAlignmentX( JComponent.LEFT_ALIGNMENT );
        menuFile.add( actNew );
        menuFile.add( actOpen );
        menuFile.add( actSave );
        menuFile.add( actSaveAs );

        // need to set all keybindings explicitly, because some may already be
        // used by swing L&F ( like 'control C' for copy
        Action[] editActions = { actAddFiles, actRemove, actExtractSelected };
        InputMap im = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        for ( Action a : editActions ){
            im.put((KeyStroke)a.getValue( a.ACCELERATOR_KEY ), a.getValue( a.ACTION_COMMAND_KEY ));
            am.put(a.getValue( a.ACTION_COMMAND_KEY ), a );
        }


        //Box b = new Box( BoxLayout.Y_AXIS );
        //b.add(mbar);
        //b.add(toolbar);

        //add( toolbar, BorderLayout.NORTH );
        //add( b, BorderLayout.NORTH );
        setFileVersion( erf.getVersion() );
        setTransferHandler(fileDropHandler);
        showToolbar(true);
        setVisible( true );
        setIsModified(false);
    }

    public ErfEdit( File erfFile ) throws IOException{
        this( Version.getDefaultVersion() );
        open( erfFile );
        setIsModified(false);
        setFileVersion( erf.getVersion() );
    }

    private void redoList(){
        model.clear();
        Iterator it = erf.getResourceIDs().iterator();
        while ( it.hasNext() )
            model.addElement( it.next() );
        tableModel.fireTableDataChanged();
    }

    private Action actAddFiles = new AbstractAction(){
        public void actionPerformed( ActionEvent e ){
            fChooser.setMultiSelectionEnabled(true);
            fChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
            if ( fChooser.showOpenDialog( table ) == JFileChooser.APPROVE_OPTION ){
                File[] files = fChooser.getSelectedFiles();
                for ( int i = 0; i < files.length; i++ )
                    erf.putResource( files[i] );
                redoList();
            }
        }
    };

    protected TransferHandler fileDropHandler = new FileDropHandler() {
        public void importFiles(List<File> files) {
            for ( File f : files )
                erf.putResource(f);
            redoList();
        }
    };

    private Action actSave = new AbstractAction( "save" ){
        public void actionPerformed( ActionEvent e ){
            try{
                save();
            } catch ( IOException ioex ){
                JOptionPane.showMessageDialog( table, ioex, "error : save failed", JOptionPane.ERROR_MESSAGE );
            }
        }
    };

    private Action actNew = new AbstractAction( "new erf" ){
        public void actionPerformed( ActionEvent e ){
            GffCExoLocString desc = new GffCExoLocString( "erf_desc" );
            erf = new ErfFile( new File("new erf"), ErfFile.ERF, desc  );
            redoList();
            actSave.setEnabled(false);
        }
    };

    private Action actSaveAs = new AbstractAction( "save as" ){
        public void actionPerformed( ActionEvent e ){
            fChooser.setMultiSelectionEnabled(false);
            fChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
            if ( fChooser.showOpenDialog( table ) == JFileChooser.APPROVE_OPTION )
                try{
                    saveAs( fChooser.getSelectedFile(), erf.getVersion() );
                    actSave.setEnabled( true );
                } catch ( IOException ioex ){
                    JOptionPane.showMessageDialog( table, ioex, "error : save failed", JOptionPane.ERROR_MESSAGE );
                }
        }
    };

    private Action actOpen = new AbstractAction( "open" ){
        public void actionPerformed( ActionEvent e ){
            fChooser.setMultiSelectionEnabled(false);
            fChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
            fChooser.setFileFilter( fFilterErf );
            if ( fChooser.showOpenDialog( table ) == JFileChooser.APPROVE_OPTION )
                try{
                    open( fChooser.getSelectedFile() );
                } catch ( IOException ioex ){
                    JOptionPane.showMessageDialog( table, ioex, "error : open failed", JOptionPane.ERROR_MESSAGE );
                }
        }
    };

    private Action actRemove = new AbstractAction(){
        public void actionPerformed( ActionEvent e ){
            int[] selection = table.getSelectedRows();
            for ( int i = selection.length-1; i>-1; i-- ){
                ResourceID id = ( ResourceID ) model.get( table.convertRowIndexToModel(selection[i]) );
                erf.remove( id );
                model.remove(  table.convertRowIndexToModel(selection[i]) );
                tableModel.fireTableRowsDeleted(selection[0],selection[selection.length-1]);
            }
        }
    };

    /**
     * action for extracting files to a directory, overwrites existing files
     * */
    private Action actExtractSelected = new AbstractAction(){
        private JButton btnOK = new JButton(
                new AbstractAction( "OK" ){
            public void actionPerformed( ActionEvent e ){
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
        );
        private JProgressBar pBar = new JProgressBar();
        private JDialog dialog = new JDialog(){
            {
                setTitle("extracting files...");
                getContentPane().setLayout( new BorderLayout() );
                getContentPane().add( pBar, BorderLayout.CENTER );
                getContentPane().add( btnOK, BorderLayout.SOUTH );
                pBar.setStringPainted(true);
                pBar.setVisible(true);
                //pack();
            }
        };
        public void actionPerformed( ActionEvent e ){
            final int[] selection = table.getSelectedRows();
            if ( selection.length > 0 ){
                fChooser.setMultiSelectionEnabled(false);
                fChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                if ( fChooser.showDialog( table, "extract here" ) == JFileChooser.APPROVE_OPTION ){
                    final File outputDir = fChooser.getSelectedFile();
                    new Thread(){
                        public void run(){
                            pBar.setMinimum( 0 );
                            pBar.setMaximum( selection.length );
                            dialog.pack();
                            dialog.setVisible( true );
                            dialog.setLocation( toolbar.getLocationOnScreen() );
                            btnOK.setEnabled( false );
                            try{
                                for ( int i = 0; i < selection.length; i++ ){
                                    ResourceID id = (ResourceID) model.get(table.convertRowIndexToModel(selection[i]));
                                    pBar.setValue( i );
                                    pBar.setString( id.toFileName() );
                                    erf.extractToDir( id, outputDir );
                                }
                            } catch ( IOException ioex ){
                                JOptionPane.showMessageDialog( table, ioex, "error : file extraction failed", JOptionPane.ERROR_MESSAGE );
                            } finally{
                                btnOK.setEnabled(true);
                            }
                        }
                    }.start();
                }
            }
        }
    };

    // setup version dependant stuff
    private void setFileVersion( Version v ){
        TableCellEditor resRefEd =
                new FormattedCellEditor(
                new JFormattedTextField(
                ResRefUtil.instance(erf.getVersion())
                .getStringFormatter(false)));
        table.getColumnModel().getColumn(0).setCellEditor( resRefEd );
    }

    @Override public Version getFileVersion(){
        return erf.getVersion();
    }

    public void open( File f ) throws IOException{
        erf = new ErfFile(f);
        cbTypeSelector.setSelectedItem( erf.getType() );
        //cbTypeSelector.setEnabled(false);
        actSave.setEnabled(true);
        descEditor.setCExoLocString( erf.getDescription() );
        redoList();
    }

    public void save() throws IOException{
        erf.write();
        setIsModified(false);
    }

    public void saveAs(File f, Version nwnVersion) throws IOException{
        erf.write(f);
        actSave.setEnabled(true);
        setIsModified(false);
    }

    public static void main( String[] args ) throws IOException{
        JFrame f = new JFrame(args[0]);
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.getContentPane().add( new ErfEdit( new File( args[0] ) ) );
        f.pack();
        f.setVisible( true );
    }

    /**
     * @throws IOException
     */
    public void close(){
        try{
            erf.close();
        } catch ( IOException ioex ){
            ioex.printStackTrace();
        }
    }

    public static boolean accept( File f ){
        return f.isFile() && fFilterErf.accept(f);
    }

    public File getFile() {
        return erf.getFile();
    }

    public boolean canSave(){
        return actSave.isEnabled();
    }

    public boolean canSaveAs(){
        return true;
    }

    public JMenu[] getMenus(){
        return new JMenu[]{menuErf};
    }

    public File extractAsTempFile(ResourceID id, boolean replaceWithFile)
    throws IOException {
        return erf.extractAsTempFile(id, replaceWithFile);
    }

    /**
     * @param id
     * @param file
     */
    public void putResource(ResourceID id, File file) {
        erf.putResource(id, file);
    }

    public ResourceID[] getSelectedResources(){
        int[] selection = table.getSelectedRows();
        ResourceID[] ids = new ResourceID[ selection.length ];
        for ( int i = 0; i < selection.length; i++ )
            ids[i] = (ResourceID) model.get( table.convertRowIndexToModel(selection[i]) );
        return ids;
    }

    public JToolBar getToolbar() {
        return toolbar;
    }

    public void showToolbar(boolean b) {
        if ( b )
            add(toolbar, java.awt.BorderLayout.NORTH);
        else
            remove( toolbar );
    }

}
