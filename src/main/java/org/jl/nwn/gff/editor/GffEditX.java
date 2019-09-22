/*
 * GffEditX.java
 *
 * Created on 16. Mai 2005, 16:06
 */

package org.jl.nwn.gff.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.EventObject;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.AbstractCellEditor;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.DefaultEditorKit;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.RealTimeSpellChecker;
import org.dts.spell.swing.finder.DocumentWordFinder;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jdesktop.swingx.table.TableColumnExt;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;
import org.jl.nwn.editor.SimpleFileEditorPanelX;
import org.jl.nwn.editor.StatusBar;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.DefaultGffReader;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.Gff2Xml;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffContent;
import org.jl.nwn.gff.GffField;
import org.jl.nwn.gff.GffList;
import org.jl.nwn.gff.GffStruct;
import org.jl.nwn.gff.GffVoid;
import org.jl.nwn.resource.ResRefUtil;
import org.jl.nwn.spell.Dictionaries;
import org.jl.nwn.tlk.TlkLookup;
import org.jl.swing.Actions;
import org.jl.swing.HexEdit;
import org.jl.swing.I18nUtil;
import org.jl.swing.TableSearchAndReplace;
import org.jl.swing.table.FormattedCellEditor;
import org.jl.swing.table.MappedCellEditor;
import org.jl.swing.table.StringPopupCellEditor;
import org.jl.swing.undo.Mutator;
import org.jl.swing.undo.MyUndoManager;
import org.jl.text.VectorFormat;
import org.w3c.dom.Node;

/**
 *
 * @author
 */
public class GffEditX extends SimpleFileEditorPanelX implements ClipboardOwner {

    protected File gffFile = null;
    protected GffContent gff = null;

    protected JToolBar toolbar = new JToolBar();
    protected JMenu editMenu = new JMenu();
    protected JMenu[] menus = { editMenu };
    protected GffTreeTableModel model = null;
    protected JXTreeTable treeTable = null;
    protected MyUndoManager undoManager = new MyUndoManager();

    protected Action aPaste = null;
    protected Action aCopy = null;
    protected Action aCut = null;

    protected Action aUndo = null;
    protected Action aRedo = null;

    protected GffSearchAndReplace searchDialog;
    protected Action aFind = null;
    protected Action aFindAgain = null;

    protected Action aNewListStruct = null; // Action for inserting new struct in list
    protected Action aNewNode = null; // Action for creating new field

    protected Action[] actions;
    protected InputMap gffInputMap;

    public static final UIDefaults uid = new UIDefaults();

    static {
        uid.addResourceBundle("org.jl.nwn.gff.editor.uidefaults");
        uid.addResourceBundle("settings.keybindings");
    }

