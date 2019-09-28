package org.jl.nwn.gff;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.jl.nwn.Version;

public class GffContent{

    protected GffStruct topLevelStruct = GffStruct.mkTopLevelStruct();

    // the file type string is always in upper case with length 4
    private String fileTypeString = "GFF ";

    public GffContent( String filetype, GffStruct root ){
        setFiletype( filetype );
        setTopLevelStruct(root);
    }

    public GffContent( String filetype ){
        setFiletype( filetype );
    }

    protected GffContent( GffContent c ){
        setFiletype( c.fileTypeString );
        setTopLevelStruct( c.topLevelStruct );
    }

    public GffStruct getTopLevelStruct(){
        return topLevelStruct;
    }

    public void setTopLevelStruct( GffStruct struct ){
        topLevelStruct = struct;
    }

    /**
     * @return the file type string of this gff content, length is always 4
     */
    public String getFiletype() {
        return fileTypeString;
    }

    public void write( File file, Version nwnVersion ) throws IOException{
        new DefaultGffWriter(nwnVersion).write( topLevelStruct, fileTypeString, file );
    }

    /**
     * @throws IllegalArgumentException if string.length() > 4
     * @param string the gff file type string
     */
    public void setFiletype(String string) throws IllegalArgumentException{
        if ( string.length() > 4 )
            throw new IllegalArgumentException( "invalid gff file type string : '" + string + "'" );
        fileTypeString = string.toUpperCase() + "    ".substring( 0, 4 - string.length() );
    }

    public static void main( String[] args ) throws Exception{
        GffContent c =
                new DefaultGffReader(Version.getDefaultVersion())
                .load(new File( args[0]) );
        final Iterator<GffField> it = c.getTopLevelStruct().getDFIterator();
        while ( it.hasNext() ){
            final GffField f = it.next();
            System.out.println("gffcontent.java "+ f.getLabel() + "(" + f.getTypeName() + ")" );
        }
    }
}
