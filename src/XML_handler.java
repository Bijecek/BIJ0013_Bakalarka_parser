import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

public class XML_handler {
    DocumentBuilder documentBuilder;
    public XML_handler(String output) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilder = documentBuilderFactory.newDocumentBuilder();

        Document doc = this.documentBuilder.newDocument();
        Element rootElement = doc.createElement("statements");
        doc.appendChild(rootElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);

        //Document document = documentBuilder.parse("server.xml");
        //Element root = document.getDocumentElement();

    }
    public void addToXML(String text, String path) throws IOException, SAXException, TransformerException {
        Document doc = this.documentBuilder.parse(path);
        Node root=doc.getFirstChild();
        Element newserver=doc.createElement("statement");
        newserver.setTextContent(text);
        root.appendChild(newserver);


        DOMSource source = new DOMSource(doc);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        //Transformer transformer = transformerFactory.newTransformer();
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(new File("src/prettyprint.xslt")));

        StreamResult result = new StreamResult(new File(path));
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);

    }
}
