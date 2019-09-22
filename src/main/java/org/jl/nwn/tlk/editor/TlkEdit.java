package org.jl.nwn.tlk.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.UIDefaults;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.JTextComponent;

import org.dts.spell.SpellChecker;
import org.dts.spell.dictionary.SpellDictionary;
import org.dts.spell.swing.JSpellDialog;
import org.dts.spell.swing.JSpellPanel;
import org.dts.spell.swing.RealTimeSpellChecker;
import org.dts.spell.swing.event.UIErrorMarkerListener;
import org.dts.spell.swing.finder.DocumentWordFinder;
import org.dts.spell.swing.utils.SeparatorLineBorder;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.FilterPipeline;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.jdesktop.swingx.event.MessageListener;
import org.jdesktop.swingx.event.MessageSource;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;
import org.jl.nwn.editor.SimpleFileEditorPanel;
import org.jl.nwn.spell.Dictionaries;
import org.jl.nwn.spell.TlkWordFinder;
import org.jl.nwn.tlk.DefaultTlkReader;
import org.jl.nwn.tlk.TlkContent;
import org.jl.nwn.tlk.TlkEntry;
import org.jl.nwn.tlk.TlkLookup;
import org.jl.nwn.twoDa.cellEditors.BitFlagEditor;
import org.jl.swing.Actions;
import org.jl.swing.I18nUtil;
import org.jl.swing.table.CellEditorDecorator;
import org.jl.swing.table.StringPopupCellEditor;
import org.jl.swing.table.TextCellEditor;
import org.jl.swing.undo.MappedListSelectionModel;
import org.jl.swing.undo.MyUndoManager;
import org.jl.swing.undo.RowMutator;

public class TlkEdit extends SimpleFileEditorPanel implements PropertyChangeListener, MessageSource {

    private Preferences uPrefs = Preferences.userNodeForPackage(TlkEdit.class);

    private MessageSourceSupport messageSupport = new MessageSourceSupport(this);

    @Override
    public void removeMessageListener(MessageListener l) {
        messageSupport.removeMessageListener(l);
    }

    @Override
    public MessageListener[] getMessageListeners() {
        return messageSupport.getMessageListeners();
    }

    @Override
    public void addMessageListener(MessageListener l) {
        messageSupport.addMessageListener(l);
    }

    private static final UIDefaults uid = new UIDefaults();
    //private static final UIDefaults uid = UIManager.getDefaults();
    private static boolean useSound;
    static {
        uid.addResourceBundle("org.jl.nwn.tlk.editor.MessageBundle");
        /*
        uid.addResourceBundle("org.jl.nwn.tlk.editor.MessageBundle",
        Locale.getDefault(),
        TlkEdit.class.getClassLoader() );
         */
        uid.addResourceBundle("settings.keybindings");
        try {
            Class.forName("javazoom.jl.player.Player");
            System.out.println("zoom player found, sound playback enabled");
            useSound = true;
        } catch (ClassNotFoundException e1) {
            //System.out.println( "zoom player not found, sound playback disabled" );
            useSound = false;
        }
    }

    protected TlkContent tlkContent;
    protected TlkModel model;
    private ButtonModel isUserTlkBM;
    protected MyUndoManager undoManager = new MyUndoManager();
    protected TlkModelMutator mutator;
    protected RowMutator rowMutator;
    //protected ListMutator listMutator;
    Action aUndo;
    Action aRedo;
    Action aPasteOver;

    // for the language selector radio buttons
    final String languagePropertyKey = "nwLang";
    ButtonGroup languageButtons = new ButtonGroup();

    private File tlkFile;

    protected JXTable tlkTable;
    protected TableColumn col_StrRef = new TableColumn(0, 120);
    protected TableColumn col_SoundResRef = new TableColumn(1, 150);
    protected TableColumn col_String = new TableColumn(2, 500);
    protected TableColumn col_SoundLength = new TableColumn(3, 100);
    protected TableColumn col_Flags = new TableColumn(4, 50);
    protected BitFlagEditor flagEditor;

    private final JToolBar toolbar;
    private static final byte[] tlkHead = {0x54, 0x4c, 0x4b, 0x20}; // "TLK "
    private static final String ERROR_DIALOG_TITLE = uid.getString("TlkEdit.error_dialog_title"); //$NON-NLS-1$
    private JMenu diffMenu = null;
    private JMenu editMenu = null;
    private JMenu langSubMenu = null;

    private JMenu viewMenu = null;
    private JPopupMenu headerPopup = null;

    protected boolean noRealTimeSpellChecking = false;

    protected final TextCellEditor cellEditor = new StringPopupCellEditor() {
        SpellChecker checker = null;
        SpellDictionary dict = null;
        RealTimeSpellChecker rtChecker = null;
        DocumentWordFinder finder = null;

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            Component c = super.getTableCellEditorComponent(table, value, isSelected, row, column);
            dict = Dictionaries.forLanguage(tlkContent.getLanguage());
            if (!noRealTimeSpellChecking && dict != null) {
                if (checker == null) {
                    checker = new SpellChecker(dict);
                    finder = new DocumentWordFinder(textArea.getDocument());
                    rtChecker = new RealTimeSpellChecker(checker, textArea, finder);
                } else {
                    checker.setDictionary(dict);
                    finder.setDocument(textArea.getDocument());
                }
                rtChecker.start();
            }
            return c;
        }

        @Override
        public boolean stopCellEditing() {
            tlkTable.setRowHeight(tlkTable.getEditingRow(), tlkTable.getRowHeight()); //default row height is 16
            stopMarkErrors();
            super.stopCellEditing();
            tlkTable.requestFocus();
            return true;
        }

