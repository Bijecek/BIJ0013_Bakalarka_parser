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
import java.util.ArrayList;

public class XMLHandler {
    private DocumentBuilder documentBuilder;
    private Document doc;
    private Node root;
    private Element newserver;
    private TransformerFactory transformerFactory = TransformerFactory.newInstance();
    private Transformer transformer;
    private File f_tmp;
    private File f;

    public XMLHandler(String output) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        this.documentBuilder = documentBuilderFactory.newDocumentBuilder();

        this.doc = this.documentBuilder.newDocument();
        Element rootElement = doc.createElement("statements");
        doc.appendChild(rootElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);

        this.f_tmp = new File("src/prettyprint.xslt");


        this.doc = this.documentBuilder.parse(output);
        this.root = this.doc.getFirstChild();
        this.f = new File(output);

    }

    public void addToXML(ArrayList<String> text, String path, ArrayList<Integer> id) throws IOException, SAXException, TransformerException {
        this.transformerFactory = TransformerFactory.newInstance();
        this.transformer = this.transformerFactory.newTransformer(new StreamSource(this.f_tmp.getAbsolutePath()));
        this.transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        this.transformer.setOutputProperty(OutputKeys.INDENT, "yes");


        int index = 0;
        for (String i : text) {
            this.newserver = this.doc.createElement("statement");
            this.newserver.setTextContent(i);
            this.root.appendChild(this.newserver);
            this.newserver.setAttribute("id", String.valueOf(id.get(index)));
            index++;
        }

        this.transformer.transform(new DOMSource(this.doc), new StreamResult(f.getAbsolutePath()));


        this.newserver = null;
        this.transformer = null;
    }
}
