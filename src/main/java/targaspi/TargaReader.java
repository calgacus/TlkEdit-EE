package targaspi;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class TargaReader extends ImageReader {

    protected TargaImage image = null;

    protected TargaReader(ImageReaderSpi originatingProvider) {
        super( originatingProvider );
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        return image.getSize().width;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return Arrays.asList(image.getImageTypeSpecifier()).iterator();
    }

    @Override
    public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        return image.getSize().height;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if ( getInput() instanceof ImageInputStream )
            image = new TargaImage((ImageInputStream)getInput(), false);
        else
            image = new TargaImage((File)getInput(), false);
        BufferedImage target = param.getDestination();
        if ( target != null ){
            target.getRaster().setRect( image.getImage().getRaster() );
            return target;
        }
        return image.getImage();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public void reset() {
        super.reset();
        image = null;
    }
}
