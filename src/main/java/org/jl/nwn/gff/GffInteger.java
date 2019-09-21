package org.jl.nwn.gff;
import java.math.BigInteger;

public class GffInteger extends GffField<BigInteger>{
    
    private BigInteger value = BigInteger.ZERO;
    
    public GffInteger( String label, byte type ){
        super( label, type );
        if ( type < 0 || type > 7 ) throw new IllegalArgumentException( "invalid type" );
        this.type = type;
    }
    
    public GffInteger( String label, byte type, long value ){
        this( label, type );
        this.value = BigInteger.valueOf( value );
    }
    
    public GffInteger( String label, byte type, BigInteger value ){
        this( label, type );
        this.value = value;
    }
    
    public void setLongValue(long longValue) {
        value = BigInteger.valueOf( longValue );
    }
    
    public long getLongValue(){
        return value.longValue();
    }
    
    @Override
    public BigInteger getData(){
        return value;
    }
    
    @Override
    public void setData( BigInteger data ){
        value = data;
    }
    
}
