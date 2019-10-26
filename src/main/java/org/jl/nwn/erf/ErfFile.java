package org.jl.nwn.erf;

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
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.nio.file.Files;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
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
 * This class represents an erf file.
 * <p>
 * Differences between V1.0 and V1.1 (nwn2):
 * <ul>
 * <li>header version is V1.1 instead of V1.0 (duh)</li>
 * <li>Localized strings : in V1.1 ERFs the language id is used as it is,
 *     and not as (LanguageID*2+Gender)</li>
 * <li>resource names are 32 instead of 16 characters long</li>
 * </ul>
 */
public class ErfFile extends AbstractRepository{

    private File file;
    private RandomAccessFile raf;
    private GffCExoLocString description;
    private ErfType type;
    private int buildYear;
    private int buildDay;
    /** Map with {@link ResourceListEntry}, {@link File} or {@link InputStream} objects. */
    private final Map<ResourceID, Object> resources = new TreeMap<>();
    private Version nwnVersion;

    private static final String TMPFILEPREFIX = "erftmp_";
    public static final ErfType HAK = new ErfType( "HAK ", "hak" ){
        @Override
        protected void initializeErf(ErfFile erf) {
            final GffCExoLocString desc = new GffCExoLocString("");
            desc.addSubstring(new CExoLocSubString(
                "<HAK NAME>\n<URL>\n<DESCRIPTION>",
                NwnLanguage.ENGLISH,
                Gff.GENDER_MALE
            ));
            erf.setDescription(desc);
        }
    };
    public static final ErfType ERF = new ErfType( "ERF ", "erf" );
    public static final ErfType MOD = new ErfType( "MOD ", "mod / sav" );
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
        protected void initializeErf(ErfFile erf){}
    }

    /**
     * Create a new erf file with the given type and description.
     */
    public ErfFile(File file, ErfType type, GffCExoLocString description, Version nwnVersion) {
        this(type, nwnVersion);
        this.description = description;
        this.file = file;
    }

    public ErfFile(File file, ErfType type, GffCExoLocString description) {
        this(file, type, description, Version.getDefaultVersion());
    }

    public ErfFile(ErfType type, Version nwnVersion) {
        this.type = type;
        this.nwnVersion = nwnVersion;
        type.initializeErf(this);
    }

    public ErfFile(ErfType type){
        this( type, Version.getDefaultVersion() );
    }

    /**
     * Open given file as erf file
     */
    public ErfFile( File erf ) throws IOException{
        this.file = erf;
        if ( !erf.exists() )
            throw new FileNotFoundException( erf.toString() );
        raf = new RandomAccessFile( file, "r" );
        final byte[] buf = new byte[32000];

        raf.read( buf, 0, 4 );
        type = ErfType.forTypeString(new String(buf, 0, 4, US_ASCII));

        raf.read( buf, 0, 4 );
        nwnVersion = determineVersion(buf);

        final int languageCount = readIntLE( raf );
        final int localizedStringSize = readIntLE( raf );
        final int entryCount = readIntLE( raf );
        final int offsetToLocalizedString = readIntLE( raf );
        final int offsetToKeyList = readIntLE( raf );
        final int offsetToResourceList = readIntLE( raf );
        buildYear = readIntLE( raf );
        buildDay = readIntLE( raf );
        final int descriptionStrRef = readIntLE( raf );

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
            final String s = new String(buf, 0, stringSize, lang.getEncoding());

            description.addSubstring(new CExoLocSubString(s, lang, gender));
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

    public GffCExoLocString getDescription() {
        return description;
    }

    public ErfType getType() {
        return type;
    }

    //<editor-fold defaultstate="collapsed" desc="NwnRepository">
    @Override
    public Set<ResourceID> getResourceIDs(){
        return Collections.unmodifiableSet( resources.keySet() );
    }

    /**
     * @return {@code null} if no such resource exists in this erf
     */
    @Override
    public InputStream getResource(ResourceID id) throws IOException {
        Object o = resources.get( id );
        if (o instanceof ResourceListEntry) {
            final ResourceListEntry rle = (ResourceListEntry)o;
            return new RafInputStream( raf, rle.offset, rle.offset + rle.size );
        }
        if (o instanceof File) {
            return new FileInputStream((File)o);
        }
        if (o instanceof InputStream) {
            return (InputStream) o;
        }
        return null;
    }

    @Override
    public void close() throws IOException{
        if (raf!=null) raf.close();
    }

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
     * Return an {@link OutputStream} for adding a new Resource, closing the
     * returned OutputStream rewrites this erf file.
     */
    @Override
    public OutputStream putResource(ResourceID id) throws IOException {
        final File f = File.createTempFile( TMPFILEPREFIX, id.getFileName() );
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

    @Override
    public File getResourceLocation(ResourceID id) {
        return contains(id) ? file : null;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public long lastModified(ResourceID id) {
        final Object o = resources.get(id);
        if (o instanceof File) {
            return ((File)o).lastModified();
        }
        if (o != null) {
            return file.lastModified();
        }
        return super.lastModified(id);
    }
    //</editor-fold>

    /**
     * Write erf file.
     */
    public void write() throws IOException{
        write( file );
    }

    /**
     * Write this erf file to a new location.
     */
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
                        int len;
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
                    if ( !isFileResource(id) ) {
                        resources.put( id, new ResourceListEntry(offset, resourceSize) );
                    }

                    offset+=resourceSize;
                    resIdCounter++;
                }
            }

            // copy temp file
            if ( raf!=null ) raf.close();
            Files.copy(tmpErf.toPath(), file.toPath(), REPLACE_EXISTING);
            raf = new RandomAccessFile( file, "r" );
        } finally {
            tmpErf.delete();
        }
    }

    /**
     * Remove a resource from this erf.
     *
     * @return {@code true} if the resource was removed, {@code false} if there
     *         was no such resource
     */
    public boolean remove( ResourceID id ){
        return resources.remove( id ) != null;
    }

    /**
     * Adds file as resource under the given resource id, replace resource with same id
     */
    public void putResource( ResourceID id, File file ){
        resources.put( id, file );
    }

    /**
     * Rename a given resource.
     *
     * @param newName the new name of the resource ( without type extension ! )
     *
     * @return the new ResourceID for the renamed resource or {@code null} if the
     *         file doesn't contain the given resource
     */
    public ResourceID renameResource(ResourceID id, String newName) {
        if ( !resources.containsKey( id ) )
            return null;
        final ResourceID nId = new ResourceID(newName, id.getType());
        if ( !nId.equals( id ) ){
            resources.put( nId, resources.get(id) );
            resources.remove( id );
        }
        return nId;
    }

    /**
     * Adds all resource from erf to this file, if they do not already exist in
     * this file and rewrites this erf.
     */
    public void merge( ErfFile erf ) throws IOException{
        for (final ResourceID id : erf.resources.keySet()) {
            if (!resources.containsKey(id)) {
                resources.put(id, erf.getResource(id));
            }
        }
        write();
    }

    /**
     * Adds a file as a resource, replace resource with same id.
     *
     * @return ResourceID under which the new resource was stored
     */
    public ResourceID putResource( File file ){
        ResourceID id = ResourceID.forFile( file );
        resources.put( id, file );
        return id;
    }

    public File getFile() {
        return file;
    }

    public void setType(ErfType type) {
        this.type = type;
    }

    /**
     * Extracts all files to a directory.
     *
     * @param outputDir all files are extracted to this directory
     */
    public void extractToDir( File outputDir ) throws IOException{
        if ( !outputDir.exists() ) outputDir.mkdirs();
        for (final ResourceID id : getResourceIDs()) {
            try (final InputStream is = getResource(id)) {
                writeStreamToFile(is, new File(outputDir, id.getFileName()));
            }
        }
    }

    /**
     * Extracts a resource to a temp file. The file can optionally replace the
     * resource in the erf so a call to {@link #write()} will write the contents
     * of the file to the erf file.
     * <p>
     * The file will be deleted on system exit.
     *
     * @param id the ID of the resource to be extracted
     * @param replaceWithFile if {@code true} the extracted file will replace
     *        the resource contained in the erf when the erf file is written again
     *
     * @return {@code null} if no resource with the given id exists, otherwise
     *         return a File object pointing to the extracted resource
     *
     * @throws IOException If resource can not be readed or destination file can
     *         not be created or writed
     */
    public File extractAsTempFile( ResourceID id, boolean replaceWithFile ) throws IOException{
        try (final InputStream is = getResource( id )) {
            if (is == null) return null;

            final File f = File.createTempFile( TMPFILEPREFIX, file.getName()+"_"+id.getFileName() );
            f.deleteOnExit();
            writeStreamToFile( is, f );
            if ( replaceWithFile ) putResource( id, f );
            return f;
        }
    }

    /**
     * Extract resource with given id to directory.
     *
     * @param id the ID of the resource to be extracted
     * @param directory Directory to which save extracted file. If directory do
     *        not exists, it will be created.
     *
     * @return {@code null} if no resource with the given id exists, otherwise
     *         return a File object pointing to the extracted resource
     */
    public File extractToDir( ResourceID id, File directory ) throws IOException{
        try (final InputStream is = getResource(id)) {
            if (is == null) return null;

            if ( !directory.exists() ) directory.mkdirs();
            final File f = new File( directory, id.getFileName() );
            writeStreamToFile( is, f );
            return f;
        }
    }

    /**
     * Writes stream to file.
     */
    private static synchronized void writeStreamToFile( InputStream is, File file ) throws IOException{
        Files.copy(is, file.toPath(), REPLACE_EXISTING);
    }

    /**
     * @return {@code true} if resource was added with {@link #putResource(ResourceID, File)}
     */
    public boolean isFileResource( ResourceID id ){
        Object o = resources.get(id);
        return ( o!=null && o instanceof File );
    }

    public static byte[] getLocStringData(GffCExoLocString cels, Version nwnVersion) {
        int stringdatasize = 0;
        try {
            for (final CExoLocSubString sub : cels) {
                stringdatasize += sub.getBytes().length;
            }
            int datasize = 12 + stringdatasize + 8 * cels.getSubstringCount();
            byte[] ret = new byte[datasize];
            ByteBuffer b = ByteBuffer.wrap(ret);
            b.order(ByteOrder.LITTLE_ENDIAN);
            b.putInt(datasize - 4);
            b.putInt(cels.getStrRef());
            b.putInt(cels.getSubstringCount());

            for (final CExoLocSubString sub : cels) {
                int languageId = 0;
                switch (nwnVersion) {
                    case NWN1 : {
                        languageId = sub.language.getCode() * 2 + sub.gender;
                        break;
                    }
                    case NWN2 : {
                        languageId = sub.language.getCode();
                        break;
                    }
                }
                b.putInt( languageId );

                final byte[] stringBytes = sub.getBytes();
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
    }

    public Version getVersion(){
        return nwnVersion;
    }

    public void setDescription(GffCExoLocString string) {
        description = string;
    }

    /**
     * Analyzes first 4 bytes and returns version that corresponds to them.
     *
     * @param buf Buffer for analyze
     * @return Version or {@code null}, if version is unknown
     */
    private static Version determineVersion(byte[] buf) {
        final String version = new String(buf, 0, 4, US_ASCII);
        if ("V1.0".equals(version)) {
            return Version.NWN1;
        }
        if ("V1.1".equals(version)) {
            return Version.NWN2;
        }
        System.out.println("warning : unknown ERF version " + version);
        return null;
    }
}
