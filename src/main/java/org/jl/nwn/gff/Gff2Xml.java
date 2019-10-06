package org.jl.nwn.gff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Converts a {@link GffContent} or a {@link GffField} object into a dom
 * {@code Node} object.
 */
public class Gff2Xml {

    private Gff2Xml() {}

    static Document doc = null;

    public static Document convertToXml( GffField field ) throws ParserConfigurationException{
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        doc.appendChild(doc.createProcessingInstruction("xml-stylesheet", "href='mygffstyle_v2.css' type='text/css'"));
        Element root = mkElement(doc, field);
        root.setAttribute( "xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance" );
        root.setAttribute( "xsi:noNamespaceSchemaLocation", "gff.xsd" );
        return doc;
    }

    public static Document convertToXml( GffContent c ) throws ParserConfigurationException{
        return convertToXml(c.getTopLevelStruct());
    }

    protected static Element mkStructElement( Node el, GffStruct struct ){
        Element structElement = doc.createElement( "Struct" );
        el.appendChild(structElement);
        if ( struct.getLabel().length() > 0 )
            structElement.setAttribute( "label", struct.getLabel() );
        structElement.setAttribute( "sid", Integer.toString( struct.getId() ) );
        for ( int i = 0; i < struct.getSize(); i++ ){
            mkElement( structElement, struct.getChild(i) );
        }
        return structElement;
    }

    protected static Element mkListElement( Node el, GffList list ){
        Element listElement = doc.createElement( "List" );
        el.appendChild(listElement);
        listElement.setAttribute( "label", list.getLabel() );
        for ( int i = 0; i < list.getSize(); i++ ){
            mkStructElement( listElement, list.get(i) );
        }
        return listElement;
    }


    protected static Element mkFieldElement( Node el, GffField field ){
        Element fieldElement = doc.createElement( field.getTypeName() );
        el.appendChild(fieldElement);
        if ( field.getType() != GffCExoLocString.SUBSTRINGTYPE )
            fieldElement.setAttribute("label", field.getLabel());
        return fieldElement;
    }

    public static Element mkElement( Node el, GffField field ){

        if ( field.isIntegerType() )
            return mkIntegerElement( el, (GffInteger) field );
        if ( field.isDecimalType() )
            return mkDecimalElement( el, field );
        if ( field.getType() == Gff.VOID )
            return mkVoidElement( el, (GffVoid) field );
        if ( field instanceof GffCResRef )
            return mkResRefElement( el, (GffCResRef) field );
        if ( field instanceof GffCExoString )
            return mkCExoStringElement( el, (GffCExoString) field );
        if ( field instanceof GffCExoLocString )
            return mkCExoLocStringElement( el, (GffCExoLocString) field );
        if ( field.getType() == GffCExoLocString.SUBSTRINGTYPE )
            return mkCExoLocSubstringElement( el, (CExoLocSubString) field );
        if ( field instanceof GffStruct )
            return mkStructElement( el, (GffStruct) field );
        if ( field instanceof GffList )
            return mkListElement( el, (GffList) field );
        return null;
    }

    protected static Element mkIntegerElement( Node el, GffInteger field ){
        Element e = mkFieldElement( el, field );
        e.setTextContent( field.getData().toString() );
        return e;
    }

    protected static Element mkDecimalElement( Node el, GffField field ){
        Element e = mkFieldElement( el, field );
        e.setTextContent( field.getData().toString() );
        return e;
    }

    protected static Element mkCExoStringElement( Node el, GffCExoString field ){
        Element e = mkFieldElement( el, field );
        e.setTextContent( field.getData() );
        return e;
    }

    protected static Element mkCExoLocStringElement( Node el, GffCExoLocString field ){
        Element e = mkFieldElement( el, field );
        e.setAttribute( "strRef", Integer.toString( field.getStrRef() ) );
        for (final CExoLocSubString sub : field) {
            mkCExoLocSubstringElement(e, sub);
        }
        return e;
    }

    protected static Element mkCExoLocSubstringElement( Node el, CExoLocSubString field ){
        Element subElement = mkFieldElement( el, field );
        subElement.setAttribute( "language", Integer.toString( field.language.getCode() ) );
        subElement.setAttribute( "gender", Integer.toString( field.gender ) );
        subElement.setTextContent( field.string );
        return subElement;
    }

    protected static Element mkResRefElement( Node el, GffCResRef field ){
        Element e = mkFieldElement( el, field );
        e.setTextContent( field.getResRef() );
        return e;
    }

    protected static Element mkVoidElement( Node el, GffVoid field ){
        Element e = mkFieldElement( el, field );
        final StringBuilder sb = new StringBuilder();
        byte[] b = field.getData();
        for ( int i = 0; i < b.length; i++ ){
            int v = b[i] < 0 ? 256 + b[i] : b[i];
            if ( v < 16 )
                sb.append("0");
            sb.append( Integer.toHexString( v ) );
        }
        e.setTextContent( sb.toString() );
        return e;
    }
}
