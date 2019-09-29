package org.jl.nwn.editor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.jl.nwn.Version;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.ResourceID;

public class RepositoryResourceEditor extends SimpleFileEditorPanel {

    private final SimpleFileEditorPanel delegate;
    private final NwnRepository rep;
    private final ResourceID resID;
    private File savedAsFile = null;

    public RepositoryResourceEditor(SimpleFileEditorPanel delegate, NwnRepository rep, ResourceID resID) {
        this.delegate = delegate;
        this.rep = rep;
        this.resID = resID;
        setLayout(new BorderLayout());
        add(delegate, BorderLayout.CENTER);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener cl) {
        delegate.addPropertyChangeListener(cl);
    }

    @Override
    public boolean canSave() {
        return savedAsFile == null ? rep.isWritable() : delegate.canSave();
    }

    @Override
    public boolean canSaveAs() {
        return delegate.canSaveAs();
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public File getFile() {
        return savedAsFile == null ? new File(rep.getResourceLocation(resID) + "[" + resID.getFileName() + "]") : delegate.getFile();
    }

    @Override
    public boolean getIsModified() {
        return delegate.getIsModified();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener cl) {
        delegate.removePropertyChangeListener(cl);
    }

    @Override
    public void save() throws IOException {
        delegate.save();
        if (savedAsFile == null) {
            return;
        }
        try (final FileInputStream is = new FileInputStream(delegate.getFile());
             final OutputStream os = rep.putResource(resID)
        ) {
            final byte[] buffer = new byte[32000];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
        }
    }

    @Override
    public void saveAs(File f, Version v) throws IOException {
        delegate.saveAs(f, v);
        savedAsFile = f;
    }

    @Override
    public JMenu[] getMenus() {
        return delegate.getMenus();
    }

    @Override
    public JToolBar getToolbar() {
        return delegate.getToolbar();
    }

    @Override
    public void showToolbar(boolean b) {
        delegate.showToolbar(b);
    }
}
