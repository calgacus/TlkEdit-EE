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
    
    public void readFully(byte[] b, int off, int len) throws java.io.IOException {
        in.read(b, off,len);
    }
    
    public void readFully(byte[] b) throws java.io.IOException {
        in.read(b);
    }
    
    public int skipBytes(int n) throws java.io.IOException {
        return (int) in.skip(n);
    }
    
    public int readUnsignedShort() throws java.io.IOException {
        return ( in.read() | in.read()<<8 );
    }
    
    public boolean readBoolean() throws java.io.IOException {
        return in.read()!=0;
    }
    
    public byte readByte() throws java.io.IOException {
        return (byte) in.read();
    }
    
    public char readChar() throws java.io.IOException {
        return (char) readUnsignedShort();
    }
    
    public double readDouble() throws java.io.IOException {
        return Double.longBitsToDouble(readLong());
    }
    
    public float readFloat() throws java.io.IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    public int readInt() throws java.io.IOException {
        return in.read() | in.read() << 8 | in.read() << 16 | in.read() << 24;
    }
    
    /**
     @deprecated string decoding always using default charset
     */
    public String readLine() throws java.io.IOException {
        return dis.readLine();
    }
    
    public long readLong() throws java.io.IOException {
        return ((long)in.read()) | ((long)in.read()) << 8 |
                ((long)in.read()) << 16 | ((long)in.read()) << 24 |
                ((long)in.read()) << 32 | ((long)in.read()) << 40 |
                ((long)in.read()) << 48 | ((long)in.read()) << 56;
    }
    
    public short readShort() throws java.io.IOException {
        return (short) (in.read() | in.read()<<8);
    }
    
    public String readUTF() throws java.io.IOException {
        return dis.readUTF();
    }
    
    public int readUnsignedByte() throws java.io.IOException {
        return in.read();
    }
    
    public void close() throws IOException{
        try{
            dis.close();
        } finally {
            super.close();
        }
    }
    
}
