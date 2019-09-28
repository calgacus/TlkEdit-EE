package org.jl.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 * A TransferHandler for importing files per Drag and Drop, supports both
 * DataFlavor.javaFileListFlavor and mime type "text/uri-list;class=java.lang.String"
 * used by KDE
 */
public abstract class FileDropHandler extends TransferHandler{
    static DataFlavor uriListFlavor = null;
    static {
        try {
            uriListFlavor = new DataFlavor("text/uri-list;class=java.lang.String");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @Override public boolean canImport(JComponent comp, DataFlavor[] transferFlavors){

        for ( DataFlavor d : transferFlavors ){
            if ( d.equals(DataFlavor.javaFileListFlavor) || d.equals(uriListFlavor) )
                return true;
        }
        return false;
    }

    @Override public boolean importData(JComponent comp, Transferable t){
        try{
            if ( t.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ) {
                importFiles((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
            }
            if ( t.isDataFlavorSupported(uriListFlavor) ){
                String s = (String) t.getTransferData(uriListFlavor);
                String[] uris = s.split("\n");
                final ArrayList<File> l = new ArrayList<>();
                for ( String uri : uris ){
                    if ( uri.length() > 1 ) {
                        l.add(new File(new URI(uri.trim())));
                    }
                }
                importFiles(l);
            }
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return false;
    }

    abstract public void importFiles( List<File> files );

    @Override
    public int getSourceActions(JComponent c){
        return NONE;
    }
}
