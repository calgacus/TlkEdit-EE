package targaspi;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

class Test {
    public static void main( String ... args ) throws IOException{
        if ( args[0].equalsIgnoreCase("-show") ){
            for ( int i = 1; i < args.length; i++ ){
                ImageIcon icon = new ImageIcon( ImageIO.read(new File(args[i])) );
                JOptionPane.showMessageDialog(null, new JLabel(icon));
            }
        } else{
            long time = System.currentTimeMillis();
            for ( String filename : args ){
                //System.out.println(filename);
                File f = new File(filename);
                ImageIO.read(f);
            }
            System.out.printf("%d images loaded in %d ms\n", args.length, System.currentTimeMillis()-time);
        }
    }
}
