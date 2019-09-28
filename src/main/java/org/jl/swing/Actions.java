package org.jl.swing;

import java.awt.Event;
import java.awt.Toolkit;
import java.net.URL;

import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.util.OS;

public class Actions {

    public final static String EMPTYICONKEY = "empty_icon";

    public final static String[] ActionProperties = new String[]{
        Action.NAME,
                Action.LONG_DESCRIPTION,
                Action.SMALL_ICON,
                Action.ACTION_COMMAND_KEY,
                Action.SHORT_DESCRIPTION,
                Action.ACCELERATOR_KEY,
                Action.MNEMONIC_KEY
    };
    public static final String MNEMONIC_INDEX = "MnemonicIndex";

    private static Icon emptyIcon = new EmptyIcon(22,22);

    static{
        Icon i = null;
        if (emptyIcon == (i=getIcon(UIManager.getDefaults(), EMPTYICONKEY)))
            UIManager.getDefaults().put(EMPTYICONKEY, emptyIcon);
        else
            emptyIcon = i;
    }

    protected Actions() {
    }

    protected static Icon getIcon(UIDefaults uid, Object key){
        Object o = uid.get( key );
        Icon icon = null;
        if ( o == null ){
            return emptyIcon;
        }
        if ( o instanceof Icon )
            return (Icon) o;
        else if ( o instanceof String ){
            icon = loadIcon(uid, o.toString());
            if ( icon != null )
                uid.put(key, icon);
            return icon;
        }
        return null;
    }

    protected static Icon loadIcon(final UIDefaults uid, final String path) {
        if ( path == null )
            return null;
        URL url = Actions.class.getResource(path);
        Icon icon = null;
        if ( url != null ){
            icon = new ImageIcon(url);
        }
        return icon;
    }

    static final int SHORTCUTMASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    static final boolean IS_OSX = OS.isMacOSX();

    public static void configureActionUI( Action a, UIDefaults uid, String name ){
        for (final String key : ActionProperties) {
            String defKey = name + "." + key;
            if (key == Action.ACCELERATOR_KEY){
                KeyStroke ks = KeyStroke.getKeyStroke(uid.getString(defKey));
                a.putValue(key, ks);
                if ( IS_OSX && ks != null && (ks.getModifiers() & Event.CTRL_MASK) == Event.CTRL_MASK ){
                    KeyStroke ks2 = KeyStroke.getKeyStroke( ks.getKeyCode(),
                            ks.getModifiers() ^ Event.CTRL_MASK | SHORTCUTMASK );
                    a.putValue(key, ks2);
                }
                if ( a.getValue(Action.SHORT_DESCRIPTION) != null && a.getValue(Action.ACCELERATOR_KEY) != null )
                    a.putValue(Action.SHORT_DESCRIPTION, I18nUtil.makeKeyStrokeTooltip((String)a.getValue(Action.SHORT_DESCRIPTION), KeyStroke.getKeyStroke(uid.getString(defKey))));
            } else if (key == Action.SMALL_ICON){
                a.putValue(key, getIcon(uid, defKey) );
            } else if (key == Action.NAME) {
                String s = uid.getString(defKey);
                if ( s == null ) break;
                a.putValue(Action.NAME, I18nUtil.removeMnemonicInfo(s));
                int idx = I18nUtil.getMnemonicIndex(s);
                a.putValue(MNEMONIC_INDEX, idx);
                a.putValue(Action.MNEMONIC_KEY, I18nUtil.getMnemonic(s));
            } else {
                Object o = uid.get(defKey);
                if (o!=null)
                    a.putValue(key, o);
            }
        }
        if ( a.getValue(Action.ACTION_COMMAND_KEY)==null && a.getValue(Action.ACCELERATOR_KEY)!=null){
            System.out.println( "setting " + Action.ACTION_COMMAND_KEY + " to " + Action.NAME );
            a.putValue( Action.ACTION_COMMAND_KEY, a.getValue(Action.NAME) );
        }
    }

    public static void setEmptyIcon( int width, int height ){
        emptyIcon = new EmptyIcon(width, height);
    }

    public static void setEmptyIcon( Icon icon ){
        emptyIcon = icon;
    }

    public static Icon getEmptyIcon(){
        return emptyIcon;
    }

    public static void registerActions( InputMap im, ActionMap am, Action ... actions ){
        for ( Action a : actions ){
            KeyStroke ks = (KeyStroke)a.getValue( a.ACCELERATOR_KEY );
            if ( ks != null )
                im.put((KeyStroke)a.getValue( a.ACCELERATOR_KEY ), a.getValue( a.ACTION_COMMAND_KEY ));
            am.put(a.getValue( a.ACTION_COMMAND_KEY ), a );
        }
    }
}
