package org.jl.nwn.twoDa;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jl.nwn.twoDa.TwoDaBReader;

public class TwoDaTable {
    protected int[] columnWidth; // column width
    protected String[] columnHeaders;
    protected List<String[]> rows = new ArrayList<String[]>();
    protected String defaultString = null;
    
    protected File file = null;
    
    public static final String BLANK2DAVALUE = "****";
    
    protected static boolean TWODA_TSV = false;
    
    static{
        String p = null;
        if ( ( p = System.getProperty( "tlkedit.2daTsv" )) != null ){
            TWODA_TSV = Boolean.valueOf(p);
        }
    }
    
    public TwoDaTable(String[] headers) {
        columnHeaders = headers;
        columnWidth = new int[headers.length];
        for (int i = 0; i < headers.length; i++)
            columnWidth[i] = headers[i].length() + 1;
    }
    
        /* create empty table with same columns as t
         *
         */
    public TwoDaTable(TwoDaTable t) {
        this(t.columnHeaders);
    }
    
    public TwoDaTable(InputStream is) throws IOException {
        String line = null;
        
        // read 1st line from inputstream
        StringBuilder sb = new StringBuilder();
        int read = 0;
        do{
            while ( (read=is.read())!=-1 && read!=0x0a )
                sb.append((char)read);
            line = sb.toString();
            sb.delete(0, sb.length());
        } while ( line.trim().length() == 0 ); // skip empty lines
        line = line.trim().toLowerCase();
        if (!(line.startsWith("2da ") || line.startsWith("2da\t")) )
            throw new IOException("Not a 2da file !"); // IOException ???
        
        if ( line.equals("2da v2.b") ){ // 2da binary format
            TwoDaTable t = TwoDaBReader.readTwoDaBinary(is,false);
            this.columnHeaders = t.columnHeaders;
            this.columnWidth = t.columnWidth;
            this.rows = t.rows;
        } else{ // regular ASCII
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader in = new BufferedReader(isr);
            // skip empty lines
            while ((line = in.readLine()).trim().equals(""));
            // read default value
            if ( line.toLowerCase().startsWith("default:") ){
                defaultString = line.substring( line.indexOf(':')+1 ).trim();
                System.out.println( "DEFAULT: " + defaultString );
                // skip empty lines
                while ((line = in.readLine()).trim().length() == 0);
            }
                /* String.split version :
                 - split at whitespaces
                 - note that leading whitespace is returned as 1 empty string ( header
                   for 1st column )
                 - no good for String constants using whitespace ( like "hello world" ),
                 */
            columnHeaders = line.split("\\s+");
            //for ( int i = 0; i < columnHeaders.length; i++ ) System.out.println( "<" + columnHeaders[i] +">" );
            columnWidth = new int[this.columnHeaders.length];
            while ((line = in.readLine()) != null) {
                if (line.trim().length() == 0)
                    continue;
                String[] row = split2daLine(line);
                if ( row.length != columnHeaders.length ){
                    String lineMsg = line.length()>20?
                        line.substring(0,30) + "(...)":
                        line;
                    StringBuilder errorMsg = new StringBuilder("Broken 2DA file : ");
                    errorMsg.append("found ").append(row.length).append(" fields (expected ");
                    errorMsg.append(columnHeaders.length).append(") on line \"");
                    errorMsg.append(lineMsg).append("\"");
                    throw new IOException(errorMsg.toString());
                }
                rows.add(row);
            }
            in.close();
            isr.close();
        }
        is.close();
        updateColumnWidth();
    }
    
    public TwoDaTable(File daFile) throws IOException {
        this(new FileInputStream(daFile));
        this.file = daFile;
    }
    
