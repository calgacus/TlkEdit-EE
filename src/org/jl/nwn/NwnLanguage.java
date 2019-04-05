package org.jl.nwn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public enum NwnLanguage{    
    
    ENGLISH( "English", Version.NWN1, 0, Locale.ENGLISH ),
    FRENCH( "French", Version.NWN1, 1, Locale.FRENCH ),
    GERMAN( "German", Version.NWN1, 2, Locale.GERMAN ),
    ITALIAN( "Italian", Version.NWN1, 3, Locale.ITALIAN ),
    SPANISH( "Spanish", Version.NWN1, 4, new Locale("es") ),
    POLISH( "Polish", Version.NWN1, 5, new Locale("pl") ),
    
    KOREAN( "Korean", Version.NWN1, 128, Locale.KOREAN ),
    CHIN_TRAD( "Chinese, Traditional", Version.NWN1, 129, Locale.TRADITIONAL_CHINESE ),
    CHIN_SIMP( "Chinese, Simplified", Version.NWN1, 130, Locale.SIMPLIFIED_CHINESE ),
    JAPANESE( "Japanese", Version.NWN1, 131, Locale.JAPANESE ),
    
    NWN2_TOKENID( "GffToken", Version.NWN2, -1, Locale.ENGLISH ),    
    NWN2_ENGLISH( "English", Version.NWN2, 0, Locale.ENGLISH ),
    NWN2_FRENCH( "French", Version.NWN2, 1, Locale.FRENCH),
    NWN2_GERMAN( "German", Version.NWN2, 2, Locale.GERMAN ),
    NWN2_ITALIAN( "Italian", Version.NWN2, 3, Locale.ITALIAN ),
    NWN2_SPANISH( "Spanish", Version.NWN2, 4, new Locale("es") ),
    NWN2_POLISH( "Polish", Version.NWN2, 5, new Locale("pl") ),
    NWN2_6( "UnknownNwn2(RU?)", Version.NWN2, 6, new Locale("ru") ),    
    NWN2_KOREAN( "Korean", Version.NWN2, 128, Locale.KOREAN ),
    NWN2_CHIN_TRAD( "Chinese, Traditional", Version.NWN2, 129, Locale.TRADITIONAL_CHINESE ),
    NWN2_CHIN_SIMP( "Chinese, Simplified", Version.NWN2, 130, Locale.SIMPLIFIED_CHINESE ),
    NWN2_JAPANESE( "Japanese", Version.NWN2, 131, Locale.JAPANESE ),
    
    WI_DEBUG( "Debug", Version.WITCHER, 0, Locale.ENGLISH ),
    WI_ENGLISH( "English", Version.WITCHER, 1, Locale.ENGLISH),
    WI_FINALENGLISH("FinalEnglish", Version.WITCHER, 2, Locale.ENGLISH),
    WI_FINALENGLISH_SHORT("FinalEnglish_Short", Version.WITCHER, 3, Locale.ENGLISH),
    WI_POLISH("Polish", Version.WITCHER, 5, new Locale("pl")),
    WI_GERMAN("German", Version.WITCHER, 10, Locale.GERMAN),

    WI_FRENCH("French", Version.WITCHER, 11, Locale.FRENCH),
    WI_SPANISH("Spanish", Version.WITCHER, 12, new Locale("es")),
    WI_ITALIAN("Italian", Version.WITCHER, 13, Locale.ITALIAN),
    WI_RUSSIAN("Russian", Version.WITCHER, 14, new Locale("ru")),
    WI_CZECH("Czech", Version.WITCHER, 15, new Locale("cs")),
    WI_HUNGARIAN("Hungarian", Version.WITCHER, 16, new Locale("hu")),
    
    WI_KOREAN("Korean", Version.WITCHER, 20, Locale.KOREAN ),
    WI_CHIN_TRAD("Chinese, Traditional", Version.WITCHER, 21, Locale.TRADITIONAL_CHINESE),
    WI_CHIN_SIMP("Chinese, Simplified", Version.WITCHER, 22, Locale.SIMPLIFIED_CHINESE );

    private String name;
    private int code;
    private Version version;
    // language locale
    private Locale locale;
    
    private NwnLanguage( String name, Version v, int code, Locale locale ){
        this.name=name;
        this.code=code;
        this.version=v;
        this.locale=locale;
    }
    
    public String getName(){
        return name;
    }
    
    public int getCode(){
        return code;
    }
    
    public Version getVersion(){
        return version;
    }
    
    public Locale getLocale(){
        return locale;
    }
    
    public String getEncoding(){
        return encodings.get(this);
    }
    
    public String toString(){
        return name;
    }
    
    public static NwnLanguage find( Version v, int code ){
        Set<NwnLanguage> s = null;
        switch (v){
            case NWN1 : s=NWN1_LANGUAGES; break;
            case NWN2 : s=NWN2_LANGUAGES; break;
            case WITCHER : s=WITCHER_LANGUAGES; break;
        }
        for ( NwnLanguage l : s )
            if ( l.getCode() == code )
                return l;
        String errmsg = "Error : unsupported language, language code : "
                + code + ", Version : " + v;
        System.err.println(errmsg);
        throw new Error(
                errmsg);
    }
    
    public static List<NwnLanguage> findAll(Version v){
        List<NwnLanguage> l = new ArrayList<NwnLanguage>();
        for (NwnLanguage lang : EnumSet.allOf(NwnLanguage.class))
            if (lang.getVersion().equals(v))
                l.add(lang);
        return l;
    }
    
    public static void setEncoding(NwnLanguage l, String enc){
        encodings.put(l, enc);
    }
    
    private static EnumMap<NwnLanguage, String> encodings;
    
    public final static Set<NwnLanguage> NWN1_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(NwnLanguage.ENGLISH, NwnLanguage.JAPANESE));
    
    public final static Set<NwnLanguage> NWN2_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(NwnLanguage.NWN2_TOKENID, NwnLanguage.NWN2_JAPANESE));
    
    public final static Set<NwnLanguage> WITCHER_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(NwnLanguage.WI_DEBUG, NwnLanguage.WI_CHIN_SIMP));
    
    public final static List<NwnLanguage> LANGUAGES = Arrays.asList(NWN1_LANGUAGES.toArray(new NwnLanguage[0]));
    
    static{
        encodings = new EnumMap<NwnLanguage, String>(NwnLanguage.class);
        encodings.put( ENGLISH, "windows-1252" );
        encodings.put( FRENCH, "windows-1252" );
        encodings.put( GERMAN, "windows-1252" );
        encodings.put( ITALIAN, "windows-1252" );
        encodings.put( SPANISH, "windows-1252" );
        encodings.put( POLISH, "windows-1250" );
        encodings.put( KOREAN, "MS949" );
        encodings.put( CHIN_TRAD, "MS950" );
        encodings.put( CHIN_SIMP, "MS936" );
        encodings.put( JAPANESE, "MS932" );
        
        for (NwnLanguage l : NWN2_LANGUAGES )
            encodings.put( l, "UTF-8" );
        /*
        encodings.put( WI_DEBUG, "windows-1252" );
        encodings.put( WI_ENGLISH, "windows-1252" );
        encodings.put( WI_FINALENGLISH, "windows-1252" );
        encodings.put( WI_FINALENGLISH_SHORT, "windows-1252" );
        encodings.put( WI_POLISH, "windows-1250" );
        encodings.put( WI_FRENCH, "windows-1252" );
        encodings.put( WI_GERMAN, "windows-1252" );
        encodings.put( WI_ITALIAN, "windows-1252" );
        encodings.put( WI_SPANISH, "windows-1252" );
        //encodings.put( WI_RUSSIAN, "windows-1250" );
        encodings.put( WI_RUSSIAN, "UTF-8" );
        encodings.put( WI_CZECH, "windows-1251" );
        encodings.put( WI_HUNGARIAN, "windows-1251" );
        //encodings.put( WI_KOREAN, "MS949" );
        encodings.put( WI_KOREAN, "UTF-8" );
        encodings.put( WI_CHINESETRAD, "UTF-8" );
        //encodings.put( WI_CHINESETRAD, "MS950" );
        //encodings.put( WI_CHINESESIMP, "MS936" );
        encodings.put( WI_CHINESESIMP, "UTF-8" );
        */
        // languages.2da lists different codepages, however the tlk files in the
        // game seem to use UTF-8
        for (NwnLanguage l : WITCHER_LANGUAGES )
            encodings.put( l, "UTF-8" );
        
        //System.out.println(encodings);
        
        String enc = null;
        if ( ( enc = System.getProperty( "tlkedit.charsetOverride" )) != null ){
            String[] values = enc.split(";");
            try{
                for ( int p = 0; p < values.length; p++ ){
                    String[] triple = values[p].split(":");
                    Version v = Enum.valueOf(Version.class, triple[0]);
                    NwnLanguage l = find( v, Integer.parseInt(triple[1]) );
                    String encoding = triple[2];
                    encodings.put(l, encoding);
                    System.out.printf("encoding for %s (%s) set to %s\n",
                            l.getName(), v, encoding);
                }
            } catch ( Throwable t ){
                System.err.println(
                        "invalid format for tlkedit.encodingOverride : " +
                        enc);
                System.err.println(t);
            }
        }
        
        for (NwnLanguage l : values() ){
            System.out.printf("NwnLanguage %s : %s[%s], %s, %s:%s\n", l.name(), l.version.name(), l.getCode(), l.getName(), l.getLocale(), l.getLocale().getDisplayLanguage());
        }
        
    }
}
