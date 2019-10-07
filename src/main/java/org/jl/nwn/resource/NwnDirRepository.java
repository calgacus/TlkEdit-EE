package org.jl.nwn.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class NwnDirRepository extends AbstractRepository {

    private final File dir;

    public NwnDirRepository(File dir) {
        this.dir = dir;
    }

    @Override
    public InputStream getResource(ResourceID id) throws IOException {
        final File f = findFile(id);
        return f != null ? new FileInputStream(f) : null;
    }

    /**
     * Return the File containing the given resource. If no file with the exact
     * name returned by {@link ResourceID#getFileName() id.getFileName()} is found,
     * the method will perform a case insensitive search for the file.
     *
     * @return File object pointing to the resource identified by id or {@code null}
     *         if no such file is found
     */
    private File findFile(ResourceID id) {
        final String fName = id.getFileName();
        final File f = new File(dir, fName);
        return f.isFile() ? f : findFileIgnoreCase(fName);
    }

    private File findFileIgnoreCase(String fileName) {
        for (final String name : dir.list()) {
            if (name.equalsIgnoreCase(fileName)) {
                final File f = new File(dir, name);
                return f.isFile() ? f : null;
            }
        }
        return null;
    }

    @Override
    public File getResourceLocation(ResourceID id) {
        return contains(id) ? dir : null;//new File(dir, id.getFileName());
    }

    @Override
    public boolean contains(ResourceID id) {
        return findFile( id ) != null;
    }

    @Override
    public OutputStream putResource(ResourceID id) throws IOException {
        return new FileOutputStream(new File(dir, id.getFileName()));
    }

    @Override
    public Set<ResourceID> getResourceIDs() {
        final TreeSet<ResourceID> s = new TreeSet<>();
        for (final File file : dir.listFiles()) {
            if (file.isFile()) {
                s.add(ResourceID.forFile(file));
            }
        }
        return Collections.unmodifiableSet(s);
    }

    @Override
    public boolean isWritable() {
        return dir.canWrite();
    }

    @Override
    public long lastModified(ResourceID id) {
        final File f = findFile( id );
        return f != null ? f.lastModified() : 0;
    }

    @Override
    public int getResourceSize(ResourceID id) {
        final File f = findFile(id);
        return f != null ? (int)f.length() : 0;
    }
}
