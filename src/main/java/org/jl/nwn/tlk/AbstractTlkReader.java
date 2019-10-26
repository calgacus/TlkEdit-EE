package org.jl.nwn.tlk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import static java.nio.charset.StandardCharsets.US_ASCII;

import javax.swing.ProgressMonitor;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

/**
 * Reads a tlk file and creates a Talk Table object representation by using
 * a builder pattern.
 */
public abstract class AbstractTlkReader<TlkTable>{

    /** {@code "TLK V3.0"} - magic and version of the file. */
    public final static byte[] HEADER  = { 'T', 'L', 'K', ' ', 'V', '3', '.', '0' };

    private final Version nwnVersion;

    /** Creates a new instance of AbstractTlkReader
     * @param nwnVersion version of tlk files to be read by this object,
     *        determines the charset used to decode strings.
     */
    public AbstractTlkReader( Version nwnVersion ){
        this.nwnVersion = nwnVersion;
    }

    public Version getVersion(){
        return nwnVersion;
    }

    /**
     * Convenience method for {@link #load(InputStream, ProgressMonitor)}.
     */
    public TlkTable load( File tlkFile, ProgressMonitor pm ) throws IOException{
        try (final FileInputStream fis = new FileInputStream( tlkFile );
             final BufferedInputStream bis = new BufferedInputStream(fis)
        ) {
            return load(bis, pm);
        }
    }

    /**
     * Loads a tlk file through an InputStream. Will first call
     * {@link #createTlk} to create a new Talk Table object and then call
     * {@link #createEntry} for each Talk Table entry in the file.
     *
     * @param is InputStream to read from, should be buffered for better performance.
     * @param pm progress monitor for the loading operation, may be {@code null}
     *
     * @throws IllegalArgumentException If first 8 bytes of stream is not a {@code "TLK V3.0"}
     */
    public final TlkTable load(InputStream is, ProgressMonitor pm) throws IOException {
        final byte[] indexEntryBytes = new byte[40];
        // Read 20 bytes of header
        is.read( indexEntryBytes, 0, 20 );
        for (int i = 0; i < HEADER.length; ++i) {
            if (indexEntryBytes[i] != HEADER[i]) {
                throw new IllegalArgumentException("Invalid magic or version of file, expected '"
                        + new String(HEADER, US_ASCII)
                        + "', got '"
                        + new String(indexEntryBytes, 0, HEADER.length, US_ASCII)
                        + "'"
                );
            }
        }

        final ByteBuffer mbb = ByteBuffer.wrap(indexEntryBytes);
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.position(HEADER.length);// Skip magic and version
        NwnLanguage language = NwnLanguage.find( getVersion(), mbb.getInt() );
        int entries = mbb.getInt();
        TlkTable tlk = createTlk(entries, language, nwnVersion);

        int stringDataStart = mbb.getInt();

        if ( pm != null ){
            pm.setMinimum( 0 );
            pm.setMaximum( entries );
            //pm.setNote("reading tlk index");
        }

        int[] stringSizes = new int[entries];

        byte[] flags = new byte[entries];
        float[] sndlength = new float[entries];
        String[] resrefs = new String[entries];

        int maxStringSize = 0;

        // reading index entries
        for ( int i = 0; i < entries; i++ ){
            is.read( indexEntryBytes );

            flags[i] = indexEntryBytes[0];
            if ( indexEntryBytes[4] != 0 )
                resrefs[i] = new String( indexEntryBytes, 4, 16 ).trim();
            mbb.position( 32 );
            int stringSize = mbb.getInt();
            stringSizes[i] = stringSize;
            maxStringSize = maxStringSize<stringSize?
                stringSize :  maxStringSize;
            sndlength[i] = mbb.getFloat();
        }

        int skip = stringDataStart - ( 0x14 + (40*entries) );
        if ( skip != 0 ){
            System.out.println(
                    "unused bytes between index and string data ?!? : "
                    + skip );
            if ( skip > 0 )
                is.skip(skip);
        }

        // read string data
        byte[] strBuf = new byte[maxStringSize];
        if ( pm != null ){
            //pm.setNote("reading strings");
        }

        Charset charset = Charset.forName(language.getEncoding());
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = CharBuffer.allocate(
                (int)Math.ceil(maxStringSize * decoder.maxCharsPerByte()));
        ByteBuffer bb = ByteBuffer.wrap(strBuf);

        for ( int i = 0; i < entries; i++ ){
            is.read( strBuf, 0, stringSizes[i] );
            bb.rewind();
            bb.limit(stringSizes[i]);
            cbuf.limit(cbuf.capacity());
            decoder.reset();
            CoderResult result = decoder.decode(bb, cbuf, true);
            decoder.flush(cbuf);
            cbuf.flip();
            if ( result.isError() ){
                System.err.printf("CharsetDecoder error on entry %d : %s\n",
                        i, result );
            }
            createEntry( tlk, i,
                    flags[i],
                    resrefs[i],
                    sndlength[i],
                    cbuf.toString());

            if ( pm!=null ){
                if ( pm.isCanceled() )
                    break;
                pm.setProgress( i );
            }
        }
        if ( pm!=null && !pm.isCanceled() )
            pm.setProgress( pm.getMaximum() );
        is.close();
        return tlk;
    }

    /**
     * Called to create new Talk Table object.
     * @param size number of entries in the tlk file content being loaded
     * @param lang language of the tlk file content
     * @param nwnVersion the version with which this reader was initialized
     * @return a Talk Table object that can hold at least {@code size} entries.
     */
    protected abstract TlkTable createTlk(
            int size,
            NwnLanguage lang,
            Version nwnVersion );

    /**
     * Called to create and add a new Talk Table entry, will be called by
     * {@code AbstractTlkReader.load} strictly in the order in which the entries
     * are found in the input file/stream.
     * @param tlk Talk Table object to which the new entry should be added
     * @param position position of entry in the Talk Table ( the StrRef ).
     * @param flags the entry's flags
     * @param resRef sound resref for the entry
     * @param sndLength length of the sound refered by the resref
     * @param string the Talk Table string itself
     */
    protected abstract void createEntry( TlkTable tlk, int position,
            byte flags, String resRef, float sndLength, String string );
}
