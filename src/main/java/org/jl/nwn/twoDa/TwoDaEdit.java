package org.jl.nwn.twoDa;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.Caret;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jl.nwn.Version;
import org.jl.nwn.editor.SimpleFileEditorPanel;
import org.jl.nwn.tlk.editor.TlkEdit;
import org.jl.swing.Actions;
import org.jl.swing.I18nUtil;
import org.jl.swing.TableSearchAndReplace;
import org.jl.swing.UIDefaultsX;
import org.jl.swing.table.TableMutator;
import org.jl.swing.undo.MappedListSelectionModel;
import org.jl.swing.undo.MyUndoManager;
import org.jl.swing.undo.RowMutator;

public class TwoDaEdit extends SimpleFileEditorPanel {

    //JPanel implements SimpleFileEditor {
    static final String lineSeparator = System.getProperty("line.separator");
    //private TwoDaTable twoDa;
    private File file;
    protected JXTable table;
    private JScrollPane tableScroller;
    private JToolBar toolbar;

    private JMenu editMenu = new JMenu();
    private JMenu viewMenu = new JMenu();
    private JMenu toolsMenu = new JMenu();

    final JLabel positionLabel = new JLabel("", JLabel.RIGHT);

    private TwoDaTableModel model;
    private AbstractTableModel headerModel;
    private TwoDaMetaData metaData = null;
    private TableMutator<String[], String[]> mutator;
    private ListSelectionModel mappedLsl;
    private MyUndoManager undoManager = new MyUndoManager();
    private Action aUndo;
    private Action aRedo;

    protected static final UIDefaultsX uid = new UIDefaultsX();
    static {
        uid.addResourceBundle("org.jl.nwn.twoDa.uidefaults");
        uid.addResourceBundle("settings.keybindings");
    }

    // methods for SimpleFileEditor
    @Override
    public boolean canSave() {
        return file != null;
    }

    @Override
    public boolean canSaveAs() {
        return true;
    }

    protected JDialog alterTableDialog = null;

    @Override
    public void close() {
        if (alterTableDialog != null) {
            alterTableDialog.dispose();
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public void save() throws IOException {
        model.writeToFile(file);
        mutator.stateSaved();
    }

    @Override
    public void saveAs(File f, Version nwnVersion) throws IOException {
        model.writeToFile(f);
        this.nwnVersion = nwnVersion;
        file = f;
        mutator.stateSaved();
    }

    public static boolean accept(File f) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line = "";
            // skip empty lines
            while ((line = in.readLine()).trim().length() == 0) {
            }
            in.close();
            return line.trim().toLowerCase().startsWith("2da");
        } catch (IOException ioex) {
        }
        return false;
    }

    public TwoDaEdit(File f, Version v) throws IOException {
        this();
        load(f,v);
    }

    public TwoDaEdit() throws IOException {
        table = new JTable2da();
        mappedLsl = MappedListSelectionModel.createRowModelToViewMapper(table);
        table.setRowHeight(table.getRowHeight() + 5);
        TwoDaSelectionListener l = new TwoDaSelectionListener();
        table.getSelectionModel().addListSelectionListener(l);
        table.getColumnModel().getSelectionModel().addListSelectionListener(l);

        aUndo = undoManager.getUndoAction();
        aRedo = undoManager.getRedoAction();
        Actions.configureActionUI(aUndo, uid, "undo");
        Actions.configureActionUI(aRedo, uid, "redo");

        setup();
        resizeColumns();

        editMenu.addSeparator();
        editMenu.add(aUndo);
        editMenu.add(aRedo);

        table.putClientProperty("JTable.autoStartsEdit", Boolean.TRUE);
        table.setSurrendersFocusOnKeystroke(true);

    }

