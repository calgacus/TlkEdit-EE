package org.jl.nwn.patcher;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jl.nwn.erf.ErfFile;
import org.jl.nwn.resource.NwnChainRepository;
import org.jl.nwn.resource.NwnDirRepository;
import org.jl.nwn.resource.NwnRepository;

public class PatcherGUI extends JFrame {

	// keys for Prefences entries
	private static final String PREFS_SOURCETLK = "sourcetlk";
	private static final String PREFS_SOURCETLKISUSER = "sourcetlkuser";
	private static final String PREFS_USEOUTPUTTLK = "useoutputtlk";
	private static final String PREFS_OUTPUTTLK = "outputtlk";
	private static final String PREFS_REPACKAGEHAK = "repackagehak";
	private static final String PREFS_USEPATCHEROVERRIDE = "usesourcedir ";
	private static final String PREFS_PATCHEROVERRIDE =  "sourcedir";
	private static final String PREFS_BUILDPATCH =  "applypatch";

	private static final String PREFS_HAKMOVE =  "hakmove";
	private static final String PREFS_HAKNAME =  "hakname";

	final String PREFS_NUMJOINPATCHES = "numjoinpatches";
	final String PREFS_JOINPATCHES = "joinpatches";
	final String PREFS_JOINEDPATCH = "joinedpatch";

	final DefaultListModel<String> patchListModel = new DefaultListModel<>();
	final JTextField joinedPatchName = new JTextField( prefs.get(PREFS_JOINEDPATCH, ""), 40 );

	StdOutFrame sof = StdOutFrame.getInstance();

	private static final Preferences prefs =
		Preferences.userNodeForPackage(PatcherGUI.class);

	private NwnRepConfig repConf = new NwnRepConfig( prefs );

	JTextField sourceTlk = new JTextField(prefs.get(PREFS_SOURCETLK, ""), 40);
	JCheckBox isUserTlk = new JCheckBox( "user tlk", prefs.getBoolean( PREFS_SOURCETLKISUSER, false ) );

	JCheckBox useOutputTlk =
		new JCheckBox(
			"move output tlk file to",
			prefs.getBoolean(PREFS_USEOUTPUTTLK, false));
	JTextField outputTlk = new JTextField(prefs.get(PREFS_OUTPUTTLK, ""), 40);

	JCheckBox repackageSourcehak =
		new JCheckBox("repackage first source hak", prefs.getBoolean(PREFS_REPACKAGEHAK, false));

	JCheckBox moveHakToHakDir =
		new JCheckBox("move hak to nwn hak dir", prefs.getBoolean(PREFS_HAKMOVE, false));
	JTextField hakName = new JTextField(prefs.get(PREFS_HAKNAME, ""), 40);

	JCheckBox useSourceDir =
		new JCheckBox(
			"use patcher override dir",
			prefs.getBoolean(PREFS_USEPATCHEROVERRIDE, false));
	JTextField sourceDir = new JTextField(prefs.get(PREFS_PATCHEROVERRIDE, ""), 40);

	JTextField joinedpatch = new JTextField(40);

	JTextField applypatch = new JTextField(prefs.get(PREFS_BUILDPATCH, ""), 40);

