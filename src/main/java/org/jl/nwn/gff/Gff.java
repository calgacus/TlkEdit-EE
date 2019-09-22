/*
 * Gff.java
 *
 * Created on 7. Mai 2005, 11:07
 */

package org.jl.nwn.gff;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jl.nwn.Version;

/**
 * Constant definitions and utility methods for GFF.
 */
public class Gff {

    public static final byte BYTE = 0;
    public static final byte CHAR = 1;
    public static final byte WORD = 2;
    public static final byte SHORT = 3;
    public static final byte DWORD = 4;
    public static final byte INT = 5;
    public static final byte DWORD64 = 6;
    public static final byte INT64 = 7;

    public static final byte FLOAT = 8;
    public static final byte DOUBLE = 9;

    public static final byte CEXOSTRING = 10;
    public static final byte RESREF = 11;
    public static final byte CEXOLOCSTRING = 12;

    public static final byte VOID = 13;

    public static final byte STRUCT = 14;
    public static final byte LIST = 15;

    public static final byte VECTOR = 17;

    public static final int STRUCT_ID_TOPLEVEL = -1;

    public static final int GENDER_MALE = 0;
    public static final int GENDER_FEMALE = 1;

    private static final String[] typeNames = {
        "Byte", "Char", "Word", "Short", "DWord", "Int", "DWord64", "Int64",
        "Float", "Double",
        "CExoString", "CResRef", "CExoLocString",
        "Void",
        "Struct", "List", null, "Vector"
    };

    public static final List TYPENAMES = Collections.unmodifiableList(Arrays.asList(typeNames));

    private static final String[] gffFileTypes = {
        "ifo",
        "are", "git", "gic",
        "utc", "utd", "ute", "uti", "utp", "uts", "utm", "utt", "utw",
        "dlg",
        "jrl",
        "fac",
        "itp",
        "ptm", "ptt",
        "bic",
        "gff",
        "gui",
        "ros"
    };

    public static final List GFFTYPES;

    static {
        Arrays.sort(gffFileTypes);
        GFFTYPES = Collections.unmodifiableList(Arrays.asList(gffFileTypes));
    }

    private static final BigInteger INT64_SIGNBIT1= BigInteger.ZERO.setBit( 63 );

    public static String getTypeName( int type ){
        if ( type > -1 && type < 18 )
            return typeNames[type];
        else
            return "unknown : " + type;
    }

    public static boolean isComplexType(int type){
        return !( type < 6 || type == 8 );
    }

    /**
     * converts the given BigInteger value to the byte representation used in
     * binary gff files, the length of the returned array will be either 4 or 8,
     * and the array can be written directly to the file
     */
    public static byte[] bigInt2raw( BigInteger value, byte type, byte[] data ) {
        //byte[] data = null;
        if ( data == null )
            data = ( type==INT64 || type==DWORD64 )? new byte[8] : new byte[4];
        else
            Arrays.fill(data, (byte)0);
        switch ( type ){
            case BYTE :
            case CHAR : {
                data[0] = (byte) (value.intValue() & 255);
                return data;
            }
            case WORD : {
                long l = value.longValue();
                for ( int i = 0; i < 2; i++ ){
                    data[i] = (byte) ( l & 255 );
                    l = l >> 8;
                }
                return data;
            }
            case SHORT :{
                short s = value.shortValue();
                data[0] = (byte)(s & 255);
                data[1] = (byte)(s>>8 & 255);
                return data;
            }
            case DWORD : ;
            case INT : ;
            int v = value.intValue();
            for ( int i = 0; i < data.length; i++ ){
                data[i] = (byte) ( v & 255 );
                v = (v >> 8);
            }
            return data;
            case DWORD64 :{
                ByteBuffer b = ByteBuffer.wrap(data);
                b.order( ByteOrder.LITTLE_ENDIAN );
                byte[] dwordbytes = value.toByteArray();
                flip( dwordbytes );
                b.put( dwordbytes, 0, Math.min(8, dwordbytes.length) );
                return data;
            }
            case INT64 :{
                ByteBuffer b = ByteBuffer.wrap(data);
                byte[] bytes;
                if ( value.signum() == -1 ){
                    BigInteger r = value.add( INT64_SIGNBIT1 );
                    r = r.setBit( 63 );
                    bytes = r.toByteArray();

                } else {
                    bytes = value.toByteArray();
                }
                flip( bytes );
                b.put( bytes, 0, Math.min(8, bytes.length) );
                return data;
            }
            default : {
                throw new Error( "GffInteger : unknown or unsupported type : " + type );
            }
        }
    }

    private static void flip( byte[] b ){
        byte swap = 0;
        for ( int i = 0; i < b.length / 2; i++ ){
            swap = b[i];
            b[i] = b[b.length-1-i];
            b[b.length-1-i] = swap;
        }
    }

    public static BigInteger bytes2BigInt( int type, byte[] b ){
        if ( type == SHORT ){
            return new BigInteger( new byte[]{ b[1],b[0] } );
        }
        // CHAR is signed !
        else if ( type == CHAR )
            return BigInteger.valueOf( b[0] );
        else if ( type == INT || type == INT64 ){
            flip(b);
            return new BigInteger( b );
        } else if ( type == WORD || type == BYTE || type == DWORD || type == DWORD64 ){
            flip( b );
            return new BigInteger( 1, b );
        }
        throw new IllegalArgumentException("given type is not a gff decimal type : " + type );
    }

    /**
     * tests whether the given string is a known gff file type
     * @param type a string of length 3
     * @return true if given type is known
     * @throws IllegalArgumentException if argument length != 3
     * */
    public static boolean isKnownGffFileType( String type ) throws IllegalArgumentException{
        if ( type.length() != 3 )
            throw new IllegalArgumentException( "invalid type string : " + type );
        return Arrays.binarySearch( gffFileTypes, type, String.CASE_INSENSITIVE_ORDER ) > -1;
    }

    /**
     * Encoding of CExoStrings probably depends on game version ( or maybe not )
     * @return 'UTF-8' for NWN2 and 'windows-1252' for NWN1
     */
    public static String getCExoStringEncoding(Version v){
        switch(v){
            case NWN1 : return "windows-1252";
            case NWN2 : return "UTF-8";
            default : return "UTF-8";
        }
    }
    /*
    protected static final Pattern labelPattern = Pattern.compile("[A-Za-z0-9_]{0,16}");
    protected static final Matcher labelMatcher = labelPattern.matcher("");
     * tests whether the given string is a valid gff field label
     * @return s, if it is a valid label
     * @throws ParseException, if the argument is not a valid label
     * @throws IllegalArgumentException if s is null
    public static String parseLabel( String s ) throws ParseException, IllegalArgumentException{
        if ( s==null )
            throw new IllegalArgumentException("argument is null");
        else if ( s.length() > 16 )
            throw new ParseException( "string too long",  s.length() );
        else if ( s.length() == 0 )
            throw new ParseException( "empty string",  0 );
        if ( labelMatcher.reset(s).matches() )
            return s;
        else
            throw new ParseException("illegal label : " + s, 0);
    }
     */
}