    private void initMetaDataStuff(final TwoDaMetaData metaData) {
        for (int i = 0; i < model.getColumnCount(); i++) {
            setupColumn(i);
        }
        if (metaData.getHelpfile() != null) {
            final JTextPane text = new JTextPane();
            final JScrollPane scroll = new JScrollPane(text);
            scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            text.setContentType("text/html");
            text.setEditable(false);
            JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            sPane.add(tableScroller);
            sPane.add(scroll);
            sPane.setOneTouchExpandable(true);
            sPane.setDividerLocation(0.5);
            sPane.setResizeWeight(1);
            try {
                text.setPage(metaData.getHelpfile());
            } catch (IOException ioex) {
                System.err.println("could not open help file");
                ioex.printStackTrace();
            }

            add(sPane, BorderLayout.CENTER);

            ListSelectionListener lsl = new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    int c = table.getSelectedColumn();
                    if (!e.getValueIsAdjusting() && c != -1) {

                        if (metaData.getHelpfile() != null) {
                            try {
                                text.setPage(new URL(metaData.getHelpfile(), "#" + table.getColumnName(table.getSelectedColumn())));
                            } catch (MalformedURLException e1) {
                                e1.printStackTrace(); // should not happen
                            } catch (IOException e1) {
                                e1.printStackTrace(); // should not happen
                            }
                        } else {
                            TwoDaMetaData.ColumnMetaData cm = metaData.get(model.getColumnName(table.convertColumnIndexToModel(c)));
                            if (cm != null) {
                                text.setText(cm.description != null ? cm.description : "-no description-");
                            } else {
                                text.setText("-no description-");
                            }
                        }
                    }
                }
            };
            table.getColumnModel().getSelectionModel().addListSelectionListener(lsl);
        }
    }

    public void load(File f, Version v) throws IOException {
        //putClientProperty(resources, new TreeMap());

        TwoDaTable twoDa = new TwoDaTable(f);
        twoDa.updateColumnWidth();
        file = f;
        nwnVersion = v;
        model = new TwoDaTableModel(twoDa);
        table.setModel(model);

        if (mutator!=null)
            mutator.removeUndoableEditListener(undoManager);
        undoManager.discardAllEdits();

        mutator = new TableMutator<String[], String[]>(model, mappedLsl);
        mutator.addUndoableEditListener(undoManager);
        mutator.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("modified")) {
                    setIsModified((Boolean) evt.getNewValue());
                }
            }
        });
        mutator.stateSaved();
        String tableName = f.getName().substring(0, f.getName().indexOf('.'));
        metaData = TwoDaMetaData.forTableName(tableName, getFileVersion());
        if (metaData != null) {
            initMetaDataStuff(metaData);
        }
    }

    Action aResize = new AbstractAction("pad") {

        @Override
        public void actionPerformed(ActionEvent e) {
            int currentSize = model.getRowCount() - 1;
            String input = JOptionPane.showInputDialog(table, "enter new row count ( >" + currentSize + ")", currentSize);
            try {
                int newSize = Integer.parseInt(input);
                if (newSize > currentSize) {
                    //String[] emptyRow = null;twoDa.emptyRow();
                    String[][] emptyRows = new String[newSize - currentSize][];
                    for (int n = currentSize, i = 0; n < newSize; n++, i++) {
                        String[] emptyRow = model.emptyRow();
                        emptyRow[0] = Integer.toString(n);
                        emptyRows[i] = emptyRow;
                    }
                    mutator.insertRows(model.getRowCount() - 1, Arrays.asList(emptyRows));
                }
            } catch (NumberFormatException nfe) {
            }
        }
    };

    /*
     */
    public void lockColumns(final int[] headerColumns) {
        //if ( !(getParent().getParent() instanceof JScrollPane) ) return;
        final JXTable view = new JTable2da();
        view.setRowHeight(table.getRowHeight());
        //JScrollPane spane = ( JScrollPane ) getParent().getParent();
        headerModel = new AbstractTableModel() {

            @Override
            public int getColumnCount() {
                return headerColumns.length;
            }

            @Override
            public String getColumnName(int col) {
                return model.getColumnName(headerColumns[col]);
            }

            @Override
            public int getRowCount() {
                return model.getRowCount();
            }

            @Override
            public Object getValueAt(int row, int col) {
                if (col == 0 && row < model.getRowCount() - 1) {
                    return Integer.toString(row);
                }
                return model.getValueAt(row, headerColumns[col]);
            }

            @Override
            public void setValueAt(Object value, int row, int col) {
                mutator.new SetValueAtEdit("Update", value, row, headerColumns[col]);
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return model.isCellEditable(rowIndex, headerColumns[columnIndex]);
            }
        };
        headerModel.addTableModelListener(table);
        view.setModel(headerModel);
        // set width for header columns
        for (int i = 0; i < headerColumns.length; i++) {
            view.getColumnModel().getColumn(i).setPreferredWidth(table.getColumnModel().getColumn(headerColumns[i]).getWidth());
        }
        view.setSelectionModel(table.getSelectionModel());
        view.setEnabled(false);
        model.addTableModelListener(view);
        model.addTableModelListener(table);
        tableScroller.setCorner(JScrollPane.UPPER_LEFT_CORNER, view.getTableHeader());
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(view, BorderLayout.CENTER);
        tableScroller.setViewportBorder(BorderFactory.createLineBorder(Color.black));
        //view.setBackground(Color.LIGHT_GRAY);
        view.setBackground(new Color(230, 230, 230));
        tableScroller.setRowHeaderView(p);
    }

    public void unlockColumns() {
        tableScroller.setRowHeader(null);
        tableScroller.setViewportBorder(null);
    }

    private void setup() {
        table.getTableHeader().setReorderingAllowed(false);

        // set default editor that has 'type over' behaviour (i.e. selects all text when starting)
        final JTextField text = new JTextField();
        DefaultCellEditor ce = new DefaultCellEditor(text) {

            @Override
            public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                java.awt.Component retValue;
                retValue = super.getTableCellEditorComponent(table, value, isSelected, row, column);
                Caret c = text.getCaret();
                c.setDot(0);
                c.moveDot(text.getText().length());
                return retValue;
            }
        };
        table.setDefaultEditor(String.class, ce);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableScroller = new JScrollPane(table);
        setLayout(new BorderLayout());
        add(tableScroller, BorderLayout.CENTER);
        setSize(new java.awt.Dimension(800, 600));

        setupToolbar();
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(positionLabel);
        //Box b = new Box(BoxLayout.X_AXIS);
        //b.add(toolbar);
        //b.add(positionLabel);
        //add(toolbar, BorderLayout.NORTH);
        //add(b, BorderLayout.NORTH);
        showToolbar(true);
        setVisible(true);
    }

    private void resizeColumns() {
        if (table != null && model != null) {
            int charWidth = getFontMetrics(getFont()).charWidth('a');
            for (int i = 0, n = table.getColumnModel().getColumnCount(); i < n; i++) {
                //table.getColumnModel().getColumn(i).setMinWidth((model.getColumnWidth(i) + 5) * charWidth);
                table.getColumnModel().getColumn(i).setPreferredWidth((model.getColumnWidth(i) + 5) * charWidth);
            }
        }
    }

    private void setupToolbar() {
        Action lock = new AbstractAction("lock row header") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (((JToggleButton) e.getSource()).isSelected()) {
                    lockColumns(new int[]{0, 1});
                } else {
                    unlockColumns();
                }
            }
        };
        toolbar = new JToolBar();
        toolbar.setFloatable(false);

        Action lock0 = new AbstractAction("") {

            @Override
            public void actionPerformed(ActionEvent e) {
                lockColumns(new int[]{0});
            }
        };
        Action lock1 = new AbstractAction("") {

            @Override
            public void actionPerformed(ActionEvent e) {
                lockColumns(new int[]{0, 1});
            }
        };
        Action unlock = new AbstractAction("display row header") {

            @Override
            public void actionPerformed(ActionEvent e) {
                unlockColumns();
            }
        };
        Action resize = new AbstractAction("resize columns") {

            @Override
            public void actionPerformed(ActionEvent e) {
                resizeColumns();
            }
        };
        JRadioButton ru = new JRadioButton(unlock);
        ru.setHorizontalTextPosition(JRadioButton.LEADING);
        JRadioButton r0 = new JRadioButton(lock0);
        //r0.doClick();
        //lockColumns(new int[] { 0 });
        JRadioButton r1 = new JRadioButton(lock1);
        ButtonGroup bg = new ButtonGroup();
        ru.setToolTipText("display don't display row header");
        ru.setText("");
        r0.setToolTipText("display column 0 as row header");
        r1.setToolTipText("display columns 0 and 1 as row header");
        bg.add(ru);
        bg.add(r0);
        bg.add(r1);
        /*
        toolbar.add(unlock);
        toolbar.add(lock0);
        toolbar.add(lock1);
         */

        viewMenu.setText("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        int acc = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();



        JMenuItem noRowHeader = viewMenu.add(unlock);
        noRowHeader.setText("no row header");
        noRowHeader.setMnemonic(KeyEvent.VK_N);
        noRowHeader.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, acc));

        JMenuItem rowHeader0 = viewMenu.add(lock0);
        rowHeader0.setText("column 1 as row header");
        rowHeader0.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, acc));
        rowHeader0.setMnemonic(KeyEvent.VK_1);

        JMenuItem rowHeader1 = viewMenu.add(lock1);
        rowHeader1.setText("column 1 & 2 as row header");
        rowHeader1.setMnemonic(KeyEvent.VK_2);
        rowHeader1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, acc));


        Action lockLabel = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int labelCol = model.findColumn("label");
                if ( labelCol != -1 )
                    lockColumns(new int[]{0, labelCol});
            }
        };
        //lockLabel.setEnabled(labelCol != -1);
        JMenuItem rowHeaderLabel = viewMenu.add(lockLabel);
        rowHeaderLabel.setText("number & label as row header");
        rowHeaderLabel.setMnemonic(KeyEvent.VK_L);
        rowHeaderLabel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, acc));

        JMenuItem resizeHeaders = viewMenu.add(resize);
        resizeHeaders.setText("resize columns");
        resizeHeaders.setMnemonic(KeyEvent.VK_R);
        resizeHeaders.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, acc));

        Actions.configureActionUI(actCut, uid, "2daedit.cut");
        Actions.configureActionUI(actCopyRows, uid, "2daedit.copy");
        Actions.configureActionUI(actPasteRows, uid, "2daedit.paste");
        Actions.configureActionUI(actFind, uid, "2daedit.find");
        Actions.configureActionUI(actFindNext, uid, "2daedit.findnext");

        JButton cutButton = toolbar.add(actCut);

        JButton copyButton = toolbar.add(actCopyRows);
        JButton pasteButton = toolbar.add(actPasteRows);
        toolbar.addSeparator();
        JButton findButton = toolbar.add(actFind);
        JButton findNextButton = toolbar.add(actFindNext);
        toolbar.addSeparator();

        for (Object o : toolbar.getComponents()) {
            if (o instanceof AbstractButton) {
                if (((AbstractButton) o).getIcon() != null) {
                    ((AbstractButton) o).setMnemonic(KeyEvent.VK_UNDEFINED);
                }
            }
        }
        /*
        toolbar.add(actSetAbsolute);
        toolbar.add(actRemAbsolute);
        toolbar.add(actToggleAbsolute);
        toolbar.addSeparator();
         */

        Actions.configureActionUI(aToggleUserTlk, uid, "2daedit.toggleU");

        // setup key bindings
        Action[] actions = {aUndo, aRedo, actFind, actFindNext, actCut, actCopyRows, actPasteRows, aToggleUserTlk};
        Actions.registerActions(table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), table.getActionMap(), actions);
        I18nUtil.setText(editMenu, "&Edit");
        JMenuItem miCut = editMenu.add(actCut);
        JMenuItem miCopy = editMenu.add(actCopyRows);
        JMenuItem miPaste = editMenu.add(actPasteRows);
        editMenu.addSeparator();
        JMenuItem miFind = editMenu.add(actFind);
        JMenuItem miFindNext = editMenu.add(actFindNext);
        editMenu.addSeparator();
        JMenuItem miToggleUTlk = editMenu.add(aToggleUserTlk);

        JMenuItem miAlterTable = toolsMenu.add(alterTable);
        I18nUtil.setText(miAlterTable, "&Alter ...");
        JMenuItem miResize = toolsMenu.add(aResize);
        JMenuItem miRenumber = toolsMenu.add(actRenumber);
        I18nUtil.setText(miResize, "&Resize ...");
        I18nUtil.setText(miRenumber, "Re&number");
        I18nUtil.setText(toolsMenu, "&Table");
    }

    public JToolBar getToolBar() {
        return toolbar;
    }

    @Override
    public JMenu[] getMenus() {
        return new JMenu[]{editMenu, viewMenu, toolsMenu};
    }

    @Override
    public JToolBar getToolbar() {
        return toolbar;
    }

    @Override
    public void showToolbar(boolean b) {
        if (b) {
            add(toolbar, java.awt.BorderLayout.NORTH);
        } else {
            remove(toolbar);
        }
    }

    class JTable2da extends JXTable {

        {
            addHighlighter(HighlighterFactory.createAlternateStriping());
            setSortable(false); // sorting doesn't play nice with the
            // 'virtual' last row
        }
        int forbiddenModifiers = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;

        /*process only known keystrokes or keyevents without modifiers,
        so that mnemonics wont trigger cell editing*/
        @Override
        protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
            boolean r = false;
            ActionListener al = getActionForKeyStroke(ks);
            if ((al != null && al instanceof Action ? ((Action) al).isEnabled() : true) || (e.getModifiersEx() & forbiddenModifiers) == 0) {
                r = super.processKeyBinding(ks, e, condition, pressed);
            }
            return r;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            col = convertColumnIndexToModel(col);
            row = convertRowIndexToModel(row);
            mutator.new SetValueAtEdit("Update", value, row, col).invoke();
        }

        @Override
        public void editingStopped(ChangeEvent e) {
            super.editingStopped(e);
            requestFocus();
        }

        @Override
        public void editingCanceled(ChangeEvent e) {
            super.editingCanceled(e);
            requestFocus(); // wonder why this isn't done by default ?!?
        }

        @Override
        public void tableChanged(TableModelEvent e) {
            super.tableChanged(e);
            if (e.getFirstRow() == TableModelEvent.HEADER_ROW) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        resizeColumns();
                    }
                });
            }
        }
    }

    public void addListSelectionListener(ListSelectionListener lsl) {
        table.getSelectionModel().addListSelectionListener(lsl);
        table.getColumnModel().getSelectionModel().addListSelectionListener(lsl);
    }

    public void removeListSelectionListener(ListSelectionListener lsl) {
        table.getSelectionModel().removeListSelectionListener(lsl);
        table.getColumnModel().getSelectionModel().removeListSelectionListener(lsl);
    }

    private void setColumnToolTip(int column, String tooltip) {
        TableCellRenderer r = table.getColumnModel().getColumn(column).getCellRenderer();
        if (r == null) {
            r = new DefaultTableCellRenderer();
            table.getColumnModel().getColumn(column).setCellRenderer(r);
        }
        ((JComponent) r).setToolTipText(tooltip);
    }

    protected class TwoDaSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent lse) {
            if (!lse.getValueIsAdjusting()) {
                updatePositition();
            }
        }

        // code for updating positionLabel
        private void updatePositition() {
            if (table.getSelectedColumnCount() == 0 || table.getSelectedRowCount() == 0) {
                positionLabel.setText("");
                return;
            }
            if (table.getSelectedColumnCount() == 1 && table.getSelectedRowCount() == 1) {
                positionLabel.setText(table.getSelectedRow() + ", " + table.getSelectedColumn());
            } else {
                positionLabel.setText(table.getSelectedRows()[0] + ", " + table.getSelectedColumns()[0] + " - " + table.getSelectedRows()[table.getSelectedRows().length - 1] + ", " + table.getSelectedColumns()[table.getSelectedColumns().length - 1]);
            }
        }
    }

