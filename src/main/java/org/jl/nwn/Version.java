package org.jl.nwn;

import java.util.Arrays;

public enum Version {
    NWN1("NWN 1"),
    NWN2("NWN 2"),
    WITCHER("The Witcher");

    private final String displayName;

    private Version(String s){
        this.displayName = s;
    }

    private static Version defaultVersion = NWN1;

    static{
        final String defName = System.getProperty("tlkedit.defaultNwnVersion", "NWN1");
        try {
            defaultVersion = Version.valueOf(defName.toUpperCase());
        } catch (IllegalArgumentException iae) {
            System.err.println("Invalid version in `tlkedit.defaultNwnVersion`: " + defName
                             + ", expected one of: " + Arrays.toString(Version.values())
            );
        }
    }

    @Override
    public String toString(){
        return getDisplayName();
    }

    public String getDisplayName(){
        return displayName;
    }


    public static Version getDefaultVersion() {
        return defaultVersion;
    }

    public static void setDefaultVersion(Version aDefaultVersion) {
        defaultVersion = aDefaultVersion;
    }
}
