package targaspi;

import java.io.File;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

/**
 */
public class TargaReaderSPI extends ImageReaderSpi{
    static String vendorName = "foo";
    static String version = "0.1b";
    static String[] names = new String[]{
        "Targa", "TGA", "TPIC"
    };
    static String[] suffixes = new String[]{
        "tga", "TGA"
    };
    static String[] MIMETypes = new String[]{
        "image/x-targa"
    };
    static String readerClassName = "targaspi.TargaReader";
    static Class[] inputTypes = new Class[]{ ImageInputStream.class };
    static String[] writerSpiNames = null;
    static boolean supportsStandardStreamMetadataFormat = false;
    static String nativeStreamMetadataFormatName = null;
    static String nativeStreamMetadataFormatClassName = null;
    static String[] extraStreamMetadataFormatNames = null;
    static String[] extraStreamMetadataFormatClassNames = null;
    static boolean supportsStandardImageMetadataFormat = false;
    static String nativeImageMetadataFormatName = null;
    static String nativeImageMetadataFormatClassName = null;
    static String[] extraImageMetadataFormatNames = null;
    static String[] extraImageMetadataFormatClassNames = null;
    
    /** Creates a new instance of TargaReaderSPI */
    public TargaReaderSPI() {
        super(
                vendorName,
                version,
                names,
                suffixes,
                MIMETypes,
                readerClassName,
                inputTypes,
                writerSpiNames,
                supportsStandardStreamMetadataFormat,
                nativeStreamMetadataFormatName,
                nativeStreamMetadataFormatClassName,
                extraStreamMetadataFormatNames,
                extraStreamMetadataFormatClassNames,
                supportsStandardImageMetadataFormat,
                nativeImageMetadataFormatName,
                nativeImageMetadataFormatClassName,
                extraImageMetadataFormatNames,
                extraImageMetadataFormatClassNames
                );
    }
    
    public String getDescription(java.util.Locale locale) {
        return "Targa Image Reader";
    }
    
    TargaReader readerInstance = null;
    
    public javax.imageio.ImageReader createReaderInstance(Object extension) throws java.io.IOException {
        //System.out.println("createReaderInstance");
        if ( readerInstance == null )
            readerInstance = new TargaReader(this);
        return readerInstance;
    }
    
    public boolean canDecodeInput(Object source) throws java.io.IOException {
        //System.out.println("TargaReaderSPI.canDecodeInput : " + source);
        /*
        if ( source instanceof File ){
            System.out.println("File !");
            return TargaImage.canDecode((File)source);
        }
         */
        ImageInputStream is = (ImageInputStream) source;
        is.mark();
        byte[] header = new byte[8];
        is.read(header);
        is.reset();
        //System.out.println("color map : " + header[1]);
        if ( header[1] != 0 ) // 1 for color mapped images
            return false;
        //System.out.println("picture type : " + header[2]);
        if ( header[2] != 2 && header[2] != 10 ) // uncompressed or rle true color
            return false;
        //System.out.println("TargaReaderSPI accept");
        return true;
    }
    
    @Override public void onRegistration(ServiceRegistry registry, Class<?> category){
        super.onRegistration( registry, category );
        boolean b = false;
        try{
            Class<ImageReaderSpi> wbmpReader = (Class<ImageReaderSpi>) Class.forName("com.sun.imageio.plugins.wbmp.WBMPImageReaderSpi");
            ImageReaderSpi wbmp = registry.getServiceProviderByClass(wbmpReader);
            ImageReaderSpi targa = registry.getServiceProviderByClass(TargaReaderSPI.class);
            if ( wbmp != null && targa != null ){
                b = registry.setOrdering(
                        ImageReaderSpi.class,
                        targa,
                        wbmp );
                //System.out.println("ImageProviderOrdering set : " + b);
            } else{
                //System.out.println("com.sun.imageio.plugins.wbmp.WBMPImageReaderSPI not registered");
            }
        } catch ( ClassNotFoundException cnfe ){
            //System.out.println("com.sun.imageio.plugins.wbmp.WBMPImageReaderSPI not installed");
        }        
        //System.out.println("registered TargaReader SPI, setOrdering vs WBMP : " +b);
    }
    
}
