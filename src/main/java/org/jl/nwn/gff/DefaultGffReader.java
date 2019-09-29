package org.jl.nwn.gff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

public class DefaultGffReader extends AbstractGffReader<GffField, GffStruct, GffList>{

    /**
     @deprecated unsafe : relies on correct default version
     */
    @Deprecated
    public DefaultGffReader(){
        super();
    }

    public DefaultGffReader(Version v){
        super(v);
    }

    public boolean isGffList(GffField field) {
        return field.getType() == Gff.LIST;
    }

    @Override
    public GffField mkInteger(String label, byte type, BigInteger value) {
        GffInteger i = new GffInteger( label, type );
        i.setData( value );
        return i;
    }

    @Override
    public GffField mkFloat(String label, float value) {
        return new GffFloat( label, value );
    }

    @Override
    public GffField mkDouble(String label, double value) {
        return new GffDouble( label, value );
    }

    @Override
    public GffField mkCExoString(String label, String value) {
        return new GffCExoString( label, value );
    }

    @Override
    public GffField mkCExoLocString(String label, int strRef, int[] stringIDs,
            String[] strings) {
        GffCExoLocString s = new GffCExoLocString( label );
        s.setStrRef(strRef);
        for ( int i = 0; i < stringIDs.length; i++ )
            s.addSubstring( new CExoLocSubString(strings[i], NwnLanguage.find(getVersion(), stringIDs[i] / 2), stringIDs[i] % 2 ) );
        return s;
    }

    @Override
    public GffField mkCResRef(String label, String value) {
        return new GffCResRef( label, value );
    }

    @Override
    public GffStruct mkStruct(String label, int structID) {
        return new GffStruct(label, structID);
    }

    @Override
    public GffList mkList(String label) {
        return new GffList( label );
    }

    @Override
    public GffField mkVoid(String label, byte[] value) {
        return new GffVoid( label, value );
    }

    @Override
    public void listAdd(GffList list, GffStruct struct) {
        list.add( struct );
    }

    @Override
    public void structAdd(GffStruct struct, GffField field) {
        struct.addChild(field);
    }

    @Override
    public void structSetID(GffStruct struct, int ID) {
        struct.setId(ID);
    }

    @Override
    public GffContent mkGffObject(GffStruct tls, String fileType, File file){
        return new GffContent( fileType, tls );
    }

    @Override
    public GffField mkVector(String label, float[] value){
        GffVector v = (GffVector) GffField.createField(Gff.VECTOR);
        v.setLabel(label);
        v.setData(value);
        return v;
    }

    @Override
    public GffContent load( File f ) throws IOException{
        return (GffContent) super.load(f);
    }

    @Override
    public GffContent load( InputStream in ) throws IOException{
        return (GffContent) super.load(in);
    }

    public static void main( String[] args ) throws IOException{
        AbstractGffReader reader = new DefaultGffReader();
        long time = System.currentTimeMillis();
        int fileCount = 0;
        long totalSize = 0;
        for ( String fName : args ){
            final File f = new File(fName);
            try (final FileInputStream fis = new FileInputStream(f);
                 final BufferedInputStream bin = new BufferedInputStream(fis)
            ) {
                reader.load(bin);
            }
            fileCount++;
            totalSize += f.length();
            System.out.println("def...gffreader.java 178  "+f);
        }
        System.out.printf("loaded %d files ( %f kb ) in %d ms\n", fileCount, totalSize/1024.0, System.currentTimeMillis()-time);
    }
}
