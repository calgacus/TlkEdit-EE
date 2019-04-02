package org.jl.nwn.resource;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * Implementation of DataInput over a stream that can be switched between
 * little endian and big endian byte order.
 */
public class OrderedDataInputStream extends FilterInputStream implements DataInput{
    
    DataInputStream dataIn;
    LittleEndianDataInputStream inLE;
    DataInput current;
    
    /** Creates a new instance of OrderedDataInputStream */
    public OrderedDataInputStream( InputStream is ) {
        super(is);
        dataIn = new DataInputStream(is);
        inLE = new LittleEndianDataInputStream(is);
        current = dataIn;
    }
    
    public void setByteOrder(ByteOrder order){
        current = order.equals(ByteOrder.LITTLE_ENDIAN)?
            inLE : dataIn;
    }
    
    public ByteOrder getByteOrder(){
        return (current==dataIn)? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }
    
    
    public void readFully(byte[] b, int off, int len) throws IOException{
        current.readFully(b, off, len);
    }
    
    public void readFully(byte[] b) throws IOException{
        current.readFully(b);
    }
    
    public int skipBytes(int n) throws IOException{
        return current.skipBytes(n);
    }
    
    public int readUnsignedShort() throws IOException{
        return current.readUnsignedShort();
    }
    
    public boolean readBoolean() throws IOException{
        return current.readBoolean();
    }
    
    public byte readByte() throws IOException{
        return current.readByte();
    }
    
    public char readChar() throws IOException{
        return current.readChar();
    }
    
    public double readDouble() throws IOException{
        return current.readDouble();
    }
    
    public float readFloat() throws IOException{
        return current.readFloat();
    }
    
    public int readInt() throws IOException{
        return current.readInt();
    }
    
    /**
     @deprecated string decoding always using default charset
     */
    public String readLine() throws IOException{
        return dataIn.readLine();
    }
    
    public long readLong() throws IOException{
        return current.readLong();
    }
    
    public short readShort() throws IOException{
        return current.readShort();
    }
    
    public String readUTF() throws IOException{
        return dataIn.readUTF();
    }
    
    public int readUnsignedByte() throws IOException{
        return dataIn.readUnsignedByte();
    }
    
    public void close() throws IOException{
        try{
            dataIn.close();
            inLE.close();
        }finally{
            super.close();
        }
    }
    
}
