package org.jl.nwn.tlk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.List;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

/*
 	( note : all integer values are in little endian byte order )

	header :
		8 byte : Version etc
		4 byte UINT32 : language ID ( see TlkContent )
		4 byte UINT32 : number of entries
		4 byte UINT32 : start offset ( position of 1st entry within the tlk file, should be = 40*number of entries + 20 )
	header size : 20 byte

	( offset 0x14 : first index entry )

	index entry :
		 4 byte type ??? ( ( 00 | 07 | 06 | 01 ) 00 00 00 ? )
		 		( type 0 and 6 entries are completely empty, type 7 use sound resref, type 1 is simple text )
		 16 byte string resource name
		 8 byte ??? ( always 00 )
		 				( supposedly sound settings. but i haven't seen it
		 					used anywhere - currently ignored ! )
		 4 byte UINT32 : position of the entry relative to start offset, used only if size > 0
		 	( since patch 1.30 some 0-length entries use offset entries anyway )
		 4 byte UINT32 : size of entry
		 4 byte ???
	index entry size : 40 byte
*/

public class TlkTool{

	public final static int TLKAPPEND = -1;
/*
	public static final int LANGUAGE_KOREAN = 128;
	public static final int LANGUAGE_CHINESE_TRADITIONAL = 129;
	public static final int LANGUAGE_CHINESE_SIMPLIFIED = 130;
	public static final int LANGUAGE_JAPANESE = 131;
*/
	public static void writeTlkFile( List tlkList, File file ) throws IOException{
		writeTlkFile( tlkList, file, NwnLanguage.ENGLISH );
	}

	public static void writeTlkFile( List tlkList, File file, NwnLanguage lang ) throws IOException{
		( new TlkContent( tlkList, lang ) ).saveAs( file, Version.getDefaultVersion() );
	}

	// returns number of entries in tlk file
	public static int getTlkFileSize( File file ) throws IOException{
		RandomAccessFile raf = new RandomAccessFile( file, "r" );
		raf.seek( 0x0c );
		int entries = readIntLE( raf );
		raf.close();
		return entries;
	}

	public static int getTlkFileLanguage( File file ) throws IOException{
		RandomAccessFile raf = new RandomAccessFile( file, "r" );
		raf.seek( 0x08 );
		int language = raf.readByte();
		raf.close();
		return language;
	}

	public static void setTlkFileLanguage( File file, int lang ) throws IOException{
		RandomAccessFile raf = new RandomAccessFile( file, "rw" );
		raf.seek( 0x08 );
		raf.write( lang );
		raf.close();
	}


	/* reads an int value stored in little endian byte order, starting at current file pointer */
	private static int readIntLE( RandomAccessFile raf ) throws IOException{
		return	raf.readUnsignedByte() |
			(raf.readUnsignedByte() << 8)  |
			(raf.readUnsignedByte() << 16) |
			(raf.readUnsignedByte() << 24);
	}

	private static int readIntLE( InputStream raf ) throws IOException{
			return	raf.read() |
				(raf.read() << 8)  |
				(raf.read() << 16) |
				(raf.read() << 24);
		}

	/*
	public static List readTlkFileMapped( File tlkFile ) throws IOException{
		return ( new TlkContent( tlkFile ) ).getEntries();
	}
	*/

	private static void writeIntLE( FileOutputStream out, int i ) throws IOException{
		out.write( i & 255 );
		out.write( (i >> 8) & 255 );
		out.write( (i >> 16) & 255 );
		out.write( (i >> 24) & 255 );
	}

	/* concatenate tlk files f1 and f2, output to File out.
	 * entries from f2 will start at position f2_start, use TlkTool.TLKAPPEND to
	 * append f2 directly at the end of f1
	 * language ID will be that of File f1
	*/
	public static void concat( File f1, File f2, File out, int f2_start ) throws IOException{
		TlkContent c1 = new DefaultTlkReader(Version.getDefaultVersion()).load( f1, null );
		TlkContent c2 = new DefaultTlkReader(Version.getDefaultVersion()).load( f2, null );;
		if ( f2_start != TlkTool.TLKAPPEND ){
			if ( f2_start < c1.size() )
				throw new IllegalArgumentException( "cannot append starting at position " + f2_start + ", size of 1st tlk file is " + c1.size() );
			for ( int i = 0; i < f2_start - c1.size(); i++ )
				c1.add( new TlkEntry() );
		}
		c1.addAll( c2 );
		c1.saveAs( out, Version.getDefaultVersion() );
	}

	public static void main( String args[] ) throws IOException {

		/*
		readTlkFileMapped( new File( args[0] ) );
		//writeTlkFile( readTlkFileMapped( new File( args[0] ) ), new File( "out.tlk" ) );
		//readTlkFile( new File( args[0] ) );
		System.exit( 0 );
		*/

		// System.out.println("..."); readTlkFile( new File( args[0] ) );
	/*
		String usage = "usage : \n" +
				"TlkTool -sublist <tlk file> <start index> [end index]\n" +
				"\twrite all entries from <start index> (inclusive) to [end index] (exclusive)(optional) to sublist.tlk\n" +
				"TlkTool -concat <input tlk file> <append tlk file> [start index]\n" +
				"\tconcatenates the two tlk files, writes output to sublist.tlk. in sublist.tlk, the entries contained in <append tlk file> start at position [start index]\n" +
				"TlkTool -size <tlk file>\n" +
				"\tprint number of entries in tlk file\n";

		if ( args.length == 0 ){
			System.out.println( usage );
			System.exit( 0 );
		}

		try {

		if ( args[0].equals( "-sublist" ) ){
			int start = Integer.parseInt( args[2] );
			int end = -1;
			if ( args.length == 4 )
				end = Integer.parseInt( args[3] );
			List tlkList = readTlkFile( new File( args[1] ) );
			if ( end == -1 ) end = tlkList.size();
			writeTlkFile( tlkList.subList( start, end ), new File( "sublist.tlk" ) );
		}
		else if ( args[0].equals( "-concat" ) ){
			File masterFile = new File( args[1] );
			File appendFile = new File( args[2] );
			int inputSize = getTlkFileSize( masterFile );
			int start = -1;
			if ( args.length == 4 )
				start = Integer.parseInt( args[3] );
			if ( start != -1 && start < inputSize ){
				System.out.println( "invalid starting position : " +start+ " ( size of input tlk file : " +inputSize+ " )" );
				System.exit( 0 );
			}
			List tlkList = readTlkFile( masterFile );
			List tlkApp = readTlkFile( appendFile );
			if ( start == -1 )
				start = tlkList.size();
			for ( int i = tlkList.size(); i < start; i++ )
				tlkList.add( new TlkEntry() );
			tlkList.addAll( start, tlkApp );
			writeTlkFile( tlkList, new File( "concat.tlk" ) );
		}
		else if ( args[0].equals( "-size" ) ){
			System.out.println( getTlkFileSize( new File( args[1] ) ) );
		}
		else System.out.println( usage );

		} catch ( NumberFormatException nfe ){
			System.out.println( "Not a number : " + nfe.getMessage() + "\n" + usage );
		}
*/
	}

}
