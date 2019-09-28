/*
 * Created on 29.12.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.erf;

import java.io.BufferedOutputStream;
import java.io.DataInput;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.jl.nwn.NwnLanguage;
import org.jl.nwn.Version;
import org.jl.nwn.gff.CExoLocSubString;
import org.jl.nwn.gff.Gff;
import org.jl.nwn.gff.GffCExoLocString;
import org.jl.nwn.resource.AbstractRepository;
import org.jl.nwn.resource.RafInputStream;
import org.jl.nwn.resource.ResRefUtil;
import org.jl.nwn.resource.ResourceID;

/**
 * this class represents an erf file.
 *
 * Differences between V1.0 and V1.1 (nwn2) :
 * 1) header version is V1.1 instead of V1.0 (duh)
 * 2) Localized strings : in V1.1 ERFs the language id is used as it is,
 *    and not as (LanguageID*2+Gender)
 * 3) resource names are 32 instead of 16 characters long
 */
public class ErfFile extends AbstractRepository{

    private File file;
    private RandomAccessFile raf;
    private GffCExoLocString description;
    private ErfType type;
    private int buildYear;
    private int buildDay;
    private final Map<ResourceID, Object> resources = new TreeMap<>();
    private Version nwnVersion;

    private static final String TMPFILEPREFIX = "erftmp_";
    public static final ErfType HAK = new ErfType( "HAK ", "hak" ){
        @Override protected void initializeErf(ErfFile erf){
            GffCExoLocString cels = new GffCExoLocString("");
            CExoLocSubString s = new CExoLocSubString(
                    "<HAK NAME>\n<URL>\n<DESCRIPTION>",
                    NwnLanguage.ENGLISH,
                    Gff.GENDER_MALE
                    );
            cels.addSubstring(s);
            erf.setDescription(cels);
        }
    };
    //public static final ErfType SAV = new ErfType( "MOD ", "sav" );
    public static final ErfType ERF = new ErfType( "ERF ", "erf" );
    public static final ErfType MOD = new ErfType( "MOD ", "mod" );
    public static final ErfType[] TYPES = { HAK, MOD, ERF };

    public static class ErfType{
        private final String typeString;
        private final String name;
        private ErfType( String typeString, String name ){
            this.typeString = typeString;
            this.name = name;
        }
        @Override
        public String toString(){
            return name;
        }
        public static ErfType forTypeString( String s ){
            for (final ErfType type : TYPES) {
                if (type.typeString.equals(s)) {
                    return type;
                }
            }
            return ERF;
        }
        public String getName() {
            return name;
        }
        public String getTypeString() {
            return typeString;
        }
        protected void initializeErf(ErfFile erf){}
    }

    /**
     * create a new erf file with the given type and description
     * */
    public ErfFile( File file,
            ErfType type,
            GffCExoLocString description,
            Version nwnVersion ){
        this(type);
        this.description = description;
        this.file = file;
        this.nwnVersion = nwnVersion;
    }

    public ErfFile( File file,
            ErfType type,
            GffCExoLocString description ){
        this(file, type, description, Version.getDefaultVersion());
    }

    public ErfFile(ErfType type, Version nwnVersion){
        type.initializeErf(this);
        this.nwnVersion = nwnVersion;
    }

    public ErfFile(ErfType type){
        this( type, Version.getDefaultVersion() );
    }