    protected TransferHandler transfer = new TransferHandler(){

        protected final String gffMime =
                DataFlavor.javaJVMLocalObjectMimeType + ";class=org.jl.nwn.gff.GffField";
        protected final DataFlavor flavorXml = new DataFlavor("text/xml","text/xml");
        protected final DataFlavor flavorGffObject = new DataFlavor(gffMime, "gff object");

        protected final DataFlavor[] transferFlavors = new DataFlavor[]{
            flavorGffObject, flavorXml
        };

        protected TreePath selectionPath;

        @Override public void exportToClipboard(final JComponent comp, Clipboard clip, int action){
            JXTreeTable tt = (JXTreeTable) comp;
            selectionPath = tt.getTreeSelectionModel().getSelectionPath();
            if ( selectionPath == null )
                return;
            final GffField o = (GffField)((GffField) selectionPath.getLastPathComponent()).clone();
            Transferable trans = makeTransferable(o);
            clip.setContents( trans, GffEditX.this );
            exportDone( comp, trans, action );
        }

        @Override protected void exportDone(JComponent source, Transferable data, int action){
            if ( (action & MOVE) != 0 ){
                int r = treeTable.getSelectedRow();
                model.remove(selectionPath);
                treeTable.getSelectionModel().setLeadSelectionIndex(Math.min(r, treeTable.getRowCount()-1));
            }
        }

        @Override public int getSourceActions(JComponent c){
            return COPY_OR_MOVE;
        }

        protected Transferable makeTransferable( final GffField field ){
            return new Transferable(){
                @Override
                public Object getTransferData( DataFlavor df ){

                    if ( df.equals(flavorXml) ){
                        try{
                            final PipedInputStream pin = new PipedInputStream();
                            final PipedOutputStream pout = new PipedOutputStream(pin);

                            Node doc = Gff2Xml.convertToXml(field);
                            final DOMSource source = new DOMSource(doc);
                            final Transformer trans = TransformerFactory.newInstance().newTransformer();
                            final StreamResult result = new StreamResult(pout);
                            trans.setOutputProperty(OutputKeys.INDENT, "yes");

                            //final Element e = Gff2Xml.mkElement(new DOMDocument(), field);
                            new Thread(){
                                @Override public void run(){
                                    try{
                                        BufferedOutputStream bos = new BufferedOutputStream(pout);
                                        StreamResult result = new StreamResult(bos);
                                        trans.transform(source, result);
                                        //new XMLWriter(bos, OutputFormat.createPrettyPrint()).write(e);
                                        bos.close();
                                        pout.close();

                                    } catch ( Exception ex){
                                        ex.printStackTrace();
                                    }
                                }
                            }.start();
                            return pin;
                        } catch ( IOException ioex ){
                            System.out.println("GFFEditX "+ioex);
                        } catch ( Exception e ){} // Parser & TransformerConfEx
                        return null;
                    } else if ( df.equals(flavorGffObject) )
                        return field;
                    else return null;
                }
                @Override public DataFlavor[] getTransferDataFlavors(){
                    return transferFlavors;
                }
                @Override public boolean isDataFlavorSupported( DataFlavor df ){
                    return df.getRepresentationClass().equals( GffField.class );
                }
            };
        }

        @Override public boolean canImport(JComponent comp, DataFlavor[] transferFlavors){
            boolean b = containsGffFlavor( transferFlavors );
            return b;
        }

        boolean containsGffFlavor( DataFlavor[] transferFlavors ){
            for ( DataFlavor flavor : transferFlavors ){
                int i = 0;
                if ( flavor.getRepresentationClass().equals( GffField.class ) ){
                    return true;
                }
            }
            return false;
        }

        @Override public boolean importData(JComponent comp, Transferable t){
            TreePath selectionPath = treeTable.getTreeSelectionModel().getSelectionPath();
            if ( selectionPath == null )
                return false;
            DataFlavor[] dfs = t.getTransferDataFlavors();

            if ( canImport(treeTable,dfs) ){
                try{
                    GffField f = (GffField) t.getTransferData(flavorGffObject);
                    doInsert(f, selectionPath);
                } catch ( UnsupportedFlavorException ufex ){ // should never happen
                    ufex.printStackTrace();
                } catch ( IOException ioex ){
                    ioex.printStackTrace();
                }
            }
            return false;
        }
    };


    public void setGffContent( GffContent c, File f ){
        model.setRoot( c.getTopLevelStruct() );
        File old = gffFile;
        gffFile = f;
        firePropertyChange( FILE_PROPERTY, old, gffFile );
        setIsModified( false );
        undoManager.discardAllEdits();
    }

    protected static class HexCellEditor extends AbstractCellEditor implements TableCellEditor{
        HexEdit hex = new HexEdit();
        ByteBuffer buffer;
        JLabel dummyLabel = new JLabel();
        JDialog popup = new JDialog();
        JTable table;

