package targaspi;

import java.io.IOException;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

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
    static Class<?>[] inputTypes = { ImageInputStream.class };
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

    @Override
    public String getDescription(java.util.Locale locale) {
        return "Targa Image Reader";
    }

    TargaReader readerInstance = null;

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        if ( readerInstance == null )
            readerInstance = new TargaReader(this);
        return readerInstance;
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        /*
        if ( source instanceof File ){
            return TargaImage.canDecode((File)source);
        }
         */
        ImageInputStream is = (ImageInputStream) source;
        is.mark();
        byte[] header = new byte[8];
        is.read(header);
        is.reset();
        if ( header[1] != 0 ) // 1 for color mapped images
            return false;
        if ( header[2] != 2 && header[2] != 10 ) // uncompressed or rle true color
            return false;
        return true;
    }

    @Override
    public void onRegistration(ServiceRegistry registry, Class<?> category) {
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
            }
        } catch ( ClassNotFoundException cnfe ){
        }
    }
}
