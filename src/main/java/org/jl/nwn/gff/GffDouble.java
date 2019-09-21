package org.jl.nwn.gff;

/**
 *
 */
public class GffDouble extends GffField<Double>{
    
    protected Double data;
    
    /** Creates a new instance of GffFloat */
    public GffDouble( String label, Double value ) {
        super(label, Gff.DOUBLE);
        this.data = value;
    }
    
    @Override public Double getData(){
        return data;
    }
    
    @Override public void setData( Double f ){
        data = f;
    }
    
}
