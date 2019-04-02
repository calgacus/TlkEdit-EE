/*
 * AbstractGffWriter.java
 *
 * Created on 12. Mai 2005, 11:11
 */

package org.jl.nwn.gff;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;



/**
 *
 */
public abstract class AbstractGffWriter <Fld, Strct extends Fld, Lst extends Fld>{
    
    protected List<Fld> fields = new ArrayList<Fld>();
    protected List<Integer> labelIndices = new ArrayList<Integer>();
    protected List<String> labels = new ArrayList<String>();
    protected int labelDataSize = 0;
    
    protected List<Lst> lists = new ArrayList<Lst>();
    protected List<Integer> listIndices = new ArrayList<Integer>();
    protected List<Strct> structs = new ArrayList<Strct>();
    protected List<Integer> structIndices = new ArrayList<Integer>();
    
    // map field index to field data for complex field with variable size
    protected Map<Integer, byte[]> fieldDataMap = new HashMap<Integer, byte[]>();
    
    // map <field array index, struct array index>
    protected Map<Integer, Integer> namedStructs = new HashMap<Integer, Integer>();
    
    //protected int[] dataPointers;
    protected List<Integer> dataPointerList = new ArrayList<Integer>();
    
    private Version nwnVersion;
    
    /** Creates a new instance of AbstractGffWriter */
    public AbstractGffWriter(){
        this(Version.getDefaultVersion());
    }
    
    public AbstractGffWriter(Version nwnVersion){
        this.nwnVersion = nwnVersion;
    }
    
    /**
     *retrieve field label
     *@return field label
     */
    protected abstract String fieldLabel( Fld field );
    /**
     *retrieve field type
     *@see Gff constant definitions in class Gff
     *@return field type
     */
    protected abstract int fieldType( Fld field );
    
    protected abstract int structSize( Strct struct );
    protected abstract int structID( Strct struct );
    protected abstract Fld structGet( Strct struct, int index );
    
    protected abstract int listSize( Lst list );
    protected abstract Strct listGet( Lst list, int index );
    
    /**
     * @see Gff#bigInt2raw conversion method in class Gff
     */
    protected abstract byte[] intFieldData( Fld field, int type );
    protected abstract float floatFieldData( Fld field );
    protected abstract double doubleFieldData( Fld field );
    protected abstract String resRefFieldData( Fld field );
    protected abstract String cExoStringFieldData( Fld field );
    protected abstract void vectorData( Fld f, float[] vector );
    /**
     * return the bytes making up the void data (without the length field)
     */
    protected abstract byte[] voidFieldData( Fld field );
    
    /**
     *write CExoLocString data into given arrays.
     *intValues[0] = strRef<br/>
     *intValues[1] = number of substrings <br/>
     *languages / genders / strings arrays for holding substrings,
     *gender : 0 = neutral/masc., 1 = feminine
     */
    protected abstract void cExoLocStringData( Fld Field, int[] intValues, NwnLanguage[] languages, int[] genders, String[] strings );
    
    public Version getVersion(){
        return nwnVersion;
    }
    
    /**
     * @throws IOException
     * @throws IllegalArgumentException if length of gffType != 4, or any argument is null
     */
    public void write( Strct topLevelStruct, String gffType, File file ) throws IOException, IllegalArgumentException{
        if ( topLevelStruct == null || gffType == null || file == null )
            throw new IllegalArgumentException( "no null arguments allowed" );
        if ( gffType.length() != 4 )
            throw new IllegalArgumentException( "gff type string must have length 4 : \'" + gffType + "\'" );
        FileOutputStream fos = new FileOutputStream(file);
        BufferedOutputStream bos = new BufferedOutputStream( fos );
        write( topLevelStruct, gffType, bos );
        bos.flush();
        bos.close();
        fos.close();
    }
    
