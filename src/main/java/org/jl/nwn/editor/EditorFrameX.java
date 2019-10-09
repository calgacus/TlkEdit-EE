package org.jl.nwn.editor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.event.MessageEvent;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jl.nwn.Version;
import org.jl.nwn.erf.ErfEdit;
import org.jl.nwn.erf.ErfResourceEditor;
import org.jl.nwn.gff.editor.GffEditX;
import org.jl.nwn.gff.editor.GffTlkLookup;
import org.jl.nwn.gui.RepositoryFCAccessory;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.Repositories;
import org.jl.nwn.resource.ResourceID;
import org.jl.nwn.tlk.editor.TlkEdit;
import org.jl.nwn.tlk.editor.TlkLookupPanel;
import org.jl.nwn.twoDa.TwoDaEdit;
import org.jl.nwn.twoDa.TwoDaTlkLookupLSListener;
import org.jl.swing.Actions;
import org.jl.swing.CheckBoxAction;
import org.jl.swing.FileDropHandler;
import org.jl.swing.I18nUtil;
import org.jl.swing.UIDefaultsX;

public class EditorFrameX extends JXFrame implements PropertyChangeListener {

    private String title = Messages.getString("EditorFrame.WindowTitle"); //$NON-NLS-1$
    //private DirectoryTree dTree;
    JFileChooser navigator = new JFileChooser();
    private JTabbedPane tPane;
    private JSplitPane spane; // main split pane
    private JSplitPane leftSPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JMenuBar menuBar;

    private JToolBar toolbar;
    private Box toolbars = new Box(BoxLayout.X_AXIS);
    private JToolBar editorToolbar = null;
    private Component noEditorGlue = Box.createHorizontalGlue();

    private JMenu fileMenu;
    private final Component hGlue = Box.createHorizontalGlue();

    private static Preferences prefs = Preferences.userNodeForPackage(EditorFrameX.class);

    private JDialog lookupDialog = new JDialog(this, "StrRef lookup");
    private TlkLookupPanel tlp = new TlkLookupPanel();
    private GffTlkLookup gffTlkLookup = new GffTlkLookup(tlp);

    private JFileChooser fChooser = new JFileChooser();

    protected class VersionSelectionFilter extends FileFilter{
        private Version v;

        public VersionSelectionFilter(Version v) {
            this.v = v;
        }

        @Override
        public boolean accept(File f) {
            return true;
        }

        @Override
        public String getDescription() {
            return "All Files - " + v.getDisplayName();
        }

        public Version getVersion(){
            return v;
        }

    }

    private static final UIDefaultsX uid = new UIDefaultsX();

    private StatusBar statusBar = new StatusBar();
    private MessageSourceSupport msgSup = new MessageSourceSupport(this);
    static {
        Toolkit.getDefaultToolkit().setDynamicLayout(true);
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        //UIManager.put("swing.aaText", Boolean.TRUE);
        uid.addResourceBundle("org.jl.nwn.editor.MessageBundle");
        /*
        try{
        UIManager.setLookAndFeel(new SubstanceLookAndFeel());
        } catch (Exception e){
        e.printStackTrace();
        }
         */
        //UIManager.put(SubstanceLookAndFeel.TABBED_PANE_CLOSE_BUTTONS_PROPERTY, Boolean.TRUE);
        NwnFileView.setUIDefaults();
    }

    protected class OpenAction extends AbstractAction {

        Version openVersion = null;

        public OpenAction(Version v) {
            super("Open file (" + v + ")...");
            this.openVersion = v;
        }

        public OpenAction() {
            super("Open file...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (tPane.getSelectedIndex() != -1) {
                if (((SimpleFileEditor) tPane.getSelectedComponent()).getFile() != null) {
                    fChooser.setCurrentDirectory(((SimpleFileEditor) tPane.getSelectedComponent()).getFile().getParentFile());
                }
            }
            if (fChooser.showOpenDialog(navigator) == JFileChooser.APPROVE_OPTION) {
                final Version fileOpenVersion = ((VersionSelectionFilter)fChooser.getFileFilter()).getVersion();
                //openFile(fChooser.getSelectedFile());
                SwingWorker worker = new SwingWorker() {

                    @Override
                    public Object doInBackground() {
                        try {
                            openFile(fChooser.getSelectedFile(), fileOpenVersion);
                        } finally {
                            setEnabled(true);
                        }
                        return null;
                    }
                };
                setEnabled(false); // disable 'open' action
                worker.execute();
            }
        }
    }

    private Action actOpen = new OpenAction();

