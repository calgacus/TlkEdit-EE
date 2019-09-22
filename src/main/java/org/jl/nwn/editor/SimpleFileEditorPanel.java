/*
 * Created on 04.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.editor;

import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jl.nwn.Version;

/**
 */
public abstract class SimpleFileEditorPanel extends JPanel implements SimpleFileEditor {
    public final static String ISMODIFIED_PROPERTY = "hasUnsavedChanges";
    public final static String FILE_PROPERTY = "editedFile";

    protected Version nwnVersion = Version.getDefaultVersion();

    protected boolean isModified = false;

    @Override
    public abstract boolean canSave();

    @Override
    public abstract boolean canSaveAs();

    @Override
    public abstract void save() throws IOException;

    @Override
    public abstract void saveAs(File f, Version nwnVersion) throws IOException;

    @Override
    public abstract void close();

    @Override
    public abstract File getFile();

    @Override
    public Version getFileVersion(){
        return nwnVersion;
    }

    @Override
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
