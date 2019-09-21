package org.jl.nwn.gff;

public class GffCResRef extends GffField<String>{
    
    private String resRef = "";
    
    public GffCResRef( String label ){
        super( label, Gff.RESREF );
    }
    
    public GffCResRef( String label, String rr ){
        this( label );
        resRef = rr;
    }
    
    public String getResRef() {
        return resRef;
    }
    
    public void setResRef(String string){
        resRef = string;
                //.substring( 0, Math.min( string.length(), Gff.RESREFSIZE ) );
    }
    
    public String getData(){
        return resRef;
    }
    
    public void setData( String data ){
        setResRef( data );
    }
    
}