    private Action actRepositoryOpen = new AbstractAction("Extract resources") {
        RepositoryFCAccessory acc = new RepositoryFCAccessory();
        JFileChooser dirChooser = new JFileChooser();
        JCheckBox cbOpenAfterExtracting = new JCheckBox("Open extracted resources");
        {
            acc.setButtonAction(this);
            fChooser.setAccessory(acc.getAccessoryComponent());
            fChooser.addPropertyChangeListener(acc);
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            dirChooser.setCurrentDirectory(fChooser.getCurrentDirectory());
            Box options = new Box(BoxLayout.PAGE_AXIS);
            options.add(cbOpenAfterExtracting);
            dirChooser.setAccessory(options);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            fChooser.cancelSelection();
            final List<ResourceID> list = acc.getSelectedResources();
            final NwnRepository rep = acc.getRepository();
            final Version fileOpenVersion = ((VersionSelectionFilter)fChooser.getFileFilter()).getVersion();
            //if (dirChooser.showOpenDialog(fChooser) == JFileChooser.APPROVE_OPTION) {
            if (dirChooser.showDialog(fChooser, "Extract to directory" ) == JFileChooser.APPROVE_OPTION) {
                final File dir = dirChooser.getSelectedFile();
                msgSup.fireProgressStarted(0, list.size());
                new SwingWorker<List<File>, File>() {
                    final List<File> extractedFiles = new ArrayList<>();
                    @Override
                    public List<File> doInBackground() {
                        actOpen.setEnabled(false);
                        msgSup.fireProgressStarted(0, list.size());
                        for (final ResourceID id : list) {
                            try {
                                final File rFile = new File(dir, id.getFileName());
                                Repositories.extractResourceToFile(rep, id, rFile);
                                extractedFiles.add(rFile);
                                super.publish(rFile);
                                if (cbOpenAfterExtracting.isSelected()){
                                    openFile(rFile, fileOpenVersion);
                                }
                            } catch (IOException ioex) {
                                msgSup.fireMessage(id + ": " + ioex.getMessage());
                            }
                        }
                        return extractedFiles;
                    }

                    @Override
                    protected void process(List<File> arg0) {
                        super.process(arg0);
                        msgSup.fireProgressIncremented(extractedFiles.size());
                        msgSup.fireMessage("extracted " + arg0.get(arg0.size()-1));
                    }

                    @Override
                    protected void done() {
                        actOpen.setEnabled(true);
                        msgSup.fireProgressEnded();
                        msgSup.fireMessage("extracted " + extractedFiles.size() + " files");
                        super.done();
                    }
                }.execute();

            }
        }
    };

