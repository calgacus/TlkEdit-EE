package org.jl.nwn.bif;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.jl.nwn.resource.RafInputStream;

/**
 * Key file of version 1.0, used by "Neverwinter Nights".
 *
 * @see KeyFileV10
 * @author ich
 */
final class BifFileV10 extends BifFile {
    /** String {@code "BIFFV1  "} -- magic and version of the file. */
    public static final byte[] HEADER = {'B', 'I', 'F', 'F', 'V', '1', ' ', ' '};

    private static final int BIF_INDEX_ENTRY_SIZE = 16;

    public BifFileV10(File file) throws IOException {
        super(file);
    }

    @Override
    public InputStream getEntry(int idx) throws IOException {
        checkIndex(idx);
        raf.seek(variableResourceOffset + idx * BIF_INDEX_ENTRY_SIZE);
        int keyfileID = readIntLE(raf);
        int offset = readIntLE(raf);
        int length = readIntLE(raf);
        int type = readIntLE(raf);
        return new RafInputStream(raf, offset, offset + length);
    }

    @Override
    public int getEntrySize(int idx) {
        checkIndex(idx);
        try {
            raf.seek(variableResourceOffset + idx * BIF_INDEX_ENTRY_SIZE + 8);
            return readIntLE(raf);
        } catch (IOException ioex) {
            System.err.println(ioex);
            ioex.printStackTrace();
        }
        return 0;
    }

    @Override
    public void transferEntryToChannel(int entryIndex, WritableByteChannel c) throws IOException {
        checkIndex(entryIndex);
        raf.seek(variableResourceOffset + entryIndex * BIF_INDEX_ENTRY_SIZE);
        int keyfileID = readIntLE(raf);
        int offset = readIntLE(raf);
        int length = readIntLE(raf);
        int type = readIntLE(raf);
        fc.transferTo(offset, length, c);
    }

    @Override
    public MappedByteBuffer getEntryAsBuffer(int idx) throws IOException {
        checkIndex(idx);
        raf.seek(variableResourceOffset + idx * BIF_INDEX_ENTRY_SIZE);
        int keyfileID = readIntLE(raf);
        int offset = readIntLE(raf);
        int length = readIntLE(raf);
        int type = readIntLE(raf);
        return fc.map(FileChannel.MapMode.READ_ONLY, offset, length);
    }
}