        Action aOK = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                stopCellEditing();
                fireEditingStopped();
                table.setEnabled(true);
                popup.setVisible(false);
                popup.dispose();
            }
        };
        Action aCancel = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                cancelCellEditing();
                table.setEnabled(true);
                popup.setVisible(false);
                popup.dispose();
            }
        };
        public HexCellEditor(){
            Dimension d = new Dimension( 640, 300 );
            hex.setPreferredSize(d);
            hex.setMinimumSize(d);
            InputMap iMap = popup.getRootPane().getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
            ActionMap aMap = popup.getRootPane().getActionMap();
            iMap.put(KeyStroke.getKeyStroke("ESCAPE"), "CANCEL");
            aMap.put("CANCEL", aCancel);
            popup.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton(aOK);
            I18nUtil.setText( okButton, "&OK" );
            JButton cancelButton = new JButton(aCancel);
            I18nUtil.setText( cancelButton, "&Cancel" );
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            popup.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
            popup.getRootPane().setDefaultButton(okButton);
            //popup.setModal(true);
            popup.setAlwaysOnTop(true);
            popup.getContentPane().add(hex.sPane);
            //popup.pack();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            byte[] b = (byte[]) value;
            buffer = ByteBuffer.allocate( b.length );
            buffer.put(b);
            hex.setBuffer(buffer);
            popup.pack();
            popup.setVisible(true);
            this.table = table;
            table.setEnabled(false);
            return dummyLabel;
        }

        @Override
        public Object getCellEditorValue(){
            System.out.println("getCellEditorValue");
            return buffer.array();
        }
    }

    protected NwnLanguage defaultLanguage = null;

    TableCellEditor subStringEd = new StringPopupCellEditor(){
        SpellChecker checker = null;
        SpellDictionary dict = null;
        RealTimeSpellChecker rtChecker = null;
        DocumentWordFinder finder = null;

        @Override public java.awt.Component getTableCellEditorComponent(
        JTable table,
                Object value,
                boolean isSelected,
                int row,
                int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            if ( defaultLanguage != null )
                dict = Dictionaries.forLanguage(defaultLanguage);
            else{
                CExoLocSubString sub = (CExoLocSubString) treeTable.getPathForRow(row).getLastPathComponent();
                dict = Dictionaries.forLanguage(sub.language);
            }
            if ( dict != null ){
                if ( checker == null ){
                    checker = new SpellChecker( dict );
                    finder = new DocumentWordFinder(getTextComponent().getDocument());
                    rtChecker = new RealTimeSpellChecker(checker, getTextComponent(), finder);
                } else {
                    checker.setDictionary(dict);
                    finder.setDocument(getTextComponent().getDocument());
                }
                rtChecker.start();
            }
            return c;
        }

        @Override public boolean stopCellEditing(){
            boolean stop = super.stopCellEditing();
            if ( stop && rtChecker != null )
                rtChecker.stop();
            return stop;
        }
        @Override public void cancelCellEditing() {
            super.cancelCellEditing();
            if ( rtChecker != null )
                rtChecker.stop();
        }
    };

    protected final VectorFormat vectorFormat = new VectorFormat(DecimalFormat.getInstance());

    protected TableCellEditor createValueEditor(){
        DecimalFormat bigIntFormat = new DecimalFormat(){
            // we need BigInteger instead of BigDecimal ...
            @Override public Object parseObject(String s) throws ParseException{
                BigDecimal d = (BigDecimal) super.parseObject(s);
                return d!=null?d.toBigInteger():null;
            }
        };
        FormattedCellEditor intEditor = new FormattedCellEditor( bigIntFormat );
        DecimalFormat decFormat = (DecimalFormat) DecimalFormat.getInstance();
        FormattedCellEditor floatEditor = new FormattedCellEditor( decFormat ){
        @Override public Object getCellEditorValue() {
                Number n = (Number) super.getCellEditorValue();
                return n!=null ? Float.valueOf(n.floatValue()) : n;
            }
        };
        FormattedCellEditor doubleEditor = new FormattedCellEditor( decFormat ){
            @Override public Object getCellEditorValue() {
                Number n = (Number) super.getCellEditorValue();
                return n!=null ? Double.valueOf(n.doubleValue()) : n;
            }
        };
        FormattedCellEditor structIdEdit = new FormattedCellEditor(bigIntFormat,null,null){
            @Override public Object getCellEditorValue(){
                Number n = (Number) super.getCellEditorValue();
                return n!=null? Integer.valueOf(n.intValue()) : n;
            }
        };

        FormattedCellEditor strRefEd = new FormattedCellEditor(
                bigIntFormat, BigInteger.valueOf(-1), BigInteger.valueOf(TlkLookup.USERTLKOFFSET * 2 - 1) );

        FormattedCellEditor byteEd = new FormattedCellEditor(
                bigIntFormat, BigInteger.valueOf(0), BigInteger.valueOf(255) );

        // TODO: see if version differences for editors can be sorted out
        FormattedCellEditor resRefEditor =
                new FormattedCellEditor( new JFormattedTextField(
                ResRefUtil.instance(Version.NWN2).getStringFormatter(false)) );

        FormattedCellEditor vectorEditor =
                new FormattedCellEditor( vectorFormat );

        TableCellEditor stringEd = new StringPopupCellEditor();
        FormattedCellEditor charEd =
                new FormattedCellEditor( bigIntFormat, BigInteger.valueOf(-128), BigInteger.valueOf(127) );
        TableCellEditor hexEd = new HexCellEditor();

        bigIntFormat.setParseBigDecimal(true);
        bigIntFormat.setParseIntegerOnly(true);
        decFormat.setMaximumFractionDigits(340);

        MappedCellEditor.KeyFunction getType = new MappedCellEditor.KeyFunction(){
            @Override public Object computeKey(Object value, int row, int col){
                Byte b = ((GffField)treeTable.getPathForRow(row).getLastPathComponent()).getType();

                return b;
            }
        };

        MappedCellEditor ed = new MappedCellEditor(getType);
        ed.map(Gff.BYTE, byteEd, null);
        ed.map(Gff.CHAR, charEd, null);
        ed.map(Gff.RESREF, resRefEditor, null);
        ed.map(Gff.CEXOSTRING, stringEd, null);
        ed.map(Gff.CEXOLOCSTRING, strRefEd, null);
        ed.map(Gff.FLOAT, floatEditor, null);
        ed.map(Gff.DOUBLE, doubleEditor, null);
        ed.map(Gff.VOID, hexEd, null);
        ed.map(GffCExoLocString.SUBSTRINGTYPE, subStringEd, null);
        ed.map(Gff.STRUCT, structIdEdit, null);
        ed.map(Gff.VECTOR, vectorEditor, null);
        ed.map(null, intEditor, null);
        return ed;
    };

    protected TableCellRenderer renderer = new DefaultTableCellRenderer(){
        private final NumberFormat intFormat = NumberFormat.getIntegerInstance();
        private final NumberFormat floatFormat = DecimalFormat.getInstance();

        @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
            GffField f = (GffField) treeTable.getPathForRow(row).getLastPathComponent();
            super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
            setEnabled( true );
            if ( f.isIntegerType() ){
                setText(intFormat.format(f.getData()));
            } else if ( f.isDecimalType() )
                setText( floatFormat.format(f.getData()) );
            else {
                switch ( f.getType() ){
                    case Gff.CEXOSTRING : setText(f.getData().toString()); break;
                    case Gff.RESREF : setText(f.getData().toString()); break;
                    case Gff.CEXOLOCSTRING : {
                        setText( intFormat.format(f.getData()) ); break;
                    }
                    case GffCExoLocString.SUBSTRINGTYPE :
                        setText( ((CExoLocSubString) f).string );
                        break;
                    case Gff.VOID :
                        //setText("click here to view ..."); break;
                        setText(GffVoid.printHex((byte[])f.getData(), 16)); break;
                    case Gff.LIST : setText(""); break;
                    case Gff.STRUCT : setText( intFormat.format(((GffStruct)f).getId()) ); break;
                    case Gff.VECTOR : setText( vectorFormat.format(f.getData()) ); break;
                }
            }
            return this;
        }
    };

    /** Creates a new instance of GffEditX */
    public GffEditX() {
        // setup treetable
        treeTable = new JXTreeTable(){
            @Override public void editingStopped( ChangeEvent e ){
                super.editingStopped( e );
                treeTable.requestFocusInWindow();
            }
            @Override public void editingCanceled( ChangeEvent e ){
                super.editingCanceled( e );
                treeTable.requestFocusInWindow();
            }
            int forbiddenModifiers =
                    InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK |
                    InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;
            /*process only known keystrokes or keyevents without modifiers, so that
            mnemonics wont trigger cell editing*/
            @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e,
            int condition, boolean pressed) {
                boolean r = false;
                ActionListener al = getActionForKeyStroke(ks);
                if (( al != null && al instanceof Action && ((Action)al).isEnabled() )
                || ( e.getModifiersEx() & forbiddenModifiers ) == 0 )
                    r = super.processKeyBinding(ks,e,condition,pressed);
                return r;
            }
        };
        model = new GffTreeTableModel(GffStruct.mkTopLevelStruct());
        model.addUndoableEditListener(undoManager);

        treeTable.setRootVisible(true);
        treeTable.setAutoCreateColumnsFromModel(false);
        treeTable.setSurrendersFocusOnKeystroke(true);
        //model.addTreeModelListener(tml);
        treeTable.setTreeTableModel(model);
        final GffTreeCellRenderer treeRenderer = new GffTreeCellRenderer(model);

        treeTable.addHighlighter(HighlighterFactory.createAlternateStriping());

        // set up editor for hiearchical column
        final int[] editedRow = new int[1];
        final JFormattedTextField tfLabel = new JFormattedTextField(
                ResRefUtil.instance(Version.NWN1).getStringFormatter(false) ){
            @Override public void setBounds( int x, int y, int width, int height ){
                int ident = treeRenderer.tree.getRowBounds( editedRow[0] ).x;
                ident += 20;
                super.setBounds( x + ident, y, width - ident, height );
            }
        };
        TableCellEditor labelEd = new FormattedCellEditor( tfLabel ){

            @Override public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
                editedRow[0] = row;
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override public boolean stopCellEditing(){
                GffField f = (GffField) treeTable.getPathForRow(editedRow[0]).getLastPathComponent();
                if ( f.getParent() != null && f.getParent().getType() == Gff.STRUCT ){
                    GffStruct s = (GffStruct) f.getParent();
                    String oldLabel = f.getLabel();
                    if ( !oldLabel.equals( tfLabel.getText() ) && s.getChild(tfLabel.getText()) != null ){
                        msgSup.fireMessage( "duplicate label : " + tfLabel.getText(), Level.WARNING );
                        tfLabel.setValue(oldLabel);
                        return false;
                    }
                }
                return super.stopCellEditing();
            }

            @Override public boolean isCellEditable(EventObject anEvent){
                if ( anEvent instanceof MouseEvent )
                    return ((MouseEvent)anEvent).getClickCount() == 2;
                else
                    return super.isCellEditable( anEvent );
            }
        };
        TableColumnExt c0 = new TableColumnExt(0,200);
        c0.setHeaderValue("Label");
        c0.setCellEditor(labelEd);
        c0.setEditable(true);

        TableColumnExt c1 = new TableColumnExt( 1, 100 );
        c1.setHeaderValue("Type");
        TableColumnExt c2 = new TableColumnExt( 2 );
        c2.setHeaderValue("Value");
        c2.setCellEditor(createValueEditor());
        c2.setCellRenderer(renderer);
        treeTable.addColumn(c0);
        treeTable.addColumn(c1);
        treeTable.addColumn(c2);
        treeTable.setTreeCellRenderer( treeRenderer );
        treeTable.setTransferHandler(transfer);
        setTransferHandler(transfer);
        treeTable.setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        treeTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        treeTable.getSelectionModel().addListSelectionListener(rowSelectionListener);
        treeTable.getTableHeader().setReorderingAllowed(false);
        //treeTable.putClientProperty("JTable.autoStartsEdit", new Boolean(false));

        InputMap im = treeTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        gffInputMap = new InputMap();
        gffInputMap.setParent(im);
        treeTable.setInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, gffInputMap);
        im = gffInputMap;
        ActionMap am = treeTable.getActionMap();

        aCopy = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                transfer.exportToClipboard(treeTable, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.COPY);
            }
        };
        aCut = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                transfer.exportToClipboard(treeTable, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.MOVE);
            }
        };
        aPaste = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                transfer.importData(treeTable, Toolkit.getDefaultToolkit().getSystemClipboard().getContents(treeTable));
            }
        };

        final NewNodeDialog newNodeDialog = new NewNodeDialog(this, msgSup);
        aNewNode = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                newNodeDialog.newNode();
            }
        };
        Actions.configureActionUI( aNewNode,uid,"gffedit.newNode" );

        aNewListStruct = createAction( this, "doNewListStruct", null );
        Actions.configureActionUI( aNewListStruct,uid,"gffedit.newListStruct" );


        Action aExpand = new AbstractAction(){
            @Override
            public void actionPerformed( ActionEvent e ){
                int row = treeTable.getSelectedRow();
                if ( row > -1 ){
                    if ( treeTable.isExpanded(row) )
                        treeTable.collapseRow(row);
                    else
                        treeTable.expandRow(row);
                    treeTable.getSelectionModel().setSelectionInterval(row,row);
                }
            }
        };
        Actions.configureActionUI( aExpand,uid,"gffedit.expand" );

        Actions.configureActionUI(aCut,uid, "gffedit.cut");
        Actions.configureActionUI(aCopy,uid, "gffedit.copy");
        Actions.configureActionUI(aPaste,uid, "gffedit.paste");

        aUndo = undoManager.getUndoAction();
        aRedo = undoManager.getRedoAction();

        Actions.configureActionUI(aUndo,uid, "undo");
        Actions.configureActionUI(aRedo,uid, "redo");

        PropertyChangeListener pcl = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ( evt.getPropertyName().equals(Mutator.PROP_MODIFIED) )
                    setIsModified((Boolean)evt.getNewValue());
            }
        };
        model.getMutator().addPropertyChangeListener(pcl);

        aFind = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                if ( searchDialog == null ){
                    searchDialog = new GffSearchAndReplace(
                            (JFrame)SwingUtilities.getWindowAncestor(treeTable),
                            treeTable
                            );
                }
                searchDialog.setVisible(true);
                aFindAgain.setEnabled(true);
            }
        };
        Actions.configureActionUI(aFind,uid, "find");
        aFindAgain = new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                searchDialog.doSearch();
                treeTable.requestFocusInWindow();
            }
        };
        Actions.configureActionUI(aFindAgain,uid, "findNext");

        actions = new Action[]{
            aFind, aFindAgain, aRedo, aUndo, aCut, aCopy, aPaste,
            aNewNode, aNewListStruct, aExpand
        };

        ActionMap amTop = getActionMap();

        for ( Action a : actions ){
            im.put((KeyStroke)a.getValue( a.ACCELERATOR_KEY ), a.getValue( a.ACTION_COMMAND_KEY ));
            am.put(a.getValue( a.ACTION_COMMAND_KEY ), a );
        }

        for ( Action a : actions ){
            amTop.put(a.getValue( a.ACTION_COMMAND_KEY ), a );
        }
        amTop.put( DefaultEditorKit.copyAction, aCopy );
        amTop.put( DefaultEditorKit.cutAction, aCut );
        amTop.put( DefaultEditorKit.pasteAction, aPaste );

        toolbar.add(aCut);
        toolbar.add(aCopy);
        toolbar.add(aPaste);
        toolbar.addSeparator();
        toolbar.add(aFind);
        toolbar.add(aFindAgain);
        toolbar.addSeparator();
        toolbar.add(aNewNode);
        toolbar.add(aNewListStruct);
        toolbar.add( Box.createHorizontalGlue() );
        toolbar.setFloatable(false);

        for ( Object o : toolbar.getComponents() ){
            if ( o instanceof AbstractButton )
                if ( ((AbstractButton)o).getIcon() != null )
                    ((AbstractButton)o).setMnemonic(KeyEvent.VK_UNDEFINED);
        }

        // menu for selecting default spell checker language ...
        JMenu spellCheckLanguage = new JMenu();
        I18nUtil.setText(spellCheckLanguage, "Spell Checking &Language");
        JRadioButtonMenuItem miNoDefault = new JRadioButtonMenuItem("CExoLocString language", true);
        spellCheckLanguage.add( miNoDefault );
        final String lPropKey = "checkerLanguage";
        final ButtonGroup langBg = new ButtonGroup();
        langBg.add( miNoDefault );
        ActionListener al = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                defaultLanguage = (NwnLanguage)
                ((JMenuItem)e.getSource()).getClientProperty(lPropKey);
            }
        };
        miNoDefault.addActionListener(al);
        NwnLanguage[] checkableLanguages = new NwnLanguage[]{
            NwnLanguage.ENGLISH, NwnLanguage.FRENCH, NwnLanguage.GERMAN,
            NwnLanguage.ITALIAN, NwnLanguage.SPANISH, NwnLanguage.POLISH
        };
        for ( NwnLanguage l : checkableLanguages ){
            JRadioButtonMenuItem mi = new JRadioButtonMenuItem( l.getName(), false );
            langBg.add(mi);
            mi.addActionListener(al);
            mi.putClientProperty(lPropKey, l);
            spellCheckLanguage.add(mi);
        }
        spellCheckLanguage.setIcon(Actions.getEmptyIcon());

        I18nUtil.setText( editMenu, "&Edit" );
        editMenu.add( aCut );
        editMenu.add( aCopy );
        editMenu.add( aPaste );
        editMenu.addSeparator();
        editMenu.add( aNewNode );
        editMenu.add( aNewListStruct );
        editMenu.addSeparator();
        Actions.configureActionUI( aCheckSpelling, uid, "spellcheck" );
        editMenu.add(aCheckSpelling);
        editMenu.add(spellCheckLanguage);
        editMenu.addSeparator();
        editMenu.add( aUndo );
        editMenu.add( aRedo );

        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(treeTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add( scrollPane, BorderLayout.CENTER );
        treeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        c0.setPreferredWidth(250);
        c1.setPreferredWidth(100);
        c2.setPreferredWidth(400);
        add( toolbar, BorderLayout.NORTH );
    }

    static protected AbstractAction createAction( final Object target, final String action, final String eventPropertyName ){
        return new AbstractAction(){
            Action delegate = EventHandler.create(Action.class, target, action, eventPropertyName, "actionPerformed");
            @Override
            public void actionPerformed( ActionEvent e ){
                delegate.actionPerformed(e);
            }
        };
    }

    protected ListSelectionListener rowSelectionListener = new ListSelectionListener(){
        @Override
        public void valueChanged( ListSelectionEvent e ){
            if ( !e.getValueIsAdjusting() ){
                int row = treeTable.getSelectionModel().getMinSelectionIndex();
                if ( row != -1 ){
                    TreePath p = treeTable.getPathForRow(row);
                    GffField f = (GffField) p.getLastPathComponent();
                    aNewListStruct.setEnabled( f.getType() == Gff.LIST || (f.getParent()!=null && f.getParent().getType() == Gff.LIST) );
                    aNewNode.setEnabled( f.getType() != Gff.LIST );
                    /*
                    aCopy.setEnabled( f.getType() != GffCExoLocString.SUBSTRINGTYPE );
                    aCut.setEnabled( f.getType() != GffCExoLocString.SUBSTRINGTYPE );
                    aPaste.setEnabled( f.getType() != GffCExoLocString.SUBSTRINGTYPE );
                     */
                }
            }
        }
    };

    protected void disableGffInputMap(){
        if ( treeTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        == gffInputMap ){
            treeTable.setInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT,
                    treeTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).getParent()
                    );
        }
    }

    /**
     *retrieve selected field, may be null
     */
    protected GffField getSelectedField(){
        if ( !treeTable.getTreeSelectionModel().isSelectionEmpty() )
            return (GffField) treeTable.getTreeSelectionModel().getSelectionPath().getLastPathComponent();
        else
            return null;
    }