    /**
     * open given file as erf file
     * */
    public ErfFile( File erf ) throws IOException{
        this.file = erf;
        if ( !erf.exists() )
            throw new FileNotFoundException( erf.toString() );
        raf = new RandomAccessFile( file, "r" );
        byte[] buf = new byte[32000];
        raf.read( buf, 0, 4 );
        type = ErfType.forTypeString( new String( buf,0,4 ) );
        raf.read( buf, 0, 4 );
        String version = new String(buf,0,4);
        if ( "V1.1".equals( new String(buf,0,4) ) ) {
            nwnVersion = Version.NWN2;
        } else
            if ( !"V1.0".equals( new String(buf,0,4) ) )
                System.out.println( "warning : unknown ERF version "
                        + new String(buf,0,4) );
            else
                nwnVersion = Version.NWN1;
        int languageCount;
        int localizedStringSize;
        int entryCount;
        int offsetToLocalizedString;
        int offsetToKeyList;
        int offsetToResourceList;
        int descriptionStrRef;
        languageCount = readIntLE( raf );
        localizedStringSize = readIntLE( raf );
        entryCount = readIntLE( raf );
        offsetToLocalizedString = readIntLE( raf );
        offsetToKeyList = readIntLE( raf );
        offsetToResourceList = readIntLE( raf );
        buildYear = readIntLE( raf );
        buildDay = readIntLE( raf );
        descriptionStrRef = readIntLE( raf );

        // read localized strings
        description = new GffCExoLocString( "erf_desc" );
        description.setStrRef( descriptionStrRef );
        raf.seek( offsetToLocalizedString );
        for ( int i = 0; i < languageCount; i++ ){
            int languageID = readIntLE( raf );
            int stringSize = readIntLE( raf );
            raf.read( buf, 0, stringSize );

            int languageCode = languageID;
            int gender = 0;
            if ( nwnVersion == Version.NWN1 ){
                languageCode /= 2;
                gender = languageCode % 2;
            }
            NwnLanguage lang = NwnLanguage.find( nwnVersion, languageCode );
            String s = new String(
                    buf,0,stringSize,lang.getEncoding() );

            description.addSubstring(
                    new CExoLocSubString( s, lang, gender ) );
        }

        // use a MappedByteBuffer for reading KeyList and ResourceList
        MappedByteBuffer mbb = raf.getChannel().map( FileChannel.MapMode.READ_ONLY, offsetToKeyList, offsetToResourceList-offsetToKeyList+entryCount*8 );
        mbb.order( ByteOrder.LITTLE_ENDIAN );
        mbb.position( 0 );
        int rlOffset = offsetToResourceList - offsetToKeyList;
        int resrefsize = ResRefUtil.resRefSize(nwnVersion);
        for ( int i = 0; i < entryCount; i++ ){
            mbb.position( i * (resrefsize+8) );
            mbb.get( buf,0,resrefsize );
            String filename = new String( buf,0,resrefsize ).trim();
            mbb.getInt(); // resID - not needed;
            short type = mbb.getShort();
            mbb.position( rlOffset + (i*8) );
            int resourceOffset = mbb.getInt();
            int resourceSize = mbb.getInt();
            resources.put( new ResourceID( filename, type ), new ResourceListEntry( resourceOffset, resourceSize ) );
        }
    }

    private static int readIntLE( DataInput raf ) throws IOException {
        return raf.readUnsignedByte()
        | (raf.readUnsignedByte() << 8)
        | (raf.readUnsignedByte() << 16)
        | (raf.readUnsignedByte() << 24);
    }

    private static void writeIntLE( RandomAccessFile out, int i)
    throws IOException {
        out.write(i & 255);
        out.write((i >> 8) & 255);
        out.write((i >> 16) & 255);
        out.write((i >> 24) & 255);
    }

    private static class ResourceListEntry{
        final int offset;
        final int size;
        ResourceListEntry( int offset, int size ){
            this.offset = offset;
            this.size = size;
        }
    }

    public int getBuildDay() {
        return buildDay;
    }

    public int getBuildYear() {
        return buildYear;
    }

    public GffCExoLocString getDescription() {
        return description;
    }

    public ErfType getType() {
        return type;
    }

    @Override
    public Set<ResourceID> getResourceIDs(){
        return Collections.unmodifiableSet( resources.keySet() );
    }

    /**
     * @return null if no such resource exists in this erf
     * */
    @Override
    public InputStream getResource( ResourceID id ) throws IOException{
        Object o = resources.get( id );
        if ( o != null ){
            if ( o instanceof ResourceListEntry )
                return getStream( (ResourceListEntry) o );
            else if ( o instanceof File )
                return getStream( (File) o );
            else if ( o instanceof InputStream )
                return (InputStream) o;
        }
        return null;
    }

    @Override public MappedByteBuffer getResourceAsBuffer( ResourceID id ) throws IOException{
        Object o = resources.get( id );
        if ( o != null ){
            if ( o instanceof ResourceListEntry ){
                ResourceListEntry rle = (ResourceListEntry) o;
                return raf.getChannel().map( FileChannel.MapMode.READ_ONLY, rle.offset, rle.size );
            }
            if ( o instanceof File ){
                try (final RandomAccessFile r = new RandomAccessFile((File)o, "r")) {
                    // MappedByteBuffer will be valid even after close file, that created it
                    return r.getChannel().map( FileChannel.MapMode.READ_ONLY, 0, r.length() );
                }
            }
            /*
            else if ( o instanceof InputStream )
                return (InputStream) o;
             */
        }
        return null;
    }

    private InputStream getStream( ResourceListEntry rle ) throws IOException{
        return new RafInputStream( raf, rle.offset, rle.offset + rle.size );
    }

    private InputStream getStream( File f ) throws IOException{
        return new FileInputStream( f );
    }

