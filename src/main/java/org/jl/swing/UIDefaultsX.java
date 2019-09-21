package org.jl.swing;

import java.net.URL;
import java.util.Enumeration;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIDefaults;

/**
 * Extends UIDefaults to add icon loading through an url string specified in
 * a properties files, e.g. when the properties file contains the line
 * icon=/resources/foo.png, a call to getIcon("icon") will try to load an icon
 * from /resources/foo.png through the classloader's getResource() method.
 */
public class UIDefaultsX extends UIDefaults{
    /** Creates a new instance of UIDefaultsX */
    public UIDefaultsX() {
        super();
        for (Object e : keySet())
            System.out.println("UIDefaultsX "+e);
    }
    
    public Icon getIcon(Object key){
        Object o = get( key );
        if ( o == null )
            return null;
        if ( o instanceof Icon )
            return (Icon) o;
        else if ( o instanceof String ){
            Icon icon = loadIcon(o.toString());
            if ( icon != null )
                put( key, icon );
            return icon;
        }
        return null;
    }
    
    protected Icon loadIcon( String name ){
        URL url = getClass().getResource(name);
        Icon i = null;
        if ( url != null ){
            i = new ImageIcon(url);
        }
        return i;
    }
    
    @Override public void addResourceBundle(String bundle){
        super.addResourceBundle(bundle);
    }
    
    public void addResourceBundle(String bundle, Locale locale, ClassLoader loader){
        ResourceBundle rb = ResourceBundle.getBundle(bundle, locale, loader);
        Enumeration<String> e = rb.getKeys();
        while ( e.hasMoreElements() ){
            String key = e.nextElement();
            put(key, rb.getString(key));
        }
        //super.addResourceBundle(bundle);
    }
    
}