// gui stuff for entering a list index
    final SpinnerNumberModel indexModel = new SpinnerNumberModel();
    JOptionPane indexOptions = createIndexOptionPane();
    JDialog indexDialog = indexOptions.createDialog( treeTable, "insert into list" );

    protected boolean doInsert( GffField newField, TreePath insPath ){
        GffStruct struct = null;
        GffField insertAt = (GffField)insPath.getLastPathComponent();
        if ( newField.getType() == GffCExoLocString.SUBSTRINGTYPE ) {
            try {
                if ( insertAt.getType() == Gff.CEXOLOCSTRING )
                    model.insert(insPath, newField, 0);
                else if ( insertAt.getType() == GffCExoLocString.SUBSTRINGTYPE )
                    model.insert(model.makePath(insertAt.getParent()), newField, 0 );
                else
                    msgSup.fireMessage( "paste : cannot paste field, substrings are only allowed within CExoLocStrings", Level.WARNING );
            } catch ( IllegalArgumentException iae ){
                msgSup.fireMessage( "paste : cannot paste substring, the CExoLocString already contains a substring for the same language/gender", Level.WARNING );
            }
        } else if ( insertAt.isDataField() ){
            struct = (GffStruct) insertAt.getParent();
        } else
            if ( insertAt.getType() == Gff.STRUCT )
                struct = (GffStruct) insertAt;
            else{
            if ( insertAt.getType() == Gff.LIST ){
                GffList list = (GffList) insertAt;
                if ( newField.getType() != Gff.STRUCT ){
                    msgSup.fireMessage("cannot insert into list : clipboard content is not a struct", Level.WARNING);
                    return false;
                } else {
                    indexModel.setMaximum( list.getSize() );
                    indexModel.setValue( list.getSize() );
                    indexDialog.setVisible( true );
                    indexDialog.dispose();
                    if ( indexOptions.getValue() == "OK" ){
                        model.insert( insPath, (GffStruct) newField.clone(), ((Number)indexModel.getValue()).intValue() );
                        return true;
                    }
                }
            }
            }
        if ( struct != null ){
            if ( struct.getChild(newField.getLabel()) != null ){
                msgSup.fireMessage("cannot insert : duplicate label : " + newField.getLabel(), Level.WARNING);
                return false;
            } else {
                TreePath insertionPath = (insertAt!=struct)?model.makePath(struct):insPath;
                int insertionIndex = (insertAt!=struct)?struct.indexOf(insertAt):struct.getSize();
                model.insert(insertionPath, (GffField) newField.clone(), insertionIndex);
                return true;
            }
        }
        return false;
    }

    protected JOptionPane createIndexOptionPane(){
        if ( indexOptions == null ){
            JSpinner indexSpinner = new JSpinner( indexModel );
            JLabel indexLabel = new JLabel("Index");
            indexLabel.setDisplayedMnemonic('i');
            JPanel p = new JPanel();
            p.add( indexLabel );
            p.add( indexSpinner );
            return new JOptionPane(p, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_OPTION, null, new String[]{"OK"}, "OK" ){
                @Override public Object getInputValue(){
                    return indexModel.getValue();
                }
            };
        }
        return indexOptions;
    }

    /**
     * insert a new struct into list at selected position
     */
    public void doNewListStruct(){
        TreePath p = treeTable.getTreeSelectionModel().getSelectionPath();
        if ( p != null ){
            GffField f = (GffField) p.getLastPathComponent();
            GffList list = null;
            int position = 0;
            if ( f.getType() == Gff.LIST )
                list = (GffList) f;
            else {
                if ( f.getType() == Gff.STRUCT && (f.getParent() != null && f.getParent().getType() == Gff.LIST) ){
                    list = (GffList) f.getParent();
                    position = list.indexOf( f ) + 1;
                }
            }
            if ( list != null ){
                int id = 0;
                if ( list.getSize() > 0 )
                    id = list.get(0).getId();
                GffStruct struct = new GffStruct(id);
                model.insert( model.makePath(list), struct, position );
            }
        }
    }

