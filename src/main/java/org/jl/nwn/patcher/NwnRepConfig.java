package org.jl.nwn.patcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
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
import javax.swing.JToolBar;
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
    private final JTextField nwnhome;
    private final JCheckBox useSourcehak;
    private final DefaultListModel<File> hakListModel;
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
		configPanel = new JPanel();
		configPanel.setLayout( new BoxLayout( configPanel, BoxLayout.Y_AXIS ) );
		//new JPanel( new GridLayout(0, 1));

		Box selectNwnHomePanel = new Box( BoxLayout.X_AXIS );//new JPanel(new FlowLayout(FlowLayout.LEFT));
		selectNwnHomePanel.add(new JLabel("NWN dir"));

		selectNwnHomePanel.add(nwnhome);
		// nwn home textfield + selector
		Action selectHome = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser();
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setMultiSelectionEnabled(false);
				if (nwnhome.getText().length()>0)
					fc.setCurrentDirectory(new File(nwnhome.getText()));
				if (fc.showDialog(nwnhome, "select")
					== JFileChooser.APPROVE_OPTION) {
					nwnhome.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		selectNwnHomePanel.add(new JButton(selectHome));

        final JPanel sourceHakPanel = createHakPaksPanel(useSourcehak, hakListModel);
		//JPanel keyFilePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
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

		configPanel.add(selectNwnHomePanel);
		configPanel.add(sourceHakPanel);

		JPanel overridePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		overridePanel.add( useOverride );
		configPanel.add( overridePanel );
		configPanel.add(keyFilePanel);

		Action store = new AbstractAction("apply settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				store();
			}
		};
		//configPanel.add(new JButton(store));
		configPanel.setBorder(new TitledBorder("Repository settings"));
		//configPanel.setPreferredSize( new Dimension( 620, 320 ) );
		return configPanel;
	}

	public File getNwnHome(){
		return new File( nwnhome.getText() );
	}

	public void setNwnHome( File f ){
		if ( f.isDirectory() ) nwnhome.setText( f.getAbsolutePath() );
	}

	public File[] getHakList(){
		File[] r = new File[ hakListModel.size() ];
		hakListModel.copyInto( r );
		return r;
	}

	public NwnRepository getNwnRepository() throws IOException {
		store();
        final ArrayList<NwnRepository> reps = new ArrayList<>();
		File nwnhome = new File(prefs.get(PREFS_NWNHOME, ""));
		if (prefs.getBoolean(PREFS_USEHAK, false)){
			int haknum = prefs.getInt( PREFS_HAKNUM, 0 );
			for ( int i = 0; i < haknum; i++ ){
				reps.add(new ErfFile(new File(prefs.get(PREFS_HAK + i, ""))));
				//System.out.println( new File(prefs.get(PREFS_HAK + i, "")));
			}
		}
		if (prefs.getBoolean(PREFS_USEOVERRIDE, false))
			reps.add(new NwnDirRepository(new File(nwnhome, "override")));
		if (prefs.getBoolean(PREFS_USEBIFS, false)){
			String keynames = prefs.get(PREFS_BIFKEYS, "");
			if ( keynames.length() == 0 ){
				reps.add( new BifRepository( nwnhome ) );
				/*
				for ( int i = 0; i < defaultkeys.length; i++ )
					if ( new File( nwnhome, defaultkeys[i] ).exists() )
						keynames += defaultkeys[i] + " ";
				*/
			}
			//System.out.println( keynames );
			else reps.add(
				new BifRepository(
					nwnhome,
					keynames.trim().split("\\s+")));
		}
		NwnRepository r = null;
		if (reps.size() > 0)
            r = reps.get(0);
		if (reps.size() > 1)
			for (int i = 1; i < reps.size(); i++)
                r = new NwnChainRepository(r, reps.get(i));
		return r;
	}
    /**
     * Creates panel with list of hak paks and actions to maniputale this list.
     *
     * @param useHaks Checkbox, that controls, whether list of hakpaks must be enabled
     * @param haksModel Model, that contains hak paks to load
     *
     * @return Panel with settings
     */
    private JPanel createHakPaksPanel(JCheckBox useHaks, DefaultListModel<File> haksModel) {
        final JList<File> list = new JList<>(haksModel);

        final Action addHak = new AbstractAction("Add") {
            JFileChooser fc = new JFileChooser();
            {
                if (haksModel.size() > 0)
                    fc.setCurrentDirectory(haksModel.getElementAt(0));
                else{
                    if ( getNwnHome() != null ){
                        File defHakDir = new File( getNwnHome(), "hak" );
                        if ( defHakDir.exists() )
                            fc.setCurrentDirectory( defHakDir );
                        else
                            fc.setCurrentDirectory( getNwnHome() );
                    }
                }
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fc.setMultiSelectionEnabled(true);
                if (haksModel.isEmpty() && getNwnHome() != null) {
                    File defHakDir = new File( getNwnHome(), "hak" );
                    if ( defHakDir.exists() )
                        fc.setCurrentDirectory( defHakDir );
                    else
                        fc.setCurrentDirectory( getNwnHome() );
                }
                if (fc.showDialog(nwnhome, "add") == JFileChooser.APPROVE_OPTION) {
                    for (final File selectedFile : fc.getSelectedFiles()) {
                        haksModel.addElement(selectedFile);
                    }
                }
            }
        };
        final Action del = new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = list.getSelectedIndex();
                if (line != -1) {
                    haksModel.remove(line);
                    if (haksModel.size() > 0)
                        list.setSelectedIndex(Math.max(line - 1, 0));
                }
            }
        };
        final Action up = new AbstractAction("Up") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = list.getSelectedIndex();
                if (line > 0) {
                    //Object o = model.elementAt( line );
                    haksModel.insertElementAt(
                        haksModel.remove(line),
                        line - 1);
                    list.setSelectedIndex(line - 1);
                }
            }
        };
        final Action down = new AbstractAction("Down") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int line = list.getSelectedIndex();
                if (line != -1 && line < haksModel.size() - 1) {
                    //Object o = model.elementAt( line );
                    haksModel.insertElementAt(
                        haksModel.remove(line),
                        line + 1);
                    list.setSelectedIndex(line + 1);
                }
            }
        };

        final JToolBar tbar = new JToolBar(JToolBar.VERTICAL);
        tbar.setFloatable(false);
        tbar.add(addHak);
        tbar.add(del).setToolTipText("Remove selected hak from list");
        tbar.add(up);
        tbar.add(down);

        list.setEnabled(useHaks.isSelected());
        tbar.setVisible(useHaks.isSelected());
        useHaks.addChangeListener(e -> {
            list.setEnabled(useHaks.isSelected());
            tbar.setVisible(useHaks.isSelected());
        });

        final JPanel panel = new JPanel(new BorderLayout());
        panel.add(useHaks, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        panel.add(tbar, BorderLayout.EAST);
        return panel;
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
