package org.jl.nwn;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.tlk.TlkContent;

/**
 * Enumeration of known language codes, used by NWN 1, NWN 2 and The Witcher,
 * and some metainformation about it.
 *
 * @author Mingun
 */
public enum NwnLanguage {
    // Neverwinter Nights languages
    ENGLISH  (Version.NWN1,   0, "English", Locale.ENGLISH  ),
    FRENCH   (Version.NWN1,   1, "French" , Locale.FRENCH   ),
    GERMAN   (Version.NWN1,   2, "German" , Locale.GERMAN   ),
    ITALIAN  (Version.NWN1,   3, "Italian", Locale.ITALIAN  ),
    SPANISH  (Version.NWN1,   4, "Spanish", new Locale("es")),
    POLISH   (Version.NWN1,   5, "Polish" , new Locale("pl")),
    KOREAN   (Version.NWN1, 128, "Korean" , Locale.KOREAN   ),
    CHIN_TRAD(Version.NWN1, 129, "Chinese, Traditional", Locale.TRADITIONAL_CHINESE ),
    CHIN_SIMP(Version.NWN1, 130, "Chinese, Simplified", Locale.SIMPLIFIED_CHINESE ),
    JAPANESE (Version.NWN1, 131, "Japanese", Locale.JAPANESE),

    // Neverwinter Nights 2 languages
    NWN2_TOKENID    (Version.NWN2,  -1, "GffToken", Locale.ENGLISH ),
    NWN2_ENGLISH    (Version.NWN2,   0, "English" , Locale.ENGLISH ),
    NWN2_FRENCH     (Version.NWN2,   1, "French"  , Locale.FRENCH),
    NWN2_GERMAN     (Version.NWN2,   2, "German"  , Locale.GERMAN ),
    NWN2_ITALIAN    (Version.NWN2,   3, "Italian" , Locale.ITALIAN ),
    NWN2_SPANISH    (Version.NWN2,   4, "Spanish" , new Locale("es") ),
    NWN2_POLISH     (Version.NWN2,   5, "Polish"  , new Locale("pl") ),
    NWN2_6          (Version.NWN2,   6, "UnknownNwn2(RU?)", new Locale("ru") ),
    NWN2_KOREAN     (Version.NWN2, 128, "Korean"  , Locale.KOREAN ),
    NWN2_CHIN_TRAD  (Version.NWN2, 129, "Chinese, Traditional", Locale.TRADITIONAL_CHINESE ),
    NWN2_CHIN_SIMP  (Version.NWN2, 130, "Chinese, Simplified", Locale.SIMPLIFIED_CHINESE ),
    NWN2_JAPANESE   (Version.NWN2, 131, "Japanese", Locale.JAPANESE ),

    // The Witcher languages
    WI_DEBUG             (Version.WITCHER,  0, "Debug"    , Locale.ENGLISH ),
    WI_ENGLISH           (Version.WITCHER,  1, "English"  , Locale.ENGLISH),
    WI_FINALENGLISH      (Version.WITCHER,  2, "FinalEnglish", Locale.ENGLISH),
    WI_FINALENGLISH_SHORT(Version.WITCHER,  3, "FinalEnglish_Short", Locale.ENGLISH),
    WI_POLISH            (Version.WITCHER,  5, "Polish"   , new Locale("pl")),
    WI_GERMAN            (Version.WITCHER, 10, "German"   , Locale.GERMAN),
    WI_FRENCH            (Version.WITCHER, 11, "French"   , Locale.FRENCH),
    WI_SPANISH           (Version.WITCHER, 12, "Spanish"  , new Locale("es")),
    WI_ITALIAN           (Version.WITCHER, 13, "Italian"  , Locale.ITALIAN),
    WI_RUSSIAN           (Version.WITCHER, 14, "Russian"  , new Locale("ru")),
    WI_CZECH             (Version.WITCHER, 15, "Czech"    , new Locale("cs")),
    WI_HUNGARIAN         (Version.WITCHER, 16, "Hungarian", new Locale("hu")),
    WI_KOREAN            (Version.WITCHER, 20, "Korean"   , Locale.KOREAN ),
    WI_CHIN_TRAD         (Version.WITCHER, 21, "Chinese, Traditional", Locale.TRADITIONAL_CHINESE),
    WI_CHIN_SIMP         (Version.WITCHER, 22, "Chinese, Simplified", Locale.SIMPLIFIED_CHINESE );

    /** Version of game engine, which uses that language. */
    private final Version version;
    /** Language code, that used by the game engine. */
    private final int code;
    /** Name for language, used in the GUI. */
    private final String name;
    /** Locale, used for spell checking for this language. */
    private final Locale locale;

    private NwnLanguage(Version v, int code, String name, Locale locale) {
        this.name=name;
        this.code=code;
        this.version=v;
        this.locale=locale;
    }

    /** @return Name for language, used in the GUI. */
    public String getName(){
        return name;
    }

    /** @return Language code, that used by the game engine. */
    public int getCode(){
        return code;
    }

