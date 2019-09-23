package org.jl.nwn.gff;

public class GffVoid extends GffField<byte[]>{
    
    private byte[] data = new byte[0];
    
    public GffVoid( String label ){
        super( label, Gff.VOID );
    }
    
    public GffVoid( String label, byte[] data ){
        this( label );
        this.data = data;
    }
    
    @Override
    public byte[] getData() {
        return data;
    }
    
    @Override
    public void setData(byte[] bs) {
        data = bs;
    }
    
    public static String printHex(byte[] data, int maxLen){
        StringBuilder sb = new StringBuilder();
        for ( int i = 0; i < Math.min( data.length, maxLen ); i++ ){
            int uByte = data[i] < 0 ? data[i] + 256 : data[i];
            String hex = Integer.toHexString(uByte);
            if (uByte < 16)
                sb.append("0");
            sb.append(hex);
            if ( (i & 15) == 15 )
                sb.append("\n");
            else
                sb.append(" ");
        }
        return sb.toString();
    }
    
    @Override
    public String toString(){
        return getLabel() + " (VOID) size=" + data.length;
    }
    
}