    @Override
    public void close() throws IOException{
        if (raf!=null) raf.close();
    }

    /**
     * @return size of resource, -1 if no such resource exists
     * */
    @Override
    public int getResourceSize( ResourceID id ){
        Object o = resources.get(id);
        if ( o == null )
            return -1;
        int r = -1;
        if ( !isFileResource( id ) ){
            r= ((ResourceListEntry) o).size;
        } else {
            File f = (File) o;
            r = (int)f.length();
        }
        return r;
    }

    /**
     * write erf file.
     * */
    public void write() throws IOException{
        write( file );
    }

    /**
     * write this erf file to a new location.
     * */
    public void write( File outputFile ) throws IOException{
        this.file=outputFile;

        String fileType = type.typeString;
        String version = null;
        switch( nwnVersion ){
            case NWN1 : { version = "V1.0"; break; }
            case NWN2 : { version = "V1.1"; break; }
        }
        final int resrefsize = ResRefUtil.resRefSize(nwnVersion);

        int languageCount = description.getSubstringCount();
        byte[] locStringData = getLocStringData(description, nwnVersion);
        int localizedStringSize = locStringData.length - 12;
        int entryCount = resources.size();
        int offsetToLocalizedString = 160; // starts directly after header
        int offsetToKeyList = offsetToLocalizedString + localizedStringSize;
        int offsetToResourceList =
                offsetToKeyList + (entryCount*(resrefsize+8)); // note : this is not the case in files created by the toolset
        int descriptionStrRef = description.getStrRef();
        //buildYear = 0;
        //buildDay = 0;
        File tmpErf = File.createTempFile( TMPFILEPREFIX + file.getName(), "" );
        byte[] buf = new byte[32000];
        try{
            try (final RandomAccessFile out = new RandomAccessFile( tmpErf, "rw" )) {
                out.write( fileType.getBytes() );
                out.write( version.getBytes() );
                writeIntLE( out, languageCount );
                writeIntLE( out, localizedStringSize );
                writeIntLE( out, entryCount );
                writeIntLE( out, offsetToLocalizedString );
                writeIntLE( out, offsetToKeyList );
                writeIntLE( out, offsetToResourceList );
                Calendar rightNow = Calendar.getInstance();
                writeIntLE( out, rightNow.get( Calendar.YEAR ) - 1900 );
                writeIntLE( out, rightNow.get( Calendar.DAY_OF_YEAR ) );
                writeIntLE( out, descriptionStrRef );
                out.write( buf,0,116 ); // should be all 0

                // write localized strings
                out.write( locStringData, 12, locStringData.length -12 );

                int offsetToResourceData = offsetToResourceList + (entryCount*8);
                int resIdCounter = 0;
                int offset = offsetToResourceData;
                byte[] zero = new byte[resrefsize];
                for (final ResourceID id : resources.keySet()) {
                    // write key list entry
                    out.seek( offsetToKeyList + (resIdCounter*(resrefsize+8)) );

                    byte[] nameBytes = id.getName().getBytes("ASCII");
                    int nameLength = Math.min(
                            nameBytes.length,
                            resrefsize);
                    out.write( nameBytes, 0, nameLength );
                    out.write( zero,0,resrefsize-nameLength );
                    writeIntLE( out, resIdCounter );
                    out.write(id.getType() & 255);
                    out.write((id.getType() >> 8) & 255);
                    out.write( zero,0,2 );

                    //write resource data
                    int resourceSize = 0;
                    out.seek(offset);
                    try (final InputStream is = getResource( id )) {
                        int len = 0;
                        while ( (len=is.read(buf))!=-1 ){
                            resourceSize += len;
                            out.write( buf,0,len );
                        }
                    }

                    //write resource list entry
                    out.seek( offsetToResourceList + (8*resIdCounter) );
                    writeIntLE( out, offset );
                    writeIntLE( out, resourceSize );

                    // update resource map
                    // this should not interfere with the iterator, as all ids are already in the key set
                    if ( !isFileResource(id) )
                        resources.put( id, new ResourceListEntry(offset, resourceSize) );

                    offset+=resourceSize;
                    resIdCounter++;
                }
            }

            // copy temp file
            if ( raf!=null ) raf.close();
            try (final FileInputStream is = new FileInputStream( tmpErf );
                 final FileOutputStream fos = new FileOutputStream( file );
                 final FileChannel rc = is.getChannel();
                 final FileChannel wc = fos.getChannel()
            ) {
                wc.transferFrom( rc, 0, rc.size() );
                fos.flush();
            }
            raf = new RandomAccessFile( file, "r" );
        } finally {
            tmpErf.delete();
        }
    }

