package org.jl.nwn.resource;

import java.io.DataInput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Wraps a DataInput object and provides additional support for reading 
 * primitive values in little endian byte order.
 * Internally, this class uses a ByteBuffer for converting bytes to primitive
 * types. Methods that do not depend on the byte order are delegated to
 * the wrapped DataInput.
 */
public class OrderedDataInput implements DataInput{
    
    DataInput in;
    byte[] bytes = new byte[8];
    ByteBuffer bb = ByteBuffer.wrap(bytes);
    
    /** Creates a new instance of OrderedDataInput */
    public OrderedDataInput(DataInput in) {
        this.in = in;
    }
    
    public void setByteOrder(ByteOrder order){
        bb.order(order);
    }
    
    public ByteOrder getByteOrder(){
        return bb.order();
    }    
    
    @Override
    public void readFully(byte[] b, int off, int len) throws IOException{
        in.readFully(b, off, len);
    }
    
    @Override
    public void readFully(byte[] b) throws IOException{
        in.readFully(b);
    }
    
    @Override
    public int skipBytes(int n) throws IOException{
        return in.skipBytes(n);
    }
    
    @Override
    public int readUnsignedShort() throws IOException{
        in.readFully(bytes,0,2);
        char c = bb.getChar(0);
        return (int) c;
    }
    
    @Override
    public boolean readBoolean() throws IOException{
        return in.readBoolean();
    }
    
    @Override
    public byte readByte() throws IOException{
        return in.readByte();
    }
    
    @Override
    public char readChar() throws IOException{
        in.readFully(bytes,0,2);
        return bb.getChar(0);
    }
    
    @Override
    public double readDouble() throws IOException{
        in.readFully(bytes,0,8);
        return bb.getDouble(0);
    }
    
    @Override
    public float readFloat() throws IOException{
        in.readFully(bytes,0,4);
        return bb.getFloat(0);
    }
    
    @Override
    public int readInt() throws IOException{
        if ( bb.order() == ByteOrder.LITTLE_ENDIAN )
            return in.readUnsignedByte() | in.readUnsignedByte() << 8 | in.readUnsignedByte() << 16 | in.readUnsignedByte() << 24;
        else
            return in.readInt();
        //in.readFully(bytes,0,4);
        //return bb.getInt(0);
    }
    
    @Override
    public String readLine() throws IOException{
        return in.readLine();
    }
    
    @Override
    public long readLong() throws IOException{
        in.readFully(bytes,0,8);
        return bb.getLong(0);
    }
    
    @Override
    public short readShort() throws IOException{
        in.readFully(bytes,0,2);
        return bb.getShort(0);
    }
    
    @Override
    public String readUTF() throws IOException{
        return in.readUTF();
    }
    
    @Override
    public int readUnsignedByte() throws IOException{
        return in.readUnsignedByte();
    }
    
}
