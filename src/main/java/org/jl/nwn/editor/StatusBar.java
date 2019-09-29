package org.jl.nwn.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.event.MessageEvent;
import org.jdesktop.swingx.event.MessageListener;
import org.jdesktop.swingx.event.ProgressEvent;
import org.jdesktop.swingx.event.ProgressListener;

public class StatusBar implements MessageListener, ProgressListener {

    protected final JProgressBar progressBar = new JProgressBar();
    protected final MessageLabel messageLabel = new MessageLabel(new JLabel(), Level.ALL, 5000);
    protected final MessageLabel heapLabel = new MessageLabel(new JLabel(), Level.ALL, Integer.MAX_VALUE);
    protected final JXStatusBar statusBar = new JXStatusBar();

    protected volatile int progress = 0;

    protected final Timer progressUpdater = new Timer(50, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent evt) {
            progressBar.setValue(progress);
        }
    });

    public static class MessageLabel implements MessageListener {

        protected final Level messageLevel;
        protected final JLabel messageLabel;

        protected final Timer wipeTimer = new Timer(5000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                messageLabel.setText("");
            }
        });

        public MessageLabel(JLabel label, Level level, int wipeDelay) {
            this.messageLabel = label;
            this.messageLevel = level;
            wipeTimer.setDelay(Integer.MAX_VALUE);
            wipeTimer.setInitialDelay(wipeDelay);
        }

        public JLabel getLabel() {
            return messageLabel;
        }

        @Override
        public void message(final MessageEvent evt) {
            if (evt.getLevel().intValue() >= messageLevel.intValue()) {
                if (!SwingUtilities.isEventDispatchThread()) {
                    SwingUtilities.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            messageLabel.setText(evt.getMessage());
                            wipeTimer.restart();
                        }
                    });
                } else {
                    messageLabel.setText(evt.getMessage());
                    wipeTimer.restart();
                }
            }
        }
    }

    public StatusBar() {
        JXStatusBar.Constraint cProgressBar = new JXStatusBar.Constraint(150);
        JXStatusBar.Constraint cHeap = new JXStatusBar.Constraint(130);
        JXStatusBar.Constraint cMessageLabel = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
        statusBar.add(heapLabel.getLabel(), cHeap);
        statusBar.add(messageLabel.getLabel(), cMessageLabel);
        statusBar.add(progressBar, cProgressBar);
        statusBar.setVisible(true);
    }

    public JXStatusBar getStatusBar() {
        return statusBar;
    }

    @Override
    public void message(final MessageEvent evt) {
        messageLabel.message(evt);
    }

    @Override
    public void progressEnded(ProgressEvent evt) {
        progressUpdater.stop();
        progressBar.setValue(0);
        progressBar.setEnabled(false);
    }

    @Override
    public void progressIncremented(ProgressEvent evt){
        progress = evt.getProgress();
    }

    @Override
    public void progressStarted(ProgressEvent evt) {
        progressBar.setEnabled(true);
        progress = 0;
        progressUpdater.restart();
        progressBar.setMinimum(evt.getMinimum());
        progressBar.setMaximum(evt.getMaximum());
    }
}
