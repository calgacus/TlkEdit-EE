package org.jl.nwn.gff;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GffCExoString extends GffField<String>{
    
    private String cExoString = "";
    
    public GffCExoString( String label ){
        super( label, Gff.CEXOSTRING );
    }
    
    public GffCExoString( String label, String value ){
        this( label );
        this.cExoString = value;
    }
    
    @Override
    public String getData(){
        return cExoString;
    }
    
    @Override
    public void setData( String data ){
        cExoString = data;
    }
    
}
