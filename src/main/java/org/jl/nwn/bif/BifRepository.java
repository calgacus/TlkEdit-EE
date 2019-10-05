package org.jl.nwn.bif;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.jl.nwn.resource.AbstractRepository;
import org.jl.nwn.resource.ResourceID;

public class BifRepository extends AbstractRepository{
    private final KeyFile[] keyFiles;
    /** BIF file names contained in {@code .key} files are all relative to this dir. */
    private final File baseDir;
    /** Cache of resources from all {@code .key} files. */
    private TreeSet<ResourceID> resources;

    /** Cache of loaded BIF files. */
    private final Map<String, BifFile> bifFiles = new HashMap<>();

    /** Array of the known names of {@code .key} files in preference order their loadings. */
    private static final String[] DEFAULT_KEYS = {
        "xp3.key",
        "xp2patch.key",
        "xp2.key",
        "xp1patch.key",
        "xp1.key",
        "patch.key",
        "chitin.key"
    };

    /**
     * Reads the specified {@code .key}-files with the index of all resources
     * which a game can use.
     *
     * @param baseDir bif file names contained in keyfiles are all relative to basedir
     * @param keyFiles Array of key file names, {@code keyFiles[0]} has highest priority
     *
     * @throws IOException If one of specified {@code .key} files do not exist or
     *         cann't be readed
     * @throws IllegalArgumentException If one of specified {@code .key} files is
     *         not one of supported file type
     */
    public BifRepository(File baseDir, String[] keyFiles) throws IOException {
        this.baseDir = baseDir;
        this.keyFiles = new KeyFile[keyFiles.length];
        for (int i = 0; i < keyFiles.length; i++){
            this.keyFiles[i] = KeyFile.open(new File(baseDir, keyFiles[i]));
        }
    }

    public BifRepository( File baseDir ) throws IOException{
        this(baseDir, filterExisting(baseDir, DEFAULT_KEYS));
    }

    private static String[] filterExisting(File baseDir, String[] keys) {
        final ArrayList<String> v = new ArrayList<>(keys.length);
        for (final String key : keys) {
            if (new File(baseDir, key).exists()) {
                v.add(key);
            }
        }
        return v.toArray( new String[ v.size() ] );
    }

    //<editor-fold defaultstate="collapsed" desc="NwnRepository">
    @Override
    public InputStream getResource(ResourceID id) throws IOException {
        final KeyFile.BifResourceLocation loc = findResourceLocation(id);
        final BifFile bif = loc == null ? null : getBifFile(loc);
        return bif == null ? null : bif.getEntry(loc.getBifIndex());
    }

    @Override
    public MappedByteBuffer getResourceAsBuffer(ResourceID id) throws IOException {
        final KeyFile.BifResourceLocation loc = findResourceLocation(id);
        final BifFile bif = loc == null ? null : getBifFile(loc);
        return bif == null ? null : bif.getEntryAsBuffer(loc.getBifIndex());
    }

    @Override
    public File getResourceLocation(ResourceID id) {
        final KeyFile.BifResourceLocation loc = findResourceLocation(id);
        final BifFile bif = loc == null ? null : getBifFile(loc);
        return bif == null ? null : bif.getFile();
    }

    @Override
    public int getResourceSize(ResourceID id) {
        final KeyFile.BifResourceLocation loc = findResourceLocation(id);
        final BifFile bif = loc == null ? null : getBifFile(loc);
        return bif == null ? 0 : bif.getEntrySize(loc.getBifIndex());
    }

    @Override
    public Set<ResourceID> getResourceIDs() {
        if (resources == null) {
            resources = new TreeSet<>();
            for (final KeyFile key : keyFiles) {
                resources.addAll(key.getResources());
            }
        }
        return Collections.unmodifiableSet( resources );
    }

    @Override
    public boolean contains(ResourceID id) {
        return findResourceLocation(id) != null;
    }
    //</editor-fold>

    public boolean transferResourceToFile(ResourceID id, File file) throws IOException {
        final KeyFile.BifResourceLocation loc = findResourceLocation(id);
        final BifFile bif = loc == null ? null : getBifFile(loc);
        if (bif != null) {
            bif.transferEntryToFile(loc.getBifIndex(), file);
            return true;
        }
        return false;
    }

    /**
     * Search for first index {@code .key} file, that contains entry about specified
     * resource. Returns the most priority file of a repository
     *
     * @param resRef Pointer to resource
     *
     * @return Location in first index file that contains entry about specified
     *         resource or {@code null}, if such file does not exist
     */
    private KeyFile.BifResourceLocation findResourceLocation(ResourceID resRef) {
        for (final KeyFile kf : keyFiles) {
            final KeyFile.BifResourceLocation loc = kf.findResource(resRef);
            if (loc != null) {
                return loc;
            }
        }
        return null;
    }

    private BifFile getBifFile(KeyFile.BifResourceLocation loc) {
        final String bifName = loc.getBifName();
        BifFile bif = bifFiles.get(bifName);
        if (bif == null) {
            try{
              bif = BifFile.openBifFile(new File(baseDir, bifName));
              bifFiles.put(bifName, bif);
            } catch ( IOException ioex ){
                System.err.println(ioex);
                bifFiles.put( bifName, null );
            }
        }
        return bif;
    }

