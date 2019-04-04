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
                    "/dict/dictionaries.properties");
            if ( is != null ) {
                props.load(is);
                System.out.println("found props 1 " );
            }else {
                InputStream is2 = Dictionaries.class.getClassLoader().getResourceAsStream(
                        "resource/dict/dictionaries.properties");
                if ( is2 != null ) {
                    props.load(is2);
                    System.out.println("found props 2 " );
                }

            }
        } catch (IOException ioex){
            ioex.printStackTrace();
        }
    }
    
    public static SpellDictionary forLanguage( NwnLanguage lang ){
        String dictName = (String) props.get(lang.getLocale().getLanguage());
        if ( dictName != null ){
            if ( dicts.containsKey(lang) )
                return dicts.get(lang);
            System.out.println("loading dictionary for : " + lang);
            URL url = Dictionaries.class.getClassLoader().getResource(dictName);
            System.out.println(url);
            try {
                long time = System.currentTimeMillis();
                System.out.println("loading dictionary for dictname : " + dictName);
                System.out.println("loading dictionary for url : " + url.toString());
                File zip = new File(url.toURI());
                //File userDict = new File(zip.getParentFile(), lang.getName()+".userDict");
                //userDict.createNewFile();
                OpenOfficeSpellDictionary dict = new OpenOfficeSpellDictionary(
                        new ZipFile(zip)//, false
                        //url.openStream(),
                        //userDict
                        );
                dicts.put( lang, dict );
                System.out.println(System.currentTimeMillis()-time);
                return dict;
            } catch (Exception e){
                e.printStackTrace();
                dicts.put( lang, null );
            }
        }else{
            System.out.println("dictname is null " );
            System.out.println("lang is  " + lang.getLocale().getLanguage());
            System.out.println("props " + props.toString() );

        }
        return null;
    }
    
}
