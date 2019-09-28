package org.jl.nwn.gff;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;

import org.jl.nwn.Version;

public class DefaultGffWriter extends AbstractGffWriter<GffField, GffStruct, GffList>{

    public DefaultGffWriter() {
    }

    public DefaultGffWriter(Version v) {
        super(v);
    }

    @Override
    protected GffStruct listGet(GffList list, int index) {
        return list.get(index);
    }

    @Override
    protected int listSize(GffList list) {
        return list.getSize();
    }

    @Override
    protected GffField structGet(GffStruct struct, int index) {
        return struct.getChild(index);
    }

    @Override
    protected int fieldType(GffField field) {
        return field.getType();
    }

    @Override
    protected String fieldLabel(GffField field) {
        return field.getLabel();
    }

    @Override
    protected int structSize(GffStruct struct) {
        return struct.getSize();
    }

    @Override
    protected void cExoLocStringData(GffField Field, int[] intValues, org.jl.nwn.NwnLanguage[] languages, int[] genders, String[] strings) {
        GffCExoLocString s = (GffCExoLocString) Field;
        intValues[0] = s.getStrRef();
        intValues[1] = s.getSubstringCount();
        for ( int i = 0; i < s.getSubstringCount(); i++ ){
            CExoLocSubString sub = s.getSubstring(i);
            languages[i] = sub.language;
            genders[i] = sub.gender;
            strings[i] = sub.string;
        }
    }

    //protected byte[] b4 = new byte[4];
    //protected byte[] b8 = new byte[8];

    /*
     * type mapping GFF -> java ???
     * byte     8-bit unsigned      char
     * char     8-bit signed        byte
     * word     16-bit unsigned     char
     * short    16-bit signed       short
     * dword    32-bit unsigned     long
     * int      32-bit signed       int
     * dword64  64-bit unsigned     BigInteger
     * int64    64-bit signed       long
     */

    @Override
    protected byte[] intFieldData(GffField field, int type) {
        //byte[] data = ( type==Gff.INT64 || type==Gff.DWORD64 )? b8 : b4;
        return Gff.bigInt2raw( (BigInteger)field.getData(), (byte)type, null );
    }

    @Override
    protected byte[] voidFieldData(GffField field) {
        return ((GffVoid)field).getData();
    }

    @Override
    protected String resRefFieldData(GffField field) {
        return ((GffCResRef)field).getResRef();
    }

    @Override
    protected float floatFieldData(GffField field) {
        return ((GffFloat)field).getData().floatValue();
    }

    @Override
    protected double doubleFieldData(GffField field) {
        return ((GffDouble)field).getData().doubleValue();
    }

    @Override
    protected String cExoStringFieldData(GffField field) {
        return ((GffCExoString)field).getData();
    }

    @Override
    protected void vectorData(GffField field, float[] vector){
        System.arraycopy(((GffVector)field).getData(),0,vector,0,3);
    }

    @Override
    protected int structID(GffStruct struct){
        return struct.getId();
    }

    public static void main( String... args ) throws Exception{
        long startTime = System.currentTimeMillis();
        long totalSize = 0;
        DefaultGffReader reader = new DefaultGffReader(Version.getDefaultVersion());
        DefaultGffWriter writer = new DefaultGffWriter(Version.getDefaultVersion());
        final ArrayList<File> errors = new ArrayList<>();
        for ( String filename : args ){

            File file = new File(filename);
            GffContent c = reader.load( file );
            File tmp = File.createTempFile(file.getName(), "gffstresstest");
            totalSize += file.length();
            tmp.deleteOnExit();
            writer.write( c.getTopLevelStruct(), c.getFiletype(), tmp );

            GffContent c2 = reader.load( tmp );
            boolean equal = c.getTopLevelStruct().equalsGff(c2.getTopLevelStruct());
            if ( !equal )
                errors.add(file);
            System.out.printf( "DefaultGFFWriter.java %s %b\n", file, equal );
        }
        long endTime = System.currentTimeMillis();
        System.out.printf("DefaultGFFWriter.java files processed : %d, total size : %d bytes, time : %d s\n", args.length, totalSize, (endTime-startTime)/1000);
        System.out.printf("DefaultGFFWriter.java %d errors : %s\n", errors.size(), errors );
    }
}
