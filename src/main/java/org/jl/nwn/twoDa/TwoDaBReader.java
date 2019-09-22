package org.jl.nwn.twoDa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 *Utility class for reading a TwoDaObject from a 2DA binary file ( 2DA V2.b )
 *
 *<pre>
 *2DA V2.b file format :
 *"2DA V2.b" 0x0a             // 8 bytes header + 0x0a
 *({column header}0x09)*      // column header strings, each terminated by 0x09
 *0x00                        // terminates column headers
 *{row count}                 // 4-bytes unsigned integer : number of rows
 *({row header}0x09)*         // {row count} row header strings, each terminated by 0x09
 *{index block}               // index block, {row count}*{column count} 2-byte unsigned integer values
 *{data block size}           // 2-bytes unsigned integer : size of data block in bytes
 *{data block}                // list of 0-terminated strings
 *EOF
 *</pre>
 *The index block represents the 2DA table. Each entry is an offset into the data block
 *pointing to a 0-terminated string. {row count} consecutive values represent one row.
 */
public class TwoDaBReader {

    private static String readStringTerminated(InputStream is, int term) throws IOException{
        StringBuilder sb = new StringBuilder();
        int b = 0;
        while( (b=is.read())!=term && b!=-1 )
            sb.append((char)b);
        return b != -1 ? sb.toString() : null;
    }

    private static String readString0(byte[] buffer, int offset){
        StringBuilder sb = new StringBuilder();
        int b = 0;
        while( (b=buffer[offset++])!=0 )
            sb.append((char)b);
        return sb.toString();
    }

    private static int readIntLE(InputStream in) throws IOException{
        return in.read() | in.read() << 8 | in.read() << 16 | in.read() << 24;
    }

    /**
     @param withHeader try to parse first 9 bytes as header, else assume the inputstream is already at position 9.
     */
    public static TwoDaTable readTwoDaBinary( InputStream is, boolean withHeader ) throws IOException{
        PushbackInputStream pbis = new PushbackInputStream(is);
        if ( withHeader ){
            //String header = "2DA V2.b";
            String s = readStringTerminated(pbis,0x0a);
            if ( s == null || !s.equals("2DA V2.b") )
                throw new IOException("Not a 2DA file !");
            //pbis.skip(8);
            //pbis.read(); // ???
        }
        final List<String> headers = new ArrayList<>();
        while ( true ){
            String s = readStringTerminated(pbis,9);
            headers.add(s);
            int b = pbis.read();
            if ( b==0 )
                break;
            else
                pbis.unread(b);
        }
        //System.out.println(headers);
        int columns = headers.size();
        headers.add(0," ");
        TwoDaTable twoDa = new TwoDaTable(headers.toArray(new String[headers.size()]));

        int rows = readIntLE(pbis);
        //System.out.printf("size : %s x %s\n", rows, headers.size());

        final List<String> rowHeaders = new ArrayList<>();
        for ( int i = 0; i < rows; i++ )
            rowHeaders.add(readStringTerminated(pbis,9));
        int indexSize = rows*columns*2;
        byte[] index = new byte[indexSize];
        pbis.read(index);
        ByteBuffer bb = ByteBuffer.wrap(index);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int dataSize = pbis.read() | pbis.read()<<8; // unsigned short
        byte[] data = new byte[dataSize];
        pbis.read(data);

        for ( int i = 0; i < rows; i++ ){
            String[] row = new String[twoDa.getColumnCount()];
            row[0] = rowHeaders.get(i);
            for ( int j = 0; j < columns; j++ ){
                //int offset = index[pIndex++] | (index[pIndex++]<<8);
                String s = readString0(data,bb.getChar()).trim();
                if ( s.length() == 0 )
                    s = "****"; // ???
                row[j+1] = s;
            }
            twoDa.appendRow(row);
        }
        //twoDa.write(System.out);
        return twoDa;
    }

    public static TwoDaTable readTwoDaBinary( File f ) throws IOException{
        return readTwoDaBinary(new FileInputStream(f), true);
    }

    public static TwoDaTable readTwoDaBinary( String filename ) throws IOException{
        return readTwoDaBinary(new FileInputStream(filename), true);
    }

    public static void main( String ... args ) throws IOException{
        TwoDaTable twoDa = TwoDaBReader.readTwoDaBinary(args[0]);
        twoDa.write(System.out);
    }
}