    public void write( Strct topLevelStruct, String gffType, OutputStream out ) throws IOException{
        int fieldIndicesEntries = 0; //number of values in the FieldIndicesArray
        int listIndicesCount = 0; // size of the list indices block
        int fieldDataBlockSize = 0;
        
        try{
            //System.out.printf("AbstractGffWriter.write(%1s)\n", file);
            
            /* do a breadth-first traversal of the gff tree to build
             lists of fields, structs, and lists.
             because of the traversal order, fields belonging to one struct will
             be on consecutive positions in the fields list - that means we only
             need to know the index of the first field in the list
             (that index is saved in structIndices), same for list contents
             *
             also construct data (byte arrays) for complex field with unknown size,
             i.e. CResRef, CExoString, CExoLocString & Void in order to compute
             the size of the field data block and the data pointers
             */
            List<Fld> queue = new LinkedList<Fld>();
            queue.add(topLevelStruct);
            structs.add(topLevelStruct);
            
            int[] celsInts = new int[2];
            NwnLanguage[] celsLang = new NwnLanguage[NwnLanguage.findAll(nwnVersion).size()*2];
            int[] celsGenders = new int[celsLang.length];
            String[] celsStrings = new String[celsLang.length];
            
            while (!queue.isEmpty()){
                Fld f = queue.remove(0);
                int type = fieldType(f);
                if ( type==Gff.STRUCT ){
                    Strct struct = (Strct) f;
                    int fieldListIndex = fields.size();
                    structIndices.add( fieldListIndex );
                    int size = structSize(struct);
                    if (size > 1)
                        fieldIndicesEntries += size;
                    for ( int i = 0; i < size; i++ ){
                        Fld field = structGet(struct, i);
                        int fType = fieldType(field);
                        fields.add( field );
                        
                        // get field data & compute data pointer
                        int fieldIndex = fields.size() - 1;
                        int dataSize = 0;
                        byte data[];
                        switch (fType) {
                            case Gff.DOUBLE : case Gff.DWORD64 : case Gff.INT64 : {
                                dataSize = 8;
                                break;
                            }
                            case Gff.CEXOSTRING : {
                                data = buildCExoStringData( cExoStringFieldData(field) );
                                fieldDataMap.put( fieldIndex, data );
                                dataSize = data.length;
                                break;
                            }
                            case Gff.CEXOLOCSTRING : {
                                cExoLocStringData(field, celsInts, celsLang, celsGenders, celsStrings );
                                data = buildCExoLocStringData( celsInts, celsLang, celsGenders, celsStrings );
                                fieldDataMap.put( fieldIndex, data );
                                dataSize = data.length;
                                break;
                            }
                            case Gff.RESREF : {
                                data = buildCResRefData( resRefFieldData( field ) );
                                fieldDataMap.put( fieldIndex, data );
                                dataSize = data.length;
                                break;
                            }
                            case Gff.VOID : {
                                data = voidFieldData(field);
                                fieldDataMap.put( fieldIndex, data );
                                dataSize = 4 + data.length;
                                break;
                            }
                            case Gff.VECTOR : {
                                dataSize = 12;
                                break;
                            }
                            default : {}
                        }
                        dataPointerList.add( fieldDataBlockSize );
                        fieldDataBlockSize += dataSize;
                        
                        labelIndices.add( addLabel( fieldLabel(field) ) );
                        if ( fType == Gff.LIST )
                            queue.add( field );
                        else if ( fType == Gff.STRUCT ){ //named struct
                            queue.add( field );
                            structs.add((Strct)field);
                            namedStructs.put( fields.size()-1, structs.size()-1  );
                        }
                    }
                } else if ( type==Gff.LIST ){
                    Lst list = (Lst) f;
                    int structListIndex = structs.size();
                    lists.add(list);
                    listIndices.add(structListIndex);
                    int size = listSize(list);
                    // size in listIndices is at least 4 bytes ! ( the size field )
                    listIndicesCount = listIndicesCount + 4 + 4*size;
                    for ( int i = 0; i < size; i++ ){
                        Strct struct = listGet(list, i);
                        queue.add( struct );
                        structs.add( struct );
                        // structs in lists don't have labels ( i hope )
                    }
                } else { //simple field
                }
            }
            
            //header size is 14*4 = 56 bytes
            // header fields
            int structOffset = 0;
            int structCount = 0;
            
            int fieldOffset = 0;
            int fieldCount = 0;
            
            int labelOffset = 0;
            int labelCount = 0;
            
            int fieldDataOffset = 0;
            int fieldDataCount = 0;
            
            int fieldIndicesOffset = 0;
            
            int listIndicesOffset = 0;
            
            out.write( gffType.getBytes() );
            out.write( "V3.2".getBytes() );
            
            structOffset = 56;
            writeIntLE( structOffset, out );
            structCount = structs.size();
            writeIntLE( structCount, out );
            
            fieldOffset = structOffset + structCount * 12;
            writeIntLE( fieldOffset, out );
            fieldCount = fields.size();
            writeIntLE( fieldCount, out );
            
            labelOffset = fieldOffset + fieldCount * 12;
            writeIntLE( labelOffset, out );
            labelCount = labels.size();
            writeIntLE( labelCount, out );
            
            fieldDataOffset = labelOffset + labelCount * 16;
            writeIntLE( fieldDataOffset, out );
            fieldDataCount = fieldDataBlockSize;
            writeIntLE( fieldDataCount, out );
            
            fieldIndicesOffset = fieldDataOffset + fieldDataCount;
            writeIntLE( fieldIndicesOffset, out );
            int fieldIndicesCount = fieldIndicesEntries * 4;
            writeIntLE( fieldIndicesCount, out );
            
            listIndicesOffset  = fieldIndicesOffset + fieldIndicesCount;
            writeIntLE( listIndicesOffset, out );
            writeIntLE( listIndicesCount, out );
            
            writeStructArray(out);
            writeFieldArray(out);
            
            byte[] zero = new byte[16];
            for ( int i = 0; i < labels.size(); i++ ){
                String label = labels.get(i);
                out.write( label.getBytes() );
                out.write( zero, 0, 16 - label.length() );
            }
            
            writeFieldData(out);
            writeFieldIndecesArray(out);
            writeListIndicesArray(out);
            
            out.close();
        } finally {
            dataPointerList.clear();
            labelDataSize = 0;
            fields.clear();
            labelIndices.clear();
            labels.clear();
            listIndices.clear();
            lists.clear();
            namedStructs.clear();
            structIndices.clear();
            structs.clear();
            try{
                if ( out != null )
                    out.close();
            } catch (IOException ioex){}
        }
        
    }
    