        @Override
        public void cancelCellEditing() {
            tlkTable.setRowHeight(tlkTable.getEditingRow(), tlkTable.getRowHeight()); //default row height is 16
            super.cancelCellEditing();
            tlkTable.requestFocus();

            stopMarkErrors();
        }

        protected void stopMarkErrors() {
            if (rtChecker != null) {
                rtChecker.stop();
            }
        }
    };

    private MouseAdapter headerPopupListener = new MouseAdapter() {

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                headerPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    };

    // methods for SimpleFileEditor
    @Override
    public boolean canSave() {
        return tlkFile != null;
    }

    @Override
    public boolean canSaveAs() {
        return true;
    }

    @Override
    public void close() {
        if (flagEditor != null) {
            flagEditor.dispose();
        }
        if (searchAndReplace != null) {
            searchAndReplace.dispose();
        }
    }

    @Override
    public File getFile() {
        return tlkFile;
    }

    @Override
    public void save() throws IOException {
        saveAs(tlkFile, nwnVersion);
    }

    @Override
    public void saveAs(File f, Version nwnVersion) throws IOException {
        tlkContent.saveAs(f, nwnVersion);
        this.nwnVersion = nwnVersion;
        tlkFile = f;
        mutator.stateSaved();
    }

    public static boolean accept(File f) {
        byte[] test = new byte[4];
        try {
            InputStream in = new FileInputStream(f);
            in.read(test);
            in.close();
            for (int i = 0; i < test.length; i++) {
                if (test[i] != tlkHead[i]) {
                    return false;
                }
            }
            return true;
        } catch (IOException ioex) {
        }
        return false;
    }

    public TlkEdit() {
        tlkTable = new JXTable() {

            @Override
            public Component prepareEditor(TableCellEditor editor, int row, int column) {
                Component c = super.prepareEditor(editor, row, column);
                scrollRectToVisible(getCellRect(row, column, true));
                return c;
            }

            int forbiddenModifiers = InputEvent.ALT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK | InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK;

            /*process only known keystrokes or keyevents without modifiers, so that
            mnemonics wont trigger cell editing*/
            @Override
            protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
                boolean r = false;
                ActionListener al = getActionForKeyStroke(ks);
                if ((al != null && al instanceof Action && ((Action) al).isEnabled()) || (e.getModifiersEx() & forbiddenModifiers) == 0) {
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
        };

        tlkContent = new TlkContent(NwnLanguage.ENGLISH);
        model = new TlkModel(tlkContent);
        model.addPropertyChangeListener(this);
        ListSelectionModel mappedLsl = MappedListSelectionModel.createRowModelToViewMapper(tlkTable);
        mutator = new TlkModelMutator(model, mappedLsl);
        rowMutator = new RowMutator(mutator, model, mappedLsl);
        mutator.addUndoableEditListener(undoManager);

        tlkTable.setTransferHandler(transferHandler);
        //tlkTable.putClientProperty("JTable.autoStartsEdit", new Boolean(false));
        tlkTable.setAutoCreateColumnsFromModel(false);
        tlkTable.setModel(model);
        tlkTable.getTableHeader().setReorderingAllowed(false);
        tlkTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

        col_StrRef.setMaxWidth(120);
        DefaultTableCellRenderer strRefRenderer = new DefaultTableCellRenderer();
        strRefRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        strRefRenderer.setFont(Font.getFont(Font.MONOSPACED));
        col_StrRef.setCellRenderer(strRefRenderer);

        col_SoundResRef.setMinWidth(0);
        col_SoundResRef.setMaxWidth(150);
        col_String.setMinWidth(300);
        col_SoundLength.setMaxWidth(100);
        col_SoundLength.setMinWidth(0);
        col_Flags.setMaxWidth(50);
        col_Flags.setMinWidth(50);
        tlkTable.addColumn(col_StrRef);
        tlkTable.addColumn(col_SoundResRef);
        tlkTable.addColumn(col_String);
        tlkTable.addColumn(col_SoundLength);
        tlkTable.addColumn(col_Flags);

        tlkTable.moveColumn(1, 2);

        // cell editors  ------------------------------------------------------------
        col_String.setCellEditor((TableCellEditor) cellEditor);

        String[] flagStrings = new String[]{uid.getString("TlkEdit.flags_label_useText"), uid.getString("TlkEdit.flags_label_useSound"), uid.getString("TlkEdit.flags_label_useSoundLength")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        flagEditor = new BitFlagEditor(flagStrings, new int[]{1, 2, 4});
        TableCellEditor flagEd = new CellEditorDecorator(flagEditor) {

            @Override
            public Object getCellEditorValue() {
                String s = (String) super.getCellEditorValue();
                try {
                    return Byte.parseByte(s.indexOf('x') == -1 ? s : s.substring(2));
                } catch (NumberFormatException nfe) {
                    return 1;
                }
            }
        };

        col_Flags.setCellEditor(flagEd);
        // set up editor for resource name ----------------------------------------------------
        KeyListener myKeyListener = new KeyAdapter() {
            String prohibited = "\\/ :*.?\"<>| "; //$NON-NLS-1$

            @Override
            public void keyTyped(KeyEvent e) {
                JTextField tf = (JTextField) e.getSource();
                if (prohibited.indexOf(e.getKeyChar()) != -1) {
                    e.consume();
                }
                if (tf.getText().length() > 15 && e.getKeyChar() != (char) KeyEvent.VK_BACK_SPACE) {
                    e.consume();
                }
            }
        };
        JTextField textResName = new JTextField(16);
        textResName.addKeyListener(myKeyListener);
        DefaultCellEditor resRefEditor = new javax.swing.DefaultCellEditor(textResName) {
            BmuPlayer snd = null;

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                if (useSound && value.toString().length() > 0) {
                    //System.out.println("try to play sound");
                    try {
                        new Thread(snd = new BmuPlayer(tlkTable.getModel().getValueAt(row, 1).toString().toLowerCase(), null)).start();
                    } catch (FileNotFoundException fnfe) {
                        System.out.println("sound resource not found : " + table.getModel().getValueAt(row, 1) + ".wav"); //$NON-NLS-1$
                    } /*
                    catch ( UnsupportedAudioFileException uafe ){
                    System.out.println("unknown audio file format - no mp3 SP installed ?");
                    } */ catch (Exception ex) {
                        //System.out.println(ex);
                        //ex.printStackTrace();
                    }
                }
                return super.getTableCellEditorComponent(table, value, isSelected, row, column);
            }

            @Override
            public boolean stopCellEditing() {
                fireEditingStopped();
                if (snd != null) {
                    snd.close();
                }
                tlkTable.requestFocus();
                return true;
            }
        };
        col_SoundResRef.setCellEditor(resRefEditor);
        tlkTable.setSurrendersFocusOnKeystroke(true);

        // set up toolbar actions ( save etc. ) ---------------------------------------------------------
        JTextField posField = new JTextField(6);
        posField.addKeyListener(new KeyAdapter() {
            int line = 0;

            @Override
            public void keyTyped(KeyEvent e) {
                JTextField tf = (JTextField) e.getSource();
                String s = tf.getText();
                if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
                    if (s.length() > 0) {
                        s = s.substring(0, s.length() - 1);
                    } else {
                        s = "0"; //$NON-NLS-1$
                    }
                } else if (Character.isDigit(e.getKeyChar())) {
                    //System.out.println( tf.getText() + e.getKeyChar() );
                    s = s + e.getKeyChar();
                } else {
                    // if (Character.isLetterOrDigit(e.getKeyChar()))
                    e.consume();
                }
                try {
                    line = Integer.parseInt(s);
                    if ((line & TlkLookup.USERTLKOFFSET) > 0) {
                        line = line ^ TlkLookup.USERTLKOFFSET;
                    }
                } catch (NumberFormatException nfe) {
                }
                if (line < tlkTable.getRowCount() - 1) {
                    tlkTable.changeSelection(line, 0, false, false);
                }
            }
        });

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        boolean useIcons = true;
        boolean useText = false;

        Actions.configureActionUI(actResize, uid, "TlkEdit.resize");

        Actions.configureActionUI(actCut, uid, "TlkEdit.cut");
        JButton cutButton = toolbar.add(actCut);
        cutButton.setMnemonic(KeyEvent.VK_UNDEFINED);

        Actions.configureActionUI(actCopy, uid, "TlkEdit.copy");
        JButton copyButton = toolbar.add(actCopy);

        Actions.configureActionUI(actPaste, uid, "TlkEdit.paste");
        JButton pasteButton = toolbar.add(actPaste);

        toolbar.addSeparator();

        Actions.configureActionUI(actFind, uid, "TlkEdit.find");
        JButton findButton = toolbar.add(actFind);

        Actions.configureActionUI(actFindNext, uid, "TlkEdit.findNext");
        JButton findNextButton = toolbar.add(actFindNext);


        toolbar.addSeparator();
        JLabel posLabel = new JLabel("", JLabel.RIGHT); //$NON-NLS-1$
        posLabel.setLabelFor(posField);
        I18nUtil.setText(posLabel, uid.getString("TlkEdit.label_positionField")); //$NON-NLS-1$
        toolbar.add(posLabel);
        toolbar.add(posField);
        posField.setMaximumSize(posField.getPreferredSize());

        JTextField filterField = new JTextField(12);
        final PatternFilter filter = new PatternFilter();
        filter.setColumnIndex(2);
        tlkTable.setFilters(new FilterPipeline(filter));
        filterField.addKeyListener(new KeyAdapter() {

            @Override
            public void keyTyped(KeyEvent e) {
                JTextField tf = (JTextField) e.getSource();
                String s = tf.getText();
                if (e.getKeyChar() == KeyEvent.VK_ENTER) {
                    if (s.length() > 0) {
                        try {
                            final Pattern p = Pattern.compile(s);
                            tlkTable.setEnabled(false);
                            messageSupport.fireMessage(uid.getString("TlkEdit.applyingPattern"));
                            SwingUtilities.invokeLater(new Runnable() {

                                @Override
                                public void run() {
                                    try {
                                        filter.setPattern(p);
                                        messageSupport.fireMessage(MessageFormat.format(uid.getString("TlkEdit.patternApplied"), filter.getSize(), p));
                                    } finally {
                                        tlkTable.setEnabled(true);
                                    }
                                }
                            });
                        } catch (PatternSyntaxException pse) {
                            messageSupport.fireMessage(MessageFormat.format(uid.getString("TlkEdit.invalidPattern"), s, pse.getMessage()));
                        }
                    } else {
                        filter.setPattern(null);
                    }
                }
            }
        });
        JLabel filterLabel = new JLabel("", JLabel.RIGHT); //$NON-NLS-1$
        filterLabel.setLabelFor(filterField);
        I18nUtil.setText(filterLabel, uid.getString("TlkEdit.label_filterField")); //$NON-NLS-1$
        filterLabel.setToolTipText(uid.getString("TlkEdit.tooltip_filterField"));
        toolbar.add(filterLabel);
        toolbar.add(filterField);
        filterField.setMaximumSize(filterField.getPreferredSize());

        toolbar.addSeparator();
        setupDiffStuff();

        editMenu = new JMenu();
        I18nUtil.setText(editMenu, "&Edit");
        JMenuItem miCut = editMenu.add(actCut);
        JMenuItem miCopy = editMenu.add(actCopy);
        JMenuItem miPaste = editMenu.add(actPaste);
        editMenu.addSeparator();
        JMenuItem miFind = editMenu.add(actFind);
        JMenuItem miFindNext = editMenu.add(actFindNext);
        editMenu.addSeparator();
        editMenu.add(actResize);

        langSubMenu = new JMenu();
        I18nUtil.setText(langSubMenu, "&Language");
        langSubMenu.setIcon(uid.getIcon(Actions.EMPTYICONKEY));
        buildLanguageMenu(getFileVersion());
        editMenu.add(langSubMenu);

        viewMenu = new JMenu();
        I18nUtil.setText(viewMenu, "&View");
        JCheckBoxMenuItem miShowFlags = new JCheckBoxMenuItem(actToggleFlagDisplay);
        miShowFlags.setSelected(true);
        I18nUtil.setText(miShowFlags, "Show &Flags");
        viewMenu.add(miShowFlags);
        JCheckBoxMenuItem miShowSound = new JCheckBoxMenuItem(actToggleSoundDisplay);
        miShowSound.setSelected(true);
        I18nUtil.setText(miShowSound, "Show &Sound Settings");
        viewMenu.add(miShowSound);
        viewMenu.addSeparator();
        Actions.configureActionUI(aToggleUserTlk, uid, "TlkEdit.toggleStrRef");
        JCheckBoxMenuItem miToggleNumbering = new JCheckBoxMenuItem(aToggleUserTlk);
        isUserTlkBM = miToggleNumbering.getModel();
        miToggleNumbering.setSelected(false);
        miToggleNumbering.setIcon(null);
        viewMenu.add(miToggleNumbering);
        Actions.configureActionUI(aToggleHexDisplay, uid, "TlkEdit.toggleHex");
        JCheckBoxMenuItem miToggleHex = new JCheckBoxMenuItem(aToggleHexDisplay);
        miToggleHex.setIcon(null);
        viewMenu.add(miToggleHex);

        headerPopup = new JPopupMenu();
        //headerPopup = viewMenu.getPopupMenu();
        JMenuItem pop1 = headerPopup.add(new JCheckBoxMenuItem(actToggleFlagDisplay));
        I18nUtil.setText(pop1, "Show &Flags");
        pop1.setModel(miShowFlags.getModel());
        JMenuItem pop2 = headerPopup.add(new JCheckBoxMenuItem(actToggleSoundDisplay));
        I18nUtil.setText(pop2, "Show &Sound Settings");
        pop2.setModel(miShowSound.getModel());
        tlkTable.getTableHeader().addMouseListener(headerPopupListener);
        miShowFlags.doClick();
        miShowSound.doClick();

        aUndo = undoManager.getUndoAction();
        aRedo = undoManager.getRedoAction();
        Actions.configureActionUI(aUndo, uid, "undo");
        Actions.configureActionUI(aRedo, uid, "redo");

        Actions.configureActionUI(aCheckSpelling, uid, "spellcheck");
        editMenu.addSeparator();
        editMenu.add(aCheckSpelling);

        editMenu.addSeparator();
        editMenu.add(aUndo);
        editMenu.add(aRedo);

        mutator.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("modified")) {
                    setIsModified(((Boolean) evt.getNewValue()).booleanValue());
                }
            }
        });

        // setup key bindings
        Actions.registerActions(tlkTable.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), tlkTable.getActionMap(), aUndo, aRedo, aCheckSpelling, actCut, actCopy, actPaste, actFind, actFindNext, aToggleUserTlk);
        toolbar.add(Box.createHorizontalGlue());
        // remove mnemonics from buttons with icon ...
        for (Object o : toolbar.getComponents()) {
            if (o instanceof AbstractButton) {
                if (((AbstractButton) o).getIcon() != null) {
                    ((AbstractButton) o).setMnemonic(KeyEvent.VK_UNDEFINED);
                }
            }
        }
        setLayout(new java.awt.BorderLayout());
        JScrollPane sPane = new JScrollPane(tlkTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(sPane, java.awt.BorderLayout.CENTER);
        showToolbar(true);

        setSize(new java.awt.Dimension(800, 600));
        setVisible(true);
    }

    public void load(File f, ProgressMonitor pm, Version nwnVersion) {
        try {
            tlkContent = new DefaultTlkReader(nwnVersion).load(f, pm);
            this.nwnVersion = nwnVersion;
            System.out.println("TlkEdit load: tlk file language is : " + tlkContent.getLanguage());
            System.out.println("TlkEdit load: tlkcontent encoding : " + tlkContent.getLanguage().getEncoding());
            //selectLanguageButton(tlkContent.getLanguage());
            model.setTlkContent(tlkContent);
            undoManager.discardAllEdits();
            mutator.stateSaved();
            Object oldValue = tlkFile;
            tlkFile = f;
            setFileVersion(nwnVersion);
            firePropertyChange(FILE_PROPERTY, oldValue, f);
            //System.out.println(tlkContent.size() + " entries read");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, uid.getString("TlkEdit.openTlkFile_errorMsgCouldNotOpen") + "\n" + ex, ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void selectLanguageButton(NwnLanguage lang) {
        Enumeration<AbstractButton> e = languageButtons.getElements();
        while (e.hasMoreElements()) {
            AbstractButton b = e.nextElement();
            if (lang.equals(b.getClientProperty(languagePropertyKey))) {
                b.setSelected(true);
                //System.out.println("language set to : " + lang);
            }
        }
    }

    private void buildLanguageMenu(Version v) {
        Enumeration<AbstractButton> e = languageButtons.getElements();
        langSubMenu.removeAll();
        while (e.hasMoreElements()) {
            languageButtons.remove(e.nextElement());
        }
        for (NwnLanguage lang : NwnLanguage.findAll(v)) {
            JMenuItem mi = new JRadioButtonMenuItem(lang.getName());
            if (tlkContent.getLanguage().equals(lang))
                mi.setSelected(true);
            mi.addActionListener(langSelectAction);
            mi.putClientProperty(languagePropertyKey, lang);
            langSubMenu.add(mi);
            languageButtons.add(mi);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals("language")) {
            selectLanguageButton((NwnLanguage) e.getNewValue());
        }
    }

    // diff --------------------------------------------------------------------
    private void setupDiffStuff() {
        final String set_modified = uid.getString("TlkEdit.diff_buttonLabelMarkAsModified"); //$NON-NLS-1$
        JPanel p = new JPanel();
        final JFileChooser fc = new JFileChooser();
        Action toggle = new AbstractAction(set_modified) {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean modified = false;
                modified = ((AbstractButton) e.getSource()).getText() == set_modified;
                for (final int row : tlkTable.getSelectedRows()) {
                    model.setEntryModified(row, modified);
                }
                tlkTable.requestFocus();
            }
        };

        Action loadDiff = new AbstractAction(uid.getString("TlkEdit.diff_buttonLabelMergeDiff")) {

            //$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = new File(uPrefs.get("lastDiff", ".")); //$NON-NLS-1$ //$NON-NLS-2$
                fc.setSelectedFile(f);
                if (fc.showOpenDialog(toolbar) == JFileChooser.APPROVE_OPTION) {
                    try {
                        for (final int change : tlkContent.mergeDiff(f = fc.getSelectedFile())) {
                            model.setEntryModified(change, true);
                        }
                        uPrefs.put("lastDiff", f.getAbsolutePath()); //$NON-NLS-1$
                    } catch (IOException ioex) {
                        JOptionPane.showMessageDialog(toolbar, uid.getString("TlkEdit.diff_errorMsgCouldNotOpenFile"), ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                        ioex.printStackTrace();
                    }
                }
            }
        };

        Action loadDtu = new AbstractAction(uid.getString("TlkEdit.diff_buttonLabelMergeDtu")) {

            //$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent e) {
                File f = new File(uPrefs.get("lastDtu", ".")); //$NON-NLS-1$ //$NON-NLS-2$
                fc.setSelectedFile(f);
                if (fc.showOpenDialog(toolbar) == JFileChooser.APPROVE_OPTION) {
                    try {
                        for (final int change : tlkContent.mergeDtu(f = fc.getSelectedFile())) {
                            model.setEntryModified(change, true);
                        }
                        uPrefs.put("lastDtu", f.getAbsolutePath()); //$NON-NLS-1$
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                        JOptionPane.showMessageDialog(toolbar, uid.getString("TlkEdit.diff_errorMsgCouldNotOpenFile"), ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };

        final String save_diff_button = uid.getString("TlkEdit.diff_buttonLabelSaveDiff"); //$NON-NLS-1$
        Action save = new AbstractAction(save_diff_button) {

            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println( ((AbstractButton)e.getSource()).getText() );
                File f = new File(uPrefs.get("lastDiff", ".")); //$NON-NLS-1$ //$NON-NLS-2$
                fc.setSelectedFile(f);
                if (fc.showSaveDialog(toolbar) == JFileChooser.APPROVE_OPTION) {
                    try {
                        TreeSet ts = new TreeSet();
                        for (int i = 0, n = model.size(); i < n; i++) {
                            if (model.getEntryModified(i)) {
                                ts.add(i);
                            }
                        }
                        int[] newDiffs = new int[ts.size()];
                        int itCount = 0;
                        for (Iterator it = ts.iterator(); it.hasNext(); itCount++) {
                            newDiffs[itCount] = ((Integer) it.next()).intValue();
                        }
                        tlkContent.writeDiff(fc.getSelectedFile(), newDiffs);
                        uPrefs.put("lastDiff", fc.getSelectedFile().getAbsolutePath());
                        setIsModified(false); //isModified = false;
                    } catch (IOException ioex) {
                        JOptionPane.showMessageDialog(null, uid.getString("TlkEdit.diff_errorMsgCouldNotSaveDiff") + ioex.getMessage(), ERROR_DIALOG_TITLE, JOptionPane.ERROR_MESSAGE);
                        ioex.printStackTrace();
                    }
                }
            }
        };

        Action discard = new AbstractAction(uid.getString("TlkEdit.diff_buttonLabelDiscardInfo")) {

            //$NON-NLS-1$
            @Override
            public void actionPerformed(ActionEvent e) {
                for (int i = 0, n = model.size(); i < n; i++) {
                    TlkEntry entry = (TlkEntry) model.getEditorEntry(i);
                    if (entry instanceof EditorTlkEntry) {
                        ((EditorTlkEntry) entry).setModified(false);
                    }
                    ((AbstractTableModel) tlkTable.getModel()).fireTableDataChanged();
                }
            }
        };

        Action overview = new AbstractAction(uid.getString("TlkEdit.diff_buttonLabelDisplayOverview")) {
            //$NON-NLS-1$
            JDialog diffOverview = null;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (diffOverview == null) {
                    diffOverview = createDialog();
                }
                if (!diffOverview.isDisplayable()) {
                    diffOverview.pack();
                }
                diffOverview.setVisible(true);
            }

            protected JDialog createDialog() {
                return new JDialog((JFrame) SwingUtilities.getWindowAncestor(tlkTable)) {

                    public TableModelListener tml = new TableModelListener() {

                        @Override
                        public void tableChanged(TableModelEvent e) {
                            update();
                        }
                    };
                    JList list = new JList();
                    DefaultListModel listModel = new DefaultListModel();
                    {
                        tlkTable.getModel().addTableModelListener(tml);
                        setTitle(uid.getString("TlkEdit.diff_overviewDialogTitle")); //$NON-NLS-1$
                        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        list.setModel(listModel);
                        getContentPane().setLayout(new BorderLayout());
                        getContentPane().add(new JScrollPane(list), BorderLayout.CENTER);
                        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                        list.addListSelectionListener(new ListSelectionListener() {

                            @Override
                            public void valueChanged(ListSelectionEvent e) {
                                if (!e.getValueIsAdjusting()) {
                                    Integer r = (Integer) list.getModel().getElementAt(
                                            list.getSelectedIndex());
                                    //System.out.println( r );
                                    tlkTable.changeSelection(r.intValue(), 1, false, false);
                                }
                            }
                        });
                        Action aUpdate = new AbstractAction(uid.getString("TlkEdit.diff_buttonLabelUpdateList")) {

                            //$NON-NLS-1$
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                update();
                            }
                        };
                    }

                    private void update() {
                        if (!isVisible()) {
                            return;
                        }
                        //Object[] o = new Object[ diff.diffMap.keySet().size() ];
                        try {
                            listModel.clear();
                        } catch (ArrayIndexOutOfBoundsException e) {
                        }
                        for (int i = 0; i < model.size(); i++) {
                            TlkEntry e = model.getEditorEntry(i);
                            if (e instanceof EditorTlkEntry && ((EditorTlkEntry) e).isModified()) {
                                listModel.addElement(i);
                            }
                        }
                    }

                    @Override
                    public void setVisible(boolean visibleState) {
                        pack();
                        super.setVisible(visibleState);
                        update();
                    }
                };
            }
        };

        diffMenu = new JMenu();
        I18nUtil.setText(diffMenu, uid.getString("TlkEdit.diff_menuLabel")); //$NON-NLS-1$
        diffMenu.setToolTipText(uid.getString("TlkEdit.diff_menuTooltip")); //$NON-NLS-1$
        diffMenu.add(toggle).setAccelerator(KeyStroke.getKeyStroke("alt M"));
        JMenuItem miUnmod = diffMenu.add(toggle);
        miUnmod.setText(uid.getString("TlkEdit.diff_buttonLabelMarkUnmodified")); //$NON-NLS-1$
        miUnmod.setMnemonic('u');
        miUnmod.setAccelerator(KeyStroke.getKeyStroke("alt shift M"));
        diffMenu.addSeparator();
        diffMenu.add(loadDiff).setMnemonic('m');
        diffMenu.add(save).setMnemonic('s');
        diffMenu.addSeparator();
        diffMenu.add(loadDtu).setMnemonic('u');
        diffMenu.addSeparator();
        diffMenu.add(discard);
        diffMenu.addSeparator();
        diffMenu.add(overview).setMnemonic('o');
    }

    Action aToggleUserTlk = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = !model.getIsUserTlk();
            isUserTlkBM.setSelected(b);
            model.setIsUserTlk(b);
            //tlkTable.repaint();
        }
    };

    Action aToggleHexDisplay = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = !model.isDisplayHex();
            //isUserTlkBM.setSelected(b);
            model.setDisplayHex(b);
            //tlkTable.repaint();
        }
    };

    @Override
    public JMenu[] getMenus() {
        return new JMenu[]{editMenu, viewMenu, diffMenu};
    }

    public static void removePreferences() throws BackingStoreException {
        java.util.prefs.Preferences.userNodeForPackage(TlkEdit.class).removeNode();
    }

    private Action actResize = new AbstractAction(uid.getString("TlkEdit.resize_buttonLabel")) {

        //$NON-NLS-1$
        @Override
        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog((JComponent) e.getSource(), uid.getString("TlkEdit.resize_enterNewSize"), model.size());
            if (s == null) {
                return;
            }
            int newSize = 0;
            String invalid_input_msg = uid.getString("TlkEdit.resize_errorMsgInvalidInput"); //$NON-NLS-1$
            String invalid_input_dialog_title = uid.getString("TlkEdit.resize_errorDialogTitleInvalidInput"); //$NON-NLS-1$
            try {
                newSize = Integer.parseInt(s);
            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog((JComponent) e.getSource(), invalid_input_msg, invalid_input_dialog_title, JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newSize < 0) {
                JOptionPane.showMessageDialog((JComponent) e.getSource(), invalid_input_msg, invalid_input_dialog_title, JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (newSize < model.size()) {
                // remove
                int[] indexes = new int[model.size() - newSize];
                for (int i = 0; i < indexes.length; i++) {
                    indexes[i] = newSize + i;
                }
                //listMutator.remove(indexes);
                rowMutator.new RemoveRowsEdit("Resize", indexes).invoke();
            } else {
                List<TlkEntry> entries = new ArrayList<TlkEntry>(newSize - model.size());
                for (int i = 0; i < newSize - model.size(); i++) {
                    entries.add(new EditorTlkEntry());
                }
                rowMutator.new InsertRowsEdit("Resize", model.size(), entries).invoke();
                //listMutator.addAll(model.size(), entries);
            }
        }
    };

    protected final Action actCopy = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            transferHandler.exportToClipboard(tlkTable, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.COPY);
        }
    };
    protected final Action actCut = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            transferHandler.exportToClipboard(tlkTable, Toolkit.getDefaultToolkit().getSystemClipboard(), TransferHandler.MOVE);
        }
    };
    protected final Action actPaste = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            transferHandler.importData(tlkTable, Toolkit.getDefaultToolkit().getSystemClipboard().getContents(transferHandler));
        }
    };

    private final Action actToggleFlagDisplay = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractButton btn = (AbstractButton) e.getSource();
            boolean showFlags = btn.isSelected();
            if (showFlags) {
                if (!columnVisible(col_Flags)) {
                    tlkTable.addColumn(col_Flags);
                }
            } else {
                if (columnVisible(col_Flags)) {
                    tlkTable.removeColumn(col_Flags);
                }
            }
        }
    };

    private final Action actToggleSoundDisplay = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            AbstractButton btn = (AbstractButton) e.getSource();
            boolean showFlags = btn.isSelected();
            TableColumnModel cm = tlkTable.getColumnModel();
            if (showFlags) {
                if (!columnVisible(col_SoundResRef)) {
                    cm.addColumn(col_SoundResRef);
                    cm.addColumn(col_SoundLength);
                    cm.moveColumn(cm.getColumnCount() - 1, 2);
                    cm.moveColumn(cm.getColumnCount() - 1, 2);
                }
            } else {
                if (columnVisible(col_SoundResRef)) {
                    tlkTable.removeColumn(col_SoundResRef);
                    tlkTable.removeColumn(col_SoundLength);
                }
            }
        }
    };

    private boolean columnVisible(TableColumn col) {
        TableColumnModel cm = tlkTable.getColumnModel();
        for (int i = 0; i < cm.getColumnCount(); i++) {
            if (col == cm.getColumn(i)) {
                return true;
            }
        }
        return false;
    }

    private ActionListener langSelectAction = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            mutator.new LanguageEdit("Set Language", (NwnLanguage) ((AbstractButton)e.getSource())
                        .getClientProperty(languagePropertyKey)).invoke();
        }
    };

    private TlkSearchDialog searchAndReplace = null;

    private final Action actFind = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (searchAndReplace == null) {
                Window w = SwingUtilities.getWindowAncestor(TlkEdit.this);
                JFrame owner = (w instanceof JFrame) ? (JFrame) w : null;
                searchAndReplace = new TlkSearchDialog(owner, TlkEdit.this);
                searchAndReplace.setLocationRelativeTo(tlkTable);
            }
            searchAndReplace.setVisible(true);
        }
    };

    private final Action actFindNext = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (searchAndReplace != null && searchAndReplace.haveMatch()) {
                searchAndReplace.doSearch();
            }
        }
    };

    protected Action aCheckSpelling = new AbstractAction() {
        JSpellDialog d = null;
        JSpellPanel sPanel = null;
        SpellChecker checker = null;
        SpellDictionary dict = null;
        DocumentWordFinder finder = null;
        //SpellCheckListener checkListener = null;
        boolean cancel = false;
        TlkWordFinder tlkFinder;
        UIErrorMarkerListener marker;

        ActionListener al = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancel = true;
                d.cancel();
                //d.setVisible(false);
            }
        };

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                noRealTimeSpellChecking = false;
                dict = Dictionaries.forLanguage(model.getLanguage());
                if (dict == null) {
                    return;
                }
                if (checker == null) {
                    init();
                }
                checker.setDictionary(dict);
                cancel = false;
                for (int row = 0; !cancel && row < tlkTable.getRowCount() - 1; row++) {
                    String s = String.valueOf(tlkTable.getValueAt(row, 1));
                    tlkFinder.setEntry(row);
                    if (!checker.isCorrect(tlkFinder)) {
                        tlkTable.editCellAt(row, 1);
                        JTextComponent tc = cellEditor.getTextComponent();
                        if (finder == null) {
                            finder = new DocumentWordFinder(tc.getDocument());
                        } else {
                            finder.setDocument(tc.getDocument());
                        }
                        checker.check(finder, marker);
                    }
                }
            } finally {
                noRealTimeSpellChecking = false;
            }
        }

        private void init() {
            checker = new SpellChecker(dict);
            //checker.setCaseSensitive(false);
            sPanel = new JSpellPanel();

            tlkFinder = new TlkWordFinder(model) {

                @Override
                public void updateModel(String s, int row) {
                    tlkTable.setValueAt(s, row, 1);
                }
            };
            d = new JSpellDialog((JFrame) SwingUtilities.getWindowAncestor(tlkTable), sPanel);
            marker = new UIErrorMarkerListener(d);
            //checkListener = new UISpellCheckListener(d);
            marker.setTextComponent(cellEditor.getTextComponent());
            sPanel.setCancelListener(al);

            //JPanel p = (JPanel)((BorderLayout)sPanel.getLayout()).getLayoutComponent(BorderLayout.SOUTH);
            JPanel p = new JPanel();
            final JCheckBox cbCaseSensitive = new JCheckBox("Case Sensitive", checker.isCaseSensitive());
            cbCaseSensitive.setMnemonic('s');
            cbCaseSensitive.setDisplayedMnemonicIndex(5);
            cbCaseSensitive.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    checker.setCaseSensitive(cbCaseSensitive.isSelected());
                }
            });
            p.add(cbCaseSensitive);
            JButton bResetIgnore = new JButton("Reset Ignore List");
            bResetIgnore.addActionListener(EventHandler.create(ActionListener.class, checker, "resetIgnore"));
            bResetIgnore.setToolTipText("clear the list of words that this spell checker ignores");
            JButton bResetReplace = new JButton("Reset Replace List");
            bResetReplace.addActionListener(EventHandler.create(ActionListener.class, checker, "resetReplace"));
            bResetReplace.setToolTipText("clear the list of words that this spell checker replaces");
            p.add(cbCaseSensitive);
            p.add(bResetIgnore);
            p.add(bResetReplace);
            p.setBorder(BorderFactory.createTitledBorder(SeparatorLineBorder.get(), "Spell Checker Options"));
            d.getContentPane().add(p, BorderLayout.SOUTH);
        }
    };

    private static String makeKeyStrokeTooltip(String tooltip, KeyStroke ks) {
        tooltip = "<html>" + tooltip + "  <sub><font color=#444444 size=-1>" + KeyEvent.getKeyModifiersText(ks.getModifiers()) + "-" + KeyEvent.getKeyText(ks.getKeyCode()) + "</font></sub></html>";
        return tooltip;
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

    public void setFileVersion(Version nwnVersion) {
        this.nwnVersion = nwnVersion;
        if (SwingUtilities.isEventDispatchThread()) {
            buildLanguageMenu(TlkEdit.this.nwnVersion);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    @Override
                    public void run() {
                        buildLanguageMenu(TlkEdit.this.nwnVersion);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame f = new JFrame(args[0]);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        TlkEdit ed = new TlkEdit();
        f.getContentPane().add(ed);
        ed.load(new File(args[0]), null, Version.getDefaultVersion());
        f.pack();
        f.setVisible(true);
    }

    public static final DataFlavor FLAVORSTRREF = new DataFlavor(String[].class, "StrRef Array");

    // incomplete ... tab-separated-values doesn't seem to work
    protected TransferHandler transferHandler = new TransferHandler() {
        protected final String tlkMime = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + List.class.getName() + "\"";
        protected final DataFlavor flavorTsv = new DataFlavor("text/tab-separated-values", "text/tab-separated-values");
        //protected final DataFlavor flavorTsv = new DataFlavor("text/tsv","text/tsv");
        protected final DataFlavor flavorTlkList = new DataFlavor(tlkMime, "tlk entry list");

        int[] modelSelection;

        private void convertToModelIndices(int[] selection) {
            for (int i = 0; i < selection.length; i++) {
                selection[i] = tlkTable.convertRowIndexToModel(selection[i]);
            }
        }

        @Override
        public void exportToClipboard(final JComponent comp, Clipboard clip, int action) {
            JTable table = (JTable) comp;
            // deselect virtual last row
            table.getSelectionModel().removeIndexInterval(model.size(), model.size());
            modelSelection = table.getSelectedRows();
            convertToModelIndices(modelSelection);
            //System.out.println("export ...");
            if (modelSelection.length == 0) {
                return;
            }
            Transferable trans = makeTransferable(modelSelection);
            clip.setContents(trans, null);
            exportDone(comp, trans, action);
        }

        @Override
        protected void exportDone(JComponent source, Transferable data, int action) {
            if ((action & MOVE) != 0) {
                String name = MessageFormat.format("Cut [{0}...{1}]", modelSelection[0], modelSelection[modelSelection.length - 1]);
                rowMutator.new RemoveRowsEdit(name, modelSelection).invoke();
                //listMutator.remove(selection);
                int viewRow = tlkTable.convertRowIndexToView(modelSelection[0]);
                viewRow = Math.min(tlkTable.getRowCount(), viewRow);
                tlkTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            }
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            for (DataFlavor d : transferFlavors) {
                if (d.equals(flavorTlkList)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            JTable table = (JTable) comp;
            try {
                if (t.isDataFlavorSupported(flavorTlkList)) {
                    List<TlkEntry> entries = (List<TlkEntry>) t.getTransferData(flavorTlkList);
                    List<TlkEntry> clones = new ArrayList<TlkEntry>(entries.size());
                    for (TlkEntry e : entries) {
                        clones.add(e.clone());
                    }
                    int viewRow = table.getSelectedRow();
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    if (modelRow > -1) {
                        rowMutator.new InsertRowsEdit("Paste", modelRow, clones).invoke();
                        table.getSelectionModel().setSelectionInterval(viewRow, viewRow + entries.size() - 1);
                        return true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return false;
        }

        private Transferable makeTransferable(final int[] modelSelection) {
            final TlkEntry[] rows = new TlkEntry[modelSelection.length];
            for (int i = 0; i < modelSelection.length; i++) {
                rows[i] = tlkContent.get(modelSelection[i]).clone();
            }
            final boolean hex = model.isDisplayHex();
            final boolean userTlk = model.getIsUserTlk();
            return new Transferable() {

                @Override
                public Object getTransferData(DataFlavor df) {
                    //System.out.println( "getTransferData : " + df  );
                    if (df.equals(flavorTsv)) {
                        System.out.println("tsv export ...");
                        try {
                            final PipedInputStream pin = new PipedInputStream();
                            final PipedOutputStream pout = new PipedOutputStream(pin);
                            new Thread() {

                        @Override
                                public void run() {
                                    try {
                                        BufferedOutputStream bos = new BufferedOutputStream(pout);
                                        PrintWriter p = new PrintWriter(bos);
                                        for (TlkEntry e : rows) {
                                            p.write("\"");
                                            p.write(e.getString().replaceAll("\t", "\\t"));
                                            p.write("\"\t");
                                            p.print(e.getSoundResRef());
                                            p.print("\t");
                                            p.print(e.getSoundLength());
                                            p.write("\n");
                                        }
                                        //new XMLWriter(bos, OutputFormat.createPrettyPrint()).write(e);
                                        p.close();
                                        bos.close();
                                        pout.close();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }.start();
                            return pin;
                        } catch (IOException ioex) {
                            System.out.println(ioex);
                        }
                        return null;
                    } else if (df.equals(flavorTlkList)) {
                        return Arrays.asList(rows);
                    } else if (df.equals(FLAVORSTRREF)) {
                        String[] r = new String[modelSelection.length];
                        for (int i = 0; i < r.length; i++) {
                            int strRef = userTlk ? (modelSelection[i] | TlkLookup.USERTLKOFFSET) : modelSelection[i];
                            r[i] = hex ? "0x" + Integer.toHexString(strRef) : Integer.toString(strRef);
                        }
                        return r;
                    } else {
                        return null;
                    }
                }

                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{flavorTlkList, FLAVORSTRREF};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor df) {
                    return df.equals(flavorTlkList) || df.equals(FLAVORSTRREF);
                }
            };
        }
    };
}
