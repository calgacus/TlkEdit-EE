/*
 * Created on 09.06.2004
 */
package org.jl.nwn.resource;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 */
public abstract class AbstractRepository implements NwnRepository{

	@Override
	public OutputStream putResource(ResourceID id)
		throws IOException, UnsupportedOperationException {
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
            InputStream is = getResource(id);
            if ( is == null )
                return null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            byte[] buf = new byte[4000];
            int i;
            while ( (i = bis.read(buf)) != -1 )
                baos.write(buf, 0, i);
            bis.close();
            is.close();
            return ByteBuffer.wrap(baos.toByteArray());
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