// only method of interface ClipboardOwner
    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents){
        // aPaste.setEnabled( transfer.canImport(this, clipboard.getAvailableDataFlavors() ) );
    }

    public static boolean accept( File f ){
        String extension = f.getName().substring(f.getName().lastIndexOf(".")+1);
        return (Gff.isKnownGffFileType(extension));
    }

    protected Action aCheckSpelling = new AbstractAction(){
        GffSpellChecker checker;

        @Override
        public void actionPerformed(ActionEvent e) {
            if ( checker == null )
                checker = new GffSpellChecker(
                        (JFrame) SwingUtilities.getWindowAncestor(treeTable), model
                        );
            checker.forceLanguage(defaultLanguage);
            checker.performChecking();
        }
    };

    protected class GffSearchAndReplace extends TableSearchAndReplace{
        public GffSearchAndReplace( JFrame owner, JXTreeTable table ){
            super(owner, table);
        }
        @Override public Object string2ModelObject(String s, int row, int col) {
            if ( col != 2 ){
                msgSup.fireMessage("replace works only on string data fields, not on labels or types");
                throw new IllegalArgumentException();
            } else {
                if ( !(((GffField)treeTable.getValueAt(row,col)).getData() instanceof String) ){
                    msgSup.fireMessage("replace works only on string data fields");
                    throw new IllegalArgumentException();
                } else
                    return s;
            }

        }

        @Override public String modelObject2String(Object value, int row, int col) {

            String retValue;
            Object data;
            if (value instanceof GffField){
                GffField f = (GffField) value;
                switch (col) {
                    case 0 : return f.getLabel();
                    case 1 : return f.getTypeName();
                    case 2 : {
                        if ((data = f.getData()) != null)
                            return data.toString();
                        else
                            return "";
                    }
                    default : return "";
                }
            }
            retValue = super.modelObject2String(value, row, col);
            return retValue;
        }
        @Override protected boolean nextCell(){
            treeTable.expandRow(row);
            return super.nextCell();
        }
    }

    public void addUndoableEditListener( UndoableEditListener l ){
        model.addUndoableEditListener( l );
    }

    public void setFileVersion(Version nwnVersion){
        this.nwnVersion = nwnVersion;
    }

    public static void main(String ... args){
        JFrame f = new JFrame("GffEditX test");
        StatusBar sb = new StatusBar();

        f.getContentPane().add(sb.getStatusBar(), BorderLayout.SOUTH);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        GffEditX ed = new GffEditX();
        ed.addMessageListener(sb);
        ed.addProgressListener(sb);
        f.getContentPane().add(ed);
        if ( args.length > 0 )
            ed.load(new File(args[0]), Version.getDefaultVersion());
        f.pack();
        f.setVisible(true);
    }