    public static void main(String[] args) throws Exception {
        final String keys = System.getProperty("nwn.bifkeys");
        final File baseDir = new File(System.getProperty("nwn.home", "."));
        long then = System.currentTimeMillis();
        final BifRepository br = keys == null
            ? new BifRepository(baseDir)
            : new BifRepository(baseDir, keys.split("\\s+") );
        System.out.printf("initialized repository at %s : %d ms\n", baseDir, System.currentTimeMillis()-then);
        if ( args.length == 1 && args[0].equals("-gui")) {
            br.displayGui();
            return;
        }
        if (args.length < 2 || !(args[0].equals("-l") || args[0].equals("-x"))) {
            System.out.println(
                    "usage : BifRepository [-x|-l] <regexp> <outputdir>\n"
                  + "extract / list resources matching <regexp>\n"
                  + "\n"
                  + "use GUI : BifRepository -gui"
            );
            return;
        }
        boolean extract = args[0].equals("-x");
        File outputDir = new File(".");
        if (args.length == 3 && extract) {
            outputDir = new File(args[2]);
            if (!outputDir.exists())
                outputDir.mkdirs();
        }
        final Pattern pat = Pattern.compile(args[1]);
        int count = 0;
        try {
            for (final ResourceID id : br.getResourceIDs()) {
                String s = id.toString();
                if (pat.matcher(s).matches()) {
                    System.out.println("BifRepository "+s);
                    if (extract) {
                        br.transferResourceToFile(id, new File(outputDir, s));
                        count++;
                    }
                }
            }
        } catch (IOException ioex) {
            System.out.println("files extracted : " + count);
            System.out.println(ioex);
            ioex.printStackTrace();
        }
    }

    private void displayGui() throws IOException{
        final JFrame frame = new JFrame("bifextract");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final DefaultListModel<ResourceID> model = new DefaultListModel<>();
        final JList<ResourceID> resourceList = new JList<>(model);
        System.out.print("retrieving resource ids ... ");
        final Iterator<ResourceID> it = getResourceIDs().iterator();
        System.out.println("BifRepository displayGui done");
        while (it.hasNext())
            model.addElement(it.next());
        JLabel filterLabel = new JLabel("RegExp : ");
        final JTextField regexpField = new JTextField(".+");
        Action filter = new AbstractAction("filter") {
            @Override
            public void actionPerformed(ActionEvent e){
                try {
                    final Pattern pat = Pattern.compile(regexpField.getText());
                    final Iterator<ResourceID> it = getResourceIDs().iterator();
                    model.clear();
                    while (it.hasNext()) {
                        final ResourceID id = it.next();
                        if (pat.matcher(id.toString()).matches()) {
                            model.addElement(id);
                        }
                    }
                } catch (PatternSyntaxException pse) {
                    JOptionPane.showMessageDialog(
                            frame,
                            pse.getMessage(),
                            "Bad expression",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        Box filterControls = new Box(BoxLayout.X_AXIS);
        filterControls.add(filterLabel);
        filterControls.add(regexpField);
        filterControls.add( new JButton( filter ) );

        final JTextField outputDir = new JTextField( new File("").getAbsolutePath() );
        Action selectOutputDir = new AbstractAction("select") {
            JFileChooser fc = new JFileChooser();
            {
                fc.setMultiSelectionEnabled(false);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputDir.getText().length() > 0 )
                    fc.setCurrentDirectory(new File(outputDir.getText()));
                if (fc.showDialog(outputDir, "select")
                == JFileChooser.APPROVE_OPTION) {
                    outputDir.setText(fc.getSelectedFile().getAbsolutePath());
                }
            }
        };
        Box outputControls = new Box(BoxLayout.X_AXIS);
        outputControls.add( new JLabel("Output dir ") );
        outputControls.add( outputDir );
        outputControls.add( new JButton( selectOutputDir ) );

        final JDialog infoDialog = new JDialog( frame, "extracting files ...", false );
        final JLabel fileLabel = new JLabel("abcdefghijklmn.opq");
        infoDialog.getContentPane().add( fileLabel );
        infoDialog.pack();
        infoDialog.setSize( 200, 80 );

        final Action extractSelected = new AbstractAction("extract selected") {
            private void extract(){
                int[] selected = resourceList.getSelectedIndices();
                try{
                    if ( selected.length > 0 ){
                        frame.setEnabled( false );
                        infoDialog.setVisible(true);
                        File outputDirFile = new File( outputDir.getText() );
                        for (final int index : selected) {
                            final ResourceID id = model.get(index);
                            String filename = id.toString();
                            fileLabel.setText( filename );
                            transferResourceToFile(id, new File( outputDirFile, filename ));
                        }
                    }
                } catch ( IOException ioex ){
                    ioex.printStackTrace();
                } finally{
                    infoDialog.dispose();
                    frame.setEnabled( true );
                }
            }

            @Override
            public void actionPerformed(ActionEvent e){
                infoDialog.setLocationRelativeTo( frame );
                frame.setEnabled( false );
                Thread t = new Thread(){
                    @Override
                    public void run(){
                        extract();
                    }
                };
                t.start();
            }
        };

        Box southBox = new Box(BoxLayout.Y_AXIS);
        southBox.add(new JScrollPane(resourceList));
        southBox.add(filterControls);
        southBox.add(outputControls);
        JToolBar tbar = new JToolBar();
        tbar.setFloatable( false );
        tbar.add( extractSelected );
        southBox.add( tbar );

        frame.getContentPane().setLayout( new BorderLayout() );
        //frame.getContentPane().add( new NwnRepConfig().getConfigPanel(), BorderLayout.NORTH );
        frame.getContentPane().add( new JScrollPane( resourceList ), BorderLayout.CENTER );
        frame.getContentPane().add(southBox, BorderLayout.SOUTH );
        frame.pack();
        frame.setVisible(true);
    }
}
