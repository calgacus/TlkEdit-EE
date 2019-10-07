package org.jl.nwn.bif;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;

/**
 * Read only representation of a bif file.
 * <p>
 * A BIF contains mutliple resources (files). It does not contain information
 * about each resource's name, and therefore requires its {@link KetFile KEY file}.
 */
abstract class BifFile implements Closeable {

    protected RandomAccessFile raf;
    protected File file;
    protected FileChannel fc;
    protected byte[] header;
    /** Number of variable resources in this file. */
    protected int size;
    /** Number of fixed resources in this file. Not used by any known Bioware game. */
    protected int fixedResourceCount;
    /**
     * Byte position of the Variable Resource Table from the beginning of this
     * file. Currently, this value is 20.
     */
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

    public static BifFile open(File file) throws IOException {
        final byte[] header = new byte[8];
        try (final FileInputStream in = new FileInputStream(file)) {
            in.read(header);
        }
        if (Arrays.equals(BifFileV10.HEADER, header)) {
            return new BifFileV10(file);
        }
        if (Arrays.equals(BifFileV11.HEADER, header)) {
            return new BifFileV11(file);
        }
        throw new IllegalArgumentException("Unsupported BIFF header: " + new String(header, US_ASCII));
    }

    @Override
    public void close() throws IOException {
        fc.close();
        raf.close();
    }

    public abstract InputStream getEntry(int idx) throws IOException;

    public abstract int getEntrySize(int idx);

    public abstract void transferEntryToChannel(int entryIndex, WritableByteChannel c) throws IOException;

    public void transferEntryToFile(int entryIndex, File file) throws IOException {
        try (final FileOutputStream fos = new FileOutputStream(file);
             final FileChannel c = fos.getChannel()
        ) {
            transferEntryToChannel(entryIndex, c);
            c.force(true);
        }
    }

    protected void checkIndex(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Resource index out of bounds [0; " + size + ") : " + index);
        }
    }

    protected static int readIntLE(RandomAccessFile raf) throws IOException {
        return raf.readUnsignedByte()
            | (raf.readUnsignedByte() << 8)
            | (raf.readUnsignedByte() << 16)
            | (raf.readUnsignedByte() << 24);
    }
}
