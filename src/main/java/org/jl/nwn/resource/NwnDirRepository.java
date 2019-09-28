/*
 * Created on 02.01.2004
 */
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

/**
 */
public class NwnDirRepository extends AbstractRepository {

	private final File dir;

	public NwnDirRepository( File dir ){
		this.dir = dir;
	}

	/* (non-Javadoc)
	 * @see org.jl.nwn.resource.NwnRepository#getResource(org.jl.nwn.resource.ResourceID)
	 */
	@Override
	public InputStream getResource(ResourceID id) throws IOException {
		File f = findFile( id );
		return f!=null ? new FileInputStream(f) : null;
	}

	/**
	 * return the File containing the given resource. if no file with the exact name
	 * returned by id.toFileName() is found, the method will perform a case insensitive
	 * search for the file
	 * @return File object pointing to the resource identified by id or null if no such file is found
	 * */
	private File findFile( ResourceID id ){
        final String fName = id.getFileName();
		File f = new File( dir, fName );
		return f.isFile() ? f : findFileIgnoreCase( fName );
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

	/* (non-Javadoc)
	 * @see org.jl.nwn.resource.NwnRepository#getResourceLocation(org.jl.nwn.resource.ResourceID)
	 */
	@Override
	public File getResourceLocation(ResourceID id) {
		return contains(id)? dir : null;//new File( dir, id.toFileName() );
	}

	/* (non-Javadoc)
	 * @see org.jl.nwn.resource.NwnRepository#contains(org.jl.nwn.resource.ResourceID)
	 */
	@Override
	public boolean contains(ResourceID id) {
		return findFile( id ) != null;
	}

	@Override
	public OutputStream putResource( ResourceID id ) throws IOException{
        return new FileOutputStream( new File( dir, id.getFileName() ) );
	}

	@Override
    public Set<ResourceID> getResourceIDs() {
        final TreeSet<ResourceID> s = new TreeSet<>();
        for (final File file : dir.listFiles()) {
            if (file.isFile()) {
                s.add(ResourceID.forFile(file));
            }
        }
		return Collections.unmodifiableSet( s );
	}

	@Override
	public boolean isWritable() {
		return dir.canWrite();
	}

	@Override
	public long lastModified(ResourceID id) {
		File f = findFile( id );
		return f!=null? f.lastModified() : 0;
	}

    @Override
    public int getResourceSize(ResourceID id) {
        final File f = findFile(id);
        return f != null ? (int)f.length() : 0;
    }
}
