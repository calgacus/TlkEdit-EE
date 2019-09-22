/*
 * Created on 06.08.2004
 */
package org.jl.swing;

import java.awt.event.KeyEvent;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.KeyStroke;

/**
 */
public class I18nUtil {
    
    
    public static String removeMnemonicInfo( String s ){
        return s == null ? null : s.replaceAll( "&(.)", "$1" );
    }
    
    private static char getMnemonicChar( String s ){
        int i = 0;
        while ( -1 != (i = s.indexOf('&', i)) && s.charAt(i+1) == '&' ){
            i+=2;
        }
        return  ( i != -1 ) ? s.charAt( i+1 ) : 0;
    }
    
    public static int getMnemonic( String s ){
        if ( s == null ) return KeyEvent.VK_UNDEFINED;
        KeyStroke ks = KeyStroke.getKeyStroke("pressed " +
                Character.toString(Character.toUpperCase(getMnemonicChar(s))));
        return (ks != null) ? ks.getKeyCode() : KeyEvent.VK_UNDEFINED;
    }
    
    public static int getMnemonicIndex( String s ){
        if ( s == null ) return -1;
        int i = 0, c = 0;
        while ( (i = s.indexOf('&', i)) != -1 && s.charAt(i+1) == '&' ){
            i+=2;
            c++;
        }
        return ( i != -1 ) ?  i-c: -1;
    }
    
    
    public static void setText( AbstractButton b, String s ){
        if ( s == null ) return;
        b.setText( removeMnemonicInfo(s) );
        char c = getMnemonicChar( s );
        if ( c != 0 ){
            //System.out.println( s +", "+ c  );
            b.setMnemonic( c );
            b.setDisplayedMnemonicIndex( getMnemonicIndex(s) );
        }
    }
    
    public static void setText( JLabel b, String s ){
        if ( s == null ) return;
        b.setText( removeMnemonicInfo(s) );
        char c = getMnemonicChar( s );
        if ( c != 0 ){
            b.setDisplayedMnemonic( c );
            b.setDisplayedMnemonicIndex( getMnemonicIndex(s) );
        }
    }
    
    public final static String[] ActionProperties = new String[]{
        Action.NAME,
                Action.LONG_DESCRIPTION,
                Action.SMALL_ICON,
                Action.ACTION_COMMAND_KEY,
                Action.SHORT_DESCRIPTION,
                Action.ACCELERATOR_KEY,
                Action.MNEMONIC_KEY
    };

    public static String makeKeyStrokeTooltip( String tt, KeyStroke ks ){
        if ( ks == null ) return tt;
        return "<html>" + tt + "  <sub><font color=#444444>"
                + KeyEvent.getKeyModifiersText( ks.getModifiers() ) + "-" + KeyEvent.getKeyText( ks.getKeyCode() )+"</font></sub></html>";
    }
    
}
