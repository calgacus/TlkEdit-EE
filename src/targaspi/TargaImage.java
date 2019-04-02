/*
 Targa image loader.
 Original written by Ben Stahl <benstahl@earthlink.net>
 
 This file is PUBLIC DOMAIN.
 */
package targaspi;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.image.BandedSampleModel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Arrays;
import java.util.Hashtable;
import javax.imageio.ImageTypeSpecifier;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class TargaImage {
    private static final int    NO_TRANSPARENCY = 255;
    private static final int    FULL_TRANSPARENCY = 0;
    
    private short               idLength;
    private short               colorMapType;
    private short               imageType;
    private int                 cMapStart;
    private int                 cMapLength;
    private short               cMapDepth;
    private int                 xOffset;
    private int                 yOffset;
    private int                 width;
    private int                 height;
    private short               pixelDepth;
    private short               imageDescriptor;
    
    private ColorModel          cm;
    public int[]                pixels;
    
    SampleModel                 sm;
    byte[]                      bytes;
    
    BufferedImage               image;
    
    public TargaImage(File srcFile) throws IOException {
        this(srcFile, true);
    }

    public TargaImage(File srcFile, boolean autoFlip) throws IOException {
        InputStream is = new FileInputStream(srcFile);
        BufferedInputStream bis = new BufferedInputStream(is);
        DataInputStream dis = new DataInputStream(bis);
        try {
            open(dis, autoFlip);
        } catch (IOException e) {
            dis.close();
            bis.close();
            is.close();
            throw e;
        }
    }
    
    public TargaImage(DataInput in, boolean autoFlip) throws IOException {
        open(in, autoFlip);
    }
    
    public TargaImage(byte[] buffer) throws IOException {
        this(new ByteArrayInputStream(buffer), true);
    }
    
    public TargaImage(InputStream is, boolean autoFlip) throws IOException{
        DataInputStream dis = new DataInputStream(is);
        try{
            open(dis, autoFlip);
        } finally {
            is.close();
        }
    }
    
    /* ----------------------------------------------------------- open */
    private void open(DataInput in, boolean autoFlip) throws IOException {
        int                 alpha = FULL_TRANSPARENCY;
        /* --- read targa header info --- */
        idLength = (short) in.readUnsignedByte();
        colorMapType = (short) in.readUnsignedByte();
        imageType = (short) in.readUnsignedByte();
        //Color Map Specification
        cMapStart = (int) readShortLE(in); //Color Map Origin
        cMapLength = (int) readShortLE(in); //Color Map Length
        cMapDepth = (short) in.readUnsignedByte();  //Color Map Entry Depth (16,24,32)
        //Image Specification
        xOffset = (int) readShortLE(in);
        yOffset = (int) readShortLE(in);
        width = (int) readShortLE(in);
        height = (int) readShortLE(in);
        pixelDepth = (short) in.readUnsignedByte();//Number of bits in stored pixel index
        
        imageDescriptor = (short) in.readUnsignedByte();
        /* bits           5 & 4 : image orientation :
         * bottom left    0   0  ( default )
         * bottom right   0   1
         * top left       1   0
         * top right      1   1
         */
        boolean flipHoriz = ((imageDescriptor & 32) == 0) && autoFlip;
        boolean flipVert = ((imageDescriptor & 16) == 1) && autoFlip; // unused
        int targaAlpha = imageDescriptor & 15;
        //System.out.println("alpha : " + (imageDescriptor & 15));
        
        /* --- skip over image id info (if present) --- */
        if (idLength > 0) {
            in.skipBytes(idLength);
        }
        // System.out.println("ImageType: "+imageType);
        // Color Map Starts Here
        // Types 1 & 9 use color map !
        // The following is for type  2
        if(imageType == 2) { // uncompressed RGB(A)
            bytes = new byte[width*height*(pixelDepth/8)];
            in.readFully(bytes);
            if ( flipHoriz ){
                byte[] line = new byte[width*(pixelDepth/8)];
                for ( int i = 0; i < height / 2; i++ ){
                    System.arraycopy(bytes, i * line.length, line, 0, line.length);
                    System.arraycopy(bytes, (height-1-i)*line.length,bytes,i * line.length, line.length);
                    System.arraycopy(line, 0, bytes, (height-1-i)*line.length, line.length);
                }
            }
        }
        //This is for ImageType 10, ImageType 9 is a mapped image !
        else if(imageType == 10) { // RLE encoded RGB(A)
            if (pixelDepth != 24 && pixelDepth != 32)
                throw new IOException("Unhandled Color Depth: " + pixelDepth);
            /* --- allocate the image buffer --- */
            //System.out.println("rle loader");
            pixels = new int[(width) * (height)];
            int numpix = pixels.length;
            int pix;
            int header = 0;
            for(int k = 0; k < numpix; k++) {
                header = in.readUnsignedByte();
                if((header & 0x80) > 0) { // runlength block
                    int runLength = header & 0x7F;
                    if (pixelDepth == 32) {
                        pix = in.readInt();
                    } else {
                        pix = in.readUnsignedByte() | in.readUnsignedByte() << 8 | in.readUnsignedByte() << 16;
                    }
                    Arrays.fill(pixels, k, k+runLength+1, pix);
                    k = k + runLength;
                } else { // no runlength, raw block
                    int rawCount = (header & 0x7F) + 1;
                    for(int j = 0; j < rawCount; j++) {
                        if (pixelDepth == 32) {
                            pix = in.readInt();
                        } else {
                            pix = in.readUnsignedByte() | in.readUnsignedByte() << 8 | in.readUnsignedByte() << 16;
                        }
                        pixels[k++] = pix;
                    }
                    k--;
                }
            }
            if ( flipHoriz ){
                int[] line = new int[width];
                //System.out.println("flip");
                for ( int i = 0; i < height / 2; i++ ){
                    System.arraycopy(pixels, i * line.length, line, 0, line.length);
                    System.arraycopy(pixels, (height-1-i)*line.length,pixels,i * line.length, line.length);
                    System.arraycopy(line, 0, pixels, (height-1-i)*line.length, line.length);
                }
            }
        } else {
            throw new IOException("Unhandled Image Type: " + imageType);
        }
    }
    
    public BufferedImage getImage() {
        if ( image == null ){
            if ( imageType == 2 ){
                //System.out.println("type 2");
                int[] bandOffsets24 = new int[]{2,1,0};
                int[] bandOffsets32 = new int[]{2,1,0,3}; // bands : RGBA
                sm = new PixelInterleavedSampleModel(
                        DataBuffer.TYPE_BYTE,
                        width,
                        height,
                        pixelDepth/8, // 'pixel stride'
                        width * (pixelDepth/8), // 'scanline stride'
                        (pixelDepth==32)? bandOffsets32 : bandOffsets24
                        );
                DataBuffer db = new DataBufferByte(bytes, bytes.length);
                WritableRaster r  = Raster.createWritableRaster(sm, db, null);
                /*
                ColorSpace cs = Toolkit.getDefaultToolkit().getColorModel().getColorSpace();
                cm = new ComponentColorModel(cs, new int[]{0x0000FF, 0xFF00, 0xFF0000}, false, false,
                        ComponentColorModel.OPAQUE,
                        DataBuffer.TYPE_BYTE );
                //cm = ComponentColorModel.getRGBdefault();
                image = new BufferedImage( cm, r, true, new Hashtable() );
                 */
                image = new BufferedImage( width, height,
                        (pixelDepth==32) ?
                            BufferedImage.TYPE_INT_ARGB :
                            BufferedImage.TYPE_3BYTE_BGR
                        );
                image.setData(r);
            } else if ( imageType == 10 ){
                //System.out.println("type 10");
                cm = pixelDepth == 24 ?
                    new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF) :
                    new DirectColorModel(32, 0xFF00, 0xFF0000, 0xFF000000, 0xFF);
                /*
                sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT,
                        width, height,
                        pixelDepth == 24 ?
                            new int[]{0xFF0000, 0xFF00, 0xFF}:
                            new int[]{0xFF00, 0xFF0000, 0xFF000000, 0xFF});
                 */
                sm = cm.createCompatibleSampleModel(width, height);
                DataBuffer db = new DataBufferInt(pixels, pixels.length);
                WritableRaster r  = Raster.createWritableRaster(sm, db, null);
                image = new BufferedImage( cm, r, false, new Hashtable() );
            }
        }
        return image;
    }
    
    public ImageTypeSpecifier getImageTypeSpecifier(){
        return ImageTypeSpecifier.createFromRenderedImage((RenderedImage)getImage());
    }
    
    /* --------------------------------------------------- getThumbnail */
    public Image getThumbnail(int maxSize, boolean smooth) {
        //System.out.println("getThumbnail...");
        Dimension   thumbnailSize = new Dimension(0, 0);
        int         pixel = 0;
        int         srcX = 0;
        int         srcY = 0;
        double      multiplier = 0.0;
        int         smoothArea;
        
        if (((width == maxSize) && (height == maxSize)) ||
                ((width < maxSize) && (height < maxSize))) {
            smooth = false;
        }
        
        if (width >= height) {
            thumbnailSize.width = maxSize;
            thumbnailSize.height =
                    (int) (Math.round(((float) height / (float) width)
                    * (float) maxSize));
        } else {
            thumbnailSize.height = maxSize;
            thumbnailSize.width =
                    (int) (Math.round(((float) width / (float) height)
                    * (float) maxSize));
        }
        
        multiplier = (double) width / (double) thumbnailSize.width;
        
        int[] thumbnailData =
                new int[thumbnailSize.width * thumbnailSize.height];
        
        for (int i = 0; i < thumbnailSize.height; i++) {
            srcY = (int) (i * multiplier);
            for (int j = 0; j < thumbnailSize.width; j++) {
                srcX = (int) (j * multiplier);
                /* Smoothing algorithm (nearest neighbor - t pattern) */
                if (smooth) {
                    int red = 0;
                    int green = 0;
                    int blue = 0;
                    int[] kernel = new int[5];
                    
                    /* Don't smooth as much if image is already square */
                    if (width == height) {
                        smoothArea = 1;
                    } else {
                        smoothArea = 2;
                    }
                    
                    kernel[2] = pixels[(srcY * width) + srcX];
                    
                    if ((srcY - smoothArea) < 0) {
                        kernel[0] = kernel[2];
                    } else {
                        kernel[0] = pixels[((srcY - smoothArea) * width)
                        + srcX];
                    }
                    if ((srcX - smoothArea) < 0) {
                        kernel[1] = kernel[2];
                    } else {
                        kernel[1] = pixels[(srcY * width)
                        + srcX - smoothArea];
                    }
                    
                    if ((srcX + smoothArea) > (width - 1)) {
                        kernel[3] = kernel[2];
                    } else {
                        kernel[3] = pixels[(srcY * width)
                        + srcX + smoothArea];
                    }
                    
                    if ((srcY + smoothArea) > (height - 1)) {
                        kernel[4] = kernel[2];
                    } else {
                        kernel[4] = pixels[((srcY + smoothArea) * width)
                        + srcX];
                    }
                    
                    for (int k = 0; k < kernel.length; k++) {
                        red += ((kernel[k] & 0x00FF0000) >>> 16);
                        green += ((kernel[k] & 0x0000FF00) >>> 8);
                        blue += (kernel[k] & 0x000000FF);
                    }
                    
                    red /= kernel.length;
                    green /= kernel.length;
                    blue /= kernel.length;
                    pixel = 0xFF000000 | red << 16 | green << 8 | blue;
                } else {
                    pixel = pixels[(srcY * width) + srcX];
                }
                thumbnailData[(i * thumbnailSize.width) + j] = pixel;
            }
        }
        DirectColorModel tcm =
                new DirectColorModel(24, 0xFF0000, 0xFF00, 0xFF);
        
        
        // --- set up an image from memory and return it ---
        return Toolkit.getDefaultToolkit().createImage(
                new MemoryImageSource(
                thumbnailSize.width, thumbnailSize.height, tcm,
                thumbnailData, 0, thumbnailSize.width));
    }
    
    /* -------------------------------------------------------- getSize */
    public Dimension getSize() {
        return new Dimension(width, height);
    }
    
    static short readShortLE( DataInput in ) throws IOException{
        return (short) (in.readUnsignedByte() | in.readUnsignedByte() << 8);
    }
    
    public static boolean canDecode( File f ) throws IOException{
        FileInputStream fis = new FileInputStream(f);
        byte[] header = new byte[8];
        fis.read(header);
        if ( header[1] != 0 ) // 1 for color mapped images
            return false;
        if ( header[2] != 2 && header[2] != 10 ) // uncompressed or rle true color
            return false;
        return true;
    }
    
    public static void main( String ... args ) throws IOException{
        if ( args[0].equalsIgnoreCase("-show") ){
            for ( int i = 1; i < args.length; i++ ){
                ImageIcon icon = new ImageIcon( new TargaImage(new File(args[i])).getImage() );
                JOptionPane.showMessageDialog(null, new JLabel(icon));
            }
        } else{
            long time = System.currentTimeMillis();
            for ( String filename : args ){
                File f = new File(filename);
                TargaImage image = new TargaImage(f);
            }
            System.out.printf("%d images loaded in %d ms\n", args.length, System.currentTimeMillis()-time);
        }
    }
    
}
