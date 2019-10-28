package org.jl.nwn.tlk.editor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.jl.nwn.patcher.Patcher;
import org.jl.nwn.patcher.PatcherGUI;
import org.jl.nwn.resource.NwnRepository;
import org.jl.nwn.resource.ResourceID;

public class BmuPlayer implements Runnable, AutoCloseable {

    private static NwnRepository br;
    private Clip c = null;

    Object playerObject;

    /**
     * Create player for music files.
     *
     * @param soundname name of resource in repository, as ResRef
     * @param rep Repository, in which locate resource. If {@code null}, repository
     *        configured for {@link Patcher} will be used
     *
     * @throws FileNotFoundException if specified sound resource not found
     * @throws IOException If resource can not be readed from repository
     */
    public BmuPlayer(String soundname, NwnRepository rep) throws IOException {
        if (rep == null){
            if (br== null)
                br = PatcherGUI.newRepository();
            rep = br;
        }

        final ResourceID resId = new ResourceID(soundname, "wav");
        final InputStream is = rep.getResource(resId);
        if (is == null) {
            throw new FileNotFoundException("no such resource : " + resId);
        }
        try {
            AudioInputStream as = AudioSystem.getAudioInputStream(is);
            c = (Clip) AudioSystem.getLine(new Line.Info(Clip.class));
            c.open(as);
        } catch (UnsupportedAudioFileException uafe) {
            try{
                final Class<?> player = Class.forName("javazoom.jl.player.Player");
                final Constructor<?> c = player.getConstructor(InputStream.class);
                playerObject = c.newInstance(is);
            } catch (Exception e){
                e.printStackTrace();
            }
        } catch (LineUnavailableException lue) {
            System.out.println("can't play sound, sound system busy ?" + lue);
            lue.printStackTrace();
        }
    }

    @Override
    public void run(){
        if (c != null) {
            c.start();
        } else
        if (playerObject != null) {
            try {
                playerObject.getClass().getDeclaredMethod("play").invoke(playerObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        if (c != null) {
            c.close();
        } else
        if (playerObject != null) {
            try {
                playerObject.getClass().getDeclaredMethod("close").invoke(playerObject);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
