package org.jl.nwn.bif;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import static java.nio.charset.StandardCharsets.US_ASCII;

import org.jl.nwn.resource.ResourceID;

/**
 * Key file of version 1.0, used by "Neverwinter Nights".
 *
 * @see BifFileV10
 * @author ich
 */
class KeyFileV10 extends KeyFile {
    /** String {@code "KEY V1  "} -- magic and version of the file. */
    public static final byte[] HEADER = {'K', 'E', 'Y', ' ', 'V', '1', ' ', ' '};

    public KeyFileV10(MappedByteBuffer mbb) throws IOException {
        final int bifEntries = mbb.getInt();
        final int resourceCount = mbb.getInt();
        final int bifOffset = mbb.getInt();
        final int resourceOffset = mbb.getInt();

        // seems that BIF len includes the terminating \0, so corrent on -1
        final String[] bifNames = readBifNames(mbb, bifEntries, bifOffset, -1);

        final byte[] buf = new byte[16];
        mbb.position(resourceOffset);
        for (int i = 0; i < resourceCount; i++) {
            mbb.get(buf, 0, 16);
            final String resName    = new String(buf, 0, 16, US_ASCII).trim();
            final ResourceID resRef = new ResourceID(resName, mbb.getShort());

            final int resId   = mbb.getInt();
            final String name = bifNames[resId >> 20];
            final int index   = resId % (1 << 20);
            entryMap.put(resRef, new BifResourceLocation(name, index));
        }
    }
}
