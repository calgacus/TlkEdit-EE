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
    
    public String getData(){
        return cExoString;
    }
    
    public void setData( String data ){
        cExoString = data;
    }
    
}
