/*
 * Created on 24.03.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.editor;

import java.io.File;

import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;

import org.jl.nwn.erf.ErfEdit;
import org.jl.nwn.gff.Gff;
import org.jl.swing.UIDefaultsX;

/**
 */
public class NwnFileView extends FileView {
    static final FileSystemView fsv = FileSystemView.getFileSystemView();
    static final UIDefaultsX uid = new UIDefaultsX();

    static final Icon icon_folder;
    static final Icon icon_mime;
    static final Icon icon_tlk;
    static final Icon icon_2da;
    static final Icon icon_erf;
    static final Icon icon_gff;
    static final Icon icon_broken;

    static{
        uid.addResourceBundle("org.jl.nwn.editor.fileview");
        icon_folder = uid.getIcon("icon_folder");
        icon_mime = uid.getIcon( "icon_mime" );
        icon_tlk = uid.getIcon( "icon_tlk" );
        icon_2da = uid.getIcon( "icon_2da" );
        icon_erf = uid.getIcon( "icon_erf" );
        icon_gff = uid.getIcon( "icon_gff" );
        icon_broken = uid.getIcon( "icon_broken" );
    }

    public static void setUIDefaults(){
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        defaults.put( "FileChooser.upFolderIcon", uid.getIcon("FileChooser.upFolderIcon") );
        defaults.put( "FileChooser.listViewIcon", uid.getIcon("FileChooser.listViewIcon") );
        defaults.put( "FileChooser.newFolderIcon", uid.getIcon("FileChooser.newFolderIcon") );
        defaults.put( "FileChooser.detailsViewIcon", uid.getIcon("FileChooser.detailsViewIcon") );
        defaults.put( "FileChooser.homeFolderIcon", uid.getIcon("FileChooser.homeFolderIcon") );
        /*
        "FileView.computerIcon"
        "FileView.fileIcon"
        "FileView.directoryIcon"
        "FileView.floppyDriveIcon"
        "FileView.hardDriveIcon"
        */
    }

    @Override
    public Icon getIcon(File f){
        // broken symlinks do not exist
        if (!f.exists()) return icon_broken;
        if ( f.isDirectory() )
            //return fsv.isRoot(f)? fsv.getSystemIcon(f) : icon_folder;
            return fsv.getSystemIcon(f);
        if ( f.isFile() ){
            if ( f.getName().length() > 4 ){
                String ext = f.getName().toLowerCase().substring( f.getName().length()-3 );
                //System.out.println(ext);
                if ( ext.equals("tlk") )
                    return icon_tlk;
                else if ( ext.equals("2da") )
                    return icon_2da;
                else if ( Gff.isKnownGffFileType( ext ) )
                    return icon_gff;
                else if ( ErfEdit.accept( f ) )
                    return icon_erf;
            }
        }
        return fsv.getSystemIcon(f);
    }

    @Override
    public String getTypeDescription(File f){
        if ( f.getName().length() > 4 ){
            String ext = f.getName().toLowerCase().substring( f.getName().length()-3 );
            //System.out.println(ext);
            if ( ext.equals("tlk") )
                return "NWN Talk Table";
            else if ( ext.equals("2da") )
                return "NWN Two Dimensional Array";
            else if ( Gff.isKnownGffFileType( ext ) )
                return "NWN Generic File Format";
            else if ( ErfEdit.accept( f ) )
                return "NWN Encapsulated Resource File";
        }
        return fsv.getSystemTypeDescription(f);
    }

    public static void main(String[] args) {
    }
}
