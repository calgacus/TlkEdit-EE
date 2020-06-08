package org.jl.nwn.gff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

public abstract class AbstractGffReader<Fld, Strct extends Fld, Lst extends Fld> {

    private ImageInputStream in;

    protected Version nwnVersion;

    private final List<Fld> fieldList = new ArrayList<>();
    private final List<Integer> listIndices= new ArrayList<>(); // indices of fields that are lists
    private final List<Strct> structList = new ArrayList<>();

    private final List<String> labelList = new ArrayList<>();

    private HashMap<Integer, Integer> dataPointers = new HashMap<>();

    // the file type string is always in upper case with length 4
    private String fileTypeString = "GFF ";

    private int structOffset = 0;
    private int structCount = 0;

    private int fieldOffset = 0;
    private int fieldCount = 0;

    private int labelOffset = 0;
    private int labelCount = 0;

    private int fieldDataOffset = 0;
    private int fieldDataCount = 0;

    private int fieldIndicesOffset = 0;
    private int fieldIndicesCount = 0;

    private int listIndicesOffset = 0;
    private int listIndicesCount = 0;

    final private byte[] buf = new byte[2*1024]; // max length of a CExoString
    /*
     * readField(int) will put struct fields in this map ( key = struct array position )
     * readStruct(int) will then use objects from this map
     */
    private final TreeMap<Integer, Strct> namedStructs = new TreeMap<>();

    /**
        @deprecated unsafe : relies on a correct default version.
    */
    @Deprecated
    public AbstractGffReader(){
        this( Version.getDefaultVersion() );
    };

    public AbstractGffReader(Version v){
        this.nwnVersion = v;
    };

    public Version getVersion(){
        return nwnVersion;
    }

    public Object load(File f) throws IOException{
        in = new FileImageInputStream(f);
        return doLoad(f);
    }

    public Object load(InputStream is) throws IOException{
            /* the jdk api doc says that FileCacheImageInputStream should be
             * preferred, but for small files MemoryCacheIIS seems to work just
             * fine ( and much faster ! )
             * MemoryCacheImageInputStream doesn't seem to do prebuffering, so
             * it's faster when used on a BufferedInputStream
             */
        in = new MemoryCacheImageInputStream(is);
        //in = new FileCacheImageInputStream(is, new File(System.getProperty("java.io.tmpdir")));
        return doLoad(null);
    }

