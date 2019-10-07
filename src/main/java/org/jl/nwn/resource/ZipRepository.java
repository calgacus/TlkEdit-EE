package org.jl.nwn.resource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Iterator;
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
}