    public static String[] split2daLine(String line) {
        if (line.indexOf('"') != -1) { // damn !
            line = line.trim();
            List<String> row = new ArrayList<String>();
            int pos = 0;
            StringBuilder s = new StringBuilder();
            while (pos < line.length()) {
                while (pos < line.length()
                && Character.isWhitespace(line.charAt(pos)))
                    pos++;
                if (line.charAt(pos) == '"') {
                    s.append(line.substring(pos, line.indexOf('"', pos + 1) + 1));
                    pos = line.indexOf('"', pos + 1) + 1;
                } else
                    while (pos < line.length()
                    && !Character.isWhitespace(line.charAt(pos)))
                        s.append(line.charAt(pos++));
                row.add(s.toString());
                s.delete(0, s.length());
            }
            return row.toArray(new String[row.size()]);
        }
        return line.split("\\s+");
    }
    
    protected String mk2daString(String s) { // enclose String in " if it contains spaces
        if ( s.indexOf(' ') != -1&& (s = s.trim()).indexOf(' ') != -1 ){
            StringBuffer sb = new StringBuffer( s );
            if (!s.startsWith("\""))
                sb.insert( 0, '"' );
            if (!s.endsWith("\""))
                sb.append('"');
            int p = 1;
            while ( (p = sb.indexOf( "\"", p )) != sb.length()-1 )
                sb.setCharAt( p, ' ' );
            return sb.toString();
        }
        return s;
    }
    
    public String getColumnHeader(int column) {
        if (column < 0 || column > columnHeaders.length - 1)
            throw new IllegalArgumentException(
                    "no such column : column number " + column);
        return columnHeaders[column];
    }
    
    public void setColumnHeader(int column, String name) {
        if (column < 0 || column > columnHeaders.length - 1)
            throw new IllegalArgumentException(
                    "no such column : column number " + column);
        if (name.indexOf(" ") != -1)
            throw new IllegalArgumentException("column name must not contain spaces");
        else
            columnHeaders[column] = name;
    }
    
    public int getColumnWidth(int col) {
        return columnWidth[col];
    }
    
    public int getColumnCount() {
        return columnHeaders.length;
    }
    
    public int getRowCount() {
        return rows.size();
    }
    
    // returns index of the column with the given name, -1 if no such column exists
    public int getColumnNumber(String cName) {
        for (int i = 0; i < columnHeaders.length; i++)
            if (cName.equalsIgnoreCase(columnHeaders[i]))
                return i;
        return -1;
    }
    
    public String getValueAt(int row, int col) throws IndexOutOfBoundsException{
        if (row >= rows.size() || col >= columnHeaders.length)
            throw new IndexOutOfBoundsException(
                    "no such cell : " + row + ", " + col);
        return ((String[]) rows.get(row))[col];
    }
    
    public String getValueAt(int row, String columnName) {
        return getValueAt(row, getColumnNumber(columnName));
    }
    
    public int getIntValueAt( int row, int col ){
        String s = null;
        try{
            s = getValueAt(row, col);
        } catch (IndexOutOfBoundsException iobe){
            s = defaultString;
        }
        if ( s == null ){ // invalid row/col & no default value
            return 0;
        } else {
            try{
                if ( s.equals(BLANK2DAVALUE) ){
                    return 0;
                } else {
                    int i = Integer.parseInt(s);
                    return i;
                }
            } catch ( NumberFormatException nfe ){
                return 0;
            }
        }
    }
    
    public int getIntValueAt(int row, String columnName) throws NumberFormatException{
        return getIntValueAt(row, getColumnNumber(columnName));
    }
    
    public void setValueAt(String v, int row, int col)
    throws IllegalArgumentException {
        if (row >= rows.size() || row < 0)
            throw new IllegalArgumentException("no such row : " + row);
        if (col >= columnHeaders.length || col < 0)
            throw new IllegalArgumentException("no such column : " + col);
        v = mk2daString(v);
        ((String[]) rows.get(row))[col] = v;
        // change column width if necessary
        columnWidth[col] =
                (v.length() + 1 > columnWidth[col])
                ? v.length() + 1
                : columnWidth[col];
    }
    
