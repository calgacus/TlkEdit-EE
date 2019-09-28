package org.jl.nwn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * typesafe enumeration for the different languages supported by Neverwinter Nights.
 * thile NwnLanguage objects have several attributes they are defined by the language code
 * ( i.e. 0 for English language )
 *
 * encoding seems to be "windows-1252" for anything except polish
 * ( see strref 51511 - it has a weird '...' character which is part of "windows-1250"
 * but not ISO-8859-1 )
 */
public class NwnLanguageOld{

    public static final NwnLanguageOld ENGLISH = new NwnLanguageOld( "English", 0, "windows-1252" );
    public static final NwnLanguageOld FRENCH = new NwnLanguageOld( "French", 1, "windows-1252" );
    public static final NwnLanguageOld GERMAN = new NwnLanguageOld( "German", 2, "windows-1252" );
    public static final NwnLanguageOld ITALIAN = new NwnLanguageOld( "Italian", 3, "windows-1252" );
    public static final NwnLanguageOld SPANISH = new NwnLanguageOld( "Spanish", 4, "windows-1252" );
    public static final NwnLanguageOld POLISH = new NwnLanguageOld( "Polish", 5, "windows-1250" );

    public static final NwnLanguageOld KOREAN = new NwnLanguageOld( "Korean", 128, "MS949" );
    public static final NwnLanguageOld CHIN_TRAD = new NwnLanguageOld( "Chinese, Traditional", 129, "MS950" );
    public static final NwnLanguageOld CHIN_SIMP = new NwnLanguageOld( "Chinese, Simplified", 130, "MS936" );
    public static final NwnLanguageOld JAPANESE = new NwnLanguageOld( "Japanese", 131, "MS932" );

    private String name;
    private int code;
    private String encoding;

    private static final NwnLanguageOld[] PRIVATE_VALUES = {
        ENGLISH, FRENCH, GERMAN, ITALIAN, SPANISH, POLISH,
        KOREAN, CHIN_TRAD, CHIN_SIMP, JAPANESE };

    static{
        String enc = null;
        if ( ( enc = System.getProperty( "tlkedit.encoding" )) != null ){
            System.out.println( "encoding override : " + enc );
            if ( enc.indexOf(':') != -1 ){
                final String[] pairs = enc.split(";");
                for (final String pair : pairs) {
                    final String[] tuple = pair.split(":");
                    final NwnLanguageOld l = forCode( Integer.parseInt(tuple[0]) );
                    l.encoding = tuple[1];
                    System.out.println("encoding for " + l + " set to " + l.encoding);
                }
            } else {
                for (final NwnLanguageOld lang : PRIVATE_VALUES) {
                    lang.encoding = enc;
                }
            }
        }
    }

    public static final List<NwnLanguageOld> LANGUAGES = Collections.unmodifiableList( Arrays.asList( PRIVATE_VALUES ) );

    private NwnLanguageOld( String n, int c, String e ){
        name = n;
        code = c;
        encoding = e;
    }

    public String toString(){
        return name;
    }

    public static NwnLanguageOld forCode( int code ){
        for (final NwnLanguageOld lang : PRIVATE_VALUES) {
            if (lang.getCode() == code) {
                return lang;
            }
        }
        throw new Error( "Error : unsupported language, language code : " + code );
    }

    public int getCode() {
        return code;
    }

    public String getEncoding() {
        return encoding;
    }

    public String getName() {
        return name;
    }

    /**
     * @return true if given NwnLanguage object has the same language code
     * */
    public boolean equals( Object o ){
        return ((NwnLanguageOld) o).code == this.code;
    }

    public int hashCode(){
        return code;
    }
}
