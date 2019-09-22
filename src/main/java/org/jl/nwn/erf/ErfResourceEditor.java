/*
 * Created on 10.01.2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
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

/**
 * @author ich
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ErfResourceEditor extends SimpleFileEditorPanel {
	private SimpleFileEditorPanel delegate;
	private ErfEdit erf;
	private ResourceID resID;

	public ErfResourceEditor( SimpleFileEditorPanel delegate, ErfEdit erf, ResourceID resID ){
		super();
		this.delegate = delegate;
		this.erf = erf;
		this.resID = resID;
		setLayout( new BorderLayout() );
		add( delegate, BorderLayout.CENTER );
	}

	/* (non-Javadoc)
	 * @see org.jl.nwn.editor.SimpleFileEditorPanel#addChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void addPropertyChangeListener(PropertyChangeListener cl) {
		delegate.addPropertyChangeListener(cl);
	}

	/**
	 * @return
	 */
	@Override
	public boolean canSave() {
		return erf.canSave();
	}

	/**
	 * @return
	 */
	@Override
	public boolean canSaveAs() {
		return false;
		//return delegate.canSaveAs();
	}

	/**
	 *
	 */
	@Override
	public void close() {
		delegate.close();
	}

	/**
	 * @return
	 */
	@Override
	public File getFile() {
		//return new File( erf.getFile(), resID.toFileName() );
		return new File( erf.getFile().getName()+"["+resID.toFileName()+"]" );
		//return delegate.getFile();
	}

	/* (non-Javadoc)
	 * @see org.jl.nwn.editor.SimpleFileEditorPanel#getIsModified()
	 */
	@Override
	public boolean getIsModified() {
		return delegate.getIsModified();
	}

	/* (non-Javadoc)
	 * @see org.jl.nwn.editor.SimpleFileEditorPanel#removeChangeListener(javax.swing.event.ChangeListener)
	 */
	@Override
	public void removePropertyChangeListener(PropertyChangeListener cl) {
		delegate.removePropertyChangeListener(cl);
	}

	/**
	 * @throws IOException
	 */
	@Override
	public void save() throws IOException {
		delegate.save();
		erf.save();
	}

	/**
	 * @param f
	 * @throws IOException
	 */
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
