/*
 * KeyFileV11.java
 *
 * Created on 05.12.2007, 19:22:24
 *
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import org.jl.nwn.resource.ResourceID;

/**
 *
 * @author ich
 */
public class KeyFileV11 extends KeyFile {

    protected TreeMap<ResourceID, int[]> entryMap = new TreeMap<ResourceID, int[]>();

    public KeyFileV11(File key) throws IOException {
        //bifs = new java.util.Vector();
        FileInputStream in = null;
        try {
            file = key;
            in = new FileInputStream(key);
            FileChannel fc = in.getChannel();
            MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            init11(mbb);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    private void init11(MappedByteBuffer mbb) throws IOException {
        mbb.position(8);
        byte[] buf = new byte[100]; // should be large enough for a single bif file name
        int bifEntries = mbb.getInt();
        int resourceCount = mbb.getInt();
        mbb.getInt(); // skip 4 bytes
        int bifOffset = mbb.getInt();
        int resourceOffset = mbb.getInt();

        //mbb.position( bifOffset );
        bifs = new String[bifEntries];
        for (int i = 0; i < bifEntries; i++) {
            mbb.position(bifOffset + i * 12);
            int bifSize = mbb.getInt();
            int bifNameOffset = mbb.getInt();
            int bifNameLength = mbb.getShort();
            mbb.position(bifNameOffset);
            mbb.get(buf, 0, bifNameLength);
            bifs[i] = new String(buf, 0, bifNameLength).replace('\\', File.separatorChar);
            //System.out.println( bifs[i] );
        }
        mbb.position(resourceOffset);
        //int bit21 = 1 << 20;
        for (int i = 0; i < resourceCount; i++) {
            mbb.get(buf, 0, 16);
            String resName = (new String(buf, 0, 16)).trim();
            //short type = mbb.getShort();
            //int bifID = mbb.getInt();
            entryMap.put(new ResourceID(resName, mbb.getShort()), new int[]{mbb.getInt(), mbb.getInt()});
        }
    }

    public Iterator getResourceIDs() {
        return entryMap.keySet().iterator();
    }

    public Set<ResourceID> getResourceIDSet() {
        return Collections.unmodifiableSet(entryMap.keySet());
    }


    public BifResourceLocation findResource(String resName, short resType) {
        int[] a = entryMap.get(new ResourceID(resName, resType));
        if (a == null) {
            return null;
        }
        int bifIndex = (a[1] & -1048576) >> 20;
        return new BifResourceLocation(bifs[bifIndex], a[0]);
    }
}
