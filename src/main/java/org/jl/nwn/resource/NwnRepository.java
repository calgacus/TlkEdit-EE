package org.jl.nwn.resource;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

/**
 * Simple interface for retrieving & writing resources. In NWN resources are usually
 * located in bif files, erf files or directories.
 */
public interface NwnRepository extends Iterable<ResourceID>, Closeable {
    /**
     * @return InputStream for the resource with given id, null if no such resource is found
     */
    public InputStream getResource( ResourceID id ) throws IOException;

    /**
     * @return InputStream for the resource with given name, null if no such resource is found
     */
    public InputStream getResource( String resourceName ) throws IOException;

    /**
     * @return null if the repository contains no such resource
     */
    public File getResourceLocation( ResourceID id );

    public boolean contains( ResourceID id );

    public boolean contains( String resourceName );

    public Set<ResourceID> getResourceIDs();

    public OutputStream putResource( ResourceID id ) throws IOException, UnsupportedOperationException;

    public boolean isWritable();

    public long lastModified( ResourceID id );

    /**
     * retrieves the size of the resource in bytes.
     * @return 0 if the resource does not exist OR the size cannot be determined
     */
    public int getResourceSize( ResourceID id );
}
