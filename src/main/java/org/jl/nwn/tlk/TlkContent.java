package org.jl.nwn.tlk;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
        // use BufferedOutputStream for performance and FileChannel to set the write position
        try (final FileOutputStream fos = new FileOutputStream(file);
             final FileChannel fc = fos.getChannel();
             final BufferedOutputStream out = new BufferedOutputStream( fos )
        ) {
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
        }
    }

    private static void writeIntLE(OutputStream out, int i) throws IOException {
        out.write(i & 255);
        out.write((i >> 8) & 255);
        out.write((i >> 16) & 255);
        out.write((i >> 24) & 255);
    }

    /** Reads an int value stored in little endian byte order, starting at current file pointer. */
    private static int readIntLE( DataInput raf ) throws IOException {
        return raf.readUnsignedByte()
            | (raf.readUnsignedByte() << 8)
            | (raf.readUnsignedByte() << 16)
            | (raf.readUnsignedByte() << 24);
    }

    public void writeDiff( File file, int[] selection ) throws IOException{
        try (final RandomAccessFile raf = new RandomAccessFile( file, "rw" )) {
            // write placeholder for number of entries
            raf.writeInt( 0 );
            int size = 0;
            for ( int i = 0; i < selection.length; i++ ){
                final TlkEntry e = tlkEntries.get(selection[i]);
                size++;
                System.out.print(".");
                raf.writeInt( selection[i] );
                e.writeEntry( raf );
            }
            System.out.println();
            //	write number of entries
            raf.seek( 0 );
            raf.writeInt( size );
        }
    }

    public int[] mergeDiff( File file ) throws IOException{
        try (final FileInputStream fis = new FileInputStream( file );
             final DataInputStream dis = new DataInputStream( fis )
        ) {
            final int entries = dis.readInt();
            final int[] positions = new int[entries];
            for ( int i = 0; i < entries; i++ ){
                final int pos = dis.readInt();

                TlkEntry e = new TlkEntry( dis );
                set( pos,  e );
                positions[i] = pos;
            }
            return positions;
        }
    }

    public int[] mergeDtu( File file ) throws IOException{
        try (final FileInputStream fis = new FileInputStream( file );
             final DataInputStream dis = new DataInputStream( fis )
        ) {
            final byte[] buf = new byte[ 200000 ];
            final int entries = readIntLE( dis );
            final int[] positions = new int[entries];
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
            return positions;
        }
    }

    public NwnLanguage getLanguage() {
        return language;
    }

    public void setLanguage(NwnLanguage language) {
        this.language = language;
    }
}
