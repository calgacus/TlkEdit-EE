package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Set;
import java.util.TreeMap;

import org.jl.nwn.resource.ResourceID;

/**
 * Key file of version 1.0, used by "Neverwinter Nights".
 *
 * @author ich
 */
public class KeyFileV10 extends KeyFile {

    protected final TreeMap<ResourceID, Integer> entryMap = new TreeMap<>();

    public KeyFileV10(File key) throws IOException {
        try (final FileInputStream in = new FileInputStream(key)) {
            file = key;
            FileChannel fc = in.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            init1(mbb);
        }
    }

    private void init1(MappedByteBuffer mbb) throws IOException {
        mbb.position(8);// Skip "KEY V1.0"
        final byte[] buf = new byte[100]; // should be large enough for a single bif file name
        final int bifEntries = mbb.getInt();
        final int resourceCount = mbb.getInt();
        final int bifOffset = mbb.getInt();
        final int resourceOffset = mbb.getInt();

        bifs = new String[bifEntries];
        for (int i = 0; i < bifEntries; i++) {
            mbb.position(bifOffset + i * 12);
            final int bifSize = mbb.getInt();
            final int bifNameOffset = mbb.getInt();
            final int bifNameLength = mbb.getShort();
            mbb.position(bifNameOffset);
            mbb.get(buf, 0, bifNameLength);
            // seems that bifNameLength includes the terminating \0
            bifs[i] = new String(buf, 0, bifNameLength - 1).replace('\\', File.separatorChar);
        }
        mbb.position(resourceOffset);
        for (int i = 0; i < resourceCount; i++) {
            mbb.get(buf, 0, 16);
            final String resName = new String(buf, 0, 16).trim();
            entryMap.put(new ResourceID(resName, mbb.getShort()), mbb.getInt());
        }
    }

    /**
     * @param resName resource name
     * @param resType resource type
     * @return BifID of the resource or -1 if no such resource is found
     */
    private int lookup(String resName, short resType) {
        Integer bifID = entryMap.get(new ResourceID(resName, resType));
        return (bifID == null) ? -1 : bifID.intValue();
    }

    /**
     * @param bifID
     * @return bif name in platform dependent representation ( i.e.
     * with apropriate file name separator )
     */
    private String getBifName(int bifID) {
        return bifs[bifID >> 20];
    }

    private int getBifIndex(int bifID) {
        return bifID % (1 << 20);
    }

    @Override
    public Set<ResourceID> getResources() {
        return Collections.unmodifiableSet(entryMap.keySet());
    }

    @Override
    public BifResourceLocation findResource(String resName, short resType) {
        int bifId = lookup(resName, resType);
        return bifId == -1 ? null : new BifResourceLocation(getBifName(bifId), getBifIndex(bifId));
    }
}
