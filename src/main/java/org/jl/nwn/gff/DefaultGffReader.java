/*
 * Created on 20.11.2004
 */
package org.jl.nwn.gff;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;

/**
 */
public class DefaultGffReader extends AbstractGffReader<GffField, GffStruct, GffList>{
    
    /**
     @deprecated unsafe : relies on correct default version
     */
    public DefaultGffReader(){
        super();
    }
    
    public DefaultGffReader(Version v){
        super(v);
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#isGffList(java.lang.Object)
         */
    public boolean isGffList(GffField field) {
        return field.getType() == Gff.LIST;
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffInteger(java.lang.String, byte, java.math.BigInteger)
         */
    @Override
    public GffField mkInteger(String label, byte type, BigInteger value) {
        GffInteger i = new GffInteger( label, type );
        i.setData( value );
        return i;
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffFloat(java.lang.String, float)
         */
    @Override
    public GffField mkFloat(String label, float value) {
        return new GffFloat( label, value );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffDouble(java.lang.String, double)
         */
    @Override
    public GffField mkDouble(String label, double value) {
        return new GffDouble( label, value );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffCExoString(java.lang.String, java.lang.String)
         */
    @Override
    public GffField mkCExoString(String label, String value) {
        return new GffCExoString( label, value );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffCExoLocString(java.lang.String, int, int[], java.lang.String[])
         */
    @Override
    public GffField mkCExoLocString(String label, int strRef, int[] stringIDs,
            String[] strings) {
        GffCExoLocString s = new GffCExoLocString( label );
        s.setStrRef(strRef);
        for ( int i = 0; i < stringIDs.length; i++ )
            s.addSubstring( new CExoLocSubString(strings[i], NwnLanguage.find(getVersion(), stringIDs[i] / 2), stringIDs[i] % 2 ) );
        return s;
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffResRef(java.lang.String, java.lang.String)
         */
    @Override
    public GffField mkCResRef(String label, String value) {
        return new GffCResRef( label, value );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffStruct(java.lang.String, int)
         */
    @Override
    public GffStruct mkStruct(String label, int structID) {
        return new GffStruct(label, structID);
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffList(java.lang.String)
         */
    @Override
    public GffList mkList(String label) {
        return new GffList( label );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#mkGffVoid(java.lang.String, byte[])
         */
    @Override
    public GffField mkVoid(String label, byte[] value) {
        return new GffVoid( label, value );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#listAdd(java.lang.Object, java.lang.Object)
         */
    @Override
    public void listAdd(GffList list, GffStruct struct) {
        list.add( struct );
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#structAdd(java.lang.Object, java.lang.Object)
         */
    @Override
    public void structAdd(GffStruct struct, GffField field) {
        struct.addChild(field);
    }
    
        /* (non-Javadoc)
         * @see org.jl.nwn.gff.AbstractGffBuilder#structSetID(java.lang.Object, int)
         */
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
            //if ( fName.endsWith("bic") ){
                File f = new File(fName);
                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bin = new BufferedInputStream(fis);
                reader.load(bin);
                //reader.load(f);
                bin.close();
                fis.close();
                fileCount++;
                totalSize += f.length();
                System.out.println("def...gffreader.java 164  "+f);
            //}
        }
        System.out.printf("loaded %d files ( %f kb ) in %d ms\n", fileCount, totalSize/1024.0, System.currentTimeMillis()-time);
    }

}
