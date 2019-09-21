/*
 * Created on 07.11.2003
 *
 */
package org.jl.nwn.patcher;

import java.io.File;
import org.jl.nwn.Version;
import org.jl.nwn.gff.DefaultGffReader;

import org.jl.nwn.gff.GffContent;
import org.jl.nwn.gff.GffInteger;

public class GffBatchEdit {
    
    public static void main(String[] args) throws Exception{
        String label = args[0];
        int start = Integer.parseInt( args[1] );
        int end = Integer.parseInt( args[2] );
        int shift = Integer.parseInt( args[3] );
        for ( int i = 4; i < args.length; i++ ){
            File gffFile = new File( args[i] );
            GffContent c = new DefaultGffReader(Version.getDefaultVersion()).load( gffFile );
            boolean save = false;
            GffInteger gInt = (GffInteger) c.getTopLevelStruct().getChild( label );
            if ( gInt.getLongValue() >= start && gInt.getLongValue() <= end ){
                System.out.println( "adjusting field " + label + " ("+gInt.getTypeName()+") in file " + gffFile + " : " + (gInt.getLongValue() + shift) + " (was " + gInt.getLongValue() + ")");
                gInt.setLongValue( gInt.getLongValue() + shift );
                c.write( gffFile, Version.getDefaultVersion() );
                //save = true;
            }
            //c.write( gffFile );
        }
    }
}
