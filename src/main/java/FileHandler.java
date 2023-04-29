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

public class FileHandler {
    private  File outputFile;
    private Document doc;
    private Node root;
    private  File prettyPrintFile;

    public FileHandler(String output) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

        this.doc = documentBuilder.newDocument();
        Element rootElement = doc.createElement("statements");
        doc.appendChild(rootElement);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);

        this.prettyPrintFile = new File("src/main/java/prettyprint.xslt");


        this.doc = documentBuilder.parse(output);
        this.root = this.doc.getFirstChild();
        this.outputFile = new File(output);


    }
    public FileHandler(String output, boolean appendToFile) throws IOException {
        FileWriter f = new FileWriter(output,appendToFile);
        this.outputFile = new File(output);
    }

    public void addToXML(ArrayList<String> text, ArrayList<Integer> id,ArrayList<Boolean> repairDone,ArrayList<String> repairs, int testId) throws TransformerException, IOException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer(new StreamSource(this.prettyPrintFile.getAbsolutePath()));
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");


        int index = 0;
        Element newserver;
        for (String i : text) {
            newserver = this.doc.createElement("statement");
            newserver.setTextContent(i);
            this.root.appendChild(newserver);
            newserver.setAttribute("id", String.valueOf(id.get(index)));
            if(testId != 0) {
                if (repairDone.get(index)) {
                    newserver.setAttribute("repairs", repairs.get(index) + " " + testId);
                }
                else{
                    newserver.setAttribute("repairs", repairs.get(index));
                }
            }
            index++;
        }

        transformer.transform(new DOMSource(this.doc), new StreamResult(outputFile.getAbsolutePath()));

    }

    public void addToTXTCorrect(ArrayList<String> text, ArrayList<Integer> id, ArrayList<Boolean> repairDone, ArrayList<String> repairs, int testId) throws TransformerException, IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.outputFile,true));
        int index = 0;
        for (String i : text) {
            String textTmp;
            if(testId != 0) {
                if (repairDone.get(index)) {
                    textTmp = "id=\"" + id.get(index) + "\" repairs=\"" + repairs.get(index) + " "+testId + "\"" + i;
                } else {
                    textTmp = "id=\"" + id.get(index) + "\" repairs=\"" + repairs.get(index) + "\"" + i;
                }
            }
            else{
                textTmp = "id=\""+ id.get(index) +"\""+i;
            }
            bw.write(textTmp);
            bw.newLine();
            index++;
        }
        bw.close();

    }
    public void saveSummaryResults(String wrongFile, ArrayList<String> testCombinations, ArrayList<Integer> numberOfResults) throws IOException {
        int i =0;
        BufferedWriter bw = new BufferedWriter(new FileWriter(this.outputFile,true));
        for(String s : testCombinations){
            bw.write("Test "+s + " Number of successful repairs: "+numberOfResults.get(i));
            bw.newLine();
            i++;
        }
        bw.close();
        int totalRepaired = 0;
        for(int num : numberOfResults){
            totalRepaired+=num;
        }
        int totalStatementsToRepair = 0;
        BufferedReader read = new BufferedReader(new FileReader(wrongFile + "_wrong0.xml"));
        String line;
        while ((line = read.readLine()) != null) {
            if(line.contains("<statement id")){
                totalStatementsToRepair++;
            }
        }
        read.close();
        bw = new BufferedWriter(new FileWriter(this.outputFile,true));
        bw.write("------------------------------------");
        bw.newLine();
        bw.write("Repaired "+totalRepaired+" statements of total "+totalStatementsToRepair+" statements");
        bw.newLine();
        bw.write("Which is "+(((float)totalRepaired*100)/(float)totalStatementsToRepair)+" % success rate");
        bw.newLine();
        bw.write("------------------------------------");
        bw.close();
    }
}