    public void setValueAt(String v, int row, String colName) {
        int col = getColumnNumber(colName);
        if (col == -1)
            throw new IllegalArgumentException("no such column : " + colName);
        setValueAt(v, row, col);
    }
    
    // recompute width of all columns, only used in constructor
    // setValueAt and insertRow update column width as necessary
    protected void updateColumnWidth() {
        System.out.println("updateColumnWidth : " +  columnWidth.length);
        int[] maxWidth = new int[columnWidth.length];
        for (int i = 0; i < maxWidth.length; i++)
            maxWidth[i] = columnHeaders[i].length();
        String[] row;
        for (int i = 0; i < getRowCount(); i++) {
            row = (String[]) rows.get(i);
            for (int j = 0; j < maxWidth.length; j++)
                maxWidth[j] = Math.max(maxWidth[j], row[j].length());
        }
        for (int i = 0; i < maxWidth.length; i++)
            columnWidth[i] = maxWidth[i] + 1;
    }
    
    
    public void writeToFile(File f) throws IOException {
        FileOutputStream fos = null;
        try{
            write( fos = new FileOutputStream(f) );
            this.file = f;
        } finally{
            if ( fos != null )
                fos.close();
        }
    }
    
    public void write( OutputStream os ) throws IOException{
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(os) );
        //updateColumnWidth();
        out.write("2DA V2.0\r\n");
        if ( defaultString != null )
            out.write("DEFAULT: " + defaultString);
        out.write("\r\n");
        
        int maxCWidth = 0;
        for ( int i = 0; i < columnWidth.length; i++ )
            maxCWidth = Math.max( maxCWidth, columnWidth[i] );
        char[] spaces = new char[maxCWidth+1];
        Arrays.fill( spaces, ' ' );
        String white = new String(spaces);
        
