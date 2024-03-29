import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;
import java.io.BufferedReader;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

import org.apache.commons.text.*;


public class PreloadClass {
    private SevenZFile sevenZFile;
    private FileHandler fileHandler;

    private Set<Integer> questionsId = new HashSet<>();

    private byte[] bytes;

    private String bytesString = "";

    private String line = "";

    private int lengthRead;
    private BufferedReader reader = null;

    private DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    private InputSource is = new InputSource();

    private Document doc = null;

    private String[] textInsideCodeTags = null;

    private String tags = null;

    private boolean positiveTags = false;

    private boolean negativeTags = false;

    private int questionAnswerId = 0;

    private String parentOrChildId = null;

    private boolean parentHasExpectedFlags = false;

    private ArrayList<String> passedStatements = new ArrayList<>();

    private ArrayList<Integer> passedIds = new ArrayList<>();

    private ArrayList<String> positiveT;
    private ArrayList<String> negativeT;


    public PreloadClass(String input, String output, ArrayList<String> positiveT, ArrayList<String> negativeT) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.fileHandler = new FileHandler(output+".xml");
        this.sevenZFile = new SevenZFile(new File(input));
        this.positiveT = positiveT;
        this.negativeT = negativeT;
    }

    //method used to get information from XML file such as statement ID, tags that this question contains etc ...
    private boolean getInfoFromXmlFile() throws IOException, SAXException {
        parentHasExpectedFlags = false;

        is.setCharacterStream(new StringReader(line));

        doc = db.parse(is);

        tags = doc.getDocumentElement().getAttribute("Tags");
        positiveTags = false;
        negativeTags = false;
        for(String pTag : positiveT){
            if(tags.contains(pTag)){
                positiveTags = true;
                break;
            }
        }
        for(String nTag : negativeT){
            if(tags.contains(nTag)){
                negativeTags = true;
                break;
            }
        }
        questionAnswerId = Integer.parseInt(doc.getDocumentElement().getAttribute("Id"));
        parentOrChildId = doc.getDocumentElement().getAttribute("PostTypeId");

        if (parentOrChildId.equals("2")) {
            int parentId = Integer.parseInt(doc.getDocumentElement().getAttribute("ParentId"));
            if (questionsId.contains(parentId)) {
                parentHasExpectedFlags = true;
            }
        }
        return parentHasExpectedFlags || (positiveTags && !negativeTags);
    }

    //method that removes comments from statement for faster parsing (can be disabled)
    private String removeComments(String currentLine) {

        currentLine = removeSpecificComment(currentLine, "<-", "\n");
        currentLine = removeSpecificComment(currentLine, "<-", "</statement>");
        currentLine = removeSpecificComment(currentLine, "/*", "*/");

        //Check if modifiedLine contains some -- in single brackets, if yes, don't remove -- comments
        if(currentLine.equals(removeSpecificComment(currentLine, "'-", "-'"))){
            currentLine = removeSpecificComment(currentLine, "---", "\n");
            currentLine = removeSpecificComment(currentLine, "--", "\n");
            currentLine = removeSpecificComment(currentLine, "--", "</statement>");
        }
        return currentLine;

    }

    //helping method for removeComments()
    private String removeSpecificComment(String currentLine, String commentType, String closureType) {
        if (currentLine.contains(commentType)) {
            String[] textWithoutComments = StringUtils.substringsBetween(currentLine, commentType, closureType);
            if (textWithoutComments != null && textWithoutComments.length > 0) {
                Arrays.sort(textWithoutComments, Comparator.comparingInt(String::length).reversed());
                for (String text : textWithoutComments) {
                    currentLine = currentLine.replace(commentType + text + closureType, "\n");
                }
            }
        }
        return currentLine;
    }

    //method that replace unnecessary symbols in XML file
    private String replaceMostCommonUnicodeChars(String currentLine) {

        currentLine = currentLine.replaceAll("‘", "'");
        currentLine = currentLine.replaceAll("’", "'");
        currentLine = currentLine.replaceAll("′", "'");
        currentLine = currentLine.replaceAll("…", "...");
        currentLine = currentLine.replaceAll(" "," ");
        currentLine = currentLine.replaceAll("&nbsp;"," ");
        currentLine = currentLine.replaceAll("&nbsp"," ");
        currentLine = currentLine.replaceAll("&quot;", "\"");
        currentLine = currentLine.replaceAll("&apos;", "'");
        currentLine = currentLine.replaceAll("<br>", " ");
        currentLine = currentLine.replaceAll("</br>", " ");
        currentLine = currentLine.replaceAll("<br/>", " ");
        currentLine = currentLine.replaceAll("<br />", " ");
        currentLine = currentLine.replaceAll("<pre>", " ");
        currentLine = currentLine.replaceAll("</pre>", " ");
        currentLine = currentLine.replaceAll("<p>", " ");
        currentLine = currentLine.replaceAll("</p>", " ");

        currentLine = currentLine.replaceAll("\\P{Print}", "undef");


        return currentLine;
    }

    //method used for saving statement
    private void modifyAndSaveOrDiscardStatement() {
        if (textInsideCodeTags != null && textInsideCodeTags.length > 0) {
            for (String txt : textInsideCodeTags) {
                //this condition ensures that we won't catch and work with something like this = "We are selecting..... FROM"
                if ((txt.toLowerCase().contains("select ") || txt.toLowerCase().contains("select\n")) && !txt.toLowerCase().contains("new sqlcommand") && !txt.contains("cmd.commandtext")) {
                    txt = StringEscapeUtils.unescapeXml(txt);
                    //added option to remove all comments for later faster parsing speed
                    txt = removeComments(txt);

                    txt = txt.replaceAll("\n","&#xA;");

                    txt = replaceMostCommonUnicodeChars(txt);

                    if (parentOrChildId.equals("1")) {
                        //save question ID of Parent ticket
                        questionsId.add(questionAnswerId);
                    }
                    passedStatements.add(txt);
                    passedIds.add(questionAnswerId);

                }
            }
        }
    }

    //method that reads chunk of data line by line
    private void readAllLinesFromChunkOfData() throws IOException, SAXException {
        while ((line = reader.readLine()) != null) {

            if (line.contains("<") && line.contains("/>")) {
                bytesString = "";

                //continue if parent ticket has required tags or this ticket has valid tags
                if (getInfoFromXmlFile()) {
                    //get text from <code> elements
                    textInsideCodeTags = StringUtils.substringsBetween(doc.getDocumentElement().getAttribute("Body"), "<code>", "</code>");

                    //save statement containing SELECT and if required modify unnecessary unicode characters
                    modifyAndSaveOrDiscardStatement();
                }
            } else {
                //ensure that if there is something left from reading we save before the next chunk of data;
                bytesString = line;
            }
        }
        //close BufferedReader
        reader.close();
    }

    //main method
    public void run() throws Exception {
        try {
            //print file info
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            System.out.println("Name: " + entry.getName());
            System.out.println("Size : " + entry.getSize());
            System.out.println("--------------------------------");
            long start = System.currentTimeMillis();
            lengthRead = (int) (entry.getSize() * 0.001);
            bytes = new byte[lengthRead];

            //read by chunks of data
            while (sevenZFile.read(bytes, 0, lengthRead) != -1) {

                bytesString = bytesString.concat(new String(bytes));
                bytes = new byte[lengthRead];

                //initialize BufferedReader by our line
                reader = new BufferedReader(new StringReader(bytesString));

                //read every line
                readAllLinesFromChunkOfData();


                if (passedStatements.size() > 10000) {
                    //save our statements to file
                    fileHandler.addToXML(passedStatements, passedIds,null,null,0);
                    passedStatements = new ArrayList<>();
                    passedIds = new ArrayList<>();
                }
                if ((entry.getSize() - sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount()) > 0 && (entry.getSize() - sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount()) < lengthRead) {
                    lengthRead = (int) (entry.getSize() - sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount());
                    bytes = new byte[(int) (entry.getSize() - sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount())];
                }
            }
            if (passedStatements.size() > 0) {
                //save our statements to file
                fileHandler.addToXML(passedStatements, passedIds,null,null,0);
            }
            System.out.println("Finished preParsing");
            long end = System.currentTimeMillis();
            System.out.println("PreParse time ="+ ((end - start)/60000)+" minutes");

            sevenZFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}