package org.jl.nwn.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.jl.nwn.bif.BifRepository;
import org.jl.nwn.erf.ErfFile;

/**
 * creates a repository from a configuration file
 */
public class RepConfig {

    protected File configLocation;
    protected Properties props;

    protected NwnRepository rep;

    public RepConfig(File propFile) throws IOException{
        this(new FileInputStream(propFile));
        configLocation = propFile;
    }

    public RepConfig(InputStream is) throws IOException{
        props = new Properties();
        try{
            props.load(is);
            rep = new NwnChainRepository(initRepositories(props).toArray(new NwnRepository[0]));
        } finally {
            try{
                if (is != null) is.close();
            } catch (IOException ioex){
                ioex.printStackTrace();
            }
        }
    }

    public NwnRepository getRepository(){
        return rep;
    }

    public static List<NwnRepository> initRepositories(Properties props) throws IOException{
        final ArrayList<NwnRepository> reps = new ArrayList<>();
        try{
            int filecount = Integer.parseInt( props.getProperty( "filecount", "0" ) );
            String basepath = props.getProperty("basedir");
            File base = null;
            if ( basepath != null )
                base = new File(basepath);
            for ( int i = 0; i < filecount; i++ ){
                String filename = props.getProperty( "file" + i );
                if ( filename != null ){
                    File file = base != null ?
                        new File(base, filename):
                        new File(filename);
                    System.out.println("adding repository : " + file);
                    if ( file.exists() ){
                        if ( file.isDirectory() )
                            reps.add( new NwnDirRepository(file) );
                        else
                            if ( file.getName().toLowerCase().endsWith(".zip") )
                                reps.add( new ZipRepository(file) );
                            else
                                reps.add( new ErfFile( file ) );
                    }
                }
            }
            if ( props.get("bifbasedir") != null ){
                File bifBase = new File(props.getProperty("bifbasedir"));
                if ( bifBase.exists() && bifBase.isDirectory() ){
                    String keyfilenames = (String) props.get("bifkeys");
                    if (keyfilenames != null){
                        reps.add( new BifRepository(bifBase, keyfilenames.split("\\s+")) );
                    }
                    else
                        reps.add( new BifRepository(bifBase) );
                }
            }
        } catch (IOException ioex){
            System.out.println(ioex);
            ioex.printStackTrace();
            for (NwnRepository r : reps){
                try {
                    r.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            reps.clear();
        }
        return reps;
    }
}
