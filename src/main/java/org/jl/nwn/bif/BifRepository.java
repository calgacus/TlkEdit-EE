package org.jl.nwn.bif;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.MappedByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
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

/**
 */
public class BifRepository extends AbstractRepository{
    private KeyFile[] keyFiles;
    private File baseDir;
    private TreeSet resources;

    private final Map<String, BifFile> bifFiles = new HashMap<>();

    private static final String[] defaultkeys = { "xp2patch.key", "xp2.key", "xp1patch.key", "xp1.key", "patch.key", "chitin.key" };
    public static final List<String> DEFAULTKEYFILENAMES = Collections.unmodifiableList(Arrays.asList(defaultkeys));

    public BifRepository( File baseDir, KeyFile[] keys ){
        this.baseDir = baseDir;
        this.keyFiles = keys;
    }

    /** @param baseDir bif file names contained in keyfiles are all relative to basedir
     * @param keyFiles array of key file names, keyFiles[0] has highest priority
     */
    public BifRepository(File baseDir, String[] keyFiles) throws IOException {
        this.baseDir = baseDir;
        this.keyFiles = new KeyFile[keyFiles.length];
        for (int i = 0; i < keyFiles.length; i++){
            this.keyFiles[i] = KeyFile.open(new File(baseDir, keyFiles[i]));
        }
    }

    public BifRepository( File baseDir ) throws IOException{
        this( baseDir, testKeyFiles( defaultkeys, baseDir ) );
    }

    private static String[] testKeyFiles( String[] keys, File baseDir ){
        Vector v = new Vector();
        for ( int i = 0; i < keys.length; i++ )
            if ( new File( baseDir, keys[i]).exists() )
                v.add( keys[i] );
        return (String[]) v.toArray( new String[ v.size() ] );
    }

    /**
     * returns null if resource is not found
     */
    public InputStream getResource(String resourceName, short type)
    throws IOException {
        KeyFile.BifResourceLocation loc = findResourceLocation(resourceName, type);
        return loc == null ?
            null :
            getBifFile(loc.getBifName()).getEntry(loc.getBifIndex());
    }

    @Override
    public InputStream getResource(ResourceID id) throws IOException {
        return getResource(id.getName(), id.getType());
    }

    @Override
    public int getResourceSize( ResourceID id ){
        KeyFile kf = findKeyFile( id.getName(), id.getType() );
        if ( kf != null ){
            KeyFile.BifResourceLocation loc = kf.findResource( id.getName(), id.getType() );
            String bifName = loc.getBifName();
            BifFile bif = getBifFile( bifName );
            return bif.getEntrySize(loc.getBifIndex());
        }
        else return 0;
    }

    protected KeyFile findKeyFile( String resName, short resType ){
        for (KeyFile kf : keyFiles)
            if ( kf.findResource(resName, resType) != null )
                return kf;
        return null;
    }