    /**
     * remove a resource from this erf
     * @return true if the resource was removed, false if there was no such resource
     * */
    public boolean remove( ResourceID id ){
        return resources.remove( id ) != null;
    }

    /**
     * adds file as resource under the given resource id, replace resource with same id
     * */
    public void putResource( ResourceID id, File file ){
        resources.put( id, file );
    }

    private void putResource( ResourceID id, InputStream is ){
        resources.put( id, is );
    }

    /**
     * rename a given resource.
     * @param resourceName the new name of the resource ( without type extension ! )
     * @return the new ResourceId for the renamed resource or null if the file doesn't contain the given resource
     * */
    public ResourceID renameResource( ResourceID id, String resourceName ){
        if ( !resources.containsKey( id ) )
            return null;
        ResourceID nId = new ResourceID( resourceName, id.getType() );
        if ( !nId.equals( id ) ){
            resources.put( nId, resources.get(id) );
            resources.remove( id );
        }
        return nId;
    }

    /**
     * adds all resource from erf to this file, if they do not already exist in this file and rewrites this erf
     * */
    public void merge( ErfFile erf ) throws IOException{
        for (final ResourceID id : erf.resources.keySet()) {
            if ( !resources.keySet().contains(id) )
                putResource( id, erf.getResource( id ) );
        }
        write();
    }

    /**
     * adds a file as a resource, replace resource with same id
     * @return ResourceID under which the new resource was stored
     * */
    public ResourceID putResource( File file ){
        ResourceID id = ResourceID.forFile( file );
        resources.put( id, file );
        return id;
    }

    /**
     * return an OutputStream for adding a new Resource, closing the returned OutputStream
     * rewrites this erf file
     * */
    public OutputStream put( ResourceID id ) throws IOException{
        File f = File.createTempFile( TMPFILEPREFIX, id.toFileName() );
        f.deleteOnExit();
        putResource( id, f );
        return new FileOutputStream( f ){
            @Override
            public void close() throws IOException{
                super.close();
                ErfFile.this.write();
            };
        };
    }

    @Override
    public boolean contains( ResourceID id ){
        return resources.get( id ) != null;
    }

    /**
     * @return
     */
    public File getFile() {
        return file;
    }

    /**
     * @param type
     */
    public void setType(ErfType type) {
        this.type = type;
    }

    /**
     * (convenience method)
     * Extracts all files to a directory
     * @param outputDir all files are extracted to this directory
     * */
    public void extractToDir( File outputDir ) throws IOException{
        if ( !outputDir.exists() ) outputDir.mkdirs();
        //byte[] buf = new byte[64000];
        for (final ResourceID id : getResourceIDs()) {
            writeStreamToFile(
                    getResource( id ),
                    new File( outputDir, id.toFileName() )
            );
        }
    }

    /**
     * (convenience method)
     * Extracts a resource to a temp file. The file can optionally replace the resource in the erf so a call to <code>write()</code> will write the contents of the file to the erf file.
     * The file will be deleted on system exit.
     * @param id the ID of the resource to be extracted
     * @param replaceWithFile if true the extracted file will replace the resource contained in the erf when the erf file is written again
     * @return null if no resource with the given id exists, otherwise return a File object pointing to the extracted resource
     * @see #write()
     * */
    public File extractAsTempFile( ResourceID id, boolean replaceWithFile ) throws IOException{
        InputStream is = getResource( id );
        if ( is == null ) return null;
        File f = File.createTempFile( TMPFILEPREFIX, file.getName()+"_"+id.toFileName() );
        f.deleteOnExit();
        writeStreamToFile( is, f );
        if ( replaceWithFile ) putResource( id, f );
        return f;
    }

    /**
     * extract resource with given id to directory.
     * @param id the ID of the resource to be extracted
     * @param directory
     * @return null if no resource with the given id exists, otherwise return a File object pointing to the extracted resource
     * */
    public File extractToDir( ResourceID id, File directory ) throws IOException{
        InputStream is = getResource( id );
        if ( is == null ) return null;
        if ( !directory.exists() ) directory.mkdirs();
        File f = new File( directory, id.toFileName() );
        writeStreamToFile( is, f );
        return f;
    }

    private static final byte[] streamBuffer = new byte[64000];
    /**
     * writes stream to file & close stream
     * */
    private static synchronized void writeStreamToFile( InputStream is, File file ) throws IOException{
        try (final FileOutputStream fos = new FileOutputStream( file );
             final BufferedOutputStream os = new BufferedOutputStream( fos )
        ) {
            int len;
            while ( (len=is.read(streamBuffer))!=-1 )
                os.write( streamBuffer,0,len );
            os.flush();
        }
        is.close();
    }

