package org.jl.nwn.resource;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Repository using a zip file ( e.g. NWN2 )
 */
public class ZipRepository extends AbstractRepository{

    private final ZipFile zipFile;
    private final File file;

    private Map<ResourceID, ZipEntry> entries = null;

    public ZipRepository( File zip ) throws IOException{
        this.zipFile = new ZipFile(zip);
        this.file = zip;
        buildMap();
    }

    @Override
    public Iterator<ResourceID> iterator() {
        return getResourceIDs().iterator();
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    private void buildMap(){
        entries = new TreeMap<>();
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()){
            ZipEntry entry = e.nextElement();
            //System.out.println(entry.getName());
            if (!entry.isDirectory()){
                int p = entry.getName().lastIndexOf("/");
                String resourceName = entry.getName().substring(p+1);
                entries.put(ResourceID.forFileName(resourceName), entry);
            }
        }
    }

    @Override
    public Set<ResourceID> getResourceIDs() {
        return entries.keySet();
    }

    @Override
    public OutputStream putResource(ResourceID id) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("zip repository is write only");
    }

    @Override
    public long lastModified(ResourceID id) {
        ZipEntry e = entries.get(id);
        return e!=null? e.getTime() : -1;
    }

    @Override
    public int getResourceSize(ResourceID id) {
        ZipEntry e = entries.get(id);
        long zipSize = e!=null? e.getSize() : 0;
        return zipSize==-1? 0 : (int)zipSize;
    }

    @Override
    public File getResourceLocation(ResourceID id) {
        return contains(id)? file : null;
    }

    @Override
    public InputStream getResource(ResourceID id) throws IOException {
        return contains(id) ?
            zipFile.getInputStream(entries.get(id)) :
            null;
    }

    @Override
    public boolean contains(ResourceID id) {
        return entries.containsKey(id);
    }

    @Override
    public void close() throws IOException{
        zipFile.close();
    }

    public static NwnRepository makeZipChainRepository(File[] zipFiles) throws IOException{
        ZipRepository[] zipReps = new ZipRepository[zipFiles.length];
        for ( int i=0; i < zipReps.length; i++ )
            zipReps[i] = new ZipRepository(zipFiles[i]);
        return new NwnChainRepository(zipReps);
    }

    public static FilenameFilter zipFileFilter(){
        return new FilenameFilter(){
            @Override
            public boolean accept(File dir, String name){
                return name.toLowerCase().endsWith(".zip");
            }
        };
    }

    public static void main(String ... args) throws IOException{
        File base = new File("/media/WindowsApps/spiele/NeverwinterNights2/Data/");
        File[] a = base.listFiles(zipFileFilter());
        //Arrays.sort(a);
        final List<File> list = Arrays.asList(a);
        Collections.sort(list);
        Collections.reverse(list);
        System.out.println(list);
        NwnRepository rep = makeZipChainRepository(base.listFiles(zipFileFilter()));
        //NwnRepository rep = new ZipRepository(new File(base, "2DA.zip"));
        System.out.println(rep.contains(ResourceID.forFileName("spells.2da")));
        //for ( ResourceID id : rep ) System.out.println(id);
        /*
        long l = rep.lastModified(ResourceID.forFileName("spells.2da"));
        System.out.println(new Date(l));
        new TwoDaTable(rep.getResource(ResourceID.forFileName("spells.2da"))).write(System.out);
         */
        //new TwoDaTable(rep.getResource(ResourceID.forFileName("spells.2da"))).write(System.out);
        long l = rep.lastModified(ResourceID.forFileName("spells.2da"));
        System.out.println(new Date(l));
        System.out.println(rep.getResourceSize(ResourceID.forFileName("spells.2da")));

        File[] zipFiles = base.listFiles(zipFileFilter());
        NwnRepository[] reps = new NwnRepository[zipFiles.length];
        for ( int i = 0; i < reps.length; i++ )
            reps[i] = new ZipRepository(zipFiles[i]);
        for ( int i = 0; i < reps.length; i++ ){
            for ( int j = i+1; j < reps.length; j++ ){
                System.out.println(zipFiles[i].getName() +
                        " vs " +
                        zipFiles[j].getName());
                //System.out.println(Collections.disjoint(reps[i].getResourceIDs(), reps[j].getResourceIDs()));
                for ( ResourceID id : reps[i] )
                    if ( reps[j].contains(id) )
                        System.out.println(id);
            }
        }
    }
}