    protected BifFile getBifFile( String bifName ){
        BifFile bif = (BifFile) bifFiles.get(bifName);
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

    protected BifFile findBifFile( String resName, short resType ){
        KeyFile.BifResourceLocation loc = findResourceLocation(resName, resType);
        return loc == null ? null : getBifFile(loc.getBifName());
    }

    protected KeyFile.BifResourceLocation findResourceLocation(String resName, short resType){
        for (KeyFile kf: keyFiles){
        KeyFile.BifResourceLocation loc = kf.findResource(resName, resType);
            if ( loc != null ){
                return loc;
            }
        }
        return null;
    }

    @Override
    public MappedByteBuffer getResourceAsBuffer( ResourceID id ) throws IOException{
        KeyFile.BifResourceLocation loc = findResourceLocation(id.getName(), id.getType());
        return loc == null ? null :  getBifFile(loc.getBifName()).getEntryAsBuffer(loc.getBifIndex());
    }

    @Override
    public File getResourceLocation(ResourceID id) {
        BifFile bif = findBifFile(id.getName(), id.getType());
        return bif == null ? null : bif.getFile();
    }

    public String getResourceKeyFile(ResourceID id) {
        KeyFile kf = findKeyFile(id.getName(), id.getType());
        return kf == null ? null : kf.getFileName();
    }

    @Override
    public Set getResourceIDs() {
        if ( resources == null ){
            resources = new TreeSet(this.keyFiles[0].getResourceIDSet());
            for (int i = 1; i < this.keyFiles.length; i++)
                resources.addAll(this.keyFiles[i].getResourceIDSet());
        }
        return Collections.unmodifiableSet( resources );
    }

    public boolean transferResourceToFile( ResourceID id, File file ) throws IOException{
        KeyFile.BifResourceLocation loc = findResourceLocation(id.getName(), id.getType());
        if (loc == null)
            return false;
        BifFile bif = getBifFile(loc.getBifName());
        bif.transferEntryToFile(loc.getBifIndex(), file);
        return true;
    }

    public static void main(String[] args) throws Exception {
        //System.getProperties().list( System.out );
        String keys = System.getProperty("nwn.bifkeys");
        long then = System.currentTimeMillis();
        BifRepository br = keys == null ?
            new BifRepository(new File(System.getProperty("nwn.home"))) :
            new BifRepository(new File(System.getProperty("nwn.home")), System.getProperty("nwn.bifkeys").split("\\s+") );
        System.out.printf("initialized repository : %d ms\n", System.currentTimeMillis()-then);
        if ( args.length == 1 & args[0].equals("-gui")) {
            br.displayGui();
            return;
        }
        if (args.length < 2
                || args.length == 0
                || !(args[0].equals("-l") | args[0].equals("-x"))) {
            System.out.println(
                    "usage : BifRepository [-x|-l] <regexp> <outputdir>\n extract / list resources matching <regexp>"+
                    " \n\nuse GUI : BifRepository -gui");
            System.exit(0);
        }
        boolean extract = args[0].equals("-x");
        File outputDir = new File(".");
        if (args.length == 3 && extract) {
            outputDir = new File(args[2]);
            if (!outputDir.exists())
                outputDir.mkdirs();
        }
        Matcher m = Pattern.compile(args[1]).matcher("");
        byte[] buf = new byte[50000];
        File resourceOut = null;
        OutputStream os = null;
        InputStream is;
        int count = 0;
        Iterator it = br.getResourceIDs().iterator();
        try {
            while (it.hasNext()) {
                ResourceID id = (ResourceID) it.next();
                String s = id.toString();
                if (m.reset(s).matches()) {
                    System.out.println("BifRepository "+s);
                    //+ " (" + br.getResourceLocation( id ) + ")" );
                    if (extract) {
                        br.transferResourceToFile(id, new File(outputDir, id.toString()));
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
        final JFrame f = new JFrame("bifextract");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        final DefaultListModel model = new DefaultListModel();
        final JList resourceList = new JList(model);
        System.out.print("retrieving resource ids ... ");
        Iterator it = getResourceIDs().iterator();
        System.out.println("BifRepository displayGui done");
        //Iterator it = new NwnRepConfig().getNwnRepository().listRealContents().iterator();
        while (it.hasNext())
            model.addElement(it.next());
        JLabel filterLabel = new JLabel("RegExp : ");
        final JTextField regexpField = new JTextField(".+");
        Action filter = new AbstractAction("filter") {
            @Override
            public void actionPerformed(ActionEvent e){
                Matcher m = null;
                try {
                    m = Pattern.compile(regexpField.getText()).matcher("");
                } catch (PatternSyntaxException pse) {
                    JOptionPane.showMessageDialog(
                            f,
                            "bad expression",
                            "foo",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Iterator it = getResourceIDs().iterator();
                model.clear();
                while (it.hasNext()) {
                    Object o = it.next();
                    if (m.reset(o.toString()).matches())
                        model.addElement(o);
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

        final JDialog infoDialog = new JDialog( f, "extracting files ...", false );
        final JLabel fileLabel = new JLabel("abcdefghijklmn.opq");
        infoDialog.getContentPane().add( fileLabel );
        infoDialog.pack();
        infoDialog.setSize( 200, 80 );

        final Action extractSelected = new AbstractAction("extract selected") {
            private void extract(){
                int[] selected = resourceList.getSelectedIndices();
                try{
                    if ( selected.length > 0 ){
                        f.setEnabled( false );
                        infoDialog.setVisible(true);
                        File outputDirFile = new File( outputDir.getText() );
                        for ( int index = 0; index < selected.length; index++ ){
                            ResourceID id  = ( ResourceID ) model.get( selected[index] );
                            String filename = id.toString();
                            fileLabel.setText( filename );
                            //writeFile( new File( outputDirFile, filename ), getResource( id ) );
                            transferResourceToFile(id, new File( outputDirFile, filename ));
                        }
                    }
                } catch ( IOException ioex ){
                    ioex.printStackTrace();
                } finally{
                    infoDialog.dispose();
                    f.setEnabled( true );
                }
            }

            @Override
            public void actionPerformed(ActionEvent e){
                infoDialog.setLocationRelativeTo( f );
                f.setEnabled( false );
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

        f.getContentPane().setLayout( new BorderLayout() );
        //f.getContentPane().add( new NwnRepConfig().getConfigPanel(), BorderLayout.NORTH );
        f.getContentPane().add( new JScrollPane( resourceList ), BorderLayout.CENTER );
        f.getContentPane().add(southBox, BorderLayout.SOUTH );
        f.pack();
        f.setVisible(true);
    }
/*
    private static byte[] buf = new byte[ 64000 ];
    private static void writeFile( File f, InputStream is ) throws IOException{
        FileOutputStream fos = new FileOutputStream( f );
        BufferedOutputStream os = new BufferedOutputStream( fos );
        int length = 0;
        while ( ( length = is.read( buf ) ) != -1 )
            os.write( buf, 0, length );
        os.flush();
        os.close();
        fos.close();
    }
 */
        /* (non-Javadoc)
         * @see org.jl.nwn.resource.NwnRepository#contains(org.jl.nwn.resource.ResourceID)
         */
    @Override
    public boolean contains(ResourceID id) {
        for ( int i = 0, n = keyFiles.length; i < n; i++ )
            if ( null != keyFiles[i].findResource(id.getName(), id.getType()) )
                return true;
        return false;
        //return resources.contains(id);
    }
}
