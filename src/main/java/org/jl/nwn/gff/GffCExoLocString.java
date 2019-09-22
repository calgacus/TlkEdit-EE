package org.jl.nwn.gff;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jl.nwn.NwnLanguage;

/**
 *
 * */
public class GffCExoLocString extends GffField implements Iterable<CExoLocSubString>{

    private int strRef = -1;
    private final List<CExoLocSubString> substrings = new ArrayList();
    public static final byte SUBSTRINGTYPE = 47;

    public GffCExoLocString(String label) {
        super(label, Gff.CEXOLOCSTRING);
    }

    public GffCExoLocString(String label, byte[] data) {
        this(label);
        setData(data);
    }

    @Override
    public Iterator<CExoLocSubString> iterator(){
        return substrings.iterator();
    }

    /**
     * @return
     */
    public int getStrRef() {
        return strRef;
    }

    /**
     * @param i
     */
    public void setStrRef(int i) {
        strRef = i;
    }

    public int getSubstringCount() {
        return substrings.size();
    }

    public CExoLocSubString getSubstring(int pos) {
        return (CExoLocSubString) substrings.get(pos);
    }

    /**
     * @return the substring for the given language and gender or null if no such substring exists
     * */
    public CExoLocSubString getSubstring( NwnLanguage lang, int gender ){
        for ( int i = 0; i < getSubstringCount(); i++ ){
            CExoLocSubString s = getSubstring( i );
            if ( s.language.equals( lang ) && s.gender == gender )
                return s;
        }
        return null;
    }

    /**
     * adds a substring if a substring for the same language
     * and gender doesn't exist, otherwise the existing substring is replaced
     **/
    public void addSubstring(CExoLocSubString s){
        int index = Collections.binarySearch(
                substrings, s, CExoLocSubString.comparator );
        if ( index > -1 )
            substrings.set( index, s );
        else
            substrings.add( -index - 1, s );
        s.parent = this;
    }

    public void removeSubstring(int pos) {
        substrings.remove(pos).parent = null;
    }

    @Override
    public String toString() {
        String s =
                label + " (" + getTypeName() + ") [StrRef " + getStrRef() + "] ";
        if (getSubstringCount() > 0)
            s += "\n" + getSubstring(0).string;
        return s;
    }

    @Override
    public boolean allowsChildren(){
        return true;
    }

    @Override
    public int getChildCount(){
        return getSubstringCount();
    }

    @Override
    public GffField getChild( int index ){
        return getSubstring(index);
    }

    @Override
    public int getChildIndex( GffField f ){
        return substrings.indexOf(f);
    }

    @Override
    public void addChild( int index, GffField f ){
        CExoLocSubString sub = (CExoLocSubString) f;
        if ( getSubstring(sub.language, sub.gender) != null )
            throw new IllegalArgumentException(
                    "cannot add substring : substring already exists" );
        addSubstring(sub);
    }

    @Override
    public void removeChild( GffField f ){
        substrings.remove(f);
    }

    @Override
    public Object getData(){
        return BigInteger.valueOf(getStrRef());
    }

    @Override
    public void setData( Object data ){
        setStrRef(((Number)data).intValue());
    }
}
