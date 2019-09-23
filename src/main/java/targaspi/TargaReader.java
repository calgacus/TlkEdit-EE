package targaspi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 */
public class TargaReader extends ImageReader{

    protected TargaImage image = null;

    /** Creates a new instance of TargaReader */
    protected TargaReader(ImageReaderSpi originatingProvider) {
        super( originatingProvider );
    }

    @Override
    public int getWidth(int imageIndex) throws java.io.IOException {
        return image.getSize().width;
    }

    @Override
    public java.util.Iterator<javax.imageio.ImageTypeSpecifier> getImageTypes(int imageIndex) throws java.io.IOException {
        return Arrays.asList(new ImageTypeSpecifier[]{image.getImageTypeSpecifier()}).iterator();
    }

    @Override
    public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws java.io.IOException {
        return null;
    }

    @Override
    public int getHeight(int imageIndex) throws java.io.IOException {
        return image.getSize().height;
    }

    @Override
    public java.awt.image.BufferedImage read(int imageIndex, javax.imageio.ImageReadParam param) throws java.io.IOException {
        //System.out.println("TargaReader.read()");
        if ( getInput() instanceof ImageInputStream )
            image = new TargaImage((ImageInputStream)getInput(), false);
        else
            image = new TargaImage((File)getInput(), false);
        BufferedImage target = param.getDestination();
        if ( target != null ){
            target.getRaster().setRect( image.getImage().getRaster() );
            return target;
        }
        else
            return image.getImage();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws java.io.IOException {
        return 1;
    }

    @Override
    public javax.imageio.metadata.IIOMetadata getStreamMetadata() throws java.io.IOException {
        return null;
    }

    @Override public void reset(){
        super.reset();
        image = null;
    }
}
