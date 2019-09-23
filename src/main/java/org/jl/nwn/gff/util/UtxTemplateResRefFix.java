/*
        change the TemplateResRef field in UTx files to match the file name
 */
package org.jl.nwn.gff.util;

import java.io.File;

import org.jl.nwn.Version;
import org.jl.nwn.gff.DefaultGffReader;
import org.jl.nwn.gff.GffCResRef;
import org.jl.nwn.gff.GffContent;
import org.jl.nwn.resource.ResourceID;

public class UtxTemplateResRefFix {

    public static void main(String[] args) throws Exception{
        if ( args.length == 0 ) {
            System.out.println( "usage : UtxTemplateResRefFix <files>" );
            return;
        }
        for (final String arg : args) {
            final File f = new File(arg);
            GffContent c = new DefaultGffReader(Version.getDefaultVersion()).load( f );
            GffCResRef resRef = (GffCResRef) c.getTopLevelStruct().getChild("TemplateResRef");
            if ( resRef == null )
                System.out.println( "warning : file has no TemplateResRef field : " + f );
            else{
                if ( f.getName().length() > 20 )
                    System.out.println( "warning : file name too long : " + f );
                ResourceID id = ResourceID.forFile( f );
                if ( !id.getName().equals( resRef.getResRef() ) ){
                    System.out.println( "changing TemplateResRef field for file "+ f + " to " + id.getName() );
                    resRef.setResRef( id.getName() );
                    c.write( f, Version.getDefaultVersion() );
                }
            }
        }
    }
}
