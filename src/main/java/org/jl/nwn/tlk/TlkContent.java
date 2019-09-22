package org.jl.nwn.tlk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ProgressMonitor;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

/**
 * tlk content = List of tlk entries + language ID
 */
public class TlkContent implements Iterable<TlkEntry> {

    private NwnLanguage language;
    private List<TlkEntry> tlkEntries = new ArrayList<>();

    //"TLK "
    public final static byte[] TLKHEADER = new byte[]{ 0x54, 0x4c, 0x4b, 0x20 };
    //"V3.0"
    public final static byte[] TLKVERSION = new byte[]{ 0x56, 0x33, 0x2e, 0x30 };

    public TlkContent(NwnLanguage lang) {
        this.language = lang;
    }

    public TlkContent(List<TlkEntry> entries, NwnLanguage lang) {
        this(lang);
        tlkEntries = entries;
    }

    public void set( int pos, TlkEntry e ){
        for ( int i = tlkEntries.size(); i < pos + 1; i++ )
            tlkEntries.add( new TlkEntry() );
        tlkEntries.set( pos, e );
    }

    public void add( TlkEntry e ){
        tlkEntries.add( e );
    }

    public void add( int pos, TlkEntry e ){
        tlkEntries.add( pos, e );
    }

    public void addAll( int pos, TlkContent c ){
        tlkEntries.addAll( pos, c.tlkEntries );
    }

    public void addAll( TlkContent c ){
        addAll( size(), c );
    }

    public TlkEntry remove( int pos ){
        return tlkEntries.remove( pos );
    }

    public TlkEntry get( int pos ){
        return tlkEntries.get( pos );
    }

    public int size(){
        return tlkEntries.size();
    }

    @Override
    public Iterator<TlkEntry> iterator(){
        return tlkEntries.iterator();
    }

    public void saveAs(File file, Version nwnVersion) throws IOException {
                /* use BufferedOutputStream for performance and FileChannel
                 * to set the write position
                 * */
        FileOutputStream fos = null;
        BufferedOutputStream out = null;
        FileChannel fc = null;
        try{
            fos = new FileOutputStream(file);
            out = new BufferedOutputStream( fos );
            fc = fos.getChannel();

            byte[] zero = new byte[40];
            int headerSize = 20;
            int entrySize = 40;
            // header : TLK V3.0
            out.write( TLKHEADER );
            out.write( TLKVERSION );
            // language id ( UINT32 )
            writeIntLE(out, language.getCode());
            // number of entries
            writeIntLE(out, tlkEntries.size());
            // starting position for entries
            int start = tlkEntries.size() * entrySize + headerSize;
            writeIntLE(out, start);
            out.flush();

            int posFromStart = 0;
            int[] stringSizes = new int[ tlkEntries.size() ];
            fc.position( start );
            // write strings & store byte[] sizes

            for (int i = 0; i < tlkEntries.size(); i++) {
                final TlkEntry entry = tlkEntries.get(i);
                byte[] bytes = entry.getString().getBytes(
                        language.getEncoding() );
                stringSizes[i] = bytes.length;
                out.write(bytes);
            }

            // write index entries

            out.flush();
            fc.position( headerSize );
            for (int i = 0; i < tlkEntries.size(); i++) {
                final TlkEntry entry = tlkEntries.get(i);

                out.write(entry.getFlags());
                out.write(zero, 0, 3);

                // write resName and fill with 0
                String resRef = entry.getSoundResRef();
                if ( resRef != null && resRef.length() > 0 ){
                    byte[] resRefBytes =
                            entry.getSoundResRef().getBytes("ASCII");
                    int resRefLength = Math.min(resRefBytes.length, 16);
                    out.write(resRefBytes,0,resRefLength);
                    out.write(zero, 0, 16 - resRefLength);
                }
                else
                    out.write(zero, 0, 16);

                // 8 bytes sound stuff, unused ( always 0 )
                out.write(zero, 0, 8);

                // relative position of entry, used only if length > 0
                if ( entry.getString().length() > 0 ) {
                    writeIntLE(out, posFromStart);
                    posFromStart += stringSizes[i];
                } else
                    out.write(zero, 0, 4);

                // length of entry
                writeIntLE(out, stringSizes[i]);

                //float ( sound length )
                if ( entry.getSoundLength() != 0 )
                    writeIntLE( out, Float.floatToIntBits( entry.getSoundLength() ) );
                else out.write(zero, 0, 4);
            }
            out.flush();
        } finally {
            if ( out != null ) out.close();
            if ( fc != null ) fc.close();
            if ( fos != null ) fos.close();
        }
    }


