package org.jl.nwn.erf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import org.jl.nwn.resource.Repositories;
import org.jl.nwn.resource.ResourceID;

/**
 * URLStreamHandler for reading from "erf" URLs, valid URLs have the form
 * erf:/path/to/erffile.hak#resource.xyz i.e. the path points to the local
 * erf file and the fragment is the requested resource. The URLConnection
 * object implements only {@link URLConnection#connect()} and
 * {@link URLConnection#getInputStream()}!
 */
public class Handler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        return new ErfURLConnection(url);
    }

    protected static class ErfURLConnection extends URLConnection {
        ErfFile erf;
        ResourceID id;

        protected ErfURLConnection(URL url) {
            super(url);
        }

        @Override
        public void connect() throws IOException {
            String erf = getURL().getPath();
            File erfFile = new File( erf );
            if ( !erfFile.exists() )
                throw new FileNotFoundException("ERF not found : " + erfFile);
            String resName = getURL().getRef();
            this.erf = Repositories.getInstance().getErfRepository(erfFile);
            System.out.println("Handler.java erf "+this.erf);
            id = ResourceID.forFileName(resName);
            connected = true;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            if ( !connected )
                connect();
            return erf.getResource(id);
        }
    }
}
