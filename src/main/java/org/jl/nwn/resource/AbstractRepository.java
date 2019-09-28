package org.jl.nwn.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
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
    public ByteBuffer getResourceAsBuffer(ResourceID id) throws IOException, UnsupportedOperationException {
        try (final InputStream is = getResource(id)) {
            if ( is == null )
                return null;
            try (final BufferedInputStream bis = new BufferedInputStream(is);
                 final ByteArrayOutputStream baos = new ByteArrayOutputStream()
            ) {
                final byte[] buf = new byte[32000];
                int len;
                while ( (len = bis.read(buf)) != -1 ) {
                    baos.write(buf, 0, len);
                }
                return ByteBuffer.wrap(baos.toByteArray());
            }
        }
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
