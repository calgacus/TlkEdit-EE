package org.jl.nwn.resource;

import java.text.ParseException;
import java.util.EnumMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;

import org.jl.nwn.Version;

/**
 * This is a utility class which cannot be instantiated, it contains
 * methods for checking ResRefs.
 * A ResRef has a max length of 16 and may contain only alphanumeric
 * characters or underscores ('_'), i.e. it must match the regular expression
 * [\w]{0,16}
 */
public final class ResRefUtil{
    public final Pattern RESREFPATTERN;

    private static final EnumMap<Version, ResRefUtil> map = new EnumMap<>(Version.class);

    static{
        map.put( Version.NWN1, new ResRefUtil(Pattern.compile("[\\w]{0,16}")) );
        map.put( Version.NWN2, new ResRefUtil(Pattern.compile("[\\w]{0,32}")) );
    }

    public static ResRefUtil instance(Version v){
        return map.get(v);
    }

    public static int resRefSize(Version v){
        switch (v){
            case NWN1 : return 16;
            case NWN2 : return 32;
            default : return 32;
        }
    }

    private ResRefUtil(Pattern p){
        RESREFPATTERN = p;
    }

    /**
     * tests whether the given string is a valid ResRef
     * @return true if the argument is a valid ResRef
     */
    public boolean testString(String s){
        Matcher m = RESREFPATTERN.matcher(s);
        return m.matches();
    }

    /**
     * unless the argument is a valid ResRef, this method will raise an exception
     * @return the argument
     */
    public String parseString( String s ) throws ParseException, IllegalArgumentException{
        Matcher matcher = RESREFPATTERN.matcher(s);
        if ( !matcher.matches() )
            throw new ParseException( "invalid resref", 0 );
        return s;
    }

    /**
     * formatter for strings which are valid ResRefs
     * @param accept2DA_Null if true the formatter will accept **** as valid input
     * @returns AbstractFormatter for ResRefs
     */
    public JFormattedTextField.AbstractFormatter getStringFormatter(final boolean accept2DA_Null){
        return new JFormattedTextField.AbstractFormatter(){
            @Override
            public Object stringToValue(String text) throws ParseException{
                if (accept2DA_Null && "****".equals(text))
                    return text;
                return parseString(text);
            }
            @Override
            public String valueToString(Object value) throws ParseException{
                return value!=null?value.toString():"";
            }
        };
    }
}