    private Action actQuit = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            quit();
        }
    };

    private final Action actAbout = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final Path path = Paths.get("./CHANGELOG.md");
                if (Files.exists(path)) {
                    final String changelog = new String(Files.readAllBytes(path), UTF_8);
                    //TODO: Not ideally, but something...
                    final JDialog dlg = new JDialog(EditorFrameX.this, "About TlkEdit-EE");
                    dlg.add(new JScrollPane(new JTextArea(changelog)));
                    dlg.setSize(EditorFrameX.this.getSize());
                    dlg.setVisible(true);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    };

    private Action actClose = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            closePane((JComponent) tPane.getSelectedComponent(), true);
        }
    };

    private Action newTlk = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            TlkEdit ed = new TlkEdit();
            final JComboBox<Version> cbVersions = new JComboBox<>(Version.values());
            int r = JOptionPane.showConfirmDialog(EditorFrameX.this, cbVersions, "Select Version", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r!=JOptionPane.OK_OPTION)
                return;
            ed.setFileVersion((Version)cbVersions.getSelectedItem());
            ed.addPropertyChangeListener(EditorFrameX.this);
            tPane.add(ed, Messages.getString("EditorFrame.TabNameNewTlk")); //$NON-NLS-1$
            //ed.putClientProperty(SimpleFileEditor.resources, resources);
        }
    };

    private Action newGff = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            GffEditX gff = new GffEditX();
            gff.addPropertyChangeListener(EditorFrameX.this);
            tPane.add(gff, Messages.getString("EditorFrame.TabNameNewGff")); //$NON-NLS-1$
            //ed.putClientProperty(SimpleFileEditor.resources, resources);
        }
    };

    private Action newErf = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            ErfEdit erf = new ErfEdit(Version.NWN1);
            erf.addPropertyChangeListener(EditorFrameX.this);
            tPane.add(erf, Messages.getString("EditorFrame.TabNameNewErf")); //$NON-NLS-1$
        }
    };

    private Action newErf2 = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            ErfEdit erf = new ErfEdit(Version.NWN2);
            erf.addPropertyChangeListener(EditorFrameX.this);
            tPane.add(erf, Messages.getString("EditorFrame.TabNameNewErf")); //$NON-NLS-1$
        }
    };

    private Action actSave = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveActivePane();
        }
    };

    private Action actSaveAs = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveActivePaneAs();
        }
    };

    private Action actSaveAll = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            saveAll();
        }
    };

    private Action actErfExtractForEditing = new AbstractAction() {
        {
            Actions.configureActionUI(this, uid, "EditorFrame.ErfOpenResource");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ErfEdit erf = (ErfEdit) tPane.getSelectedComponent();
            for (final ResourceID id : erf.getSelectedResources()) {
                try {
                    final SimpleFileEditorPanel ed = openFile(erf.extractAsTempFile(id, true), erf.getFileVersion());
                    if (ed != null) {
                        final ErfResourceEditor erfEd = new ErfResourceEditor(ed, erf, id);
                        tPane.add(erfEd.getFile().getName(), erfEd);
                    }
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                }
            }
        }
    };

    public SimpleFileEditorPanel openFile(final File f, Version nwnVersion) {
        SimpleFileEditorPanel ed = null;
        try {
            if (!f.exists()) {
                JOptionPane.showMessageDialog(navigator, Messages.getString("EditorFrame.ErrorMsgFileNotFound") + f.getAbsolutePath(), Messages.getString("EditorFrame.ErrorMsgTitle"), JOptionPane.ERROR_MESSAGE);
                return null;
            }
            msgSup.fireMessage(MessageFormat.format(Messages.getString("EditorFrame.OpeningFileMsg"), f), Level.INFO);
            if (TwoDaEdit.accept(f)) {
                ed = new TwoDaEdit();
                ed.showToolbar(false);
                ((TwoDaEdit) ed).load(f, nwnVersion);
                ((TwoDaEdit) ed).addListSelectionListener(new TwoDaTlkLookupLSListener((TwoDaEdit) ed, tlp));
                tPane.add(f.getName(), ed);
                tPane.setSelectedComponent(ed);
            } else if (TlkEdit.accept(f)) {
                final ProgressMonitor pm = new ProgressMonitor(this, f, "", 0, 0) {

                    @Override
                    public void setMaximum(int max) {
                        msgSup.fireProgressStarted(0, max);
                    }

                    @Override
                    public void setProgress(int p) {
                        msgSup.fireProgressIncremented(p);
                    }
                };
                pm.setMillisToPopup(Integer.MAX_VALUE);
                pm.setMaximum(-1);
                ed = new TlkEdit();
                ed.showToolbar(false);
                ((TlkEdit) ed).load(f, pm, nwnVersion);
                msgSup.fireProgressEnded();

                tPane.add(f.getName(), ed);
                tPane.setSelectedComponent(ed);
                if (tlp.getTlkEdit() == null) {
                    tlp.setTlkEdit((TlkEdit) ed);
                    //leftSPane.setRightComponent(tlp);
                } else if (tlp.getUserTlkEdit() == null) {
                    tlp.setUserTlkEdit((TlkEdit) ed);
                    //leftSPane.setRightComponent(tlp);
                }
                ((TlkEdit) ed).addMessageListener(statusBar);
            } else if (GffEditX.accept(f)) {
                GffEditX edX = new GffEditX();
                ed = edX;
                ed.showToolbar(false);
                edX.addMessageListener(statusBar);
                edX.addProgressListener(statusBar);
                tPane.add(f.getName(), ed);
                edX.load(f, nwnVersion);
                tPane.setSelectedComponent(edX);
                gffTlkLookup.registerWith(edX);
                updateTitle();
            } else if (ErfEdit.accept(f)) {
                ed = new ErfEdit(f);
                ed.showToolbar(false);
                tPane.add(f.getName(), ed);
                JMenu m = ed.getMenus()[0];
                m.addSeparator();
                m.add(actErfExtractForEditing);
                tPane.setSelectedComponent(ed);
            } else {
                JOptionPane.showMessageDialog(navigator, Messages.getString("EditorFrame.ErrorMsgUnknownFileType"), Messages.getString("EditorFrame.ErrorMsgTitle"), JOptionPane.ERROR_MESSAGE);
                return null;
            }
            ed.addPropertyChangeListener(this);
            editorPanelChanged(ed);
            return ed;
        } catch (IOException ioex) {
            JOptionPane.showMessageDialog(navigator, Messages.getString("EditorFrame.ErrorMsgCouldNotOpenFile") + ioex.getMessage(), Messages.getString("EditorFrame.ErrorMsgTitle"), JOptionPane.ERROR_MESSAGE);
            ioex.printStackTrace();
        } finally {
            if (!isDisplayable()) {
                validate();
            }
            if (!isVisible()) {
                setVisible(true);
            }
        }
        return ed;
    }

    /**
     * @return true if the pane has been closed
     */
    private boolean closePane(JComponent c, boolean remove) {
        SimpleFileEditor sfe = (SimpleFileEditor) c;
        if (sfe.getIsModified()) {
            /*
            String[] options = { Messages.getString("EditorFrame.OptionYes"), Messages.getString("EditorFrame.OptionNo"), Messages.getString("EditorFrame.OptionReturnToEditor") }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            JButton[] optionButtons = new JButton[3];
            for ( int i = 0; i < 3; i++ ){
            optionButtons[i] = new JButton();
            I18nUtil.setText( optionButtons[i], options[i] );
            }
             */
            String optionMessage = MessageFormat.format(Messages.getString("EditorFrame.WarningUnsavedChanges"), tPane.getTitleAt(tPane.getSelectedIndex()));
            int choice = JOptionPane.showOptionDialog(tPane, optionMessage, Messages.getString("EditorFrame.WarningTitle_Unsaved"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
            if (choice == JOptionPane.YES_OPTION) {
                if (sfe.canSave()) {
                    saveActivePane();
                } else {
                    saveActivePaneAs();
                }
            } else if (choice == JOptionPane.CANCEL_OPTION) {
                return false;
            }
        }
        if (c instanceof TlkEdit) {
            if (((TlkEdit) c) == tlp.getTlkEdit()) {
                tlp.setTlkEdit(null);
            }
            if (((TlkEdit) c) == tlp.getUserTlkEdit()) {
                tlp.setUserTlkEdit(null);
                //if ( tlp.getTlkEdit() == null && tlp.getUserTlkEdit() == null )
                //leftSPane.remove(tlp);
            }
        } else if (c instanceof GffEditX) {
            gffTlkLookup.deregisterWith((GffEditX) c);
        }
        sfe.close();
        if (remove) {
            tPane.remove(c);
        }
        if (c instanceof SimpleFileEditorPanel) {
            toolbars.remove(((SimpleFileEditorPanel) c).getToolbar());
        }
        //c.removePropertyChangeListener(this);
        updateTitle();
        return true;
    }

    private void saveAll() {
        Component[] eds = tPane.getComponents();
        try {
            for (int p = 0; p < eds.length; p++) {
                SimpleFileEditor ed = (SimpleFileEditor) eds[p];
                if (ed.getIsModified()) {
                    if (ed.canSave()) {
                        ed.save();
                    } else if (ed.canSaveAs()) {
                        tPane.setSelectedIndex(p);
                        saveActivePaneAs();
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    private void saveActivePane() {
        JComponent c = (JComponent) tPane.getSelectedComponent();
        try {
            ((SimpleFileEditor) c).save();
            updateTitle();
            msgSup.fireMessage(MessageFormat.format(Messages.getString("EditorFrame.FileSavedMsg"), ((SimpleFileEditor) c).getFile()), Level.INFO);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(navigator, Messages.getString("EditorFrame.ErrorMsgCouldNotSaveFile"), Messages.getString("EditorFrame.ErrorMsgTitle"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void saveActivePaneAs() {
        JComponent c = (JComponent) tPane.getSelectedComponent();
        SimpleFileEditor ed = (SimpleFileEditor) c;
        File f = ed.getFile();
        if (f != null) {
            fChooser.setSelectedFile(f);
        }
        int overwrite = 0;
        int fcOption = 0;
        FileFilter currentFilter = fChooser.getFileFilter();
        for (FileFilter filter : fChooser.getChoosableFileFilters()){
            if ( ((VersionSelectionFilter)filter).getVersion().equals(ed.getFileVersion()) )
                fChooser.setFileFilter(filter);
        }
        do {
            overwrite = JOptionPane.YES_OPTION;
            fcOption = fChooser.showSaveDialog(tPane);
            if (fcOption == JFileChooser.APPROVE_OPTION) {
                f = fChooser.getSelectedFile();
                if (f.exists()) {
                    overwrite = JOptionPane.showConfirmDialog(this, MessageFormat.format(Messages.getString("EditorFrame.FileExistsMsg"), f), Messages.getString("EditorFrame.FileExistsTitle"), JOptionPane.YES_NO_OPTION);
                }
            }
        } while (fcOption == JFileChooser.APPROVE_OPTION && overwrite == JOptionPane.NO_OPTION);
        if (fcOption != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Version saveVersion = ((VersionSelectionFilter)fChooser.getFileFilter()).getVersion();
            ed.saveAs(f, saveVersion);
            fChooser.setFileFilter(currentFilter);
            tPane.setTitleAt(tPane.getSelectedIndex(), f.getName());
            actSave.setEnabled(true);
            msgSup.fireMessage(MessageFormat.format(Messages.getString("EditorFrame.FileSavedMsg"), f), Level.INFO);
            updateTitle();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(navigator, Messages.getString("EditorFrame.ErrorMsgCouldNotSaveFile"), Messages.getString("EditorFrame.ErrorMsgTitle"), JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
        updateTitle();
    }

    private void updateTitle() {
        SimpleFileEditor ed = (SimpleFileEditor) tPane.getSelectedComponent();
        if (ed == null) {
            setTitle(title);
        } else {
            setTitle(title + " - " + (ed.getFile() != null ? ed.getFile().getAbsolutePath() : tPane.getTitleAt(tPane.getSelectedIndex())) + " (" + ed.getFileVersion() + ")" + (ed.getIsModified() ? Messages.getString("EditorFrame.TitleHint_FileModified") : "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    public EditorFrameX() {
        getRootPane().setTransferHandler(FileTransferHandler);
        setIconImage(new ImageIcon(getClass().getResource("/resource/icons/22x22/apps/package_editors.png")).getImage());
        msgSup.addMessageListener(statusBar);
        msgSup.addProgressListener(statusBar);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
        //dTree = new DirectoryTree(new File(".")); //$NON-NLS-1$
        /*
        navigator.setControlButtonsAreShown(false);
        navigator.addActionListener( new ActionListener(){
        public void actionPerformed( ActionEvent e ){
        if ( e.getActionCommand() == JFileChooser.APPROVE_SELECTION )
        openFile(navigator.getSelectedFile(), Version.getDefaultVersion());
        //System.out.println("choose file : " + fc.getSelectedFile() );
        }
        } );
        navigator.setFileView(fChooser.getFileView());
        navigator.setMinimumSize(new Dimension(0,0));
         */
        tPane = new JTabbedPane();
        Action nextTab = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tPane.setSelectedIndex((tPane.getSelectedIndex() + 1) % tPane.getTabCount());
            }
        };
        Action prevTab = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tPane.setSelectedIndex((tPane.getSelectedIndex() + tPane.getTabCount() - 1) % tPane.getTabCount());
            }
        };
        Actions.configureActionUI(nextTab, uid, "nextTab");
        Actions.configureActionUI(prevTab, uid, "prevTab");
        Actions.registerActions(tPane.getInputMap(tPane.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT), tPane.getActionMap(), prevTab, nextTab);
        /*
        VetoableTabCloseListener vtcl = new VetoableTabCloseListener() {
        public void tabClosed(JTabbedPane jTabbedPane, Component component) {
        }
        public void tabClosing(JTabbedPane jTabbedPane, Component component) {
        }
        public boolean vetoTabClosing(JTabbedPane jTabbedPane, Component component) {
        return !closePane((JComponent)component, false);
        }
        };
        TabCloseListenerManager.getInstance().registerListener(tPane, vtcl);
         */
        /*
        dTree.addMouseListener(new MouseInputAdapter() {
        public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
        File f = dTree.getSelectedFile();
        if (f != null && f.isFile())
        openFile(f);
        }
        }
        });
        dTree.setFileView( fChooser.getFileView() );
         */
        /*
        JPanel dirPanel = new JPanel();
        dirPanel.setLayout(new BorderLayout());
        dirPanel.add(new JScrollPane(dTree), BorderLayout.CENTER);
        JToolBar rootBar = dTree.getRootSelectionBar();
        rootBar.setOrientation( JToolBar.HORIZONTAL );
        dirPanel.add(rootBar, BorderLayout.NORTH);
         */
        tPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                SimpleFileEditorPanel ed = (SimpleFileEditorPanel) ((JTabbedPane) e.getSource())
                        .getSelectedComponent();
                editorPanelChanged(ed);
            }
        });

        //leftSPane.setLeftComponent(dirPanel);
        leftSPane.setLeftComponent(navigator);
        leftSPane.setOneTouchExpandable(true);
        leftSPane.setLastDividerLocation(350);

        spane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSPane, tPane);
        spane.setOneTouchExpandable(true);
        spane.setDividerLocation(0);
        spane.setLastDividerLocation(250);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tPane, BorderLayout.CENTER);
        getContentPane().add(statusBar.getStatusBar(), BorderLayout.SOUTH);

        // setup key bindings
        int accelMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        KeyStroke ksClose = KeyStroke.getKeyStroke(KeyEvent.VK_W, accelMask);
        KeyStroke ksSave = KeyStroke.getKeyStroke(KeyEvent.VK_S, accelMask);
        KeyStroke ksOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O, accelMask);
        KeyStroke ksQuit = KeyStroke.getKeyStroke(KeyEvent.VK_Q, accelMask);

        menuBar = new JMenuBar();
        fileMenu = new JMenu();
        I18nUtil.setText(fileMenu, Messages.getString("EditorFrame.MenuName_FileMenu")); //$NON-NLS-1$
        //fileMenu.setMnemonic('f');
        JMenu newSubMenu = new JMenu();
        I18nUtil.setText(newSubMenu, Messages.getString("EditorFrame.MenuName_NewFileSubmenu")); //$NON-NLS-1$
        I18nUtil.setText(newSubMenu.add(newTlk), Messages.getString("EditorFrame.MenuItemNewTlk")); //$NON-NLS-1$
        //I18nUtil.setText( newSubMenu.add(newGff), Messages.getString("EditorFrame.MenuItemNewGff")); //$NON-NLS-1$
        I18nUtil.setText(newSubMenu.add(newErf), Messages.getString("EditorFrame.MenuItemNewErf")); //$NON-NLS-1$
        I18nUtil.setText(newSubMenu.add(newErf2), Messages.getString("EditorFrame.MenuItemNewErf2")); //$NON-NLS-1$
        //fileMenu.add(newTlk);
        fileMenu.add(newSubMenu);

        JMenuItem itOpen = fileMenu.add(actOpen);
        //JMenuItem itOpen1 = fileMenu.add(new OpenAction(Version.NWN1));
        //JMenuItem itOpen2 = fileMenu.add(new OpenAction(Version.NWN2));
        I18nUtil.setText(itOpen, Messages.getString("EditorFrame.MenuItemOpen")); //$NON-NLS-1$
        //itOpen.setMnemonic('o');
        itOpen.setAccelerator(ksOpen);
        JMenuItem itClose = fileMenu.add(actClose);
        I18nUtil.setText(itClose, Messages.getString("EditorFrame.MenuItemClose")); //$NON-NLS-1$
        //itClose.setMnemonic('w');
        itClose.setAccelerator(ksClose);
        JMenuItem itSave = fileMenu.add(actSave);
        I18nUtil.setText(itSave, Messages.getString("EditorFrame.MenuItemSave")); //$NON-NLS-1$
        //itSave.setMnemonic('s');
        itSave.setAccelerator(ksSave);
        JMenuItem itSaveAs = fileMenu.add(actSaveAs);
        I18nUtil.setText(itSaveAs, Messages.getString("EditorFrame.MenuItemSaveAs")); //$NON-NLS-1$
        JMenuItem itSaveAll = fileMenu.add(actSaveAll);
        I18nUtil.setText(itSaveAll, Messages.getString("EditorFrame.MenuItemSaveAll")); //$NON-NLS-1$

        fileMenu.add(new JSeparator());
        JMenuItem itAbout = fileMenu.add(actAbout);
        I18nUtil.setText(itAbout,  Messages.getString("EditorFrame.MenuItemAbout")); //$NON-NLS-1$
        
        fileMenu.add(new JSeparator());
        JMenuItem itQuit = fileMenu.add(actQuit);
        I18nUtil.setText(itQuit, Messages.getString("EditorFrame.MenuItemExit")); //$NON-NLS-1$
        //itQuit.setMnemonic('q');
        itQuit.setAccelerator(ksQuit);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        boolean useIcons = true;

        JButton openButton = new JButton(actOpen);
        //openButton.setMnemonic('o');
        openButton.setRequestFocusEnabled(false);
        openButton.setToolTipText(makeKeyStrokeTooltip(Messages.getString("EditorFrame.ToolTipOpenFile"), ksOpen)); //$NON-NLS-1$
        if (useIcons) {
            openButton.setText(""); //$NON-NLS-1$
            openButton.setIcon(uid.getIcon("EditorFrame.open.SmallIcon")); //$NON-NLS-1$
        }

        JButton closeButton = new JButton(actClose);
        //closeButton.setMnemonic('w');
        closeButton.setRequestFocusEnabled(false);
        closeButton.setToolTipText(makeKeyStrokeTooltip(Messages.getString("EditorFrame.ToolTipCloseFile"), ksClose)); //$NON-NLS-1$
        if (useIcons) {
            closeButton.setText(""); //$NON-NLS-1$
            closeButton.setIcon(uid.getIcon("EditorFrame.close.SmallIcon")); //$NON-NLS-1$
        }

        JButton saveButton = new JButton(actSave);
        //saveButton.setMnemonic('s');
        saveButton.setRequestFocusEnabled(false);
        saveButton.setToolTipText(makeKeyStrokeTooltip(Messages.getString("EditorFrame.ToolTipSave"), ksSave)); //$NON-NLS-1$
        if (useIcons) {
            saveButton.setText(""); //$NON-NLS-1$
            saveButton.setIcon(uid.getIcon("EditorFrame.save.SmallIcon")); //$NON-NLS-1$
        }

        JButton saveAsButton = new JButton(actSaveAs);
        //saveAsButton.setMnemonic('a');
        saveAsButton.setRequestFocusEnabled(false);
        saveAsButton.setToolTipText(Messages.getString("EditorFrame.ToolTipSaveAs")); //$NON-NLS-1$
        if (useIcons) {
            saveAsButton.setText(""); //$NON-NLS-1$
            saveAsButton.setIcon(uid.getIcon("EditorFrame.saveas.SmallIcon")); //$NON-NLS-1$
        }

        toolbar.add(openButton);
        toolbar.add(closeButton);
        toolbar.add(saveButton);
        toolbar.add(saveAsButton);

        toolbar.addSeparator(new Dimension(50, 1));

        toolbar.add(noEditorGlue);
        getContentPane().add(toolbars, BorderLayout.NORTH);
        toolbars.add(toolbar);

        setSize(800, 600);
        setVisible(true);
        leftSPane.setDividerLocation(leftSPane.getMaximumDividerLocation());

        ActionListener updateMemoryLabel = new ActionListener() {
            String msg = Messages.getString("EditorFrame.InfoLabelMemoryUsage"); //$NON-NLS-1$

            @Override
            public void actionPerformed(ActionEvent e) {
                long total = Runtime.getRuntime().totalMemory();
                long free = Runtime.getRuntime().freeMemory();
                statusBar.heapLabel.message(new MessageEvent(this, MessageFormat.format(msg, (total - free) >> 10, total >> 10)));
            }
        };
        Timer timer = new Timer(3000, updateMemoryLabel);
        timer.start();

        tlp.addMessageListener(statusBar);
        msgSup.addProgressListener(statusBar);

        validate();
        restoreFromPreferences();
    }

    public EditorFrameX(String title) {
        this.title = title;
        setTitle(title);
    }

    private void savePreferences() {
        int openFiles = 0;
        File f;
        for (int i = 0; i < tPane.getTabCount(); i++) {
            SimpleFileEditor sfe = (SimpleFileEditor) tPane.getComponentAt(i);
            f = sfe.getFile();
            if (f != null) {
                prefs.put("OpenFile" + i, f.getAbsolutePath()); //$NON-NLS-1$
                prefs.put("OpenFileVersion" + i, sfe.getFileVersion().name()); //$NON-NLS-1$
                openFiles++;
            }
        }
        prefs.putInt("OpenFilesCount", openFiles); //$NON-NLS-1$
        f = fChooser.getSelectedFile();
        if (f == null) {
            f = fChooser.getCurrentDirectory();
        }
        if (f == null) {
            f = new File("."); //$NON-NLS-1$
        } else if (f.isFile()) {
            f = f.getParentFile();
        }
        prefs.put("FileChooserDir", f.getAbsolutePath()); //$NON-NLS-1$
        // window settings :
        prefs.putInt("FrameWidth", getWidth()); //$NON-NLS-1$
        prefs.putInt("FrameHeight", getHeight()); //$NON-NLS-1$
        prefs.putInt("MainDividerPos", spane.getDividerLocation()); //$NON-NLS-1$
        prefs.putInt("LeftDividerPos", leftSPane.getDividerLocation()); //$NON-NLS-1$
    }

    private void restoreFromPreferences() {
        final JLabel label = new JLabel(Messages.getString("EditorFrame.MsgPleaseWait"), JLabel.CENTER); //$NON-NLS-1$
        JDialog dialog = new JDialog(this, Messages.getString("EditorFrame.MsgRestoringSession"), false) {
            //$NON-NLS-1$
            {
                getContentPane().add(label);
                setSize(500, 100);
            }
        };
        dialog.setLocation(150, 250);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        //this.getLocationOnScreen()
        int n = prefs.getInt("OpenFilesCount", 0); //$NON-NLS-1$
        String filename;
        for (int i = 0; i < n; i++) {
            filename = prefs.get("OpenFile" + i, null); //$NON-NLS-1$
            String fileVersion = prefs.get("OpenFileVersion" + i, Version.getDefaultVersion().name()); //$NON-NLS-1$
            if (filename != null) {
                label.setText(Messages.getString("EditorFrame.MsgLoadingFile") + filename); //$NON-NLS-1$
                openFile(new File(filename), Enum.valueOf(Version.class, fileVersion));
            }
        }
        filename = prefs.get("FileChooserDir", null); //$NON-NLS-1$
        File f = null;
        if (filename != null) {
            f = new File(filename);
        } else {
            f = new File("."); //$NON-NLS-1$
        }
        fChooser.setCurrentDirectory(f);
        navigator.setCurrentDirectory(f);
        //dTree.setRoot(fChooser.getCurrentDirectory());
        // window settings :
        label.setText(Messages.getString("EditorFrame.MsgRestoringWindowSession")); //$NON-NLS-1$
        setSize(prefs.getInt("FrameWidth", 800), prefs.getInt("FrameHeight", 600)); //$NON-NLS-1$
        dialog.dispose();
        leftSPane.setDividerLocation(prefs.getInt("LeftDividerPos", 0)); //$NON-NLS-1$
        spane.setDividerLocation(prefs.getInt("MainDividerPos", 0)); //$NON-NLS-1$
        validate();
    }

    /*
     * @return false if user pressed cancel option - ( return to  editor program )
     * */
    private boolean checkUnsaved() {
        final List<SimpleFileEditor> unsaved = new ArrayList<>();
        for (int i = 0; i < tPane.getTabCount(); i++) {
            if (((SimpleFileEditor) tPane.getComponentAt(i)).getIsModified()) {
                unsaved.add((SimpleFileEditor) tPane.getComponentAt(i));
            }
        }
        if (unsaved.size() > 0) {
            JPanel inputValues = new JPanel(new GridLayout(0, 1));
            inputValues.add(new JLabel(MessageFormat.format(Messages.getString("EditorFrame.MsgListOfUnsavedFiles"), Integer.valueOf(unsaved.size())))); //$NON-NLS-1$
            final JCheckBox[] boxes = new JCheckBox[unsaved.size()];
            for (int i = 0; i < unsaved.size(); i++) {
                SimpleFileEditor sfe = unsaved.get(i);
                boxes[i] = new JCheckBox(sfe.getFile() == null ? Messages.getString("EditorFrame.FileNameUnsavedFile") : sfe.getFile().getAbsolutePath(), true);
                inputValues.add(boxes[i]);
            }

            final JDialog dialog = new JDialog(this, Messages.getString("EditorFrame.DialogTitleUnsavedFiles"), true);
            JButton YES = new JButton();
            JButton NO = new JButton();
            JButton CANCEL = new JButton();
            final JButton[] optionButtons = {YES, NO, CANCEL};
            String[] optionLabels = new String[]{Messages.getString("EditorFrame.OptionSaveSelectedNExit"), Messages.getString("EditorFrame.OptionExit"), Messages.getString("EditorFrame.OptionReturnToEditor")};
            final JOptionPane oPane = new JOptionPane(inputValues, JOptionPane.WARNING_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION, null, optionButtons, optionButtons[0]);
            Action selectValue = new AbstractAction() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    oPane.setValue(e.getSource());
                    dialog.setVisible(false);
                }
            };
            for (int btns = 0; btns < optionButtons.length; btns++) {
                optionButtons[btns].setAction(selectValue);
                I18nUtil.setText(optionButtons[btns], optionLabels[btns]);
            }
            dialog.getContentPane().add(oPane);
            dialog.pack();
            dialog.setVisible(true);
            //System.out.println( "option : " + oPane.getValue() );
            Object value = oPane.getValue();
            if (value == null || value == JOptionPane.UNINITIALIZED_VALUE || value == CANCEL) {
                //System.out.println("cancel");
                return false;
            } else if (value == YES) {
                System.out.println("saving modified files ...");
                for (int i = 0; i < boxes.length; i++) {
                    if (boxes[i].isSelected()) {
                        SimpleFileEditor sfe = unsaved.get(i);
                        tPane.setSelectedComponent((Component) sfe);
                        if (sfe.canSave()) {
                            saveActivePane();
                        } else {
                            saveActivePaneAs();
                        }
                    }
                }
            }
            //else System.out.println("no");
        }
        return true;
    }

    public static void main(String[] args) throws Exception {
        if (System.getProperty("swing.defaultlaf") == null) {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        EditorFrameX f = new EditorFrameX("TlkEdit"); //$NON-NLS-1$
        for (final String arg : args) {
            f.openFile(new File(arg), Version.getDefaultVersion());
        }
    }

    public static void removePreferences() throws BackingStoreException {
        prefs.removeNode();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SimpleFileEditorPanel.FILE_PROPERTY) || evt.getPropertyName().equals(SimpleFileEditorPanel.ISMODIFIED_PROPERTY)) {
            SimpleFileEditorPanel ed = (SimpleFileEditorPanel) evt.getSource();
            /*
            if (evt.getPropertyName() == SimpleFileEditorPanel.ISMODIFIED_PROPERTY)
            ed.putClientProperty(SubstanceLookAndFeel.WINDOW_MODIFIED, ((Boolean)evt.getNewValue()));
             */
            actSave.setEnabled(ed.canSave());
            actSaveAs.setEnabled(ed.canSaveAs());
            updateTitle();
        }
    }

    private Action actNoTlkLookup = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            SimpleFileEditor ed = (SimpleFileEditor) tPane.getSelectedComponent();
            if (ed instanceof TlkEdit) {
                if (tlp.getTlkEdit() == ed) {
                    tlp.setTlkEdit(null);
                } else if (tlp.getUserTlkEdit() == ed) {
                    tlp.setUserTlkEdit(null);
                }
            }
        }
    };

    private Action useForLookup = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            SimpleFileEditor ed = (SimpleFileEditor) tPane.getSelectedComponent();
            if (ed instanceof TlkEdit) {
                if (ed != tlp.getTlkEdit()) {
                    tlp.setTlkEdit((TlkEdit) ed);
                    if (tlp.getUserTlkEdit() == ed) {
                        tlp.setUserTlkEdit(null);
                    }
                    //tlp.setVisible(true);
                    leftSPane.resetToPreferredSizes();
                }
            }
        }
    };
    private Action useForUserLookup = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            SimpleFileEditor ed = (SimpleFileEditor) tPane.getSelectedComponent();
            if (ed instanceof TlkEdit) {
                if (ed != tlp.getUserTlkEdit()) {
                    tlp.setUserTlkEdit((TlkEdit) ed);
                    if (tlp.getTlkEdit() == ed) {
                        tlp.setTlkEdit(null);
                    }
                    //tlp.setVisible(true);
                    leftSPane.resetToPreferredSizes();
                }
            }
        }
    };

    private void editorPanelChanged(SimpleFileEditorPanel ed) {
        updateTitle();
        if (editorToolbar != null) {
            toolbars.remove(editorToolbar);
            editorToolbar = null;
        }
        if (ed != null) {
            /*
            if ( ed.getFile() != null )
            navigator.setSelectedFile(ed.getFile());
            //dTree.selectFile( ed.getFile() );
             */
            actSave.setEnabled(ed.canSave());
            actSaveAs.setEnabled(ed.canSaveAs());

            JMenu[] menus = ed.getMenus();
            menuBar.removeAll();
            menuBar.add(fileMenu);
            if (menus != null) {
                for (final JMenu menu : menus) {
                    menuBar.add(menu);
                }
            }
            //boolean enableTlkLookup = ed instanceof GffEditX || ed instanceof TwoDaEdit;
            //tlp.setEnabled(enableTlkLookup);
            if (ed instanceof TlkEdit) {
                rbItTlkLookupNone.setSelected(true);
                rbItTlkLookupMain.setSelected(ed == tlp.getTlkEdit());
                rbItTlkLookupUser.setSelected(ed == tlp.getUserTlkEdit());
                menuTlkLookup.setVisible(true);
                menuBar.add(hGlue);
                menuBar.add(menuTlkLookup);
            } else {
            }
        }
        if (ed != null) {
            editorToolbar = ed.getToolbar();
            if (editorToolbar != null) {
                ed.showToolbar(false);
                toolbar.remove(noEditorGlue);
                toolbars.add(editorToolbar);
            }
        }
        if (editorToolbar == null) {
            toolbar.add(noEditorGlue);
        }
        toolbars.validate();
        menuBar.validate();
        toolbars.repaint();
        menuBar.repaint();
    }

    private void quit() {
        boolean exit = checkUnsaved();
        if (exit) {
            savePreferences();
            setVisible(false);
            dispose();
            removeAll();
            // let the window.close() listeners run before calling exit
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    System.exit(0);
                }
            });
        }
    }

    private static String makeKeyStrokeTooltip(String tooltip, KeyStroke ks) {
        return I18nUtil.makeKeyStrokeTooltip(tooltip, ks);
    }

    private JRadioButtonMenuItem rbItTlkLookupMain = new JRadioButtonMenuItem(useForLookup);
    private JRadioButtonMenuItem rbItTlkLookupUser = new JRadioButtonMenuItem(useForUserLookup);
    private JRadioButtonMenuItem rbItTlkLookupNone = new JRadioButtonMenuItem(actNoTlkLookup);
    private JMenu menuTlkLookup = new JMenu();
    {
        fChooser.setFileView(new NwnFileView());
        fChooser.setAcceptAllFileFilterUsed(false);
        for (Version v : Version.values())
            fChooser.addChoosableFileFilter(new VersionSelectionFilter(v));
        for ( FileFilter filter : fChooser.getChoosableFileFilters() )
            if (((VersionSelectionFilter)filter).getVersion().equals(Version.getDefaultVersion()))
                fChooser.setFileFilter(filter);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbItTlkLookupMain);
        bg.add(rbItTlkLookupUser);
        bg.add(rbItTlkLookupNone);
        I18nUtil.setText(rbItTlkLookupNone, Messages.getString("EditorFrame.MenuItem_TlkLookupUnused")); //$NON-NLS-1$
        I18nUtil.setText(rbItTlkLookupMain, Messages.getString("EditorFrame.MenuItem_TlkLookupDefaultTable")); //$NON-NLS-1$
        I18nUtil.setText(rbItTlkLookupUser, Messages.getString("EditorFrame.MenuItem_TlkLookupUserTable")); //$NON-NLS-1$
        menuTlkLookup.add(rbItTlkLookupMain);
        menuTlkLookup.add(rbItTlkLookupUser);
        menuTlkLookup.add(rbItTlkLookupNone);
        I18nUtil.setText(menuTlkLookup, Messages.getString("EditorFrame.MenuTitleTlkLookupMenu")); //$NON-NLS-1$
        lookupDialog.getContentPane().add(tlp);
        lookupDialog.pack();
        CheckBoxAction aShowLookup = new CheckBoxAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                super.actionPerformed(e);
                //System.out.println("set visible : " + !lookupDialog.isVisible());
                lookupDialog.setVisible(!lookupDialog.isVisible());
            }
        };
        Actions.configureActionUI(aShowLookup, uid, "EditorFrame.showTlkLookup");
        AbstractAction aLookupAlwaysOnTop = new AbstractAction() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("set always on top : " + !lookupDialog.isAlwaysOnTop());
                lookupDialog.setAlwaysOnTop(!lookupDialog.isAlwaysOnTop());
            }
        };
        menuTlkLookup.addSeparator();
        final JCheckBoxMenuItem miView = new JCheckBoxMenuItem(aShowLookup);
        miView.setSelected(false);
        miView.setIcon(null);
        aShowLookup.connectButton(miView);
        menuTlkLookup.add(miView);
        Action[] editActions = {aShowLookup};
        for (Action a : editActions) {
            getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put((KeyStroke) a.getValue( a.ACCELERATOR_KEY ), a.getValue(a.ACTION_COMMAND_KEY));
            getRootPane().getActionMap().put(a.getValue(a.ACTION_COMMAND_KEY), a);
        }
    }

    TransferHandler FileTransferHandler = new FileDropHandler() {

        @Override
        public void importFiles(List<File> files) {
            for (File f : files) {
                openFile(f, Version.getDefaultVersion());
            }
        }
    };
}
