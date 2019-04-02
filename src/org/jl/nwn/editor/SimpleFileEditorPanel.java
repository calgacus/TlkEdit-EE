/*
 * Created on 04.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.editor;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import java.io.File;
import java.io.IOException;
import org.jl.nwn.Version;

/**
 */
public abstract class SimpleFileEditorPanel extends JPanel implements SimpleFileEditor {
    public final static String ISMODIFIED_PROPERTY = "hasUnsavedChanges";
    public final static String FILE_PROPERTY = "editedFile";
    
    protected Version nwnVersion = Version.getDefaultVersion();
    
    protected boolean isModified = false;
    
    public abstract boolean canSave();
    
    public abstract boolean canSaveAs();
    
    public abstract void save() throws IOException;
    
    public abstract void saveAs(File f, Version nwnVersion) throws IOException;
    
    public abstract void close();
    
    public abstract File getFile();
    
    public Version getFileVersion(){
        return nwnVersion;
    }
    
    public boolean getIsModified(){
        return isModified;
    }
    
    protected void setIsModified( boolean modified ){
        boolean oldValue = isModified;
        isModified = modified;
        firePropertyChange(ISMODIFIED_PROPERTY, oldValue, modified);
    }
    
    public JMenu[] getMenus(){
        return null;
    }
    
    public abstract void showToolbar( boolean b );
    
    public abstract JToolBar getToolbar();
    
}