    private static void writeIntLE( OutputStream out, int i)
    throws IOException {
        out.write(i & 255);
        out.write((i >> 8) & 255);
        out.write((i >> 16) & 255);
        out.write((i >> 24) & 255);
    }

    private static void writeIntLE( DataOutput out, int i)
    throws IOException {
        out.write(i & 255);
        out.write((i >> 8) & 255);
        out.write((i >> 16) & 255);
        out.write((i >> 24) & 255);
    }

    /* reads an int value stored in little endian byte order, starting at current file pointer */
    private static int readIntLE( DataInput raf ) throws IOException {
        return raf.readUnsignedByte()
        | (raf.readUnsignedByte() << 8)
        | (raf.readUnsignedByte() << 16)
        | (raf.readUnsignedByte() << 24);
    }

        /*
                loads file into memory using a MappedByteBuffer
                might also throw a number of runtime exceptions ( java.nio.BufferUnderflowException,
                java.lang.IndexOutOfBoundsException or java.lang.IllegalArgumentException )
                if the input file is not a tlk file.
    private void load() throws IOException {
        load( null );
    }
         */

    // loads the file through an inputstream
    private void load2( Version v, InputStream inputStream, final ProgressMonitor pm ) throws IOException{
        long time = System.currentTimeMillis();
        long streamSize = inputStream.available();

        BufferedInputStream bis = new BufferedInputStream( inputStream, 16384 );
        byte[] indexEntryBytes = new byte[40];
        ByteBuffer mbb = ByteBuffer.wrap( indexEntryBytes );
        mbb.order( ByteOrder.LITTLE_ENDIAN );
        bis.read( indexEntryBytes, 0, 20 );

        for ( int i = 0; i < 4; i++ ){
            if ( indexEntryBytes[i] != TLKHEADER[i] ){
                System.err.println("not a tlk file !");
                throw new IllegalArgumentException("not a tlk file !");
            }
        }
        for ( int i = 0; i < 4; i++ ){
            if ( indexEntryBytes[4+i] != TLKVERSION[i] ){
                System.err.println("wrong tlk version ! "+new String(indexEntryBytes,4,4) );
                throw new IllegalArgumentException("wrong tlk file version");
            }
        }

        mbb.position(8);
        language = NwnLanguage.find( v, mbb.getInt() );
        //System.out.println( "TlkContent : " + language );
        int entries = mbb.getInt();
        int stringDataStart = mbb.getInt();

        if ( pm != null ){
            pm.setMinimum( 0 );
            pm.setMaximum( entries );
            //pm.setNote("reading tlk index");
        }

        tlkEntries = new ArrayList<>( entries );

        int[] stringSizes = new int[entries];
        int maxStringSize = 0;

        // reading index entries & create tlk entries
        for ( int i = 0; i < entries; i++ ){
            bis.read( indexEntryBytes );

            TlkEntry e = new TlkEntry();
            e.setFlags( indexEntryBytes[0] );
            if ( indexEntryBytes[4] != 0 )
                e.setSoundResRef( new String( indexEntryBytes, 4, 16 ).trim() );
            mbb.position( 32 );
            int stringSize = mbb.getInt();
            stringSizes[i] = stringSize;
            maxStringSize = maxStringSize < stringSize ? stringSize :  maxStringSize;
            e.setSoundLength( mbb.getFloat() );
            tlkEntries.add( e );
        }

        int skip = stringDataStart - ( 0x14 + (40*entries) );
        if ( skip != 0 ){
            System.out.println( "unused bytes between index and string data ?!? : " + skip );
            if ( skip > 0 )
                bis.skip(skip);
        }

        // read string data
        byte[] strBuf = new byte[maxStringSize];

        if ( pm != null ){
            //pm.setNote("reading strings");
        }

        Charset charset = Charset.forName(language.getEncoding());
        CharsetDecoder decoder = charset.newDecoder();
        CharBuffer cbuf = CharBuffer.allocate((int)Math.ceil(maxStringSize * decoder.maxCharsPerByte()));
        ByteBuffer bb = ByteBuffer.wrap(strBuf);

        for ( int i = 0; i < entries; i++ ){
            bis.read( strBuf, 0, stringSizes[i] );
            bb.rewind();
            bb.limit(stringSizes[i]);
            cbuf.limit(cbuf.capacity());
            decoder.reset();
            decoder.decode(bb, cbuf, true);
            decoder.flush(cbuf);
            cbuf.flip();
            get(i).setString( cbuf.toString() );
            if ( pm!=null ){
                if ( pm.isCanceled() )
                    break;
                pm.setProgress( i );
            }
        }
        if ( pm!=null && !pm.isCanceled() )
            pm.setProgress( pm.getMaximum() );
        bis.close();
        inputStream.close();
        System.out.printf("loaded %d entries (%d KB) in %d ms\n", entries, streamSize/1024, System.currentTimeMillis()-time);
    }

