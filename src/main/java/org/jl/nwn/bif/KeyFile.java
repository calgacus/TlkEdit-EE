package org.jl.nwn.bif;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
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

    public abstract Iterator<ResourceID> getResourceIDs();

    public abstract Set<ResourceID> getResourceIDSet();

    public abstract BifResourceLocation findResource(String resName, short resType);

    public static void main(String[] args) throws Exception {
        /*
        KeyFile k = new KeyFile(new File(args[0]));
        Iterator it = k.getResourceIDs();
        String white16 = "                ";
        String zero = "0x0000";
        String hex = "";
        while (it.hasNext()) {
        ResourceID id = (ResourceID) it.next();
        hex = Integer.toHexString(id.getType());
        hex = zero.substring(0, 6 - hex.length()) + hex;
        int bifID = k.lookup(id.getName(), id.getType());
        System.out.println(id.getName() + white16.substring(id.getName().length()) + " " + ResourceID.type2extensionMap.get(new Integer(id.getType())) + " (" + hex + ")  " + k.getBifName(bifID) + " [" + (bifID % (1 << 20)) + "]");
        }
         */
    }
}
