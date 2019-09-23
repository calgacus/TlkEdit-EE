/*
 * VectorFormat.java
 *
 * Created on 10. September 2006, 11:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jl.text;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 *
 * @author ich
 */
public class VectorFormat extends Format{
    
    protected NumberFormat nf;
    
    /** Creates a new instance of VectorFormat */
    public VectorFormat(NumberFormat df) {
        this.nf = df;
    }

    @Override
    public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
        float[] v = (float[]) obj;
        toAppendTo.append("(").append(nf.format(v[0])).append(" ");
        toAppendTo.append(nf.format(v[1])).append(" ");
        toAppendTo.append(nf.format(v[2])).append(")");
        return toAppendTo;
    }
    
    private static void skipWhitespace(String s, ParsePosition pos){
        int p = pos.getIndex();
        while ( Character.isWhitespace(s.charAt(p)) )
            pos.setIndex(++p);        
    }

    @Override
    public float[] parseObject(String source, ParsePosition pos){
        float[] v = new float[3];
        System.out.println(pos);
        source = source.trim();
        if ( source.charAt(pos.getIndex()) == '(' ){
            //while ( Character.isWhitespace(source.charAt(p))) )
            pos.setIndex(pos.getIndex()+1);
            try {
                skipWhitespace(source, pos);
                v[0] = nf.parse(source, pos).floatValue();
                skipWhitespace(source, pos);
                v[1] = nf.parse(source, pos).floatValue();
                skipWhitespace(source, pos);
                v[2] = nf.parse(source, pos).floatValue();
                skipWhitespace(source, pos);
            } catch ( NullPointerException npe ) {
                //System.out.println("npe");
                return null;
            }
            if ( source.charAt(pos.getIndex()) == ')' ){
                pos.setIndex(pos.getIndex()+1);
                return v;
            }
            else
                return null;            
        }
        else
            return null;
    }
    
    public static void main(String ... args) throws ParseException{
        VectorFormat vf = new VectorFormat(new DecimalFormat());
        System.out.println(vf.format(vf.parseObject("(1,4 3,14 23,1)")));
    }
    
}
