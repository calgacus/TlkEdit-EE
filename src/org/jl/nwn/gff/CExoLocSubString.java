/*
 * Created on 29.12.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.gff;

import java.util.Comparator;

import org.jl.nwn.NwnLanguage;


public class CExoLocSubString extends GffField<String> implements Cloneable{
    
    public static final Comparator comparator = new Comparator(){
        public int compare( Object o1, Object o2 ){
            CExoLocSubString s1 = (CExoLocSubString) o1;
            CExoLocSubString s2 = (CExoLocSubString) o2;
            return (s1.language.getCode()*2+s1.gender) - (s2.language.getCode()*2+s2.gender);
        }
    };
    
    public byte[] getRawData(){return null;}
    
    public String getData(){
        return string;
    }
    
    public void setData( String data ){
        string = data;
    }
    
    public String getTypeName(){
        return "Substring";
    }
    
    public Object clone(){
        GffField f = (GffField) super.clone();
        f.parent = null;
        return f;
    }
    
    @Override public String getLabel(){
        return language.getName() + (gender==0?"-M":"-F");
    }
    
    public String string = "";
    public NwnLanguage language = NwnLanguage.ENGLISH;
    public int gender = 0;
    public CExoLocSubString(String s, NwnLanguage lang, int gender) {
        super( "", GffCExoLocString.SUBSTRINGTYPE );
        this.string = s;
        this.language = lang;
        this.gender = gender;
    }
}