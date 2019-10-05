package org.jl.nwn.bif;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;

import org.jl.nwn.resource.ResourceID;

/**
 * Resource index file, used by "The Witcher".
 *
 * @author ich
 */
class KeyFileV11 extends KeyFile {
    /** String {@code "KEY V1.1"} -- magic and version of the file. */
    public static final byte[] HEADER = {'K', 'E', 'Y', ' ', 'V', '1', '.', '1'};

    public KeyFileV11(MappedByteBuffer mbb) throws IOException {
        final int bifEntries = mbb.getInt();
        final int resourceCount = mbb.getInt();
        mbb.getInt(); // skip 4 bytes
        final int bifOffset = mbb.getInt();
        final int resourceOffset = mbb.getInt();

        final String[] bifNames = readBifNames(mbb, bifEntries, bifOffset, 0);

        final byte[] buf = new byte[16];
        mbb.position(resourceOffset);
        for (int i = 0; i < resourceCount; i++) {
            mbb.get(buf, 0, 16);
            final String resName    = new String(buf, 0, 16, US_ASCII).trim();
            final ResourceID resRef = new ResourceID(resName, mbb.getShort());

            final int fileIndex = mbb.getInt();
            final String name   = bifNames[mbb.getInt() >> 20];
            entryMap.put(resRef, new BifResourceLocation(name, fileIndex));
        }
    }
}
