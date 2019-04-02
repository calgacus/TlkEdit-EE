package org.jl.nwn.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.jl.nwn.bif.BifRepository;
import org.jl.nwn.erf.ErfFile;

/**
 * Singleton registry for open repositories, also contains factory methods for opening
 * repositories
 */
public final class Repositories {

    private HashMap<Descriptor, NwnRepository> repositories;

    /** Creates a new instance of Repositories */
    private Repositories() {
        repositories = new HashMap<Descriptor, NwnRepository>();
    }

    private static class InstanceHolder {

        static Repositories instance = new Repositories();
    }

    public static Repositories getInstance() {
        return InstanceHolder.instance;
    }

    private class Descriptor {

        Class rClass;
        File[] files;

        private Descriptor(Class c, File[] files) {
            this.rClass = c;
            this.files = files;
        }

        @Override
        public int hashCode() {
            return Arrays.deepHashCode(files) + rClass.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Descriptor) && ((Descriptor) o).rClass.equals(rClass) && Arrays.deepEquals(files, ((Descriptor) o).files);
        }
    }

    public BifRepository getBifRepository(File baseDir, String[] keyfiles) throws IOException {
        File[] files = keyfiles == null ? new File[]{baseDir} : new File[1 + keyfiles.length];
        if (keyfiles != null) {
            for (int i = 0; i < keyfiles.length; i++) {
                File key = new File(baseDir, keyfiles[i]);
                if (!key.exists() || key.isDirectory()) {
                    throw new FileNotFoundException("Keyfile not found : " + keyfiles[i]);
                }
                files[1 + i] = key;
            }
        }
        Descriptor d = new Descriptor(BifRepository.class, files);
        NwnRepository r = repositories.get(d);
        if (r == null) {
            r = keyfiles == null ? new BifRepository(baseDir) : new BifRepository(baseDir, keyfiles);
            repositories.put(d, r);
        }
        return (BifRepository) r;
    }

    public ErfFile getErfRepository(File erf) throws IOException {
        Descriptor d = new Descriptor(ErfFile.class, new File[]{erf});
        NwnRepository r = repositories.get(d);
        if (r == null) {
            r = new ErfFile(erf);
            repositories.put(d, r);
        }
        return (ErfFile) r;
    }

    public ZipRepository getZipRepository(File zip) throws IOException {
        Descriptor d = new Descriptor(ZipRepository.class, new File[]{zip});
        NwnRepository r = repositories.get(d);
        if (r == null) {
            r = new ZipRepository(zip);
            repositories.put(d, r);
        }
        return (ZipRepository) r;
    }

    public NwnChainRepository getChainRepository(File propertiesFile) throws IOException {
        Descriptor d = new Descriptor(NwnChainRepository.class, new File[]{propertiesFile});
        NwnRepository r = repositories.get(d);
        if (r == null) {
            InputStream is = null;
            try {
                Properties p = new Properties();
                is = new FileInputStream(propertiesFile);
                p.load(is);
                r = new NwnChainRepository(p);
                repositories.put(d, r);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (NwnChainRepository) r;
    }

    protected void register(NwnRepository rep, File[] files) {
        Descriptor d = new Descriptor(rep.getClass(), files);
        repositories.put(d, rep);
    }

    public static void extractResourceToFile(NwnRepository rep, ResourceID id, File f) throws IOException {
        System.out.println(f);
        InputStream is = rep.getResource(id);
        if (is == null) {
            return;
        }
        OutputStream os = null;
        try {
            byte[] buffer = new byte[32000];
            os = new FileOutputStream(f);
            int l = 0;
            while ((l = is.read(buffer)) != -1) {
                os.write(buffer, 0, l);
            }
            os.flush();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
            try {
                if (os != null) {
                    os.close();
                }
            } catch (IOException ioex) {
                ioex.printStackTrace();
            }
        }
    }

    protected static Map<NwnRepository, File> tmpDirMap = new HashMap<NwnRepository, File>();

    public static File extractAsTempFile(NwnRepository rep, ResourceID id) throws IOException {
        File dir = tmpDirMap.get(rep);
        if (dir == null) {
            dir = File.createTempFile("nwn_rep", "tmp");
            dir.delete();
            dir.mkdirs();
            dir.deleteOnExit();
            tmpDirMap.put(rep, dir);
        }
        File f = new File(dir, id.getNameExt());
        if (f.exists()) {
            f = File.createTempFile(id.getName() + ".", "." + id.getExtension(), dir);
        }
        f.deleteOnExit();
        extractResourceToFile(rep, id, f);
        return f;
    }
}
