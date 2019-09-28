package org.jl.nwn.editor;

import java.io.File;
import java.io.IOException;

import org.jl.nwn.Version;

public interface SimpleFileEditor extends AutoCloseable {
	public boolean getIsModified();

	/** @return {@code true} if save is supported and can be called. */
	public boolean canSave();

	/** @return {@code true} if save as is supported and can be called. */
	public boolean canSaveAs();

	public void save()  throws IOException;

	public void saveAs( File f, Version nwnVersion )  throws IOException;

    @Override
	public void close();

	public File getFile();

    public Version getFileVersion();
}
