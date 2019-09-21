/*
 * KeyFileV10.java
 * 
 * Created on 05.12.2007, 19:16:24
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
public class KeyFileV10 extends KeyFile{

        protected TreeMap<ResourceID, Integer> entryMap = new TreeMap<ResourceID, Integer>();

        public KeyFileV10(File key) throws IOException {
            //bifs = new java.util.Vector();
            FileInputStream in = null;
            try {
                file = key;
                in = new FileInputStream(key);
                FileChannel fc = in.getChannel();
                MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                mbb.order(ByteOrder.LITTLE_ENDIAN);
                init1(mbb);
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }

        private void init1(MappedByteBuffer mbb) throws IOException {
            mbb.position(8);
            byte[] buf = new byte[100]; // should be large enough for a single bif file name
            int bifEntries = mbb.getInt();
            int resourceCount = mbb.getInt();
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
                // seems that bifNameLength includes the terminating \0
                bifs[i] = new String(buf, 0, bifNameLength - 1).replace('\\', File.separatorChar);
            }
            mbb.position(resourceOffset);
            for (int i = 0; i < resourceCount; i++) {
                mbb.get(buf, 0, 16);
                String resName = (new String(buf, 0, 16)).trim();
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

        public Iterator getResourceIDs() {
            return entryMap.keySet().iterator();
        }
        
        public Set<ResourceID> getResourceIDSet(){
            return Collections.unmodifiableSet(entryMap.keySet());
        }

        public BifResourceLocation findResource(String resName, short resType) {
            int bifId = lookup(resName, resType);
            return bifId == -1 ? null : new BifResourceLocation(getBifName(bifId), getBifIndex(bifId));
        }


}
