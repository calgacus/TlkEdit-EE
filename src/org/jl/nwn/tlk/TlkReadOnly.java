/*
 * TlkReadOnly.java
 */
package org.jl.nwn.tlk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;
/**
 * read only tlk table using a memory mapped file
 *
 */
public class TlkReadOnly {
    
    LinkedHashMap<Integer, String> cache;
    ByteBuffer index;
    ByteBuffer stringBytes;
    int size;
    NwnLanguage lang;
    Version nwnVersion;
    
    FileInputStream fis;
    FileChannel fc;
    
    /** Creates a new instance of TlkReadOnly */
    public TlkReadOnly(
            File file,
            final int cacheSize,
            Version nwnVersion ) throws IOException{
        this.nwnVersion = nwnVersion;
        fis = new FileInputStream( file );
        fc = fis.getChannel();
        ByteBuffer header = ByteBuffer.allocate(20);
        header.order( ByteOrder.LITTLE_ENDIAN );
        fc.read(header);
        
        String fileHeader = new String(header.array(),0,8);
        if ( !fileHeader.startsWith("TLK") )
            throw new IOException( "Error : not a tlk file " );
        if ( !fileHeader.equals("TLK V3.0") )
            throw new IOException( "Error : wrong tlk file version : "
                    + fileHeader );
        
        header.position(8);
        lang = NwnLanguage.find( nwnVersion, header.getInt() );
        size = header.getInt();
        //System.out.println("size : "+size);
        int stringDataStart = header.getInt();
        //System.out.println("allocating index");
        //index = ByteBuffer.allocate( 40 * size );
        index = fc.map( FileChannel.MapMode.READ_ONLY, 20, 40*size );
        index.order( ByteOrder.LITTLE_ENDIAN );
        //fc.read(index, 20);
        //System.out.println("mapping file");
        stringBytes = fc.map(
                FileChannel.MapMode.READ_ONLY,
                stringDataStart,
                fc.size() - stringDataStart );
        //System.out.println("creating cache");
        cache = new LinkedHashMap<Integer, String>(cacheSize, 0.75f, true){
            protected boolean removeEldestEntry(
                    java.util.Map.Entry<Integer, String> eldest) {
                return size() > cacheSize;
            }
        };
        fis.close();
        fc.close();
    }
    
    public TlkReadOnly( File file, final int cacheSize ) throws IOException{
        this( file, 1000, Version.getDefaultVersion() );
    }
    
    public TlkReadOnly( File file ) throws IOException{
        this(file, 1000);
    }
    
    public String getString( int strRef ){
        //System.out.println("getString : " + strRef);
        String s;
        synchronized( cache ){
            s = cache.get(strRef);
            if ( s != null )
                return s;
        }
        int offset, length;
        synchronized( index ){
            index.position( 28 + (strRef * 40) );
            offset = index.getInt();
            length = index.getInt();
        }
        //System.out.println("offset : "+offset);
        //System.out.println("length : "+length);
        byte[] bytes = new byte[length];
        synchronized( stringBytes ){
            stringBytes.position(offset);
            stringBytes.get(bytes);
        }
        try{
            s = new String( bytes, lang.getEncoding() );
        } catch ( UnsupportedEncodingException uee ){
            // should not happen
            throw new Error("Java Runtime doesn't support encoding : "
                    + lang.getEncoding(), uee);
        }
        synchronized ( cache ){
            cache.put( strRef, s );
        }
        return s;
    }
    
    public int size(){
        return size;
    }
    
    public static void main( String ... args ) throws Exception{
        if ( args.length == 0 )
            args = new String[]{"/usr/local/neverwinter/dialog.v130", "43", "42"};
        File f = new File( args[0] );
        long start = System.currentTimeMillis();
        TlkReadOnly tlk = new TlkReadOnly( f, 1000, Version.getDefaultVersion() );
        
        for ( int i = 1; i < args.length; i++ )
            System.out.println(tlk.getString(Integer.parseInt(args[i])));
        //System.out.printf("time : %dms", System.currentTimeMillis()-start);
    }
    
}
