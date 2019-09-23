package org.jl.nwn.resource;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/** Create an {@link InputStream} that reads a portion of a {@link RandomAccessFile}. */
public class RafInputStream extends InputStream {
    private final RandomAccessFile raf;
    private final long start;
    private final long end;
    private long markPos;
    private long streamPosition = 0;

    public RafInputStream(RandomAccessFile raf, long start, long end) throws IOException {
        this.raf = raf;
        this.start = start;
        streamPosition = start;
        this.end = end;
        raf.seek(start);
    }

    @Override
    public int read() throws IOException {
        if ( !(streamPosition < end )  ) return -1;
        raf.seek( streamPosition++ );
        return raf.read();
    }

    @Override
    public int available(){
        return (int)(end - streamPosition);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r = 0;
        if ( !(streamPosition < end )  ) return -1;
        raf.seek( streamPosition );
        r = raf.read( b, off, (int) Math.min( len, end - streamPosition ) );
        streamPosition += r;
        return r;
    }

    @Override
    public boolean markSupported(){
        return true;
    }
    @Override
    public void mark( int readLimit ){ markPos = streamPosition; }

    @Override
    public void reset() throws IOException{ streamPosition = markPos; }

    @Override
    public long skip( long skip ) throws IOException{
        long r = Math.min( skip, end - streamPosition );
        streamPosition += r;
        return r;
    }
}
