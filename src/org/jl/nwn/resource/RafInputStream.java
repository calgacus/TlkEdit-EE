package org.jl.nwn.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 Create an InputStream that reads a portion of a RandomAccessFile.
 */
public class RafInputStream extends InputStream {
    private RandomAccessFile raf;
    private long start, end, markPos, streamPosition = 0;
    
    public RafInputStream(RandomAccessFile raf, long start, long end)
    throws IOException {
        this.raf = raf;
        this.start = start;
        streamPosition = start;
        this.end = end;
        raf.seek(start);
    }
    
    public int read() throws IOException {
        if ( !(streamPosition < end )  ) return -1;
        raf.seek( streamPosition++ );
        return raf.read();
    }
    
    public int available(){
        return (int)(end - streamPosition);
    }
    
    public int read(byte[] b, int off, int len) throws IOException {
        int r = 0;
        if ( !(streamPosition < end )  ) return -1;
        raf.seek( streamPosition );
        r = raf.read( b, off, (int) Math.min( len, end - streamPosition ) );
        streamPosition += r;
        return r;
    }
    
    public boolean markSupported(){
        return true;
    }
    public void mark( int readLimit ){ markPos = streamPosition; }
    
    public void reset() throws IOException{ streamPosition = markPos; }
    
    public long skip( long skip ) throws IOException{
        long r = Math.min( skip, end - streamPosition );
        streamPosition += r;
        return r;
    }
    
}
