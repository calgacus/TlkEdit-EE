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
 * Resource index file, used by "The Witcher".
 *
 * @author ich
 */
public class KeyFileV11 extends KeyFile {

    protected final TreeMap<ResourceID, int[]> entryMap = new TreeMap<>();

    public KeyFileV11(File key) throws IOException {
        try (final FileInputStream in = new FileInputStream(key)) {
            file = key;
            FileChannel fc = in.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            init11(mbb);
        }
    }

    private void init11(MappedByteBuffer mbb) throws IOException {
        mbb.position(8);// Skip "KEY V1.1"
        final byte[] buf = new byte[100]; // should be large enough for a single bif file name
        final int bifEntries = mbb.getInt();
        final int resourceCount = mbb.getInt();
        mbb.getInt(); // skip 4 bytes
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
            bifs[i] = new String(buf, 0, bifNameLength).replace('\\', File.separatorChar);
        }
        mbb.position(resourceOffset);
        //int bit21 = 1 << 20;
        for (int i = 0; i < resourceCount; i++) {
            mbb.get(buf, 0, 16);
            final String resName = new String(buf, 0, 16).trim();
            //short type = mbb.getShort();
            //int bifID = mbb.getInt();
            entryMap.put(new ResourceID(resName, mbb.getShort()), new int[]{mbb.getInt(), mbb.getInt()});
        }
    }

    @Override
    public Set<ResourceID> getResources() {
        return Collections.unmodifiableSet(entryMap.keySet());
    }

    @Override
    public BifResourceLocation findResource(ResourceID resRef) {
        final int[] a = entryMap.get(resRef);
        if (a == null) {
            return null;
        }
        final int bifIndex = a[1] >> 20;
        return new BifResourceLocation(bifs[bifIndex], a[0]);
    }
}
