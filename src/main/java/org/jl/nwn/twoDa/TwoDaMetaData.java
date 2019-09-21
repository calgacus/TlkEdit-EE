/*
 * Created on 28.11.2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.jl.nwn.twoDa;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jl.nwn.Version;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TwoDaMetaData{
    
    /**
     * map conatining all loaded table<->meta data mapping
     * */
    private static TreeMap metaDataMap = new TreeMap();
    private static final String NOMETADATA = "<no meta data>";
    
    private static final Set blockedEditors = new TreeSet();
    
    private static String metaDataPrefix = "meta/";
    
    HashMap map = new HashMap();
    private String tablename;
    private URL helpfile = null;
    
    public void put( String columnLabel, ColumnMetaData d ){
        map.put( columnLabel, d );
    }
    
    public ColumnMetaData get( String columnLabel ){
        return ( ColumnMetaData ) map.get( columnLabel );
    }
    
    public static boolean useEditorClass( Class c ){
        return !blockedEditors.contains( c.getName() );
    }
    
    public boolean useEditor( String columnLabel ){
        return ( map.get( columnLabel ) == null )?
            false :
            ((ColumnMetaData) map.get( columnLabel )).useEditor;
    }
    
    public static TwoDaMetaData forTableName( String tablename, Version v ){
        String res = metaDataPrefix +
                tablename + "." + v.toString() +".meta.xml";
        res = res.toLowerCase().replaceAll("\\s","");
        Object o = metaDataMap.get( res );
        if ( o != null && o != NOMETADATA ){
            TwoDaMetaData meta = ( TwoDaMetaData ) ((WeakReference)o).get();
            if ( meta != null )
                return meta;
        }

        URL url = TwoDaMetaData.class.getClassLoader().getResource(res);
        if ( url == null ){
            System.out.println( "twodametadata.java failed to load : " + res );
            return null;
        }
        TwoDaMetaData data = readMetaData( url );
        if ( data == null )
            return null;
        data.tablename = tablename;
        metaDataMap.put( res, new WeakReference(data) );
        return data ;
    }
    
    private static TwoDaMetaData readMetaData( URL url ){
        Document doc = null;
        TwoDaMetaData data = new TwoDaMetaData();
        try{
            InputStream is = new BufferedInputStream( url.openStream() );
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( is );
            NodeList helpfileElements = doc.getElementsByTagName( "helpfile" );
            if ( helpfileElements.getLength() > 0 ){
                String loc = helpfileElements.item(0).getAttributes().getNamedItem( "location" ).getNodeValue();
                data.helpfile = new URL( url, loc );
            }
            
            NodeList columnEntries = doc.getElementsByTagName( "column" );
            for ( int i = 0; i < columnEntries.getLength(); i++ ){
                Element columnNode = (Element) columnEntries.item(i);
                ColumnMetaData cMeta = new ColumnMetaData();
                String columnLabel = columnNode.getAttributes().getNamedItem( "label" ).getNodeValue();
                
                String pos = columnNode.getAttributes().getNamedItem( "position" ).getNodeValue();
                if ( pos == null ){
                    System.out.println( "twodametadata.java missing 'position' attribute in column element " + columnLabel );
                    continue;
                }
                cMeta.position = Integer.parseInt( pos );
                

                NodeList list = columnNode.getElementsByTagName( "tooltip" );
                if ( list.getLength() > 0 ){
                    list.item(0).normalize();
                    cMeta.tooltip = list.item(0).getFirstChild().getTextContent();//getNodeValue();

                }
                list =  columnNode.getElementsByTagName( "description" );
                if ( list.getLength() > 0 ){
                    cMeta.description = list.item(0).getFirstChild().getTextContent();//getNodeValue();

                }
                list =  columnNode.getElementsByTagName( "editor" );
                if ( list.getLength() > 0 ){
                    Node n = list.item(0);
                    System.out.println( "setting up editor for column : " + columnLabel );
                    Node attrClass = n.getAttributes().getNamedItem( "class" );
                    if ( attrClass == null ){
                        System.out.println( "missing 'class' attribute on 'editor' element" );
                        continue;
                    }
                    Class editorClass = Class.forName( attrClass.getNodeValue() );
                    Constructor cons = editorClass.getConstructor( new Class[]{Element.class} );
                    cMeta.editor = (TableCellEditor) cons.newInstance( new Object[]{ (Element)n } );
                    
                    if ( n.getAttributes().getNamedItem( "use" ) != null ){
                        cMeta.useEditor = Boolean.valueOf( n.getAttributes().getNamedItem( "use" ).getNodeValue() ).booleanValue();
                    }
                }
                list =  columnNode.getElementsByTagName( "renderer" );
                if ( list.getLength() > 0 ){
                    Node n = list.item(0);
                    System.out.println( "setting up renderer for column : " + columnLabel );
                    Class editorClass = Class.forName( n.getAttributes().getNamedItem( "class" ).getNodeValue() );
                    Constructor cons = editorClass.getConstructor( new Class[]{Element.class} );
                    cMeta.renderer = (TableCellRenderer) cons.newInstance( new Object[]{ (Element)n } );
                }
                data.put( columnLabel, cMeta );
            }
        } catch ( Exception e ){
            e.printStackTrace();
        }
        return data;
    }
    
    public static class ColumnMetaData{
        public TableCellEditor editor = null;
        public TableCellRenderer renderer = null;
        
        public String tooltip = "";
        public String description = "";
        public Class columnClass;
        public int position;
        public boolean useEditor = true;
        ColumnMetaData(){}
        ColumnMetaData( TableCellEditor ed, TableCellRenderer r, String tt, String desc, Class c ){
            editor = ed;
            renderer = r;
            tooltip = tt;
            description = desc;
            columnClass = c;
        }
    }
    
    public String getTablename() {
        return tablename;
    }
    
    public URL getHelpfile() {
        return helpfile;
    }
    
}
