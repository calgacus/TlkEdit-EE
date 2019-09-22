/*
 * SimpleFileEditorPanelX.java
 *
 * Created on 15. Mai 2005, 19:30
 */

package org.jl.nwn.editor;

import java.io.File;

import org.jdesktop.swingx.event.MessageListener;
import org.jdesktop.swingx.event.MessageSource;
import org.jdesktop.swingx.event.MessageSourceSupport;
import org.jdesktop.swingx.event.ProgressListener;
import org.jdesktop.swingx.event.ProgressSource;
import org.jl.nwn.Version;

public abstract class SimpleFileEditorPanelX extends SimpleFileEditorPanel implements MessageSource, ProgressSource {
    protected final MessageSourceSupport msgSup = new MessageSourceSupport(this);

    public abstract boolean load( File file, Version nwnVersion );

    @Override
    public void removeMessageListener(MessageListener messageListener) {
        msgSup.removeMessageListener(messageListener);
    }

    @Override
    public void addMessageListener(MessageListener messageListener) {
        msgSup.addMessageListener(messageListener);
    }

    @Override
    public MessageListener[] getMessageListeners() {
        return msgSup.getMessageListeners();
    }

    @Override
    public void removeProgressListener(ProgressListener progressListener) {
        msgSup.removeProgressListener(progressListener);
    }

    @Override
    public void addProgressListener(ProgressListener progressListener) {
        msgSup.addProgressListener(progressListener);
    }

    @Override
    public ProgressListener[] getProgressListeners() {
        return msgSup.getProgressListeners();
    }
}
