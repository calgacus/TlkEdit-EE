package org.jl.nwn.twoDa;

import java.io.BufferedInputStream;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeMap;

import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jl.nwn.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class TwoDaMetaData{

    /** Map containing all loaded table name<->meta data mapping. */
    private static TreeMap<String, TwoDaMetaData> metaDataMap = new TreeMap<>();

    private static final HashSet<String> blockedEditors = new HashSet<>();

    private static String metaDataPrefix = "meta/";

    HashMap<String, ColumnMetaData> map = new HashMap<>();
    private String tablename;
    private URL helpfile = null;

    public void put( String columnLabel, ColumnMetaData d ){
        map.put( columnLabel, d );
    }

    public ColumnMetaData get( String columnLabel ){
        return map.get( columnLabel );
    }

    public static boolean useEditorClass( Class<?> c ){
        return !blockedEditors.contains( c.getName() );
    }

    public boolean useEditor( String columnLabel ){
        final ColumnMetaData meta = map.get(columnLabel);
        return meta == null ? false : meta.useEditor;
    }

    public static TwoDaMetaData forTableName( String tablename, Version v ){
        String res = metaDataPrefix + tablename + "." + v + ".meta.xml";
        res = res.toLowerCase().replaceAll("\\s","");
        final TwoDaMetaData meta = metaDataMap.get( res );
        if (meta != null) {
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
        metaDataMap.put(res, data);
        return data ;
    }

    private static TwoDaMetaData readMetaData( URL url ){
        TwoDaMetaData data = new TwoDaMetaData();
        try (final BufferedInputStream is = new BufferedInputStream( url.openStream() )) {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse( is );
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
                    final Class<?> editorClass = Class.forName( attrClass.getNodeValue() );
                    final Constructor<?> cons = editorClass.getConstructor( new Class[]{Element.class} );
                    cMeta.editor = (TableCellEditor) cons.newInstance( new Object[]{ (Element)n } );

                    if ( n.getAttributes().getNamedItem( "use" ) != null ){
                        cMeta.useEditor = Boolean.parseBoolean(n.getAttributes().getNamedItem( "use" ).getNodeValue());
                    }
                }
                list =  columnNode.getElementsByTagName( "renderer" );
                if ( list.getLength() > 0 ){
                    Node n = list.item(0);
                    System.out.println( "setting up renderer for column : " + columnLabel );
                    final Class<?> editorClass = Class.forName( n.getAttributes().getNamedItem( "class" ).getNodeValue() );
                    final Constructor<?> cons = editorClass.getConstructor( new Class[]{Element.class} );
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
        public Class<?> columnClass;
        public int position;
        public boolean useEditor = true;
        ColumnMetaData(){}
        ColumnMetaData(TableCellEditor ed, TableCellRenderer r, String tt, String desc, Class<?> c) {
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
