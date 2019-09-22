/*
 */

package org.jl.nwn.spell;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipFile;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;
import org.dts.spell.dictionary.SpellDictionary;
import org.jl.nwn.NwnLanguage;

/**
 */
public class Dictionaries {

    static Map<NwnLanguage, SpellDictionary> dicts = new HashMap<NwnLanguage, SpellDictionary>();

    static Properties props = new Properties();

    static{
        try{
            InputStream is = Dictionaries.class.getClassLoader().getResourceAsStream(
                    "dict/dictionaries.properties");
            if ( is != null ) {
                props.load(is);
                System.out.println("Dictionaries.java   found props  " );
            }
        } catch (IOException ioex){
            ioex.printStackTrace();
        }
    }

    public static SpellDictionary forLanguage( NwnLanguage lang ){
        String dictName = (String) props.get(lang.getLocale().getLanguage());
        OpenOfficeSpellDictionary dict = null;
        if ( dictName != null ){
            if ( dicts.containsKey(lang) ) {
                return dicts.get(lang);
            }
            System.out.println("Dictionaries.java forLanguage loading dictionary for : " + lang);
            URL url = Dictionaries.class.getClassLoader().getResource(dictName);
            System.out.println("Dictionaries.java forLanguage url is "+url);
            try {
                System.out.println("Dictionaries.java forLanguage loading dictionary for dictname : " + dictName);
                System.out.println("Dictionaries.java forLanguage loading dictionary for url : " + url.toString());
                File zip = new File(url.toURI());
                dict = new OpenOfficeSpellDictionary( new ZipFile(zip) );
            } catch (Exception e){
                e.printStackTrace();
            }
        }else{
            System.out.println("Dictionaries.java forLanguage dictname is null " );
            System.out.println("Dictionaries.java forLanguage lang is  " + lang.getLocale().getLanguage());
            System.out.println("Dictionaries.java forLanguage props " + props.toString() );
        }
        dicts.put( lang, dict );
        return dict;
    }
}
