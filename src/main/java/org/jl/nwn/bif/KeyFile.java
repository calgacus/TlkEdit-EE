package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import org.jl.nwn.resource.ResourceID;

/** Read only representation of a key file. */
public abstract class KeyFile {

    protected File file;

    protected String[] bifs;

    protected static final byte[] HEADERV10 = {75, 69, 89, 32, 86, 49, 32, 32};
    protected static final byte[] HEADERV11 = {75, 69, 89, 32, 86, 49, 46, 49};

//protected Map<ResourceID, Integer> entryMap = new TreeMap<ResourceID, Integer>();
    public static final class BifResourceLocation {

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

    public String getFileName() {
        return file.getName();
    }

    public File getFile() {
        return file;
    }

    public static KeyFile open(File file) throws IOException {
        final byte[] header = new byte[8];
        try (final FileInputStream in = new FileInputStream(file)) {
            in.read(header);
        }
        if (Arrays.equals(HEADERV11, header)) {
            return new KeyFileV11(file);
        } else {
            return new KeyFileV10(file);
        }
    }

    /**
     * Get set of resource pointers that is known by this index file.
     *
     * @return Unmodifiable set of resources in this index
     */
    public abstract Set<ResourceID> getResources();

    public abstract BifResourceLocation findResource(String resName, short resType);
}