    /**
     * @deprecated Some localizations (for example, Russian NWN1) use language
     * code 0 (English) and spell checking for such TLKs must be performed with
     * russian dictionaries. So, dictionary must not depend on the default locale
     * for language.
     *
     * @return Locale, used for spell checking for this language.
     */
    @Deprecated
    public Locale getLocale(){
        return locale;
    }

    /**
     * Encoding, that used for reading and writing GFF {@link GffCExoLocString}
     * fields and {@link TlkContent TLK files}. By default this encoding determined
     * by the engine language, but it can be overrided via JVM command-line argument
     * {@code -Dtlkedit.charsetOverride=<charset>}.
     *
     * @return String with name of encoding, that used for encoding/decoding
     *         strings of that language.
     */
    public String getEncoding(){
        return ENCODINGS.get(this);
    }

    @Override
    public String toString(){
        return name;
    }

    /**
     * Resolves specified language code againist specified game version.
     *
     * @param version Game version. Different games uses different codes to encode
     *        languages
     * @param code Language code, used by the game engine
     *
     * @return Object that represents language and can be used for retrivieng
     *         encoding and locale information
     *
     * @throws IllegalArgumentException If specified code in not known for the engine version
     */
    public static NwnLanguage find(Version version, int code) {
        for (final NwnLanguage lang : findAll(version)) {
            if (lang.code == code) { return lang; }
        }
        throw new IllegalArgumentException(version + ": unsupported language code " + code);
    }

    /**
     * Returns set of languages, that is known for the specified game engine version.
     *
     * @param version Game version for which need to return languages
     *
     * @return Unmodifiable set of all languages for the specified game version
     */
    public static Set<NwnLanguage> findAll(Version version) {
        switch (version) {
            case NWN1   : return NWN1_LANGUAGES;
            case NWN2   : return NWN2_LANGUAGES;
            case WITCHER: return WITCHER_LANGUAGES;
        }
        throw new IllegalArgumentException("Unknown game version " + version);
    }

    /**
     * Customizable mapping from engine language to Charset, used for
     * encoding/decoding strings in GFF {@link GffCExoLocString} fields
     * and TLK files.
     */
    private final static EnumMap<NwnLanguage, String> ENCODINGS;

    /** Unmodifiable collection of all languages for version = NWN1. */
    private final static Set<NwnLanguage> NWN1_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(ENGLISH, JAPANESE));

    /** Unmodifiable collection of all languages for version = NWN2. */
    private final static Set<NwnLanguage> NWN2_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(NWN2_TOKENID, NWN2_JAPANESE));

    /** Unmodifiable collection of all languages for version = WITCHER. */
    private final static Set<NwnLanguage> WITCHER_LANGUAGES = Collections.unmodifiableSet(
            EnumSet.range(WI_DEBUG, WI_CHIN_SIMP));

    public final static NwnLanguage[] LANGUAGES = NWN1_LANGUAGES.toArray(new NwnLanguage[NWN1_LANGUAGES.size()]);

    static{
        ENCODINGS = new EnumMap<>(NwnLanguage.class);
        ENCODINGS.put( ENGLISH, "windows-1252" );
        ENCODINGS.put( FRENCH, "windows-1252" );
        ENCODINGS.put( GERMAN, "windows-1252" );
        ENCODINGS.put( ITALIAN, "windows-1252" );
        ENCODINGS.put( SPANISH, "windows-1252" );
        ENCODINGS.put( POLISH, "windows-1250" );
        ENCODINGS.put( KOREAN, "MS949" );
        ENCODINGS.put( CHIN_TRAD, "MS950" );
        ENCODINGS.put( CHIN_SIMP, "MS936" );
        ENCODINGS.put( JAPANESE, "MS932" );

        for (NwnLanguage l : NWN2_LANGUAGES )
            ENCODINGS.put( l, "UTF-8" );
        // languages.2da lists different codepages, however the tlk files in the
        // game seem to use UTF-8
        for (NwnLanguage l : WITCHER_LANGUAGES )
            ENCODINGS.put( l, "UTF-8" );

        final String enc = System.getProperty("tlkedit.charsetOverride");
        if (enc != null) {
            String[] values = enc.split(";");
            try{
                for (final String value : values) {
                    final String[] triple = value.split(":");
                    final Version v = Version.valueOf(triple[0]);
                    final NwnLanguage l = find( v, Integer.parseInt(triple[1]) );
                    final String encoding = triple[2];
                    ENCODINGS.put(l, encoding);
                    System.out.printf("NwnLanguage encoding for %s (%s) set to %s\n",
                            l.name, v, encoding);
                }
            } catch ( Throwable t ){
                System.err.println(
                        "invalid format for tlkedit.encodingOverride : " +
                        enc);
                System.err.println(t);
            }
        }

        for (final NwnLanguage l : values()) {
            System.out.printf("NwnLanguage %s : %s[%s], %s, %s:%s, %s\n",
                l.name(), l.version.name(), l.code, l.name,
                l.locale, l.locale.getDisplayLanguage(),
                l.getEncoding()
            );
        }
    }
}