    private Object doLoad(File file) throws IOException{

        fieldList.clear();
        listIndices.clear();
        structList.clear();
        labelList.clear();

        namedStructs.clear();
        dataPointers.clear();

        in.read( buf, 0, 8 );
        fileTypeString = new String( buf, 0, 4 );

        in.setByteOrder( ByteOrder.LITTLE_ENDIAN );
        structOffset = in.readInt();
        structCount = in.readInt();
        fieldOffset = in.readInt();
        fieldCount = in.readInt();
        labelOffset = in.readInt();
        labelCount = in.readInt();
        fieldDataOffset = in.readInt();
        fieldDataCount = in.readInt();
        fieldIndicesOffset = in.readInt();
        fieldIndicesCount = in.readInt();
        listIndicesOffset = in.readInt();
        listIndicesCount = in.readInt();


        in.seek( labelOffset );
        for ( int i = 0; i < labelCount; i++ ){
            in.read( buf, 0 , 16 );
            labelList.add( new String( buf, 0, 16 ).trim() );
        }

        in.seek( fieldOffset );
        ///System.out.printf("abstractGffReader  132,  fieldCount is %d\n ",fieldCount);
        for ( int i = 0; i < fieldCount; i++ ){
            System.out.printf("abstractGffReader  134,  i is %d\n ",i);
            try{
                readField( i );
            }catch(  Exception e) {
                System.err.println(
                        "abstractgffreader.java 138, this should not happen ! IOException"
                        + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        //System.out.printf("abstractGffReader  144");

        for ( int i = 0; i < structCount; i++ ){
           // System.out.printf("abstractGffReader  147,  i is %d\n ",i);
            try{
                Strct s = readStruct( i );
                structList.add(s  );
            }catch(  Exception e) {
                System.err.println(
                        "abstractgffreader.java 150, this should not happen ! Exception"
                        + e.getMessage());
                e.printStackTrace();
                throw e;
            }
        }
        //System.out.printf("abstractGffReader  150");  
        for ( int i : listIndices )
            fillList(i);
                /*
                for ( int i = 0; i < fieldList.size(); i++ ){
                        Fld field = (Fld) fieldList.get(i);
                        if ( isGffList(field) )
                                fillList(i);
                }
                 */
        in.close();
        Strct topLevelStruct = structList.get(0);
        fieldList.clear();
        structList.clear();
        labelList.clear();

        namedStructs.clear();
        dataPointers.clear();
        return mkGffObject(topLevelStruct, fileTypeString, file );
    }

    //public abstract boolean isGffList(Fld field);

    public abstract Fld mkInteger( String label, byte type, BigInteger value );

    public abstract Fld mkFloat( String label, float value );

    public abstract Fld mkDouble( String label, double value );

    public abstract Fld mkCExoString( String label, String value );

    public abstract Fld mkCExoLocString( String label, int strRef, int[] stringIDs, String[] strings );

    public abstract Fld mkCResRef( String label, String value );

    public abstract Strct mkStruct( String label, int structID );

    public abstract Lst mkList( String label );

    public abstract Fld mkVoid( String label, byte[] value );

    public abstract Fld mkVector( String label, float[] value );

    public abstract void listAdd( Lst list, Strct struct );

    public abstract void structAdd( Strct struct, Fld field );

    public abstract void structSetID( Strct struct, int ID );

    public abstract Object mkGffObject( Strct topLevelStruct, String gffType, File file );

    // read field at position pos
    private void readField( int pos ) throws IOException{
        in.seek( fieldOffset + pos*12 );
        int type = in.readInt();

        int labelIndex = in.readInt();
        String label = labelList.get( labelIndex );

        switch (type){
            case 0 : case 1 : case 2 : case 3 : case 4 : case 5 : {
                byte[] b4 = new byte[4];
                in.read( b4 );
                fieldList.add( mkInteger( label, (byte) type, Gff.bytes2BigInt(type,b4) ) );
                break;
            }
            case Gff.INT64 : case Gff.DWORD64 : {
                int dataPointer = in.readInt();

                in.seek( fieldDataOffset + dataPointer );
                byte[] b8 = new byte[8];
                in.read( b8 );
                fieldList.add( mkInteger( label, (byte) type, Gff.bytes2BigInt(type,b8) ) );
                break;
            }
            case Gff.FLOAT : {
                fieldList.add( mkFloat( label, in.readFloat() ) );
                break;
            }
            case Gff.DOUBLE :{
                int dataPointer = in.readInt();

                in.seek( fieldDataOffset + dataPointer );
                fieldList.add( mkDouble( label, in.readDouble() ) );
                break;
            }
            case Gff.CEXOSTRING :{
                int dataPointer = in.readInt();
                in.seek( fieldDataOffset + dataPointer );
                int length = in.readInt();
                
                in.read( buf, 0, length );
                String v =  new String(
                    buf, 0, length,
                    Gff.getCExoStringEncoding(nwnVersion)
                    );
                fieldList.add( mkCExoString(
                        label,
                        v
                        ) );
                break;
            }
            case Gff.RESREF :{
                int dataPointer = in.readInt();
                in.seek( fieldDataOffset + dataPointer );
                int length = in.readUnsignedByte();
                in.read( buf, 0, length );
                fieldList.add( mkCResRef(
                        label, new String( buf, 0, length )
                        ) );
                break;
            }
            case Gff.CEXOLOCSTRING :{
                int dataPointer = in.readInt();

                in.seek( fieldDataOffset + dataPointer );
                int totalSize = in.readInt();
                byte[] data = new byte[totalSize];
                in.read( data );
                // TODO
                //fieldList.add( new GffCExoLocString( label, data ) );

                ByteBuffer b = ByteBuffer.wrap(data);
                b.order(ByteOrder.LITTLE_ENDIAN);
                int strRef = b.getInt();
                int stringCount = b.getInt();
                String[] substrings = new String[stringCount];
                int[] stringIDs = new int[stringCount];

                for (int i = 0; i < stringCount; i++) {
                    int stringId = b.getInt();
                    int stringLength = b.getInt();
                    String s = "!!!-encoding error-!!!";

                    NwnLanguage lang = NwnLanguage.find( nwnVersion, stringId / 2);
                    try {
                        s = new String(
                            data,
                            b.position(),
                            stringLength,
                            lang.getEncoding());
                        b.position( b.position() + stringLength );
                    } catch (UnsupportedEncodingException e) {
                        System.err.println(
                                "abstractgffreader.java this should not happen ! UnsupportedEncodingException"
                                + e.getMessage());
                        e.printStackTrace();
                    }

                    substrings[i] = s;
                    stringIDs[i] = stringId;
                }
                fieldList.add(mkCExoLocString(label, strRef, stringIDs, substrings ));
                break;
            }
            case Gff.LIST :{
                int dataPointer = in.readInt();
                Lst list = mkList( label );
                fieldList.add( list );
                //l.dataPointer = dataPointer;
                dataPointers.put( pos, dataPointer );
                listIndices.add(pos);
                break;
            }
            case Gff.STRUCT :{
                int dataPointer = in.readInt();

                Strct struct = mkStruct( label, 0 );

                fieldList.add( struct );
                namedStructs.put(dataPointer, struct);
                break;
            }
            case Gff.VOID :{
                int dataPointer = in.readInt();

                in.seek( fieldDataOffset + dataPointer );
                int length = in.readInt(); // it's an unsigned int really (DWORD)
                byte[] data = new byte[length];
                in.read( data );
                fieldList.add( mkVoid( label, data ) );
                break;
            }
            case Gff.VECTOR :{
                int dataPointer = in.readInt();

                in.seek( fieldDataOffset + dataPointer );
                float[] floats = new float[3];
                for ( int i = 0; i < 3; i++ )
                    floats[i] = in.readFloat();
                fieldList.add( mkVector( label, floats ) );
                break;
            }
            default : throw new Error( "type not supported : " + type );
        }
    }

    private void fillList(int pos) throws IOException{
        Lst list = (Lst) fieldList.get(pos);
         in.seek( listIndicesOffset + dataPointers.get(pos).intValue() );
        int length = in.readInt();

        for ( int i = 0; i < length; i++ ){
            int sIndex = in.readInt();

            Strct struct = structList.get( sIndex );
            listAdd( list, struct );
        }
    }

    private Strct readStruct( int structNum ) throws IOException{

        Strct struct = null;
        in.seek(structOffset + 12 * structNum );
        int sID = in.readInt();
        if ( namedStructs.containsKey(structNum) ){
            struct = namedStructs.get(structNum);
            structSetID( struct, sID );
        } else struct = mkStruct( null, sID );
        int structDataPointer = in.readInt();
        int structSize = in.readInt();

        if ( structSize == 1 ){
            structAdd( struct, fieldList.get( structDataPointer ) );

        } else{

            in.seek( fieldIndicesOffset + structDataPointer );
            for ( int i = 0; i < structSize; i++ ){
                int fieldIndex = in.readInt();
                Fld field = fieldList.get( fieldIndex );
                structAdd( struct, field );
            }
        }
        return struct;
    }
}
