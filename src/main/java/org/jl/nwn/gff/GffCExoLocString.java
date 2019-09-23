package org.jl.nwn.gff;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.jl.nwn.NwnLanguage;

public class GffCExoLocString extends GffField<Integer> implements Iterable<CExoLocSubString> {
    public static final byte SUBSTRINGTYPE = 47;

    private int strRef = -1;
    private final ArrayList<CExoLocSubString> substrings = new ArrayList<>();

    public GffCExoLocString(String label) {
        super(label, Gff.CEXOLOCSTRING);
    }

    @Override
    public Iterator<CExoLocSubString> iterator() { return substrings.iterator(); }

    public int getStrRef() { return strRef; }

    public void setStrRef(int i) { strRef = i; }

    public int getSubstringCount() { return substrings.size(); }

    public CExoLocSubString getSubstring(int pos) { return substrings.get(pos); }

    /**
     * @return the substring for the given language and gender or null if no such substring exists
     */
    public CExoLocSubString getSubstring( NwnLanguage lang, int gender ){
        for (final CExoLocSubString s : substrings) {
            if ( s.language.equals( lang ) && s.gender == gender ) {
                return s;
            }
        }
        return null;
    }

    /**
     * adds a substring if a substring for the same language
     * and gender doesn't exist, otherwise the existing substring is replaced
     **/
    public void addSubstring(CExoLocSubString s){
        int index = Collections.binarySearch(substrings, s, CExoLocSubString.COMPARATOR );
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
        final String s = label + " (" + getTypeName() + ") [StrRef " + getStrRef() + ']';
        if (substrings.isEmpty()) {
            return s;
        }
        return s + '\n' + substrings.get(0).string;
    }

    @Override
    public boolean allowsChildren() { return true; }

    @Override
    public int getChildCount() { return substrings.size(); }

    @Override
    public GffField getChild( int index ) { return substrings.get(index); }

    @Override
    public int getChildIndex( GffField f ) { return substrings.indexOf(f); }

    @Override
    public void addChild( int index, GffField f ){
        CExoLocSubString sub = (CExoLocSubString) f;
        if ( getSubstring(sub.language, sub.gender) != null )
            throw new IllegalArgumentException(
                    "cannot add substring : substring already exists" );
        addSubstring(sub);
    }

    @Override
    public void removeChild(GffField f) { substrings.remove(f); }

    @Override
    public Integer getData() { return getStrRef(); }

    @Override
    public void setData(Integer data) { setStrRef(data.intValue()); }
}
