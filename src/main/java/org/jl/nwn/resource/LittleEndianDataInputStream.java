package org.jl.nwn.resource;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LittleEndianDataInputStream extends FilterInputStream implements DataInput{
    
    private DataInputStream dis;
    
    /** Creates a new instance of LittleEndianDataInputStream */
    public LittleEndianDataInputStream( InputStream in) {
        super(in);
        dis = new DataInputStream(in);
    }
    
    @Override
    public void readFully(byte[] b, int off, int len) throws java.io.IOException {
        in.read(b, off,len);
    }
    
    @Override
    public void readFully(byte[] b) throws java.io.IOException {
        in.read(b);
    }
    
    @Override
    public int skipBytes(int n) throws java.io.IOException {
        return (int) in.skip(n);
    }
    
    @Override
    public int readUnsignedShort() throws java.io.IOException {
        return ( in.read() | in.read()<<8 );
    }
    
    @Override
    public boolean readBoolean() throws java.io.IOException {
        return in.read()!=0;
    }
    
    @Override
    public byte readByte() throws java.io.IOException {
        return (byte) in.read();
    }
    
    @Override
    public char readChar() throws java.io.IOException {
        return (char) readUnsignedShort();
    }
    
    @Override
    public double readDouble() throws java.io.IOException {
        return Double.longBitsToDouble(readLong());
    }
    
    @Override
    public float readFloat() throws java.io.IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    @Override
    public int readInt() throws java.io.IOException {
        return in.read() | in.read() << 8 | in.read() << 16 | in.read() << 24;
    }
    
    /**
     @deprecated string decoding always using default charset
     */
    @Override
    public String readLine() throws java.io.IOException {
        return dis.readLine();
    }
    
    @Override
    public long readLong() throws java.io.IOException {
        return ((long)in.read()) | ((long)in.read()) << 8 |
                ((long)in.read()) << 16 | ((long)in.read()) << 24 |
                ((long)in.read()) << 32 | ((long)in.read()) << 40 |
                ((long)in.read()) << 48 | ((long)in.read()) << 56;
    }
    
    @Override
    public short readShort() throws java.io.IOException {
        return (short) (in.read() | in.read()<<8);
    }
    
    @Override
    public String readUTF() throws java.io.IOException {
        return dis.readUTF();
    }
    
    @Override
    public int readUnsignedByte() throws java.io.IOException {
        return in.read();
    }
    
    @Override
    public void close() throws IOException{
        try{
            dis.close();
        } finally {
            super.close();
        }
    }
    
}
