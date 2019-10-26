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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import org.jl.nwn.Version;
import org.jl.nwn.editor.SimpleFileEditorPanel;
import org.jl.nwn.gff.editor.CExoLocStringEditor;
import org.jl.nwn.resource.ResRefUtil;
import org.jl.nwn.resource.ResourceID;
import org.jl.swing.Actions;
import org.jl.swing.FileDropHandler;
import org.jl.swing.I18nUtil;
import org.jl.swing.UIDefaultsX;
import org.jl.swing.table.FormattedCellEditor;

/**
 * Editor pane for ERF, HAK and MOD (including SAV) files. Allows to rename files
 * in the archive, add, remove and extract files.
 */
public class ErfEdit extends SimpleFileEditorPanel {
    private static final UIDefaultsX UID = new UIDefaultsX();
    /**
     * Filter, that returns {@code true} for directories and files with extensions
     * {@code "mod"}, {@code "hak"}, {@code "sav"}, {@code "erf"} and {@code "nwm"}.
     */
    private static final FileFilter ERF_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File f) {
            final String s = f.getName().toLowerCase();
            return !f.isFile()
                || s.endsWith(".mod")
                || s.endsWith(".hak")
                || s.endsWith(".sav")
                || s.endsWith(".erf")
                || s.endsWith(".nwm");
        }
        @Override
        public String getDescription() {
            return "erf files";
        }
    };

    /** Edited ERF. */
    private ErfFile erf;
    /** Table model with content of the ERF archive. */
    private final ErfContentModel contentModel = new ErfContentModel();
    private final JXTable table = new JXTable(contentModel);

    private final JToolBar toolbar = new JToolBar();
    private final JMenu menuErf = new JMenu();
    private final JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

    private final JComboBox<ErfFile.ErfType> cbTypeSelector = new JComboBox<>(ErfFile.TYPES);
    private final CExoLocStringEditor descEditor = new CExoLocStringEditor();
    /** GUI element with ERF description data. */
    private final Box descriptionBox = new Box(BoxLayout.Y_AXIS);
    private final JFileChooser fChooser = new JFileChooser( new File(".") );

    //<editor-fold defaultstate="collapsed" desc="Actions">
    private final Action actSave = new AbstractAction("Save") {
        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                save();
            } catch ( IOException ioex ){
                JOptionPane.showMessageDialog( table, ioex, "error : save failed", JOptionPane.ERROR_MESSAGE );
            }
        }
    };

    private final Action actAddFiles = new AbstractAction() {
        @Override
        public void actionPerformed( ActionEvent e ){
            fChooser.setMultiSelectionEnabled(true);
            fChooser.setFileSelectionMode( JFileChooser.FILES_ONLY );
            if ( fChooser.showOpenDialog( table ) == JFileChooser.APPROVE_OPTION ){
                for (final File file : fChooser.getSelectedFiles()) {
                    erf.putResource(file);
                }
                contentModel.reload();
            }
        }
    };

    private final Action actRemove = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            final int[] selection = table.getSelectedRows();
            for (int i = selection.length-1; i >= 0; --i) {
                contentModel.remove(table.convertRowIndexToModel(selection[i]));
            }
        }
    };

    /**
     * Action for extracting files to a directory, overwrites existing files.
     */
    private final Action actExtractSelected = new AbstractAction() {
        private JButton btnOK = new JButton(
                new AbstractAction( "OK" ){
                    @Override
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
        @Override
        public void actionPerformed(ActionEvent e) {
            final int[] selection = table.getSelectedRows();
            if ( selection.length > 0 ){
                fChooser.setMultiSelectionEnabled(false);
                fChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
                if ( fChooser.showDialog( table, "extract here" ) == JFileChooser.APPROVE_OPTION ){
                    final File outputDir = fChooser.getSelectedFile();
                    new Thread(){
                        @Override
                        public void run(){
                            pBar.setMinimum( 0 );
                            pBar.setMaximum( selection.length );
                            dialog.pack();
                            dialog.setVisible( true );
                            dialog.setLocation( toolbar.getLocationOnScreen() );
                            btnOK.setEnabled( false );
                            try{
                                for ( int i = 0; i < selection.length; i++ ){
                                    final ResourceID id = contentModel.resources.get(table.convertRowIndexToModel(selection[i]));
                                    pBar.setValue( i );
                                    pBar.setString( id.getFileName() );
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
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Internal classes">
    private final class ErfContentModel extends AbstractTableModel {
        /** List of resources in the ERF archive. */
        private final ArrayList<ResourceID> resources = new ArrayList<>();
        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0 : return "Name";
                case 1 : return "Type";
                default: return "Size";
            }
        }
        @Override
        public int getColumnCount() { return 3; }
        @Override
        public int getRowCount() { return resources.size(); }
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0;
        }
        @Override
        public Object getValueAt(int row, int column) {
            final ResourceID id = resources.get(row);
            switch (column) {
                case 0: return id.getName();
                case 1: {
                    final String ext = ResourceID.getExtensionForType(id.getType());
                    return ext == null ? toHex(id.getType()) : ext;
                }
                case 2: return erf.getResourceSize(id);
            }
            return null;
        }
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                final ResourceID oldId = resources.get(rowIndex);
                if (!oldId.getName().equals(aValue)) {
                    try {
                        final String name = ResRefUtil.instance(erf.getVersion())
                                                      .parseString(aValue.toString());
                        final ResourceID id = erf.renameResource(oldId, name);
                        reload();
                        final int index = resources.indexOf(id);
                        table.setRowSelectionInterval(index, index);
                    } catch (ParseException pex){
                        // should not happen since ResRef editor is used
                        pex.printStackTrace();
                    }
                }
            }
        }
        @Override
        public void fireTableChanged( TableModelEvent e ){
            super.fireTableChanged(e);
            setIsModified(true);
        }

        public void remove(int index) {
            erf.remove(resources.remove(index));
            fireTableRowsDeleted(index, index);
        }

        /** Reload list of resources from current ERF file. */
        private void reload() {
            resources.clear();
            resources.addAll(erf.getResourceIDs());
            fireTableDataChanged();
        }
        private String toHex(short s) {
            String hex = Integer.toHexString(s);
            return "0x0000".substring(0, 6-hex.length())+hex;
        }
    }
    //</editor-fold>

    static {
        UID.addResourceBundle("org.jl.nwn.erf.uidefaults");
        UID.addResourceBundle("settings.keybindings");
    }

    {
        toolbar.setFloatable(false);
        table.getTableHeader().setReorderingAllowed(false);
        table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        table.getColumnModel().getColumn(0).setMinWidth(200);
        cbTypeSelector.addItemListener( new ItemListener(){
            @Override
            public void itemStateChanged( ItemEvent e ){
                if ( e.getStateChange() == ItemEvent.SELECTED ){
                    ErfFile.ErfType type = (ErfFile.ErfType) cbTypeSelector.getSelectedItem();
                    erf.setType( type );
                    descriptionBox.setVisible( type != ErfFile.ERF );
                    sPane.setDividerLocation(sPane.getDividerLocation());
                }
            }
        });
        table.setDefaultRenderer( Object.class, new DefaultTableCellRenderer(){
            Color defaultColor = getBackground();
            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value,
                    boolean isSelected,
                    boolean hasFocus,
                    int row,
                    int column){
                setBackground( defaultColor );
                Component c = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column );
                if (erf.isFileResource(contentModel.resources.get(table.convertRowIndexToModel(row))))
                    c.setBackground( isSelected? Color.ORANGE:Color.YELLOW );
                setHorizontalAlignment( column==2?JLabel.TRAILING:JLabel.LEADING );
                return c;
            }
        });
        descEditor.addChangeListener( new ChangeListener(){
            @Override
            public void stateChanged( ChangeEvent e ){
                setIsModified(true);
            }
        } );
    }

    private ErfEdit(ErfFile erf) {
        super();
        setLayout( new BorderLayout() );
        sPane.setOneTouchExpandable(true);
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

        Actions.configureActionUI(actAddFiles, UID, "ErfEdit.add");
        Actions.configureActionUI(actRemove, UID, "ErfEdit.remove");
        Actions.configureActionUI(actExtractSelected, UID, "ErfEdit.extract");
        JButton addButton = toolbar.add( actAddFiles );
        JButton rmButton = toolbar.add( actRemove );
        toolbar.add( new JToolBar.Separator() );
        JButton exButton = toolbar.add( actExtractSelected );
        addButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        rmButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        exButton.setMnemonic(KeyEvent.VK_UNDEFINED);
        I18nUtil.setText(menuErf, UID.getString("ErfEdit.erfMenuTitle"));
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

        // need to set all keybindings explicitly, because some may already be
        // used by swing L&F ( like 'control C' for copy
        Action[] editActions = { actAddFiles, actRemove, actExtractSelected };
        InputMap im = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = table.getActionMap();
        for ( Action a : editActions ){
            final Object action = a.getValue(Action.ACTION_COMMAND_KEY);
            im.put((KeyStroke)a.getValue( Action.ACCELERATOR_KEY ), action);
            am.put(action, a);
        }

        setTransferHandler(new FileDropHandler() {
            @Override
            public void importFiles(List<File> files) {
                for (final File f : files) {
                    erf.putResource(f);
                }
                contentModel.reload();
            }
        });
        showToolbar(true);
        setVisible( true );

        setErf(erf);
    }

    /**
     * Create editor with new empty HAK file for specified version of game
     * (i.e. file format).
     *
     * @param nwnVersion Format version of the new file
     */
    public ErfEdit(Version nwnVersion) {
        this(new ErfFile(ErfFile.HAK, nwnVersion));
    }

    /**
     * Create editor and open existent file.
     *
     * @param erfFile File that must be opened
     *
     * @throws IOException If file cann't be readed
     */
    public ErfEdit(File erfFile) throws IOException {
        this(new ErfFile(erfFile));
    }

    //<editor-fold defaultstate="collapsed" desc="SimpleFileEditor">
    @Override
    public boolean canSave() { return actSave.isEnabled(); }
    @Override
    public boolean canSaveAs() { return true; }

    @Override
    public void save() throws IOException {
        erf.write();
        setIsModified(false);
    }

    @Override
    public void saveAs(File f, Version nwnVersion) throws IOException {
        erf.write(f);
        setIsModified(false);
    }

    @Override
    public void close() {
        try{
            erf.close();
        } catch ( IOException ioex ){
            ioex.printStackTrace();
        }
    }

    @Override
    public File getFile() { return erf.getFile(); }

    @Override
    public Version getFileVersion() { return erf.getVersion(); }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="SimpleFileEditorPanel">
    @Override
    protected void setIsModified(boolean modified) {
        super.setIsModified(modified);
        actSave.setEnabled(modified);
    }

    @Override
    public JMenu[] getMenus() { return new JMenu[]{menuErf}; }

    @Override
    public JToolBar getToolbar() { return toolbar; }

    @Override
    public void showToolbar(boolean b) {
        if ( b )
            add(toolbar, BorderLayout.NORTH);
        else
            remove( toolbar );
    }
    //</editor-fold>

    public File extractAsTempFile(ResourceID id, boolean replaceWithFile) throws IOException {
        return erf.extractAsTempFile(id, replaceWithFile);
    }

    public ResourceID[] getSelectedResources(){
        int[] selection = table.getSelectedRows();
        ResourceID[] ids = new ResourceID[ selection.length ];
        for ( int i = 0; i < selection.length; i++ )
            ids[i] = contentModel.resources.get( table.convertRowIndexToModel(selection[i]) );
        return ids;
    }

    private void setErf(ErfFile erf) {
        this.erf = erf;
        contentModel.reload();

        descEditor.setCExoLocString(erf.getDescription());
        cbTypeSelector.setSelectedItem(erf.getType());

        // Setup restrictions for resource name length
        final TableCellEditor resRefEd =
                new FormattedCellEditor(
                new JFormattedTextField(
                ResRefUtil.instance(erf.getVersion())
                .getStringFormatter(false)));
        table.getColumnModel().getColumn(0).setCellEditor( resRefEd );

        setIsModified(false);
    }

    public static boolean accept(File f) {
        return f.isFile() && ERF_FILE_FILTER.accept(f);
    }

    public static void main( String[] args ) throws IOException{
        JFrame f = new JFrame(args[0]);
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        f.getContentPane().add( new ErfEdit( new File( args[0] ) ) );
        f.pack();
        f.setVisible( true );
    }
}
