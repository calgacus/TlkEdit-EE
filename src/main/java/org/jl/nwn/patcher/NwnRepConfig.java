package org.jl.nwn.patcher;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import org.jl.nwn.bif.BifRepository;
import org.jl.nwn.erf.ErfFile;
import org.jl.nwn.resource.NwnChainRepository;
import org.jl.nwn.resource.NwnDirRepository;
import org.jl.nwn.resource.NwnRepository;

/**
 * Configuration of an NWN Repository (typically a chain of hak packs, override dir,
 * bif files ( indexed by .key files ), stored in Preferences.
 *
 * @author ich
 */
public class NwnRepConfig {

	private static final String PREFS_USEHAK = "usesourcehak";
    /** Prefix for entries in source hak list. */
	private static final String PREFS_HAK = "sourcehak";
    /** Number of entries in source hak list. */
	private static final String PREFS_HAKNUM = "sourcehaknum";
	private static final String PREFS_BIFKEYS = "keyfilesoverride";
	private static final String PREFS_USEBIFS = "usekeyfiles";
	private static final String PREFS_USEOVERRIDE = "useoverride";
    /** Preferences key for path to the directory, that contains game. */
	private static final String PREFS_NWNHOME = "nwnhome";

	//private String[] defaultkeys = { "xp2patch.key", "xp2.key", "xp1patch.key", "xp1.key", "patch.key", "chitin.key" };

	/*(forum post by sidney tang,
	 * In order of lowest to highest priority, resources are loaded in this order:
	 *
		chitin.key, patch.key, xp1.key, xp1patch.key, xp2.key, xp2patch.key,
		override, module temp dir (if using toolset),
		module/savegame erf (if running game), texture pack erf, hak paks.
	 * */

	private Preferences prefs = null;

    /** Field with absolute path to the game directory. */
    private final JTextField nwnhome;
    /** Use resources from hakpaks, listed in {@link #hakListModel}. */
    private final JCheckBox useSourcehak;
    /**
     * List model with hakpaks that must be loaded. Hakpaks from begin of list
     * has more priority than from end, i.e. if the resource is present in both
     * hakpaks, it will be taken from hakpak which in the list is higher.
     */
    private final DefaultListModel<File> hakListModel;
    /** Also load resources from {@code override} folder in {@link #nwnhome game directory}. */
    private final JCheckBox useOverride;
    private final JCheckBox useKeyfiles;
    private final JTextField keyfiles;
    private JPanel configPanel = null;

	public NwnRepConfig() {
		this(Preferences.userNodeForPackage(NwnRepConfig.class));
	}

	public NwnRepConfig(Preferences prefs) {
        this.prefs = prefs;
        nwnhome = new JTextField(prefs.get(PREFS_NWNHOME, ""), 40);
        nwnhome.setEditable( false );
        useSourcehak = new JCheckBox("Use haks",
            prefs.getBoolean(PREFS_USEHAK, false)
        );
        hakListModel = new DefaultListModel<>();
        final int hakNum = prefs.getInt(PREFS_HAKNUM, 0);
        for (int i = 0; i < hakNum; i++) {
            final String fname = prefs.get(PREFS_HAK + i, null);
            if (fname != null) {
                final File f = new File(fname);
                if (f.exists()) {
                    hakListModel.addElement(f);
                }
            }
        }
        useOverride = new JCheckBox("Use NWN override dir",
            prefs.getBoolean(PREFS_USEOVERRIDE, false)
        );
        useKeyfiles = new JCheckBox("Use nwn bif data",
            prefs.getBoolean(PREFS_USEBIFS, false)
        );
        keyfiles = new JTextField(prefs.get(PREFS_BIFKEYS, ""), 40);
	}

	public void store(){
		prefs.put(PREFS_NWNHOME, nwnhome.getText());
		prefs.putBoolean(PREFS_USEHAK, useSourcehak.isSelected());
		prefs.putInt(PREFS_HAKNUM, hakListModel.size());
		for (int i = 0; i < hakListModel.size(); i++)
			prefs.put(PREFS_HAK + i, hakListModel.get(i).toString());
		prefs.putBoolean(PREFS_USEOVERRIDE, useOverride.isSelected());
		prefs.putBoolean(PREFS_USEBIFS, useKeyfiles.isSelected());
		prefs.put(PREFS_BIFKEYS, keyfiles.getText().trim());
	}

