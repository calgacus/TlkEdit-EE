package org.jl.nwn.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

public abstract class AbstractRepository implements NwnRepository{

    @Override
    public OutputStream putResource(ResourceID id) throws IOException, UnsupportedOperationException {
        throw new UnsupportedOperationException("putResource : repository is read-only");
    }

    @Override
    public InputStream getResource(String resourceName) throws IOException{
        return getResource(ResourceID.forFileName(resourceName));
    }

    @Override
    public boolean contains(String resourceName){
        return contains(ResourceID.forFileName(resourceName));
    }

    @Override
    public boolean isWritable() {
        return false;
    }

    @Override
    public long lastModified(ResourceID id) {
        return 0;
    }

    @Override
    public Iterator<ResourceID> iterator(){
        return getResourceIDs().iterator();
    }

    @Override
    public void close() throws IOException{}
}