    /*
    private void load( final ProgressMonitor pm ) throws IOException {
        tlkEntries = new Vector();
        FileInputStream in = new FileInputStream(file);
        FileChannel fc = in.getChannel();
        MappedByteBuffer mbb =
                fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
        mbb.load();
        mbb.order(ByteOrder.LITTLE_ENDIAN);
        mbb.position(8);

        language = NwnLanguage.forCode( mbb.getInt() );
        //System.out.println( "TlkContent : " + language );
        int entries = mbb.getInt();
        tlkEntries = new Vector( entries );
        int start = mbb.getInt();
        // System.out.println( entries + ", " + start );
        int indexPos = 0;
        int contentPos = 0;
        int contentSize = 0;
        TlkEntry entry;
        byte type = 0;
        String content = "";
        String soundResRef = "";
        float soundLength = 0;

        if ( pm != null ){
            pm.setMinimum( 0 );
            pm.setMaximum( entries );
        }

        byte[] buf = new byte[200000];
        for ( int count=0; count < entries; count++ ) {
            if ( pm != null && ( count & 255 ) == 0 ) pm.setProgress( count );
            entry = new TlkEntry();
            indexPos = count * 40 + 0x14;
            // get Type
            type = mbb.get(indexPos);
            // read resource name
            mbb.position(indexPos + 4);
            mbb.get(buf, 0, 16);
            if (buf[0] != 0)
                soundResRef = (new String(buf, 0, 16)).trim();
            else
                soundResRef = "";
            // 8 byte sound stuff ( unused ), skip
            mbb.position(indexPos + 28);

            // 4 byte offset
            contentPos = mbb.getInt();
            // 4 byte length ( size )
            contentSize = mbb.getInt();
            //if ( ( contentSize == 0 ) && ( contentPos != 0 ) )
            //    System.out.println( "offset used for 0-length entry at " + Integer.toHexString( mbb.position() - 8 ) );
            // 4 bytes float : sound length
            soundLength = mbb.getFloat();
            // entry
            mbb.position(contentPos + start);
            mbb.get(buf, 0, contentSize);
            content = new String(buf, 0, contentSize, language.getEncoding() );

            entry = new TlkEntry( type, content, soundResRef, soundLength );
            tlkEntries.add(entry);
            // System.out.println( count + " : " + entry.resName + " : " + entry.content );
        }
        //mbb.clear();
        if ( pm != null ) pm.close();
        mbb = null;
        fc.close();
        in.close();
    }
     */

