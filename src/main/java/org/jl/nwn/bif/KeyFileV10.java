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

    @Override
    public Set<ResourceID> getResources() {
        return Collections.unmodifiableSet(entryMap.keySet());
    }

    @Override
    public BifResourceLocation findResource(ResourceID resRef) {
        final Integer bifID = entryMap.get(resRef);
        if (bifID != null) {
            final int id = bifID.intValue();
            final String name = bifs[id >> 20];
            final int index = id % (1 << 20);
            return new BifResourceLocation(name, index);
        }
        return null;
    }
}
