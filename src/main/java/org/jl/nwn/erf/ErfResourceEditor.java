package org.jl.nwn.erf;

import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.jl.nwn.Version;
import org.jl.nwn.editor.SimpleFileEditorPanel;
import org.jl.nwn.resource.ResourceID;

public class ErfResourceEditor extends SimpleFileEditorPanel {
	private final SimpleFileEditorPanel delegate;
	private final ErfEdit erf;
	private final ResourceID resID;

	public ErfResourceEditor( SimpleFileEditorPanel delegate, ErfEdit erf, ResourceID resID ){
		super();
		this.delegate = delegate;
		this.erf = erf;
		this.resID = resID;
		setLayout( new BorderLayout() );
		add( delegate, BorderLayout.CENTER );
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener cl) {
		delegate.addPropertyChangeListener(cl);
	}

	@Override
	public boolean canSave() {
		return erf.canSave();
	}

	@Override
	public boolean canSaveAs() {
		return false;
		//return delegate.canSaveAs();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public File getFile() {
        //return new File( erf.getFile(), resID.getFileName() );
        return new File( erf.getFile().getName()+"["+resID.getFileName()+"]" );
		//return delegate.getFile();
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
		erf.save();
	}

	@Override
	public void saveAs(File f, Version v) throws IOException {
		//delegate.saveAs(f);
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
