package org.jl.nwn.editor;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.UIManager;

import org.jl.nwn.Version;

public class EditorServer {

    static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    static {
        logger.setLevel(Level.OFF);
        /*
        try{
        Handler h = new FileHandler(".tlkedit.log");
        h.setFormatter(new SimpleFormatter());
        logger.addHandler(h);
        } catch (IOException ioex){
        logger.log(Level.WARNING, "", ioex);
        }
         */
    }

    private static int serverPort = 4712;

    public static void main(String[] args) throws Exception {
        try {
            if (System.getProperty("swing.defaultlaf") == null) {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            }
            try {
                serverPort = Integer.parseInt(System.getProperty("tlkedit.serverport", "4712"));
            } catch (NumberFormatException nfe) {
            }

            int argsStart = 0;
            if (args.length > 0 && args[0].startsWith("-port=")) {
                serverPort = Integer.parseInt(args[0].substring(6));
                argsStart = 1;
            }

            boolean useServer = serverPort != -1;
            boolean serverAvailable = false;

            if (useServer) {
                try {
                    //SocketAddress addr = new InetSocketAddress("localhost", registryPort);
                    SocketAddress addr = new InetSocketAddress(InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1}), serverPort);
                    try (final Socket sock = new Socket()) {
                        sock.bind(addr);
                    }
                    SocketServer socketServer = new SocketServer(serverPort, new EditorFrameX());
                    socketServer.start();
                    serverAvailable = true;
                } catch (BindException ioex) {
                    logger.log(Level.INFO, "Unable to start server on port {0} : {1}", new Object[]{serverPort, ioex});
                    serverAvailable = true;
                } catch (IOException ioex) {
                    logger.log(Level.INFO, "Unable to start server on port {0} : {1}", new Object[]{serverPort, ioex});
                }
            }
            logger.info("server available : " + serverAvailable);
            if (useServer) {
                logger.log(Level.INFO, "trying to send filenames to port " + serverPort);
                for (int i = argsStart; i < args.length; i++) {
                    logger.info(args[i]);
                    try {
                        send(serverPort, (new File(args[i])).getAbsolutePath());
                    } catch (IOException ioex) {
                        logger.log(Level.INFO, "could not send filename to server ({0}) : {1}", new Object[]{args[i], ioex});
                    }
                }
            } else {
                EditorFrameX ed = new EditorFrameX();
                for (int i = argsStart; i < args.length; i++) {
                    ed.openFile((new File(args[i])).getAbsoluteFile(), Version.getDefaultVersion());
                }
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "", e);
            throw e;
        }
    }

    private static void send(int port, String s) throws IOException {
        try (final Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByAddress("localhost", new byte[]{127, 0, 0, 1}), port));
            PrintStream out = new PrintStream(socket.getOutputStream(), true);
            out.println(s);
            System.out.println(s);
        }
    }

    private static class SocketServer extends Thread {

        ServerSocket sock;
        EditorFrameX ed;

        SocketServer(int port, EditorFrameX ed) throws IOException {
            this.ed = ed;
            sock = new ServerSocket(port);
            logger.info("Server bound : " + sock.isBound());
            ed.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent e) {
                    super.windowClosed(e);
                    shutdown();
                }
            });
        }

        @Override
        public void run() {
            while (!isInterrupted()) {
                logger.info("waiting...");
                try (final Socket s = sock.accept()) {
                    //System.out.println("accept");
                    logger.info("accept");
                    BufferedReader r = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    String filename = r.readLine();
                    logger.info(filename);
                    File f = new File(filename);
                    ed.openFile(f, Version.getDefaultVersion());
                } catch (ClosedByInterruptException cbie) {
                    logger.log(Level.INFO, "", cbie);
                    break;
                } catch (IOException ioex) {
                    logger.log(Level.INFO, "", ioex);
                } catch (Exception e) {
                }
            }
        }

        public void shutdown() {
            this.interrupt();
            try {
                sock.close();
            } catch (IOException ioex) {
                logger.log(Level.INFO, "", ioex);
            }
        }
    }
}
