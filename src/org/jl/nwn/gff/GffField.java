package org.jl.nwn.gff;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import org.jl.nwn.Version;

/*
 * composite gff field representation, treats CExoLocString 
 * as node with CExoLocSubstring children
 *
 * types for value parameter in setData() and getData() are :
 * <dl>
 *   <dt>integers ( type 0-7 )</dt><dd>BigInteger</dd>
 *   <dt>Float ( type 8 )</dt><dd>Float</dd>
 *   <dt>Double ( type 9 )</dt><dd>Double</dd>
 *   <dt>CExoString ( type 10 )</dt><dd>String</dd>
 *   <dt>CResRef ( type 11 )</dt><dd>String</dd>
 *   <dt>CExoLocString ( type 12 )</dt><dd>Integer ( the StrRef )</dd>
 *   <dt>Void ( type 13 )</dt><dd>Byte[]</dd>
 *   <dt>Struct ( type 14 )</dt><dd>Integer ( the struct ID )</dd>
 *   <dt>List ( type 15 ) </dt><dd>none, a list does not have additional data</dd>
 *   <dt>CExoLocSubstring ( always use type GffCompField.CEXOLOCSUBSTRING )</dt><dd>String</dd>
 * </dl>
 */
public abstract class GffField<Data extends Object> implements Cloneable{
    
    protected String label;
    protected byte type;
    
    protected GffField parent = null;
    
    public String getTypeName(){
        return Gff.getTypeName( type );
    }
    
    public String toString(){
        return label + " (" + getTypeName() + ") " + getData();
    }
    
    /**
     * Constructor for GffField.
     */
    protected GffField( String label, byte type ){
        //this.label = label;
        setLabel( label );
        this.type = type;
    }
    
    /**
     * Returns the type.
     * @return byte
     */
    public byte getType() {
        return type;
    }
    
    protected boolean isComplexType(){
        return !( type < 6 || type == 8 );
    }
    
    public boolean isIntegerType(){
        return type < 8;
    }
    
    public boolean isDecimalType(){
        return type == 8 || type == 9;
    }
    
    /**
     * @return true if field is not a list or struct
     * */
    public boolean isDataField(){
        return ( type != Gff.STRUCT && type != Gff.LIST );
    }
    
    public String getLabel(){
        return label;
    }
    
    public void setLabel( String label ) {
        this.label = label.substring( 0, Math.min( label.length(), 16 ) );
    }
    
    public GffField getParent(){
        return parent;
    }
    
    public static GffField createField(byte type) {
        if (type < 8)
            return new GffInteger("new_" + Gff.getTypeName(type), type);
        else if (type == Gff.FLOAT)
            return new GffFloat("new_" + Gff.getTypeName(type), 0f);
        else if (type == Gff.DOUBLE)
            return new GffDouble("new_" + Gff.getTypeName(type), 0d);
        else
            switch (type) {
                case Gff.CEXOSTRING :
                    return new GffCExoString(
                            "new_" + Gff.getTypeName(type));
                case Gff.RESREF :
                    return new GffCResRef("new_" + Gff.getTypeName(type));
                case Gff.CEXOLOCSTRING :
                    return new GffCExoLocString(
                            "new_" + Gff.getTypeName(type));
                case Gff.VOID :
                    return new GffVoid("new_" + Gff.getTypeName(type));
                case Gff.STRUCT :
                    return new GffStruct("new_" + Gff.getTypeName(type));
                case Gff.LIST :
                    return new GffList("new_" + Gff.getTypeName(type));
                case Gff.VECTOR :
                    return new GffVector("new_" + Gff.getTypeName(type));
            }
            return null;
    }
    
    public Object clone(){
        try{
            return super.clone();
        } catch ( CloneNotSupportedException cnse){
        }
        return null;
    }
    
    public boolean allowsChildren(){
        return false;
    }
    
    public int getChildCount(){
        return 0;
    }
    
    public GffField getChild( int index ){
        throw new UnsupportedOperationException();
    }
    
    public int getChildIndex( GffField f ){
        throw new UnsupportedOperationException();
    }
    
    public void addChild( int index, GffField f ){
        throw new UnsupportedOperationException();
    }
    
    public void removeChild( GffField f ){
        throw new UnsupportedOperationException();
    }
    
    public abstract Data getData();
    
    public abstract void setData( Data data );
    
    /** compare 2 gff fields for testing purposes
     */
    public boolean equalsGff(GffField f){
        if ( f.getType() != getType() ){
            System.out.printf("different types : \n%s\n%s\n", this, f );
            return false;
        }
        if ( getType() == Gff.VOID ){
            byte[] b1 = (byte[]) getData();
            byte[] b2 = (byte[]) f.getData();
            if (!Arrays.equals(b1, b2)){
                System.out.printf("different data : \n%s\n%s\n", this, f );
                return false;
            }
        } else
        if ( getType() == Gff.VECTOR ){
            float[] v1 = (float[]) getData();
            float[] v2 = (float[]) f.getData();
            //System.out.printf("%d, %d, %d\n", Float.floatToRawIntBits(v1[0]),Float.floatToRawIntBits(v1[1]),Float.floatToRawIntBits(v1[2]));
            //System.out.printf("%d, %d, %d\n", Float.floatToRawIntBits(v2[0]),Float.floatToRawIntBits(v2[1]),Float.floatToRawIntBits(v2[2]));
            if (!Arrays.equals(v1, v2)){
                System.out.printf("different data : \n%s\n%s\n", this, f );
                return false;
            }
        } else{
            if ( getData() != null ){
                if (!getData().equals(f.getData())){
                    System.out.printf("different data : \n%s\n%s\n", this, f );
                    return false;
                }
            } else if (!(f.getData() == null)){
                System.out.printf("different data : \n%s\n%s\n", this, f );
                return false;
            }
        }
        if ( allowsChildren() ){
            for ( int i = 0; i < getChildCount(); i++ ){
                if ( getType() == Gff.LIST || getType() == Gff.CEXOLOCSTRING ){
                    if ( !getChild(i).equalsGff(f.getChild(i)) )
                        return false;
                } else if ( getType() == Gff.STRUCT ){
                    if ( !getChild(i).equalsGff( ((GffStruct)f).getChild(getChild(i).getLabel()) ) )
                        return false;
                }
            }
        }
        return true;
    }
    
    public static void main( String ... args ) throws Exception{
        DefaultGffReader reader = new DefaultGffReader(Version.getDefaultVersion());
        GffContent c1 = reader.load( new File(args[0]) );
        GffContent c2 = reader.load( new FileInputStream(args[1]) );
        System.out.println( c1.getTopLevelStruct().equalsGff(c2.getTopLevelStruct()) );
    }
    
}
