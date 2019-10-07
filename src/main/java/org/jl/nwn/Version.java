package org.jl.nwn;

public enum Version {
    NWN1("NWN 1"), NWN2("NWN 2"), WITCHER("The Witcher");
    
    private final String displayName;
    
    private Version(String s){
        this.displayName = s;
    }
    
    private static Version defaultVersion = NWN1;
   
    static{
        String defName = System.getProperty("tlkedit.defaultNwnVersion", "NWN1");
        defName = defName.toUpperCase();
        try {
            defaultVersion = Enum.valueOf(Version.class, defName);
        } catch (IllegalArgumentException iae) {
            System.err.println("Invalid version : " + defName );
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