/*
    private void setHeaderToolTip( int column, String tooltip ){
    TableCellRenderer r = table.getColumnModel().getColumn( column ).getHeaderRenderer();
    if ( r == null ){
    r = new DefaultTableCellRenderer();
    table.getColumnModel().getColumn( column ).setHeaderRenderer( r );
    }
    ( (JComponent) r ).setToolTipText( tooltip );
    }
     */

    private void setupColumn(int columnNumber) {
        String label = model.getColumnName(columnNumber);
        TwoDaMetaData.ColumnMetaData cMeta = metaData.get(label);
        if (cMeta != null) {

            TableColumn column = table.getColumnModel().getColumn(columnNumber);
            // if ( cMeta.renderer != null ) column.setCellRenderer( cMeta.renderer );
            boolean useEditor = true;
            if (cMeta.editor != null) {
                //&& cMeta.editor instanceof BitFlagEditor )
                if (useEditor = (TwoDaMetaData.useEditorClass(cMeta.editor.getClass()) && cMeta.useEditor)) {
                    column.setCellEditor(cMeta.editor);
                }
            }
            if (cMeta.renderer != null && useEditor) {
                column.setCellRenderer(cMeta.renderer);
            }
            setColumnToolTip(columnNumber, cMeta.tooltip);
        }
    }

    Action aToggleUserTlk = new AbstractAction("U") {
        private int USERTLKFLAG = 1 << 24;

        @Override
        public void actionPerformed(ActionEvent e) {
            int columnIndex = table.getSelectedColumn();
            if (columnIndex != -1) {
                int[] rows = table.getSelectedRows();
                mutator.beginUpdate();
                for (int i = 0; i < rows.length; i++) {
                    try {
                        String s = model.getValueAt(rows[i], columnIndex).toString();
                        if (s.startsWith("0x")) {
                            int value = Integer.parseInt(s.substring(2), 16);
                            mutator.new SetValueAtEdit("Toggle User StrRef", "0x" + Integer.toHexString(value ^ USERTLKFLAG), rows[i], columnIndex).invoke();
                        } else {
                            int value = Integer.parseInt(s);
                            mutator.new SetValueAtEdit("Toggle User StrRef", Integer.toString(value ^ USERTLKFLAG), rows[i], columnIndex).invoke();
                        }
                    } catch (NumberFormatException nfe) {
                    }
                }
                mutator.endUpdate();
            }
        }
    };

    private TableSearchAndReplace searchAndReplace = null;

    private final Action actFind = new AbstractAction("find") {
        String title = "";

        @Override
        public void actionPerformed(ActionEvent e) {
            if (searchAndReplace == null) {
                searchAndReplace = new TableSearchAndReplace((JFrame) SwingUtilities.getWindowAncestor(table), table) {

                    @Override
                    public void replaceAll() {
                        mutator.beginUpdate();
                        super.replaceAll();
                        mutator.endUpdate();
                    }
                };
                title = searchAndReplace.getTitle() + " - ";
                searchAndReplace.setLocationRelativeTo(toolbar);
            }
            searchAndReplace.setTitle(title + file.getName());
            searchAndReplace.setVisible(true);
        }
    };

    private final Action actFindNext = new AbstractAction("find again") {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (searchAndReplace != null && searchAndReplace.haveMatch()) {
                searchAndReplace.doSearch();
            }
        }
    };


    /*
     * paste from system clippboard, expects whitespace separated values
     */
    final Action actPasteRows = new AbstractAction("paste") {

        //{ super.setEnabled( false ); }
        @Override
        public void actionPerformed(ActionEvent e) {
            int rowNum = table.getSelectedRow();
            int colNum = table.getSelectedColumn();
            int[] selectedRows = table.getSelectedRows();
            int[] selectedCols = table.getSelectedColumns();
            table.editCellAt(-1, 0);
            if (rowNum != -1) {
                table.clearSelection();
                Transferable trans = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                if (trans.isDataFlavorSupported(TlkEdit.FLAVORSTRREF)) {
                    try {
                        String[] a = (String[]) trans.getTransferData(
                                TlkEdit.FLAVORSTRREF);
                        int pasteSize = Math.min(a.length, selectedRows.length * selectedCols.length);
                        if (pasteSize > 1) {
                            mutator.beginUpdate();
                        }
                        for (int i = 0; i < pasteSize; i++) {
                            mutator.setValueAt(a[i], selectedRows[i / selectedCols.length], selectedCols[i % selectedCols.length]);
                        }
                        if (pasteSize > 1) {
                            mutator.endUpdate();
                        }
                    } catch (UnsupportedFlavorException ex) {
                        ex.printStackTrace();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    try {
                        String ins = (String) trans.getTransferData(
                            DataFlavor.stringFlavor);
                        String[] lines = ins.split("\\r?\\n");
                        String[][] newRows = new String[lines.length][];
                        for (int i = 0; i < lines.length; i++) {
                            String[] row = TwoDaTable.split2daLine(lines[i]);
                            if (row.length != model.getColumnCount()) {
                                newRows[i] = new String[model.getColumnCount()];
                                System.arraycopy(row, 0, newRows[i], 0, Math.min(row.length, newRows[i].length));
                                for (int j = row.length; j < newRows[i].length; j++) {
                                    newRows[i][j] = "****";
                                }
                            } else {
                                newRows[i] = row;
                            }
                        }
                        mutator.insertRows(rowNum, Arrays.asList(newRows));
                        table.setRowSelectionInterval(rowNum, rowNum + lines.length - 1);
                    } catch (IllegalArgumentException iaex) {
                        System.err.println(iaex.getMessage());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    };

    static final DataFlavor tsvFlavor = new DataFlavor("text/tab-separated-values;class=java.lang.String", "tab separated values");
    /*
     * copy rows to the internal clipboard ( as List containing String[]
     * values ) and to the system clipboard ( as String with tab separated
     * values )
     */
    final Action actCopyRows = new AbstractAction("copy") {

        @Override
        public void actionPerformed(ActionEvent e) {
            int[] selection = table.getSelectedRows();
            if (selection.length > 0) {
                List rowBuffer = new Vector();
                String[] row;
                String value;
                StringBuffer sb = new StringBuffer();
                for (int r = 0; r < selection.length; r++) {
                    row = new String[model.getColumnCount()];
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        value = (String) model.getValueAt(selection[r], i);
                        row[i] = value;
                        sb.append(value).append('\t');
                    }

                    sb.append(lineSeparator);
                    rowBuffer.add(row);
                }
                final String transfer = sb.toString();
                Transferable trans = new Transferable() {

                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        return transfer;
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor == tsvFlavor;
                    }

                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[]{tsvFlavor, DataFlavor.stringFlavor};
                    }
                };

                //StringSelection ss = new StringSelection(sb.toString());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(trans, null);
            }
        }
    };
    final Action actRemoveSelection = new AbstractAction("remove") {

        @Override
        public void actionPerformed(ActionEvent e) {
            table.removeRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
            int[] selection = table.getSelectedRows();
            if (selection.length > 0) {
                mutator.removeRows(selection);
                table.setRowSelectionInterval(selection[0], selection[0]);
            }
        }
    };

    Action actCut = new AbstractAction("cut") {

        @Override
        public void actionPerformed(ActionEvent e) {
            actCopyRows.actionPerformed(e);
            actRemoveSelection.actionPerformed(e);
        }
    };

    Action actToggleAbsolute = new AbstractAction("x^!") {

        @Override
        public void actionPerformed(ActionEvent e) {
            int column = table.getSelectedColumns()[0];
            int[] rows = table.getSelectedRows();
            String v = "";
            mutator.beginUpdate();
            for (int i = 0; i < rows.length; i++) {
                v = (String) model.getValueAt(rows[i], column);
                if (v.startsWith("!")) {
                    mutator.new SetValueAtEdit("!", v.substring(1), rows[i], column).invoke();
                } else if (!v.startsWith("*")) {
                    mutator.new SetValueAtEdit("!", "!" + v, rows[i], column).invoke();
                }
            }
            mutator.endUpdate();
        }
    };

    Action actRenumber = new AbstractAction("renumber") {

        @Override
        public void actionPerformed(ActionEvent e) {
            mutator.beginUpdate();
            for (int row = 0; row < model.getRowCount() - 1; row++) {
                if (!((String) model.getValueAt(row, 0)).startsWith("!")) {
                    mutator.new SetValueAtEdit("renumber", Integer.toString(row), row, 0).invoke();
                }
            }
            mutator.endUpdate();
        }
    };

    Action alterTable = new AbstractAction() {
        int columnNumber = 0;
        String columnHeader = "";
        String defaultValue = "****";
        final JTextField nrField = new JTextField("0");
        final JTextField cNameField = new JTextField();
        final JTextField defValueField = new JTextField("****");

        private int parseFields() {
            try {
                columnNumber = Integer.parseInt(nrField.getText());
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(alterTableDialog, nrField.getText() + " is not a number", "Error", JOptionPane.ERROR_MESSAGE);
                return 1;
            }
            if (cNameField.getText().indexOf(" ") != -1) {
                JOptionPane.showMessageDialog(alterTableDialog, "column name must not contain spaces", "Error", JOptionPane.ERROR_MESSAGE);
                return 2;
            }
            columnHeader = cNameField.getText();
            defaultValue = defValueField.getText();
            return 0;
        }

        private void init() {
            alterTableDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(TwoDaEdit.this));
            alterTableDialog.setTitle("alter table");
            JPanel p = new JPanel(new GridLayout(3, 2));
            p.add(new JLabel("column number"));
            p.add(nrField);
            p.add(new JLabel("column name"));
            p.add(cNameField);
            p.add(new JLabel("default value"));
            p.add(defValueField);
            JPanel buttons = new JPanel(new GridLayout(1, 3));
            JButton colInsertButton = new JButton(new AbstractAction("insert column") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parseFields() == 0) {
                        try {
                            String[] values = new String[model.getRowCount() - 1];
                            Arrays.fill(values, defaultValue);
                            mutator.new InsertColumnEdit("Insert Column", columnNumber, columnHeader, String.class, values).invoke();
                            resizeColumns();
                            alterTableDialog.setVisible(false);
                        } catch (IllegalArgumentException iae) {
                            JOptionPane.showMessageDialog(alterTableDialog, iae.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            JButton colDropButton = new JButton(new AbstractAction("drop column") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parseFields() == 0) {
                        try {
                            mutator.new DropColumnEdit("Drop Column", columnNumber).invoke();
                            resizeColumns();
                            alterTableDialog.setVisible(false);
                        } catch (IllegalArgumentException iae) {
                            JOptionPane.showMessageDialog(alterTableDialog, iae.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
            JButton colRenameButton = new JButton(new AbstractAction("rename column") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    if (parseFields() == 0) {
                        if (!columnHeader.equals(model.getColumnName(columnNumber))) {
                            try {
                                mutator.new SetColumnNameEdit("Rename Column", columnNumber, columnHeader).invoke();
                                resizeColumns();
                                alterTableDialog.setVisible(false);
                            } catch (IllegalArgumentException iae) {
                                JOptionPane.showMessageDialog(alterTableDialog, iae.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            });
            colInsertButton.setToolTipText("insert column called <column name> at position <column number> and fill with <default value>");
            colDropButton.setToolTipText("drop column at position <column number>");
            colRenameButton.setToolTipText("rename column at position <column number>  as <column name>");
            buttons.add(colInsertButton);
            buttons.add(colDropButton);
            buttons.add(colRenameButton);
            alterTableDialog.getContentPane().setLayout(new BorderLayout());
            alterTableDialog.getContentPane().add(p, BorderLayout.CENTER);
            alterTableDialog.getContentPane().add(buttons, BorderLayout.SOUTH);
            alterTableDialog.pack();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (alterTableDialog == null) {
                init();
            }
            alterTableDialog.setVisible(true);
        }
    };

    public static class TwoDaTableModel extends AbstractTableModel implements RowMutator.RowMutable<String[]>, TableMutator.ColumnMutable<String[]> {

        private TwoDaTable twoDa;

        public TwoDaTableModel(TwoDaTable t) {
            twoDa = t;
        }

        @Override
        public Class getColumnClass(int c) {
            return String.class;
        }

        @Override
        public int getColumnCount() {
            return twoDa.getColumnCount();
        }

        @Override
        public String getColumnName(int col) {
            if (col == 0) {
                return twoDa.getColumnHeader(col).length() == 0 ? "Row No." : twoDa.getColumnHeader(col);
            } else {
                return twoDa.getColumnHeader(col);
            }
        }

        @Override
        public int getRowCount() {
            return twoDa.getRowCount() + 1;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return true;
            //return rowIndex > ???1;
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row > getRowCount() - 2) {
                return "";
            } else {
                return twoDa.getValueAt(row, col);
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            boolean append = false;
            Object old = getValueAt(rowIndex, columnIndex);
            if (rowIndex == getRowCount() - 1) {
                append = true;
                twoDa.appendRow(twoDa.emptyRow());
                twoDa.setValueAt(Integer.toString(rowIndex), rowIndex, 0);
            }
            twoDa.setValueAt(aValue.toString(), rowIndex, columnIndex);
            if (append) {
                fireTableRowsInserted(rowIndex, rowIndex);
            } else {
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public void insertRows(int startIndex, List<String[]> rows) {
            for (int i = 0; i < rows.size(); i++) {
                twoDa.insertRow(rows.get(i), startIndex + i);
            }
            fireTableRowsInserted(startIndex, startIndex + rows.size() - 1);
        }

        @Override
        public List<String[]> removeRows(int[] selection) {
            String[][] r = new String[selection.length][];
            Arrays.sort(selection);
            for (int i = selection.length - 1; i > -1; i--) {
                r[i] = twoDa.removeRow(selection[i]);
            }
            fireTableRowsDeleted(selection[0], selection[selection.length - 1]);
            return Arrays.asList(r);
        }

        public void writeToFile(File file) throws IOException {
            twoDa.writeToFile(file);
        }

        public int getColumnWidth(int i) {
            return twoDa.getColumnWidth(i);
        }

        public String[] emptyRow() {
            return twoDa.emptyRow();
        }

        @Override
        public void insertColumn(int columnNumber, String columnHeader, Class cClass, String[] values) {
            twoDa.insertColumn(columnNumber, columnHeader, values[0]);
            for (int i = 0; i < twoDa.getRowCount(); i++) {
                twoDa.setValueAt(values[i], i, columnNumber);
            }
            fireTableStructureChanged();
        }

        @Override
        public String[] dropColumn(int n) {
            String[] cData = new String[twoDa.getRowCount()];
            for (int i = 0; i < cData.length; i++) {
                cData[i] = twoDa.getValueAt(i, n);
            }
            twoDa.dropColumn(n);
            fireTableStructureChanged();
            return cData;
        }

        @Override
        public void setColumnName(int columnNumber, String columnHeader) {
            twoDa.setColumnHeader(columnNumber, columnHeader);
            fireTableStructureChanged();
        }

        @Override
        public int findColumn(String label) {
            return twoDa.getColumnNumber(label);
        }
    }
}
