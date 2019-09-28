package org.jl.nwn.gff;

import java.util.Comparator;

import org.jl.nwn.NwnLanguage;

public class CExoLocSubString extends GffField<String> implements Cloneable {

    public static final Comparator<CExoLocSubString> COMPARATOR =
            (CExoLocSubString s1, CExoLocSubString s2) ->
                    (s1.language.getCode()*2+s1.gender)
                  - (s2.language.getCode()*2+s2.gender);

    public String string;
    public final NwnLanguage language;
    public final int gender;

    public CExoLocSubString(String s, NwnLanguage lang, int gender) {
        super(lang.getName() + (gender==0?"-M":"-F"), GffCExoLocString.SUBSTRINGTYPE);
        this.string = s;
        this.language = lang;
        this.gender = gender;
    }

    @Override
    public String getData() { return string; }

    @Override
    public void setData(String data) { string = data; }

    @Override
    public String getTypeName() { return "Substring"; }

    @Override
    public CExoLocSubString clone() {
        final GffField f = super.clone();
        f.parent = null;
        return (CExoLocSubString)f;
    }
}