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

    @Override
    public void readFully(byte[] b, int off, int len) throws IOException{
        current.readFully(b, off, len);
    }

    @Override
    public void readFully(byte[] b) throws IOException{
        current.readFully(b);
    }

    @Override
    public int skipBytes(int n) throws IOException{
        return current.skipBytes(n);
    }

    @Override
    public int readUnsignedShort() throws IOException{
        return current.readUnsignedShort();
    }

    @Override
    public boolean readBoolean() throws IOException{
        return current.readBoolean();
    }

    @Override
    public byte readByte() throws IOException{
        return current.readByte();
    }

    @Override
    public char readChar() throws IOException{
        return current.readChar();
    }

    @Override
    public double readDouble() throws IOException{
        return current.readDouble();
    }

    @Override
    public float readFloat() throws IOException{
        return current.readFloat();
    }

    @Override
    public int readInt() throws IOException{
        return current.readInt();
    }

    /** @deprecated string decoding always using default charset */
    @Override
    @Deprecated
    public String readLine() throws IOException{
        return dataIn.readLine();
    }

    @Override
    public long readLong() throws IOException{
        return current.readLong();
    }

    @Override
    public short readShort() throws IOException{
        return current.readShort();
    }

    @Override
    public String readUTF() throws IOException{
        return dataIn.readUTF();
    }

    @Override
    public int readUnsignedByte() throws IOException{
        return dataIn.readUnsignedByte();
    }

    @Override
    public void close() throws IOException{
        try{
            dataIn.close();
            inLE.close();
        }finally{
            super.close();
        }
    }
}
