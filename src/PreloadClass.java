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

public class PreloadClass {
    XMLHandler xml_handler;

    Set<Integer> questions_ID = new HashSet<>();

    SevenZFile sevenZFile;

    byte[] bytes = new byte[50000000];

    String bytes_str;

    String line = "";

    int length_read = 50000000;

    String bytes_temp = null;

    BufferedReader reader = null;

    String previous_line = null;

    DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

    InputSource is = new InputSource();

    Document doc = null;

    String[] text = null;

    String tags = null;

    boolean positive_Tags = false;

    boolean negative_Tags = false;

    int q_a_id = 0;

    String question_answer_ID = null;

    boolean parentHasExpectedFlags = false;

    int parent_id = 0;

    ArrayList<String> passed_Statements = new ArrayList<>();

    ArrayList<Integer> passed_IDs = new ArrayList<>();

    String[] text_without_comments = null;

    public PreloadClass(String input, String output) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.xml_handler = new XMLHandler(output);
        //C:\Users\sisin\Downloads\Posts.7z
        this.sevenZFile = new SevenZFile(new File(input));
    }

    public void getInfoFromXmlFile() throws IOException, SAXException {
        parentHasExpectedFlags = false;

        is.setCharacterStream(new StringReader(line));

        doc = db.parse(is);

        tags = doc.getDocumentElement().getAttribute("Tags");
        positive_Tags = tags.contains("sql-server") || tags.contains("mssql") || tags.contains("ms-sql-server") || tags.contains("sql-srever") || tags.contains("tsql");
        negative_Tags = tags.contains("linq") || tags.contains("coldfusion") || tags.contains("db2") || tags.contains("oracle") || tags.contains("mdx") || tags.contains("postgresql") || tags.contains("mysql") || tags.contains("cfml") || tags.contains("linq-to-sql") || tags.contains("vbscript") || tags.contains(".net") || tags.contains("c++") || tags.contains("ado") || tags.contains("ms-access") || tags.contains("vba") || tags.contains("asp-classic") || tags.contains("c%23") || tags.contains("c#") || tags.contains("php") || tags.contains("xml") || tags.contains("java") || tags.contains("python");
        q_a_id = Integer.parseInt(doc.getDocumentElement().getAttribute("Id"));
        question_answer_ID = doc.getDocumentElement().getAttribute("PostTypeId");

        if (question_answer_ID.equals("2")) {
            parent_id = Integer.parseInt(doc.getDocumentElement().getAttribute("ParentId"));
            if (questions_ID.contains(parent_id)) {
                parentHasExpectedFlags = true;
            }
        }
    }

    public String removeComments(String currentLine) {
        currentLine = removeSpecificComment(currentLine, "---", "\n");
        currentLine = removeSpecificComment(currentLine, "--", "\n");
        currentLine = removeSpecificComment(currentLine, "--", "</statement>");
        currentLine = removeSpecificComment(currentLine, "/*", "*/");
        return currentLine;

    }

    public String removeSpecificComment(String currentLine, String commentType, String closureType) {
        if (currentLine.contains(commentType)) {
            text_without_comments = StringUtils.substringsBetween(currentLine, commentType, closureType);
            if (text_without_comments != null && text_without_comments.length > 0) {
                Arrays.sort(text_without_comments, Comparator.comparingInt(String::length).reversed());
                for (String tmp : text_without_comments) {
                    currentLine = currentLine.replace(commentType + tmp + closureType, "\n");
                }
            }
        }
        return currentLine;
    }

    public String prepareStatementForXml(String currentLine) {
        currentLine = currentLine.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
        currentLine = currentLine.replaceAll("\n", "&#10;");
        return currentLine;
    }

    public String replaceMostCommonUnicodeChars(String currentLine) {
        currentLine = currentLine.replaceAll("‘", "'");
        currentLine = currentLine.replaceAll("’", "'");
        currentLine = currentLine.replaceAll("′", "'");
        currentLine = currentLine.replaceAll("…", "...");
        currentLine = currentLine.replaceAll("\\P{Print}", "undef");
        return currentLine;
    }

    public void modifyAndSaveOrDiscardStatement() {
        if (text != null && text.length > 0) {
            for (String txt : text) {
                //this condition ensures that we won't catch and work with something like this = "We are selecting..... FROM"
                if ((txt.toLowerCase().contains("select ") || txt.toLowerCase().contains("select\n")) && !txt.toLowerCase().contains(" new ")) {

                    //added option to remove all comments for later faster parsing speed
                    txt = removeComments(txt);

                    txt = prepareStatementForXml(txt);
                    txt = replaceMostCommonUnicodeChars(txt);

                    if (question_answer_ID.equals("1")) {
                        //save question ID of Parent ticket
                        questions_ID.add(q_a_id);
                    }
                    passed_Statements.add(txt);
                    passed_IDs.add(q_a_id);

                }
            }
        }
    }

    public void readAllLinesFromChunkOfData() throws IOException, SAXException {
        while ((line = reader.readLine()) != null) {

            if (line.length() > 2 && line.charAt(2) == '<' && line.charAt(line.length() - 1) == '>') {

                getInfoFromXmlFile();

                //continue if parent ticket has required tags or this ticket has valid tags
                if (parentHasExpectedFlags || (positive_Tags && !negative_Tags)) {
                    //get text from <code> elements
                    text = StringUtils.substringsBetween(doc.getDocumentElement().getAttribute("Body"), "<code>", "</code>");

                    //save statement containing SELECT and if required modify unnecessary unicode characters
                    modifyAndSaveOrDiscardStatement();
                }
            } else {
                //ensure that if there is something left from reading we save before the next chunk of data;
                bytes_str = previous_line;
            }
        }
        //close BufferedReader
        reader.close();
    }

    public void run() throws Exception {
        try {

            //print file info
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();
            System.out.println("Name: " + entry.getName());
            System.out.println("Size : " + entry.getSize());
            System.out.println("--------------------------------");

            long startTime = System.currentTimeMillis();
            long endTime;

            //read by 50 MB
            while (sevenZFile.read(bytes, 0, length_read) != -1) {
                bytes_temp = new String(bytes);
                bytes = new byte[50000000];
                bytes_str = bytes_str.concat(bytes_temp);

                //initialize BufferedReader by our line
                reader = new BufferedReader(new StringReader(bytes_str));
                previous_line = "";

                //read every line
                readAllLinesFromChunkOfData();


                if (passed_Statements.size() > 10000 || length_read == 41935589) {
                    endTime = System.currentTimeMillis();
                    //save our statements to file
                    xml_handler.addToXML(passed_Statements, "preparsed_statements_withid_final1_updated_8.xml", passed_IDs);
                    passed_Statements = new ArrayList<String>();
                    passed_IDs = new ArrayList<Integer>();
                    System.out.println(
                            "Time : "
                                    + (endTime - startTime) + " ms");
                    startTime = System.currentTimeMillis();
                }
                if (sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount() == Long.parseLong("98400000000")) {
                    length_read = 41935589;
                    bytes = new byte[41935589];
                }
            }
            System.out.println("Finished");

            sevenZFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
