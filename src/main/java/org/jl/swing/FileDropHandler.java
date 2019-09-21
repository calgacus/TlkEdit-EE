package org.jl.swing;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.net.URI;
import java.util.LinkedList;
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
    
    /** Creates a new instance of FileDropHandler */
    public FileDropHandler(){
    }
    
    @Override public boolean canImport(JComponent comp, DataFlavor[] transferFlavors){

        for ( DataFlavor d : transferFlavors ){
            //System.out.println(d);
            if ( d.equals(DataFlavor.javaFileListFlavor) || d.equals(uriListFlavor) )
                return true;
        }
        return false;
    }
    
    @Override public boolean importData(JComponent comp, Transferable t){
        //System.out.println("importData");
        try{
            if ( t.isDataFlavorSupported(DataFlavor.javaFileListFlavor) ){
                LinkedList<File> l = new LinkedList<File>();
                for ( Object f : (List) t.getTransferData(DataFlavor.javaFileListFlavor) ){
                    l.add((File)f);
                }
                importFiles(l);
            }
            if ( t.isDataFlavorSupported(uriListFlavor) ){
                String s = (String) t.getTransferData(uriListFlavor);
                String[] uris = s.split("\n");
                LinkedList<File> l = new LinkedList<File>();
                for ( String uri : uris ){
                    if ( uri.length() > 1 ){
                        //System.out.println(uri);
                        File f = new File(new URI(uri.trim()));
                        l.add(f);
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
    
    public int getSourceActions(JComponent c){
        return NONE;
    }
    
}