    public void writeDiff( File file, int[] selection ) throws IOException{
        //System.out.println("writing diff");
        RandomAccessFile raf = new RandomAccessFile( file, "rw" );
        // write placeholder for number of entries
        raf.writeInt( 0 );
        int size = 0;
        TlkEntry e = null;
        for ( int i = 0; i < selection.length; i++ ){
            e = ( TlkEntry ) tlkEntries.get(selection[i]);
            size++;
            System.out.print(".");
            raf.writeInt( selection[i] );
            e.writeEntry( raf );
        }
        System.out.println();
        //	write number of entries
        raf.seek( 0 );
        raf.writeInt( size );
        raf.close();
    }

    public int[] mergeDiff( File file ) throws IOException{
        FileInputStream fis = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fis );
        int entries = dis.readInt();
        int[] positions = new int[entries];
        int pos = 0;
        for ( int i = 0; i < entries; i++ ){
            pos = dis.readInt();

            TlkEntry e = new TlkEntry( dis );
            set( pos,  e );
            positions[i] = pos;
        }
        dis.close();
        fis.close();
        return positions;
    }

    public int[] mergeDtu( File file ) throws IOException{
        FileInputStream fis = new FileInputStream( file );
        DataInputStream dis = new DataInputStream( fis );
        byte[] buf = new byte[ 200000 ];
        int entries = readIntLE( dis );
        int[] positions = new int[entries];
        for ( int i = 0; i < entries; i++ ){
            TlkEntry e = new TlkEntry();
            int pos = readIntLE( dis );
            byte resRefSize = dis.readByte();
            if ( resRefSize > 0 ){
                dis.read( buf, 0, resRefSize );
                e.setSoundResRef(new String( buf, 0, resRefSize ));
            }
            int contentSize = dis.read();
            if ( contentSize == 255 )
                contentSize = dis.read() | ( dis.read() << 8 );
            if ( contentSize > 0 ){
                dis.read( buf, 0, contentSize );
                e.setString(new String( buf, 0, contentSize ));
            }
            set( pos, e );
            positions[i] = pos;
        }
        dis.close();
        fis.close();
        return positions;
    }

    public static void main( String[] args ) throws Exception{
        //long now = System.currentTimeMillis();
        //TlkContent c = new TlkContent( new File(args[0]), new ProgressMonitor(null, "foo", "bar", 0, 1 ) );
        //TlkContent c = new TlkContent( new File("/windows/e/spiele/NeverwinterNights/nwn/dialog.tlk") );
        //TlkContent c = new TlkContent( new File(args[0]) );
        TlkContent c = new DefaultTlkReader(Version.getDefaultVersion()).load(new File(args[0]), null);
        c.saveAs(new File(args[0]+".foo"), Version.getDefaultVersion());
        //TlkContent c = new TlkContent( new FileInputStream(args[0]) );
        //long then =  System.currentTimeMillis();
        //System.out.println( "File " + args[0] + ", " + c.size()  + " entries, loaded in " + (then-now)/1000.0f + "s" );
                /*
                FileInputStream is1 = new FileInputStream( new File( args[0]) );
                FileInputStream is2 = new FileInputStream( new File( args[1]) );
                int pos = 0;
                int b1 = 0, b2 = 0;
                while ( b1 != -1 && b2 != -1 ){
                        if ( ( b1 = is1.read() ) != ( b2 = is2.read() ) ){
                                System.out.println( Integer.toHexString( pos ) );
                        }
                        pos++;
                }
                 */
    }

    public NwnLanguage getLanguage() {
        return language;
    }

    public void setLanguage(NwnLanguage language) {
        this.language = language;
    }
}