	public PatcherGUI() {
		super("2da Patcher");
		//getContentPane().setLayout(	new BoxLayout( this, BoxLayout.Y_AXIS ) );
	 	//setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				super.windowClosing(e);
				savePreferences();
				System.exit(0);
			}
		});

		JPanel c1 = new JPanel( new GridLayout( 0, 1 ) );
		JPanel c2 = new JPanel( new GridLayout( 0, 1 ) );
		JPanel c3 = new JPanel( new GridLayout( 0, 1 ) );

		//		tlk source textfield + selector
		Action selectSourceTlk = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser( repConf.getNwnHome() );
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setMultiSelectionEnabled(false);
				if (sourceTlk.getText() != "")
					fc.setCurrentDirectory(new File(sourceTlk.getText()));
				if (fc.showDialog(sourceTlk, "select")
					== JFileChooser.APPROVE_OPTION) {
					sourceTlk.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		JPanel sourceTlkPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

		Box sourceTlkBox = new Box( BoxLayout.X_AXIS );
		sourceTlkBox.add( new JLabel("source TLK file ") );
		sourceTlkBox.add( isUserTlk );

		//c1.add(new JLabel("source TLK file "));
		c1.add( sourceTlkBox );

		c2.add(sourceTlk);
		sourceTlk.setEditable( false );
		c3.add(new JButton(selectSourceTlk));

		//		tlk output textfield + selector
		Action selectoutputTlk = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser( repConf.getNwnHome() );
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setMultiSelectionEnabled(false);
				if (outputTlk.getText() != "")
					fc.setCurrentDirectory(new File(outputTlk.getText()));
				if (fc.showDialog(outputTlk, "select")
					== JFileChooser.APPROVE_OPTION) {
					outputTlk.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		//JPanel outputTlkPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		Box outputTlkPanel = new Box( BoxLayout.X_AXIS );
		useOutputTlk.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				outputTlk.setEnabled(useOutputTlk.isSelected());
			}
		});
		outputTlkPanel.add( useOutputTlk );
		outputTlkPanel.add( outputTlk );
		outputTlkPanel.add( new JButton( selectoutputTlk ) );
		/*
		c1.add(useOutputTlk);
		c2.add(outputTlk);
		c3.add(new JButton(selectoutputTlk));
		*/
		outputTlk.setEnabled(useOutputTlk.isSelected());


		//	source dir textfield + selector
		JPanel patcherOverridePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		Action selectsourceDir = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser();
			{
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				if (sourceDir.getText() != "")
					fc.setCurrentDirectory(new File(sourceDir.getText()));
				if (fc.showDialog(sourceDir, "select")
					== JFileChooser.APPROVE_OPTION) {
					sourceDir.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		sourceDir.setEditable( false );
		useSourceDir.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				sourceDir.setEnabled(useSourceDir.isSelected());
			}
		});
		c1.add(useSourceDir);
		c2.add(sourceDir);
		c3.add(new JButton(selectsourceDir));
		sourceDir.setEnabled(useSourceDir.isSelected());

		//		build patch textfield + selector
		JPanel buildPatchPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		Action selectapplypatch = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser();
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setMultiSelectionEnabled(false);
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (applypatch.getText() != "")
					fc.setCurrentDirectory(new File(applypatch.getText()));
				if (fc.showDialog(applypatch, "select")
					== JFileChooser.APPROVE_OPTION) {
					applypatch.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		c1.add(new JLabel("build patch"));
		c2.add(applypatch);
		applypatch.setEditable(false);
		c3.add(new JButton(selectapplypatch));

		Action buildPatch = new AbstractAction("build") {
			@Override
			public void actionPerformed(ActionEvent e){
				sof.setVisible( true );
				Thread buildThread = new Thread(){
				@Override
					public void run() {
						build();

						//XXX things get messy down here
						File[] haks = repConf.getHakList();
						File outputHakFile = null;
						File patchDir = new File( applypatch.getText() );
						if ( repackageSourcehak.isSelected() && haks.length > 0 )
							try {
								File hakOutputDir = Patcher.getOutputHak( new File( applypatch.getText() ) ).getParentFile();
								outputHakFile = new File( hakOutputDir, haks[0].getName() );
								//HakpakRep repackagedHak = new HakpakRep( new File( hakOutputDir, hakFile.getName() ) );
								repackage( haks[0], Patcher.getOutputDir( patchDir ), outputHakFile );
							} catch (IOException e1) {
								popupErrorMsg( "failed to repackage hak : " + e1 );
							}

						if ( moveHakToHakDir.isSelected() ){
							// move generated hak file to nwn hak dir
							if ( outputHakFile == null )
								outputHakFile = Patcher.getOutputHak( patchDir );
							String hakFileName = hakName.getText().length() > 0 ? hakName.getText() : outputHakFile.getName();
							File target = new File( new File( repConf.getNwnHome(), "hak" ), hakFileName );
							try {
								Patcher.filemove( outputHakFile, target );
							} catch (IOException e) {
								popupErrorMsg( "failed to move hak file : " + e );
								e.printStackTrace();
							}
						}
						System.out.println("-------- done ---------");
					}
				};
				buildThread.start();
				/*
				build();
				File[] haks = repConf.getHakList();
				if ( repackageSourcehak.isSelected() && haks.length > 0 )
					try {
						repackage( haks[0], new File( applypatch.getText() ) );
					} catch (IOException e1) {
						popupErrorMsg( "failed to repackage hak : " + e1 );
					}
				System.out.println("-------- done ---------");
				*/
			}

		};
		Box b = new Box( BoxLayout.Y_AXIS );

        b.add(repConf);
		//b.add( repackageSourcehak );

		JPanel p = new JPanel();
		p.setLayout( new BoxLayout( p, BoxLayout.X_AXIS ) );
		p.add( c1 );
		p.add( c2 );
		p.add( c3 );
		p.setBorder(new TitledBorder("Build settings"));
		//p.add( new JButton( buildPatch ) );

		b.add( p );

		Box b2 = new Box( BoxLayout.Y_AXIS );
		b2.add( outputTlkPanel );
		JPanel px = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		px.add( repackageSourcehak );
		Box outputHakSettings = new Box( BoxLayout.X_AXIS );
		outputHakSettings.add( moveHakToHakDir );
		outputHakSettings.add( new JLabel("hak file name : ") );
		outputHakSettings.add( hakName );
		b2.add( px );
		b2.add( outputHakSettings );
		b2.setBorder(new TitledBorder("Misc. settings"));

		b.add( b2 );

		b.add( new JButton( buildPatch ) );
		//getContentPane().add( b );
		JTabbedPane tpane = new JTabbedPane();
		getContentPane().add( tpane );
		tpane.add( b, "build" );
		tpane.add( setupJoinPanel(), "join" );
		tpane.setSelectedIndex( 0 );

		pack();
		setVisible(true);
	}

	private void popupErrorMsg( String msg ){
		JOptionPane.showMessageDialog( this, msg, "Error", JOptionPane.ERROR_MESSAGE );
	}

	private void savePreferences() {
		prefs.put(PREFS_SOURCETLK, sourceTlk.getText());
		prefs.putBoolean(PREFS_SOURCETLKISUSER, isUserTlk.isSelected());
		prefs.put(PREFS_OUTPUTTLK, outputTlk.getText());
		prefs.putBoolean(PREFS_USEOUTPUTTLK, useOutputTlk.isSelected());
		prefs.putBoolean(PREFS_REPACKAGEHAK, repackageSourcehak.isSelected());
		prefs.putBoolean(PREFS_USEPATCHEROVERRIDE, useSourceDir.isSelected());
		prefs.put(PREFS_PATCHEROVERRIDE, sourceDir.getText());
		prefs.put(PREFS_BUILDPATCH, applypatch.getText());

		prefs.put(PREFS_HAKNAME, hakName.getText());
		prefs.putBoolean(PREFS_HAKMOVE, moveHakToHakDir.isSelected());

		int numjoinpatches = patchListModel.size();
		for ( int i = 0; i < numjoinpatches; i++ )
            prefs.put(PREFS_JOINPATCHES + i, patchListModel.get( i ) );
		 prefs.putInt( PREFS_NUMJOINPATCHES, numjoinpatches );
		 prefs.put( PREFS_JOINEDPATCH, joinedPatchName.getText() );

		repConf.store();
	}

    /**
     * Creates new repository object according to the stored user preferences
     * for PatcherGUI application.
     *
     * @return a repository according to the values in the user preferences
     *         (must run PatcherGUI once before using this)
     *
     * @throws IOException If some error occured when reading some of configured
     *         repositories
     */
    public static NwnRepository newRepository() throws IOException {
        return new NwnRepConfig(prefs).newRepository();
    }

	private void build() {
		NwnRepository rep = null;
		try{
            rep = repConf.newRepository();
		} catch ( IOException ioex ){
			System.out.println( "Fatal Error : " + ioex );
			popupErrorMsg( "Could not create repository : " + ioex );
			ioex.printStackTrace();
			return;
		}
		if ( useSourceDir.isSelected() ){
			File oDir = new File( sourceDir.getText() );
			if( oDir.exists() )
				rep = new NwnChainRepository( new NwnDirRepository( oDir ), rep );
			else {
				popupErrorMsg( "Specified patcher override dir does not exist : " + oDir );
				return;
			}
		}
		File buildDir = new File( applypatch.getText() );
		if ( !buildDir.exists() ){
			popupErrorMsg( "Patch dir does not exist : " + applypatch.getText() );
			return;
		}
		File tlkFile = new File( sourceTlk.getText() );
		if ( !tlkFile.exists() ){
			popupErrorMsg( "Tlk file does not exist : " + sourceTlk.getText() );
			return;
		}
		try{
			Patcher.applyPatch( buildDir, rep, repConf.getNwnHome(), tlkFile, true, true, isUserTlk.isSelected() );
		} catch ( IOException ioex ){
			popupErrorMsg( "Patch build failed : " + ioex );
			return;
		}
		if ( useOutputTlk.isSelected() ){
			File tlk = Patcher.getOutputTlk( buildDir );
			//new File( new File( new File( buildDir, "out" ), "tlk" ), "dialog.tlk" );
			try {
			Patcher.filemove( tlk, new File( outputTlk.getText() ) );
		} catch ( IOException ioex ){
			popupErrorMsg( "Error moving tlk file : " + ioex );
			return;
			}
		}
		/*
		if ( moveHakToHakDir.isSelected() ){
			File hakDir = new File( repConf.getNwnHome(), "hak" );
			// argh! different file name if repackage is selected
			//File hak =
		}
		*/
	}

	/**
	 * add files in sourceDir to hak file hakFile (replacing resources in hakFile), write new hak file to outputHak
	 * @param hakFile source hak file
	 * @param sourceDir directory containing the files to add to hak file
	 * @param outputHak output hak file
	 * */
	private static void repackage( File hakFile, File sourceDir, File outputHak ) throws IOException{
		//File sourceDir = Patcher.getOutputDir( patchDir );
		final File[] files = sourceDir.listFiles( new FileFilter(){
			@Override
			public boolean accept( File f ){
				return f.isFile();
			}
		} );
		System.out.println( "files : " + files.length );
		System.out.println( hakFile );
		ErfFile baseHak = new ErfFile( hakFile );
        for (final File file : files) {
            baseHak.putResource(file);
        }
		baseHak.write( outputHak );
	}

	private JComponent setupJoinPanel(){
		final Box joinBox = new Box( BoxLayout.Y_AXIS );

		// read Preferences
		int n = prefs.getInt( PREFS_NUMJOINPATCHES, 0 );
		for ( int i = 0; i < n; i++ )
			patchListModel.addElement( prefs.get( PREFS_JOINPATCHES + i, "" ) );

        final JList<String> patchList = new JList<>( patchListModel );
		JPanel patchListPanel = new JPanel( new BorderLayout() );

		final JToolBar tbar = new JToolBar(JToolBar.VERTICAL );
		tbar.setFloatable( false );

		patchListPanel.add(new JScrollPane(patchList), BorderLayout.CENTER);
		Action add = new AbstractAction("add") {
			JFileChooser fc = new JFileChooser();
			{
				if (!patchListModel.isEmpty())
                    fc.setCurrentDirectory( new File( patchListModel.getElementAt(0) ));
			}
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setMultiSelectionEnabled(true);
                if (fc.showDialog( patchList, "add" ) == JFileChooser.APPROVE_OPTION) {
                    for (final File file : fc.getSelectedFiles()) {
                        patchListModel.addElement(file.getAbsolutePath());
                    }
				}
			}
		};
		Action up = new AbstractAction("up") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int line = patchList.getSelectedIndex();
				if (line > 0) {
					//Object o = model.elementAt( line );
					patchListModel.insertElementAt(
						patchListModel.remove(line),
						line - 1);
					patchList.setSelectedIndex(line - 1);
				}
			}
		};
		Action down = new AbstractAction("down") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int line = patchList.getSelectedIndex();
				if (line != -1 && line < patchListModel.size() - 1) {
					//Object o = model.elementAt( line );
					patchListModel.insertElementAt(
						patchListModel.remove(line),
						line + 1);
					patchList.setSelectedIndex(line + 1);
				}
			}
		};
		Action del = new AbstractAction("del") {
			@Override
			public void actionPerformed(ActionEvent e) {
				int line = patchList.getSelectedIndex();
				if (line != -1) {
					patchListModel.remove(line);
                    if (!patchListModel.isEmpty())
						patchList.setSelectedIndex(Math.max(line - 1, 0));
				}
			}
		};
		tbar.add( add );
		tbar.add( up );
		tbar.add( down );
		tbar.add( del ).setToolTipText( "remove selected patch from list" );
		patchListPanel.add( tbar, BorderLayout.EAST);

		final JPanel p = new JPanel();
		p.add( new JLabel( "output dir" ) );
		p.add( joinedPatchName );
		joinedPatchName.setEnabled( false );
		Action selectOutputPatchName = new AbstractAction("select") {
			JFileChooser fc = new JFileChooser();
			@Override
			public void actionPerformed(ActionEvent e) {
				fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fc.setMultiSelectionEnabled(false);
				if (joinedPatchName.getText() != "")
					fc.setCurrentDirectory(new File(joinedPatchName.getText()));
				if (fc.showDialog( joinedPatchName, "select")
					== JFileChooser.APPROVE_OPTION) {
						joinedPatchName.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		};
		p.add( new JButton( selectOutputPatchName ) );

		Action joinPatches = new AbstractAction( "join" ){
			@Override
			public void actionPerformed(ActionEvent e) {
                final ArrayList<String> v = new ArrayList<>();
				for ( int i = 0; i < patchListModel.size(); i++ )
					v.add( patchListModel.get( i ) );
                if (joinedPatchName.getText().isEmpty()) {
					JOptionPane.showMessageDialog( p, "Must specify output dir", "", JOptionPane.ERROR_MESSAGE );
					return;
				}
				new Thread(){
					@Override
					public void run(){
						try{
							setEnabled( false );
							StdOutFrame.display( true );
							Patcher.joinPatches( v, new File( joinedPatchName.getText() ) );
						} catch ( Exception ioex ){
							JOptionPane.showMessageDialog( p, "Exception during join operation : " + ioex.toString() + "\n" + ioex.getMessage(), "d'oh", JOptionPane.ERROR_MESSAGE );
						} finally {
							setEnabled( true );
						}
					}
				}.start();
			}
		};
		joinBox.add( new JLabel("patch dirs") );
		joinBox.add( patchListPanel );
		joinBox.add( p );
		joinBox.add( new JButton( joinPatches ) );
		return joinBox;
	}

	public static void removePreferences() throws BackingStoreException{
		prefs.removeNode();
	}

	public static void main(String[] args) {
		new PatcherGUI();
	}
}
