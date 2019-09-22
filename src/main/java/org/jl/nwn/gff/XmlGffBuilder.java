/*
 * XmlGffBuilder.java
 *
 * Created on 11. Mai 2005, 21:09
 */

package org.jl.nwn.gff;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jl.nwn.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * creates a dom Document object from a binary gff file/stream
 */
public class XmlGffBuilder extends AbstractGffReader<Element, Element, Element>{

    private Document doc;
    private final DocumentBuilder docBuilder;

    protected String schemaLocation = "gff.xsd";

    /** Creates a new instance of XmlGffBuilder */
    public XmlGffBuilder(Version nwnVersion) throws ParserConfigurationException{
        super(nwnVersion);
        docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Override
    public Element mkList(String label) {
        Element list = doc.createElement("List");
        list.setAttribute("label", label);
        return list;
    }

    @Override
    public Element mkFloat(String label, float value) {
        Element f = doc.createElement("Float");
        f.setAttribute("label", label);
        f.setTextContent(Float.toString(value));
        return f;
    }

    @Override
    public Element mkVoid(String label, byte[] value) {
        Element e = doc.createElement("Void");
        e.setAttribute("label", label);
        return e;
    }

    @Override
    public Element mkCExoLocString(String label, int strRef, int[] stringIDs, String[] strings) {
        Element e = doc.createElement("CExoLocString");
        e.setAttribute("label", label);
        e.setAttribute("strRef", Integer.toString(strRef));
        for ( int i = 0; i < stringIDs.length; i++ ){
            Element sub = doc.createElement("CExoLocSubstring");
            sub.setAttribute("language", Integer.toString(stringIDs[i]/2));
            sub.setAttribute("gender", Integer.toString(stringIDs[i]%2));
            sub.setTextContent(strings[i]);
            e.appendChild(sub);
        }
        return e;
    }

    public boolean isGffList(Element field){
        return field.getNodeName().equals("List");
    }

    @Override
    public void structSetID(Element struct, int ID){
        struct.setAttribute("sid", Integer.toString(ID));
    }

    @Override
    public Document load(java.io.File file) throws IOException{
        doc = docBuilder.newDocument();
        return (Document) super.load(file);
    }

    @Override
    public Document load(InputStream is) throws IOException{
        doc = docBuilder.newDocument();
        return (Document) super.load(is);
    }

    @Override
    public Element mkStruct(String label, int structID) {
        Element e = doc.createElement("Struct");
        e.setAttribute("label", label);
        e.setAttribute("sid", Integer.toString(structID));
        return e;
    }

    @Override
    public Element mkDouble(String label, double value) {
        Element f = doc.createElement("Double");
        f.setAttribute("label", label);
        f.setTextContent(Double.toString(value));
        return f;
    }

    @Override
    public Object mkGffObject(Element topLevelStruct, String gffType, java.io.File file){
        doc.appendChild(topLevelStruct);
        topLevelStruct.setAttributeNS("http://www.w3.org/2001/XMLSchema-instance","xsi:noNamespaceSchemaLocation", schemaLocation);
        //topLevelStruct.setAttribute("file", file.getAbsolutePath());
        return doc;
    }

    @Override
    public void structAdd(Element struct, Element field) {
        struct.appendChild(field);
    }

    @Override
    public void listAdd(Element list, Element struct) {
        list.appendChild(struct);
    }

    @Override
    public Element mkCExoString(String label, String value) {
        Element e = doc.createElement("CExoString");
        e.setAttribute("label", label);
        e.setTextContent(value);
        return e;
    }

    @Override
    public Element mkInteger(String label, byte type, java.math.BigInteger value) {
        Element f = doc.createElement(Gff.getTypeName(type));
        f.setAttribute("label", label);
        f.setTextContent(value.toString());
        return f;
    }

    @Override
    public Element mkCResRef(String label, String value) {
        Element e = doc.createElement("CResRef");
        e.setAttribute("label", label);
        e.setTextContent(value);
        return e;
    }

    @Override
    public Element mkVector(String label, float[] value) {
        Element e = doc.createElement("Vector");
        e.setAttribute("label", label);
        e.setTextContent(Arrays.asList(value).toString());
        return e;
    }

    public static void main( String[] args ) throws Exception{
        Transformer trans = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(System.out);
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        XmlGffBuilder builder = new XmlGffBuilder(Version.getDefaultVersion());
        for ( String filename : args ){
            Document doc = builder.load( new File(filename));
            DOMSource source = new DOMSource(doc);
            trans.transform(source, result);
        }
    }

}