	public JPanel getConfigPanel() {
		if ( configPanel != null ) return configPanel;
        configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(new TitledBorder("Repository settings"));

        // nwn home textfield + selector
        final Action selectHome = new AbstractAction("Select") {
            JFileChooser fc = new JFileChooser();
            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fc.setMultiSelectionEnabled(false);
                if (!nwnhome.getText().isEmpty()) {
                    fc.setCurrentDirectory(new File(nwnhome.getText()));
                }
                if (fc.showDialog(nwnhome, "Select") == JFileChooser.APPROVE_OPTION) {
                    nwnhome.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        };

		Box keyFilePanel = new Box( BoxLayout.X_AXIS );

		keyFilePanel.add(useKeyfiles);
		JLabel label_keyoverride = new JLabel();
		label_keyoverride.setForeground( Color.RED );
		label_keyoverride.setText( "keyfile override" );
		String tt_keyoverride = "<html>key files should be detected automatically -<br>if it doesn't work enter list of key file names here, highest priority first<br>e.g. 'xp1patch.key xp1.key patch.key chitin.key'";
		label_keyoverride.setToolTipText( tt_keyoverride );
		keyFilePanel.add( label_keyoverride );
		keyFilePanel.add(keyfiles);
		keyfiles.setToolTipText( tt_keyoverride );

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.insets = new Insets(0, 2, 4, 2);

        //--------------------- ROW 0 ------------------------------------------
        gbc.gridx = 0;
        gbc.gridy = 0;
        configPanel.add(new JLabel("NWN dir"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        configPanel.add(nwnhome, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.0;
        configPanel.add(new JButton(selectHome), gbc);
        //--------------------- ROW 1 ------------------------------------------
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 1.0;
        gbc.gridwidth = 3;
        configPanel.add(useSourcehak, gbc);
        //--------------------- ROW 2-5 ----------------------------------------
        gbc.gridy++;
        gbc.gridy = setupHakPaksPanel(useSourcehak, hakListModel, (GridBagConstraints)gbc.clone());
        //--------------------- ROW 6 ------------------------------------------
        gbc.gridy++;
        configPanel.add(useOverride, gbc);
        //--------------------- ROW 7 ------------------------------------------
        gbc.gridy++;
        configPanel.add(keyFilePanel, gbc);

        /*Action store = new AbstractAction("Apply settings") {
            @Override
            public void actionPerformed(ActionEvent e) {
                store();
            }
        };
        configPanel.add(new JButton(store), gbc);*/
		return configPanel;
	}

	public File getNwnHome(){
		return new File( nwnhome.getText() );
	}

	public File[] getHakList(){
		File[] r = new File[ hakListModel.size() ];
		hakListModel.copyInto( r );
		return r;
	}

    /**
     * Creates resource repository according to the config settings.
     *
     * @return Repository that represents configured resources or {@code null},
     *         if no repositories was loaded
     *
     * @throws IOException If some error occured when reading some of configured repositories
     */
    public NwnRepository newRepository() throws IOException {
        store();
        final ArrayList<NwnRepository> reps = new ArrayList<>();
        if (prefs.getBoolean(PREFS_USEHAK, false)) {
            final int haknum = prefs.getInt(PREFS_HAKNUM, 0);
            for (int i = 0; i < haknum; ++i) {
                reps.add(new ErfFile(new File(prefs.get(PREFS_HAK + i, ""))));
            }
        }
        final File home = getNwnHome();
        if (prefs.getBoolean(PREFS_USEOVERRIDE, false)) {
            reps.add(new NwnDirRepository(new File(home, "override")));
        }
        if (prefs.getBoolean(PREFS_USEBIFS, false)) {
            final String keynames = prefs.get(PREFS_BIFKEYS, "");
            if (keynames.isEmpty()) {
                // Will use default list of key files
                reps.add(new BifRepository(home));
            } else {
                reps.add(new BifRepository(home, keynames.trim().split("\\s+")));
            }
        }
        final int size = reps.size();
        if (size > 1) {
            return new NwnChainRepository(reps);
        }
        return size > 0 ? reps.get(0) : null;
    }
    /**
     * Creates panel with list of hak paks and actions to maniputale this list.
     *
     * @param useHaks Checkbox, that controls, whether list of hakpaks must be enabled
     * @param haksModel Model, that contains hak paks to load
     *
     * @return Last used Y-index in {@link GridBagLayout}
     */
    private int setupHakPaksPanel(JCheckBox useHaks, DefaultListModel<File> haksModel, GridBagConstraints gbc) {
        final JList<File> list = new JList<>(haksModel);

        final Action add = new AbstractAction("Add...") {
            private final JFileChooser fc = new JFileChooser();
            {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!haksModel.isEmpty()) {
                    fc.setCurrentDirectory(haksModel.get(0));
                } else {
                    final File home = getNwnHome();
                    if (home != null) {
                        final File defHakDir = new File(home, "hak");
                        fc.setCurrentDirectory(defHakDir.exists() ? defHakDir : home);
                    }
                }
                if (fc.showDialog(nwnhome, "Add") == JFileChooser.APPROVE_OPTION) {
                    for (final File selectedFile : fc.getSelectedFiles()) {
                        haksModel.addElement(selectedFile);
                    }
                }
            }
        };
        final Action del = new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int[] selected = list.getSelectedIndices();
                for (int i = selected.length - 1; i >= 0; --i) {
                    haksModel.remove(selected[i]);
                }
            }
        };
        final Action up = new AbstractAction("Up") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int[] selected = list.getSelectedIndices();
                if (selected.length != 0) {
                    final int[] newSelection = selected.clone();
                    for (int i = 0; i < selected.length; ++i) {
                        final int index = selected[i];
                        swapHaks(index - 1, index);
                        --newSelection[i];
                    }
                    list.setSelectedIndices(newSelection);
                }
            }
        };
        final Action down = new AbstractAction("Down") {
            @Override
            public void actionPerformed(ActionEvent e) {
                final int[] selected = list.getSelectedIndices();
                if (selected.length != 0) {
                    final int[] newSelection = selected.clone();
                    for (int i = selected.length - 1; i >= 0; --i) {
                        final int index = selected[i];
                        swapHaks(index, index + 1);
                        ++newSelection[i];
                    }
                    list.setSelectedIndices(newSelection);
                }
            }
        };