    // add label to list if neccessary, return label's index
    private int addLabel( String label ){
        int labelIndex = labels.indexOf(label);
        if ( labelIndex == -1 ){
            labelIndex = labels.size();
            labelDataSize += label.length();
            labels.add(label);
        }
        return labelIndex;
    }
    
    private void writeFieldData( OutputStream raf ) throws IOException{
        int offset = 0;
        byte[] data = null;
        
        byte[] bytes8 = new byte[8];
        ByteBuffer bb8 = ByteBuffer.wrap(bytes8);
        bb8.order(ByteOrder.LITTLE_ENDIAN);
        
        float[] vector = new float[3];
        byte[] bytes12 = new byte[12];
        ByteBuffer bb12 = ByteBuffer.wrap(bytes12);
        bb12.order(ByteOrder.LITTLE_ENDIAN);
        
        for ( int i = 0; i < fields.size(); i++ ){
            Fld f = fields.get( i );
            int type = fieldType(f);
            if ( Gff.isComplexType(type) &&
                    type != Gff.LIST && type != Gff.STRUCT ){
                //System.out.println( "offset " + offset );
                //System.out.println( "writing " + f );
                switch (type){
                    case 6 : //DWORD64
                    case 7 : { data = intFieldData(f, type); break; } //INT64
                    case 9 : { //DOUBLE
                        bb8.putDouble(0, doubleFieldData(f));
                        data=bytes8;
                        break; }
                    case 10 : { //CEXOSTRING
                        data = fieldDataMap.get(i);
                        break;
                    }
                    case 11 : { //RESREF
                        data = fieldDataMap.get(i);
                        break;
                    }
                    case 12 : { //CEXOLOCSTRING
                        data = fieldDataMap.get(i);
                        break;
                    }
                    case 13 : {  //VOID
                        data = fieldDataMap.get(i);
                        ByteBuffer buf = ByteBuffer.allocate(data.length + 4);
                        buf.order( ByteOrder.LITTLE_ENDIAN );
                        buf.putInt( data.length );
                        buf.put( data );
                        data = buf.array();
                        break;
                    }
                    case Gff.VECTOR : {
                        vectorData(f, vector);
                        bb12.position(0);
                        for ( float fl : vector )
                            bb12.putFloat(fl);
                        data = bb12.array();
                        break;
                    }
                    default : {}
                }
                offset += data.length;
                raf.write( data );
            }
        }
    }
    
    protected byte[] buildCExoStringData( String cExoString ){
        byte[] data = new byte[ 4 + cExoString.length() ];
        ByteBuffer b = ByteBuffer.wrap( data );
        b.order( ByteOrder.LITTLE_ENDIAN );
        b.putInt( cExoString.length() );
        try{
            b.put(
                    cExoString.getBytes(
                    Gff.getCExoStringEncoding(nwnVersion) ) );
        } catch ( UnsupportedEncodingException uee ){
            // should never happen
            throw new Error( "fatal error : cannot encode CExoString", uee );
        }
        return data;
    }
    
    protected byte[] buildCResRefData( String resRef ){
        byte[] data = new byte[ 1 + resRef.length() ];
        data[0] = ( byte ) resRef.length();
        System.arraycopy( resRef.getBytes(), 0, data, 1, data.length -1 );
        return data;
    }
    
    private void writeFieldIndecesArray( OutputStream raf ) throws IOException{
        for ( int i = 0; i < structs.size(); i++ ){
            Strct s = structs.get(i);
            int ssize = structSize(s);
            if ( structSize(s) > 1 ){
                for ( int f = 0; f < ssize; f++ ){
                    Fld field = structGet(s, f);
                    int fieldIndex = structIndices.get(i) + f;
                    writeIntLE( fieldIndex, raf );
                }
            } else if ( ssize == 0 ){
                // empty struct
            }
        }
    }
    
