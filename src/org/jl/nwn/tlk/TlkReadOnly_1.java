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
/**
 * read only tlk table using a memory mapped file
 * for tlk version 1
 * index format :
 * 4 byte : int : offset
 * 4 byte : int : length
 * 2 byte : ??? : ? 03 00
 *16 byte : string, (0-terminated ?)
 */
public class TlkReadOnly_1 {
    
    LinkedHashMap<Integer, String> cache;
    ByteBuffer index;
    ByteBuffer stringBytes;
    int size;
    NwnLanguage lang;
    
    FileInputStream fis;
    FileChannel fc;
    
    /** Creates a new instance of TlkReadOnly */
    public TlkReadOnly_1( File file, final int cacheSize ) throws IOException{
        fis = new FileInputStream( file );
        fc = fis.getChannel();
        ByteBuffer header = ByteBuffer.allocate(0x24);
        header.order( ByteOrder.LITTLE_ENDIAN );
        fc.read(header);
        
        String fileHeader = new String(header.array(),0,8);
        if ( !fileHeader.startsWith("TLK") )
            throw new IOException( "Error : not a tlk file " );
        if ( !fileHeader.startsWith("TLK V1  ") )
            throw new IOException( "Error : wrong tlk file version : " + fileHeader );
        
        header.position(8);
        //lang = NwnLanguage.forCode( header.getInt() );
        header.getShort(); // ???
        lang = NwnLanguage.GERMAN;
        size = header.getInt();
        //System.out.println("size : "+size);
        int stringDataOffset = header.getInt();

        index = fc.map( FileChannel.MapMode.READ_ONLY, 0x24, 0x1A*size );
        index.order( ByteOrder.LITTLE_ENDIAN );

        stringBytes = fc.map( FileChannel.MapMode.READ_ONLY, stringDataOffset, fc.size() - stringDataOffset );

        cache = new LinkedHashMap<Integer, String>(cacheSize, 0.75f, true){
            protected boolean removeEldestEntry(java.util.Map.Entry<Integer, String> eldest) {
                return size() > cacheSize;
            }            
        };
        fis.close();
        fc.close();
    }
    
    public String getString( int strRef ){
        //System.out.println("getString : " + strRef);
        String s;
        s = cache.get(strRef);
        if ( s != null )
            return s;
        index.position( (strRef * 0x1A) );
        int offset = index.getInt();
        int length = index.getInt();
        //System.out.println("offset : "+offset);
        //System.out.println("length : "+length);
        stringBytes.position(offset);
        byte[] bytes = new byte[length];
        stringBytes.get(bytes);
        try{
            s = new String( bytes, lang.getEncoding() );
        } catch ( UnsupportedEncodingException uee ){
            // should not happen
            throw new Error("Java Runtime doesn't support encoding : " + lang.getEncoding(), uee);
        }
        cache.put( strRef, s );
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
        TlkReadOnly_1 tlk = new TlkReadOnly_1( f, 1000 );
        
        for ( int i = 1; i < args.length; i++ )
            System.out.println("tlkreadonly 107 "+tlk.getString(Integer.parseInt(args[i])));
        //System.out.printf("time : %dms\n", System.currentTimeMillis()-start);
    }
    
}
