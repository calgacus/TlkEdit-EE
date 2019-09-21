package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import org.jl.nwn.resource.*;

/**
 * read only representation of a bif file
 */
abstract class BifFile {

    protected static byte[] header10 = new byte[]{66, 73, 70, 70, 86, 49, 32, 32};
    protected static byte[] header11 = new byte[]{66, 73, 70, 70, 86, 49, 46, 49};

    protected RandomAccessFile raf;
    protected File file;
    protected FileChannel fc;
    protected byte[] header;
    protected int size; // number of resources in this file
    protected int fixedResourceCount;
    protected int variableResourceOffset;

    protected BifFile(File f) throws IOException {
        file = f;
        raf = new RandomAccessFile(f, "r");
        fc = raf.getChannel();
        header = new byte[8];
        raf.read(header);
        //raf.seek( 0x08 );
        size = readIntLE(raf);
        fixedResourceCount = readIntLE(raf);
        variableResourceOffset = readIntLE(raf);
    }

    public File getFile(){
        return file;
    }

    public static BifFile openBifFile(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        BifFile bif = null;
        try {
            byte[] header = new byte[8];
            in.read(header);
            in.close();
            if (Arrays.equals(header, header10)) {
                bif = new BifFile10(file);
            }
            else if (Arrays.equals(header, header11)) {
                bif = new BifFile11(file);
            }
            else
                System.err.println("fnord");
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return bif;
    }

    public abstract InputStream getEntry(int idx) throws IOException;

    public abstract int getEntrySize(int idx);

    public abstract void transferEntryToChannel(int entryIndex, WritableByteChannel c) throws IOException;

    public abstract MappedByteBuffer getEntryAsBuffer(int idx) throws IOException;

    public void transferEntryToFile(int entryIndex, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        FileChannel c = fos.getChannel();
        transferEntryToChannel(entryIndex, c);
        c.force(true);
        c.close();
        fos.close();
    }

    public static class BifFile10 extends BifFile {

        public static final int BIFINDEXENTRYSIZE = 16;

        public BifFile10(File file) throws IOException {
            super(file);
        }

        @Override
        public InputStream getEntry(int idx) throws IOException {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);

            return new RafInputStream(raf, offset, offset + length);

        }

        @Override
        public int getEntrySize(int idx) {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            try {
                raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE + 8);
                return readIntLE(raf);
            } catch (IOException ioex) {
                System.err.println(ioex);
                ioex.printStackTrace();
            }
            return 0;
        }

        @Override
        public void transferEntryToChannel(int entryIndex, WritableByteChannel c) throws IOException {
            if ((entryIndex < 0) | (entryIndex >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + entryIndex);
            }
            raf.seek(variableResourceOffset + entryIndex * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);
            fc.transferTo(offset, length, c);
        }

        @Override
        public MappedByteBuffer getEntryAsBuffer(int idx) throws IOException {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);
            return fc.map(FileChannel.MapMode.READ_ONLY, offset, length);
        }
    }

    public static class BifFile11 extends BifFile {

        public static final int BIFINDEXENTRYSIZE = 20;

        public BifFile11(File file) throws IOException {
            super(file);
        }

        @Override
        public InputStream getEntry(int idx) throws IOException {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int whatever = readIntLE(raf);
            if (whatever != 0) {
                System.err.println("unknown value in biffile " + whatever);
            }
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);
            return new RafInputStream(raf, offset, offset + length);
        }

        @Override
        public int getEntrySize(int idx) {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            try {
                raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE + 12);
                return readIntLE(raf);
            } catch (IOException ioex) {
                System.err.println(ioex);
                ioex.printStackTrace();
            }
            return 0;
        }

        @Override
        public void transferEntryToChannel(int entryIndex, WritableByteChannel c) throws IOException {
            if ((entryIndex < 0) | (entryIndex >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + entryIndex);
            }
            raf.seek(variableResourceOffset + entryIndex * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int whatever = readIntLE(raf);
            if (whatever != 0) {
                System.err.println("unknown value in biffile " + whatever);
            }
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);
            fc.transferTo(offset, length, c);
        }

        @Override
        public MappedByteBuffer getEntryAsBuffer(int idx) throws IOException {
            if ((idx < 0) | (idx >= size)) {
                throw new IndexOutOfBoundsException("Resource index out of bounds : " + idx);
            }
            raf.seek(variableResourceOffset + idx * BIFINDEXENTRYSIZE);
            int keyfileID = readIntLE(raf);
            int whatever = readIntLE(raf);
            if (whatever != 0) {
                System.err.println("unknown value in biffile " + whatever);
            }
            int offset = readIntLE(raf);
            int length = readIntLE(raf);
            int type = readIntLE(raf);
            return fc.map(FileChannel.MapMode.READ_ONLY, offset, length);
        }
    }

    protected static int readIntLE(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedByte() | (raf.readUnsignedByte() << 8) | (raf.readUnsignedByte() << 16) | (raf.readUnsignedByte() << 24);
    }

    public void close() throws IOException {
        fc.close();
        raf.close();
    }
}