        // write header
        for (int i = 0; i < columnHeaders.length; i++) {
            String s = columnHeaders[i];
            out.write(s);
            if ( TWODA_TSV )
                out.write("\t");
            else{
                int fill = columnWidth[i] - s.length();
                if (fill > 0)
                    out.write(white, 0, fill);
            }
        }
        out.write("\r\n");
        // write entries
        String[] row;
        for (int i = 0; i < rows.size(); i++) {
            row = (String[]) rows.get(i);
            for (int j = 0; j < row.length; j++) {
                out.write(row[j]);
                if ( TWODA_TSV )
                    out.write("\t");
                else
                    out.write(white, 0, columnWidth[j] - row[j].length());
            }
            out.write("\r\n"); //out.newLine();
        }
        out.flush();
        out.close();
    }
    
    /**
     * inserts a copy of the given string array at postition rowNumber
     * */
    public void insertRow(String[] row, int rowNumber)
    throws IllegalArgumentException {
        if (row.length != getColumnCount())
            throw new IllegalArgumentException(
                    "error : size of row ("
                    + row.length
                    + ") doesn't match number of columns in this table ( "
                    + getColumnCount()
                    + " )");
        if (rowNumber > rows.size() || rowNumber < 0)
            throw new IllegalArgumentException(
                    "illegal row number : "
                    + rowNumber
                    + "  ( can only insert between existing lines or append )");
        if (row == null)
            throw new IllegalArgumentException("error : argument is null");
        for (int i = 0; i < columnWidth.length; i++)
            columnWidth[i] = Math.max(columnWidth[i], row[i].length() + 1);
        rows.add(rowNumber, row.clone());
    }
    
    public void appendRow(String[] row) {
        insertRow(row, rows.size());
    }
    
    // return an empty row that can be inserted into this 2da table
    public String[] emptyRow() {
        String[] ret = new String[columnHeaders.length];
        //String s = "****";
        for (int i = 0; i < ret.length; i++)
            ret[i] = BLANK2DAVALUE;
        return ret;
    }
    
    public String[] removeRow(int row) {
        if (row < 0 || row > rows.size() - 1)
            throw new IllegalArgumentException(
                    "row value out of range : " + row);
        else{
            String[] r = rows.get(row);
            rows.remove(row);
            return r;
        }
    }
    
    public void insertColumn(int pos, String header, String defaultValue) {
        if (pos < 0 || pos > getColumnCount())
            throw new IllegalArgumentException(
                    "column position out of range : " + pos);
        if (defaultValue == null)
            defaultValue = "****";
        else
            defaultValue = mk2daString(defaultValue);
        int cCount = getColumnCount() + 1;
        String[] newHeaders = new String[cCount];
        for (int i = 0, n = 0; n < cCount; n++)
            if (n == pos) {
            newHeaders[n] = header;
            } else {
            newHeaders[n] = columnHeaders[i];
            i++;
            }
        columnHeaders = newHeaders;
        
        for (int row = 0; row < rows.size(); row++) {
            String[] newRow = new String[cCount];
            String[] oldRow = (String[]) rows.get(row);
            for (int i = 0, n = 0; n < cCount; n++)
                if (n == pos) {
                newRow[n] = defaultValue;
                } else {
                newRow[n] = oldRow[i];
                i++;
                }
            rows.set(row, newRow);
        }
        
        columnWidth = new int[cCount];
        updateColumnWidth();
    }
    
    public void dropColumn(int col) {
        if (col < 0 || col >= getColumnCount())
            throw new IllegalArgumentException(
                    "column position out of range : " + col);
        int colCount = getColumnCount() - 1;
        String[] nHeaders = new String[colCount];
        for (int i = 0, n = 0; i < columnHeaders.length; i++) {
            if (i != col) {
                nHeaders[n] = columnHeaders[i];
                n++;
            }
        }
        columnHeaders = nHeaders;
        String[] oldRow;
        for (int row = 0; row < rows.size(); row++) {
            String[] newRow = new String[colCount];
            oldRow = (String[]) rows.get(row);
            for (int i = 0, n = 0; i < oldRow.length; i++) {
                if (i != col) {
                    newRow[n] = oldRow[i];
                    n++;
                }
            }
            rows.set(row, newRow);
        }
        int[] newCWidth = new int[colCount];
        for (int i = 0, n = 0; i < columnWidth.length; i++) {
            if (i != col) {
                newCWidth[n] = columnWidth[i];
                n++;
            }
        }
        columnWidth = newCWidth;
    }
    
        /*
         * append t to this table, if number == true renumber the new entries, else
         * keep original row number ( = value of 1st column )
         */
    public void append(TwoDaTable t, boolean number) {
        int startSize = getRowCount();
        boolean canAppend = (getColumnCount() == t.getColumnCount());
                /*
                if (!canAppend)
                        System.out.println("--- append by attribute name ---");
                 */
        for (int i = 0; i < t.rows.size(); i++) {
            if (canAppend)
                appendRow((String[]) ((String[]) t.rows.get(i)).clone());
            else {
                appendRow(emptyRow());
                int row = rows.size() - 1;
                for (int col = 0; col < getColumnCount(); col++) {
                    setValueAt(t.getValueAt(i, getColumnHeader(col)), row, col);
                }
            }
            if (number)
                setValueAt(Integer.toString(startSize + i), startSize + i, 0);
        }
    }
    
    public static void main(String args[]) throws Exception {
        //final File daFile = new File( args[0] );
        final File daFile = new File(args[0]);
        final TwoDaTable t = new TwoDaTable(daFile);
        t.write(System.out);
                /*
                System.out.print( daFile.getName().toLowerCase() + " : " );
                String header[] = t.columnHeaders;
                System.out.print( "\" \" " );
                for ( int i = 1; i < header.length; i++ ) System.out.print( header[i] +" " );
                System.out.println();
                 */
    }
    
}