// -----------------------------------------------------------

    @Override
    public void saveAs(java.io.File f, Version nwnVersion) throws java.io.IOException {
        gff.write(f, nwnVersion);
        this.nwnVersion = nwnVersion;
        gffFile = f;
        model.mutator.stateSaved();
    }

    @Override
    public boolean load(java.io.File file, Version nwnVersion){
        try{
            DefaultGffReader gffBuilder = new DefaultGffReader(nwnVersion);
            gff = gffBuilder.load(file);
            this.nwnVersion = nwnVersion;
            Object oldValue = gffFile;
            gffFile = file;
            firePropertyChange(FILE_PROPERTY, oldValue, file);
            model.setRoot( gff.getTopLevelStruct() );
            undoManager.discardAllEdits();
            model.mutator.stateSaved();
            return true;
        } catch ( IOException ioex ){
            ioex.printStackTrace();
            msgSup.fireMessage( ioex.getMessage(), Level.SEVERE );
        }
        return false;
    }

    @Override
    public void showToolbar(boolean b) {
        if ( true )
            add( toolbar, BorderLayout.NORTH );
        else remove( toolbar );
        validate();
    }

    @Override
    public java.io.File getFile() {
        return gffFile;
    }

    @Override
    public void save() throws java.io.IOException {
        this.saveAs(gffFile, nwnVersion);
    }

    @Override
    public javax.swing.JToolBar getToolbar() {
        return toolbar;
    }

    @Override
    public void close() {
    }

    @Override public javax.swing.JMenu[] getMenus() {
        return menus;
    }

    @Override
    public boolean canSaveAs() {
        return true;
    }

    @Override
    public boolean canSave() {
        return ( gffFile != null && gffFile.canWrite() );
    }
}