    /**
     * @return true if resource was added with putResource( ResourceID, File )
     * */
    public boolean isFileResource( ResourceID id ){
        Object o = resources.get(id);
        return ( o!=null && o instanceof File );
    }

    public static byte[] getLocStringData(
            GffCExoLocString cels, Version nwnVersion) {
        int stringdatasize = 0;
        try {
            for (int i = 0; i < cels.getSubstringCount(); i++)
                stringdatasize
                    += cels.getSubstring(i).string.getBytes(
                        cels.getSubstring(i).language.getEncoding()).length;
            int datasize = 12 + stringdatasize + 8 * cels.getSubstringCount();
            byte[] ret = new byte[datasize];
            ByteBuffer b = ByteBuffer.wrap(ret);
            b.order(ByteOrder.LITTLE_ENDIAN);
            b.putInt(datasize - 4);
            b.putInt(cels.getStrRef());
            b.putInt(cels.getSubstringCount());

            for (int i = 0; i < cels.getSubstringCount(); i++) {

                int languageId = 0;
                switch (nwnVersion) {
                    case NWN1 : {
                        languageId =
                                (cels.getSubstring(i).language.getCode() * 2)
                                + cels.getSubstring(i).gender;
                        break;
                    }
                    case NWN2 : {
                        languageId = cels.getSubstring(i).language.getCode();
                        break;
                    }
                }
                b.putInt( languageId );

                byte[] stringBytes = cels.getSubstring(i).string.getBytes(
                        cels.getSubstring(i).language.getEncoding());
                b.putInt(stringBytes.length);

                b.put( stringBytes );
            }
            return ret;
        } catch (UnsupportedEncodingException uee) {
            throw new Error("Error", uee); // should never happen
        }
    }

    public static void main( String[] args ) throws Exception{
        if ( args.length < 2 ){
            System.out.println( "erffile.java extract, create or list hak file\n(-c|-x|-l) <hak file> <dir>" );
            return;
        }
        File f = new File( args[1] );
        if ( args[0].equals("-x") ){
            File out = new File( args[2] );
            new ErfFile( f ).extractToDir( out );
        } else if ( args[0].equals("-c") ){
            ErfFile erf = new ErfFile(f, HAK, new GffCExoLocString("foo") );
            File out = new File( args[2] );
            for (final File file : out.listFiles()) {
                if (file.isFile()) {
                    erf.putResource(file);
                }
            }
            erf.write();
        } else if ( args[0].equals("-l") ){
            ErfFile erf = new ErfFile(f);
            for (final ResourceID id : erf.getResourceIDs()) {
                System.out.println("erffile.java 679 "+ id);
            }
        }
                /*
                extractErf( f, out );
                ErfFile erf = new ErfFile(f);
                erf.putResource( new ResourceID( "capture", "txt"), new File("/e/capture.txt") );

                erf.write( new File("foo.mod") );
                erf.close();
                extractErf( new File("foo.mod"), new File("foo_out") );
                 */

        // create file test
                /*
                GffCExoLocString desc = new GffCExoLocString("foo");
                desc.addSubstring( new CExoLocSubString( "hello world", NwnLanguage.ENGLISH, CExoLocSubString.GENDER_MASC ) );
                ErfFile erf = new ErfFile( f, MOD, desc );
                File[] files = out.listFiles();
                for ( int i = 0; i < files.length; i++ ){
                        if ( files[i].isFile() )
                                erf.putResource( ResourceID.forFile( files[i] ), files[i] );
                }
                erf.write( erf.getFile() );
                 */
    }

    public Version getVersion(){
        return nwnVersion;
    }

    /**
     * @param string
     */
    public void setDescription(GffCExoLocString string) {
        description = string;
    }

        /* (non-Javadoc)
         * @see org.jl.nwn.resource.NwnRepository#getResourceLocation(org.jl.nwn.resource.ResourceID)
         */
    @Override
    public File getResourceLocation(ResourceID id) {
        return contains(id)? file : null;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public long lastModified(ResourceID id) {
        if ( contains(id) ){
            if ( isFileResource( id ) )
                return ((File)resources.get(id)).lastModified();
            else
                return file.lastModified();
        }
        return super.lastModified(id);
    }

    @Override
    public OutputStream putResource(ResourceID id)
    throws IOException, UnsupportedOperationException {
        return put( id );
    }

}