    protected byte[] buildCExoLocStringData(
            int[] celsInts,
            NwnLanguage[] celsLang,
            int[] celsGenders,
            String[] celsStrings ){
        int substringCount = celsInts[1];
        int stringdatasize = 0;
        byte[] data = null;
        try {
            byte[][] substringBytes = new byte[substringCount][];
            for (int s = 0; s < substringCount; s++){
                substringBytes[s] =
                        celsStrings[s]
                        .getBytes(celsLang[s].getEncoding());
                stringdatasize += substringBytes[s].length;
            }
            int datasize = 12 + stringdatasize + 8 * substringCount;
            data = new byte[datasize];
            ByteBuffer b = ByteBuffer.wrap(data);
            b.order(ByteOrder.LITTLE_ENDIAN);
            b.putInt(datasize - 4);
            b.putInt(celsInts[0]);
            b.putInt(substringCount);
            for (int s = 0; s < substringCount; s++) {
                b.putInt( celsLang[s].getCode() * 2 + celsGenders[s]);
                b.putInt( substringBytes[s].length );
                b.put( substringBytes[s] );
            }
        } catch (UnsupportedEncodingException uee) {
            throw new Error("Error", uee); // should never happen
        }
        return data;
    }
    
    private void writeListIndicesArray( OutputStream raf ) throws IOException{
        int entries = 0;
        for ( int listNum = 0; listNum < lists.size(); listNum++ ){
            Lst list = lists.get(listNum);
            int lSize = listSize(list);
            writeIntLE( lSize, raf );
            int structIndex = listIndices.get(listNum);
            for ( int structNum = 0; structNum < lSize; structNum++ ){
                writeIntLE( structIndex + structNum, raf );
            }
        }
    }
    
    private void writeFieldArray( OutputStream raf ) throws IOException{
        // for every list compute the list indices array offset
        int[] liaOffsets = new int[lists.size()];
        int liao = 0;
        for ( int listNum = 0; listNum < lists.size(); listNum++ ){
            Lst list = lists.get(listNum);
            int listSize = listSize(list);
            liaOffsets[listNum] = liao;
            liao += ( 1 + listSize ) * 4;
        }
        int listNum = 0;
        
        byte[] type = new byte[4];
        byte[] floatBytes = new byte[4];
        ByteBuffer floatBuffer = ByteBuffer.wrap(floatBytes);
        floatBuffer.order(ByteOrder.LITTLE_ENDIAN);
        for ( int i = 0; i < fields.size(); i++ ){
            Fld field = fields.get(i);
            type[0] = (byte) fieldType(field);
            raf.write( type );
            writeIntLE( labelIndices.get(i), raf );
            if ( Gff.isComplexType(type[0]) ){
                if ( type[0] == Gff.STRUCT ){
                    writeIntLE( namedStructs.get(i), raf );
                } else if ( type[0] == Gff.LIST ){
                    writeIntLE( liaOffsets[listNum++], raf );
                } else{
                    writeIntLE( dataPointerList.get(i), raf );
                }
            } else{ // simple type, i.e. float or 4-byte integer
                if (type[0] == Gff.FLOAT){
                    floatBuffer.putFloat( 0, floatFieldData(field) );
                    raf.write(floatBytes);
                } else
                    raf.write( intFieldData(field, type[0]) );
            }
        }
    }
    
    private void writeStructArray( OutputStream raf ) throws IOException{
        // compute field indices array offsets for structs size>1
        int fiao = 0;
        int[] fiaOffsets = new int[structs.size()];
        for ( int i = 0; i < structs.size(); i++ ){
            Strct struct = structs.get(i);
            int sSize = structSize(struct);
            if ( sSize > 1 ){
                fiaOffsets[i] = fiao;
                fiao += sSize * 4;
            }
        }
        for ( int i = 0; i < structs.size(); i++ ){
            Strct s = structs.get(i);
            writeIntLE( structID(s), raf );
            int sSize = structSize(s);
            if ( sSize > 1 ){
                writeIntLE( fiaOffsets[i], raf );
            } else if ( sSize == 1 ){
                writeIntLE( structIndices.get(i), raf );
            } else if ( sSize == 0 ){
                writeIntLE( 0, raf );
            }
            writeIntLE( sSize, raf );
        }
    }
    
    private void writeIntLE( int i, OutputStream raf ) throws IOException{
        raf.write(i & 255);
        raf.write((i >> 8) & 255);
        raf.write((i >> 16) & 255);
        raf.write((i >> 24) & 255);
    }
    
}
