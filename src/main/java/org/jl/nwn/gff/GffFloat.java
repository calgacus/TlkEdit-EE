package org.jl.nwn.gff;

public class GffFloat extends GffField<Float>{

    protected Float data;

    public GffFloat( String label, Float value ) {
        super(label, Gff.FLOAT);
        this.data = value;
    }

    @Override public Float getData(){
        return data;
    }

    @Override public void setData( Float f ){
        data = f;
    }
}