        final Runnable update = () -> {
            final int[] selection = list.getSelectedIndices();
            final boolean hasSelection = useHaks.isSelected() && selection.length != 0;
            // "Remove" enabled only if something is selected
            // "Up" enabled, if minimal selected index is greater than 0
            // "Down" enabled, if maximal selected index is less than list size
            add.setEnabled(useHaks.isSelected());
            del.setEnabled(hasSelection);
            up.setEnabled(hasSelection && selection[0] > 0);
            down.setEnabled(hasSelection && selection[selection.length - 1] < haksModel.size()-1);
        };
        list.addListSelectionListener(e -> update.run());

        list.setEnabled(useHaks.isSelected());
        update.run();
        useHaks.addChangeListener(e -> {
            list.setEnabled(useHaks.isSelected());
            update.run();
        });
        gbc.gridwidth = 2;
        gbc.gridheight = 4;// 4 buttons
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        configPanel.add(new JScrollPane(list), gbc);

        gbc.anchor = GridBagConstraints.NORTH;
        gbc.gridx = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btn = new JButton(add);
        btn.setToolTipText("Select new paths to hak(s) in the filesystem");
        configPanel.add(btn, gbc);

        gbc.gridy++;
        btn = new JButton(del);
        btn.setToolTipText("Remove selected hak(s) from list");
        configPanel.add(btn, gbc);

        gbc.gridy++;
        btn = new JButton(up);
        btn.setToolTipText("Move all selected hak(s) to one position up.\n"
                + "The relative arrangement of the moved haks remains");
        configPanel.add(btn, gbc);

        gbc.gridy++;
        btn = new JButton(down);
        btn.setToolTipText("Move all selected hak(s) to one position down.\n"
                + "The relative arrangement of the moved haks remains");
        configPanel.add(btn, gbc);

        return gbc.gridy;
    }
    private void swapHaks(int index1, int index2) {
        final File f1 = hakListModel.get(index1);
        final File f2 = hakListModel.get(index2);
        hakListModel.set(index1, f2);
        hakListModel.set(index2, f1);
    }

	public static void main(String[] args) {
		final NwnRepConfig c = new NwnRepConfig();
		JFrame f = new JFrame("NWN Repository Settings");
		f.addWindowListener( new WindowAdapter(){ @Override public void windowClosing(WindowEvent e){
			c.store();
		} } );
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().add(c.getConfigPanel());
		f.pack();
		f.setVisible(true);
	}
}
