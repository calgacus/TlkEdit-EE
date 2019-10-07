package org.jl.nwn.bif;

import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

import org.jl.nwn.resource.ResourceID;

/**
 * Read only representation of a key file.
 *
 * A Key file is an index of all the resources contained within a set of BIF files.
 * The key file contains information as to which BIFs it indexes for and what
 * resources are contained in those BIFs.
 *
 * @author ich
 */
public abstract class KeyFile {
    protected final HashMap<ResourceID, BifResourceLocation> entryMap = new HashMap<>();

    /** Contains BIF archive name and index of file in it. */
    public static final class BifResourceLocation {
        /**
         * BIF name in platform dependent representation (i.e. with appropriate
         * file name separator).
         */
        private final String bifName;
        private final int bifIndex;

        protected BifResourceLocation(String bifName, int bifIndex) {
            this.bifName = bifName;
            this.bifIndex = bifIndex;
        }

        public String getBifName() {
            return bifName;
        }

        public int getBifIndex() {
            return bifIndex;
        }
    }

    /**
     * Get set of resource pointers that is known by this index file.
     *
     * @return Unmodifiable set of resources in this index
     */
    public Set<ResourceID> getResources() {
        return Collections.unmodifiableSet(entryMap.keySet());
    }

    public BifResourceLocation findResource(ResourceID resRef) {
        return entryMap.get(resRef);
    }

    public static KeyFile open(File file) throws IOException {
        final byte[] header = new byte[8];
        try (final FileChannel fc = FileChannel.open(file.toPath(), StandardOpenOption.READ)) {
            final MappedByteBuffer mbb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            mbb.order(ByteOrder.LITTLE_ENDIAN);
            mbb.get(header);
            if (Arrays.equals(KeyFileV10.HEADER, header)) {
                return new KeyFileV10(mbb);
            }
            if (Arrays.equals(KeyFileV11.HEADER, header)) {
                return new KeyFileV11(mbb);
            }
        }
        throw new IllegalArgumentException("Unsupported KEY header: " + new String(header, US_ASCII));
    }

    /**
     * Reads table of BIF names, that associated with this KEY file.
     *
     * @param mbb ByteBuffer from which data will be readed
     * @param count Size of names table. Returned array will have this length
     * @param offset Byte offset from begin of {@code .key} file to area with
     *        FileEntry table
     * @param correctLen NWN1 (V1 {@code .key} files) contains bif names with
     *        terminating {@code \0} character, but "The Witcher" files are not.
     *        This integer will be added to readed length of file to get correct
     *        length of string
     *
     * @return Array, contained BIF names, associated with this key file, never
     *         {@code null}
     */
    protected static String[] readBifNames(MappedByteBuffer mbb, int count, int offset, int correctLen) {
        byte[] buf = new byte[100];
        final String[] names = new String[count];
        for (int i = 0; i < count; ++i) {
            mbb.position(offset + i * 12);// 4+4+2+2 - FileEntry size
            final int bifSize       = mbb.getInt();
            final int bifNameOffset = mbb.getInt();
            final int bifNameLength = mbb.getShort() + correctLen;

            if (bifNameLength > buf.length) {
                buf = new byte[bifNameLength];
            }
            mbb.position(bifNameOffset);
            mbb.get(buf, 0, bifNameLength);
            names[i] = new String(buf, 0, bifNameLength, US_ASCII).replace('\\', File.separatorChar);
        }
        return names;
    }
}
