package org.jl.nwn.editor;

import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.jl.nwn.Version;

/**
 * Base editor pane for files in editor. Implements tracking {@code isModified}
 * flag and version of game, for which this editor will save data.
 */
public abstract class SimpleFileEditorPanel extends JPanel implements SimpleFileEditor {
    /** Name of {@boolean} bean property that hold {@code isModified} status of the file. */
    public final static String ISMODIFIED_PROPERTY = "hasUnsavedChanges";
    /** Name of {@link File} bean property that hold file name, associated with this pane. */
    public final static String FILE_PROPERTY = "editedFile";

    protected Version nwnVersion = Version.getDefaultVersion();

    protected boolean isModified = false;

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
