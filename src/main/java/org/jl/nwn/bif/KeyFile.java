package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.util.Arrays;
import java.util.Set;

import org.jl.nwn.resource.ResourceID;

/** Read only representation of a key file. */
public abstract class KeyFile {

    protected File file;

    /**
     * BIF names in platform dependent representation (i.e. with apropriate file
     * name separator).
     */
    protected String[] bifs;

    protected static final byte[] HEADER_V10 = {75, 69, 89, 32, 86, 49, 32, 32};
    protected static final byte[] HEADER_V11 = {75, 69, 89, 32, 86, 49, 46, 49};

    /** Contains BIF archive name and index of file in it. */
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

    public static KeyFile open(File file) throws IOException {
        final byte[] header = new byte[8];
        try (final FileInputStream in = new FileInputStream(file)) {
            in.read(header);
        }
        if (Arrays.equals(HEADER_V10, header)) {
            return new KeyFileV10(file);
        }
        if (Arrays.equals(HEADER_V11, header)) {
            return new KeyFileV11(file);
        }
        throw new IllegalArgumentException("Unsupported KEY header: " + new String(header, US_ASCII));
    }

    /**
     * Get set of resource pointers that is known by this index file.
     *
     * @return Unmodifiable set of resources in this index
     */
    public abstract Set<ResourceID> getResources();

    public abstract BifResourceLocation findResource(ResourceID resRef);
}
