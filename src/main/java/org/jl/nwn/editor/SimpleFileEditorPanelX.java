/*
 * SimpleFileEditorPanelX.java
 *
 * Created on 15. Mai 2005, 19:30
 */

package org.jl.nwn.editor;


import java.io.File;
import org.jdesktop.swingx.event.MessageSource;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jdesktop.swingx.event.ProgressSource;
import org.jl.nwn.Version;

/**
 */
public abstract class SimpleFileEditorPanelX extends SimpleFileEditorPanel implements MessageSource, ProgressSource{
    protected MessageSourceSupport msgSup = new MessageSourceSupport(this);

    public abstract boolean load( File file, Version nwnVersion );

    @Override
    public void removeMessageListener(org.jdesktop.swingx.event.MessageListener messageListener) {
        msgSup.removeMessageListener(messageListener);
    }

    @Override
    public void addMessageListener(org.jdesktop.swingx.event.MessageListener messageListener) {
        msgSup.addMessageListener(messageListener);
    }

    @Override
    public org.jdesktop.swingx.event.MessageListener[] getMessageListeners() {
        return msgSup.getMessageListeners();
    }

    @Override
    public void removeProgressListener(org.jdesktop.swingx.event.ProgressListener progressListener) {
        msgSup.removeProgressListener(progressListener);
    }

    @Override
    public void addProgressListener(org.jdesktop.swingx.event.ProgressListener progressListener) {
        msgSup.addProgressListener(progressListener);
    }

    @Override
    public org.jdesktop.swingx.event.ProgressListener[] getProgressListeners() {
        return msgSup.getProgressListeners();
    }
	
}
