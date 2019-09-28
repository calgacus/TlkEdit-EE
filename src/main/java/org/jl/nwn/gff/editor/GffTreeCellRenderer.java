package org.jl.nwn.gff.editor;

import java.awt.Component;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.UIDefaults;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.jdesktop.swingx.treetable.TreeTableModel;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.gff.GffField;

public class GffTreeCellRenderer extends DefaultTreeCellRenderer{

    protected JTree tree;

    protected TreeTableModel ttm;
    protected static final UIDefaults uid = new UIDefaults();

    protected String male, female;

    protected Map<Byte, Icon> typeIconMap;
    protected Map<NwnLanguage, Icon> languageIconMap;

    static {
        uid.addResourceBundle("org.jl.nwn.gff.editor.uidefaults");
    }

    public GffTreeCellRenderer( TreeTableModel ttm ){
        super();
        this.ttm = ttm;
        typeIconMap = new HashMap<>();
        typeIconMap.put(Gff.STRUCT, loadIcon(uid.getString("gffstruct.icon")));
        typeIconMap.put(Gff.LIST, loadIcon(uid.getString("gfflist.icon")));
        Icon voidIcon = loadIcon(uid.getString("gffvoid.icon"));
        typeIconMap.put(Gff.VOID,voidIcon);
        Icon numberIcon = loadIcon(uid.getString("gffnumber.icon"));
        typeIconMap.put(Gff.BYTE,numberIcon);
        typeIconMap.put(Gff.CHAR,numberIcon);
        typeIconMap.put(Gff.DOUBLE,numberIcon);
        typeIconMap.put(Gff.DWORD,numberIcon);
        typeIconMap.put(Gff.DWORD64,numberIcon);
        typeIconMap.put(Gff.FLOAT,numberIcon);
        typeIconMap.put(Gff.INT,numberIcon);
        typeIconMap.put(Gff.INT64,numberIcon);
        typeIconMap.put(Gff.SHORT,numberIcon);
        typeIconMap.put(Gff.VECTOR,numberIcon);
        typeIconMap.put(Gff.WORD,numberIcon);
        Icon stringIcon = loadIcon(uid.getString("gffstring.icon"));
        typeIconMap.put(Gff.CEXOLOCSTRING,stringIcon);
        typeIconMap.put(Gff.CEXOSTRING,stringIcon);
        typeIconMap.put(Gff.RESREF,stringIcon);

        languageIconMap = new HashMap<>();
        languageIconMap.put(NwnLanguage.ENGLISH, loadIcon(uid.getString("flag.canada")));
        languageIconMap.put(NwnLanguage.GERMAN, loadIcon(uid.getString("flag.germany")));
        languageIconMap.put(NwnLanguage.FRENCH, loadIcon(uid.getString("flag.france")));
        languageIconMap.put(NwnLanguage.ITALIAN, loadIcon(uid.getString("flag.italy")));
        languageIconMap.put(NwnLanguage.SPANISH, loadIcon(uid.getString("flag.spain")));
        languageIconMap.put(NwnLanguage.JAPANESE, loadIcon(uid.getString("flag.japan")));
        languageIconMap.put(NwnLanguage.POLISH, loadIcon(uid.getString("flag.poland")));
        languageIconMap.put(NwnLanguage.CHIN_SIMP, loadIcon(uid.getString("flag.china")));
        languageIconMap.put(NwnLanguage.CHIN_TRAD, loadIcon(uid.getString("flag.china")));
        languageIconMap.put(NwnLanguage.KOREAN, loadIcon(uid.getString("flag.korea")));
        languageIconMap.put(NwnLanguage.NWN2_TOKENID, voidIcon);
        languageIconMap.put(NwnLanguage.NWN2_6, voidIcon);
    }

    @Override public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean selected,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {
        //sb.delete(0, sb.length());
        String text = ttm.getValueAt(value, 0).toString();
        super.getTreeCellRendererComponent(
                tree,
                text,
                selected,
                expanded,
                leaf,
                row,
                hasFocus);
        this.tree = tree;
        GffField field = (GffField) value;
        if ( male == null ){
            male = getFont().canDisplay('\u2642') ?
                String.valueOf('\u2642') : "male";
            female = getFont().canDisplay('\u2640') ?
                String.valueOf('\u2640') : "female";
        }
        //setText( text );
        //setText( sb.toString() );
        if ( field.getType() == GffCExoLocString.SUBSTRINGTYPE ){
            CExoLocSubString sub = (CExoLocSubString) field;
            int code = sub.language.getCode();
            StringBuilder sb = new StringBuilder(sub.language.getName());
            sb.append(" ").append(sub.gender == 0? male : female);
            setText(sb.toString());
            //setText( sub.language.getName() + " " + sub.gender == 0? male : female );
            Icon icon = languageIconMap.get(sub.language);
            if (icon != null)
                setIcon(icon);
        } else {
            Icon icon = getIconForType(field.getType());
            if (icon != null) setIcon( icon );
        }
        //if (field instanceof GffInteger)	text = field.toString();
        validate();
        return this;
    }

    protected Icon loadIcon( String name ){
        URL url = getClass().getResource(name);
        Icon i = null;
        if ( url != null ){
            i = new ImageIcon(url);
        }
        return i;
    }

    /*
    public static Icon getIconForType( byte type ){
        Icon i = null;
        if (type == Gff.STRUCT)
            i = uid.getIcon("gffstruct.icon");
        else if (type == Gff.LIST)
            i = uid.getIcon("gfflist.icon");
        else if ( type < 9 && type != 7 ){
            i = uid.getIcon("gffnumber.icon");
        } else if (type == Gff.VOID)
            i = uid.getIcon("gffvoid.icon");
        else
            i = uid.getIcon("gffstring.icon");
        return i;
    }
    */

    public Icon getIconForType( byte type ){
        return typeIconMap.get(type);
    }
}
