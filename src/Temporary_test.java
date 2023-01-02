import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTree;

import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.pattern.ParseTreeMatch;
import org.antlr.v4.runtime.tree.pattern.ParseTreePattern;
import org.antlr.v4.runtime.tree.pattern.ParseTreePatternMatcher;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Temporary_test{
    public void run() throws Exception {
        XML_handler xml_handler = new XML_handler("preparsed_statements_withid.xml");
        List<String> questions_ID = new ArrayList<String>();
        try {

            SevenZFile sevenZFile = new SevenZFile(new File("C:\\Users\\sisin\\Downloads\\Posts.7z"));

            SevenZArchiveEntry entry = null;
            entry = sevenZFile.getNextEntry();
            System.out.println("Name: " + entry.getName());
            System.out.println("Size : " + entry.getSize());
            System.out.println("--------------------------------");


            byte[] bytes = new byte[100000000];
            String bytes_str;

            String leftover_line = "";
            double line_count=0;
            int temp_line=0;
            String line = "";
            double size=0;
            //1000000
            //50000000
            while(sevenZFile.read(bytes,0,100000000) != -1) {
                long startTime = System.currentTimeMillis();

                if(leftover_line.contains("/>")){
                    String bytes_temp = new String(bytes);
                    bytes_str ="";
                    bytes_str = bytes_str.concat(leftover_line + "\r");
                    bytes_str = bytes_str.concat(bytes_temp);
                }
                else{
                    bytes_str ="";
                    bytes_str = bytes_str.concat(leftover_line + "\r");
                    bytes_str = bytes_str.concat(new String(bytes));
                }
                Reader inputString = new StringReader(bytes_str);
                BufferedReader reader = new BufferedReader(inputString);


                while(temp_line <= 1){
                    line = reader.readLine();
                    temp_line++;
                }

                String previous_line = "";

                while ((line = reader.readLine()) != null) {

                    if (line.length() > 2 && line.charAt(2) == '<' && line.charAt(line.length() - 1) == '>' && temp_line == 2) {

                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(line));

                        Document doc = db.parse(is);

                        boolean tags_sql_server = doc.getDocumentElement().getAttribute("Tags").contains("sql-server");
                        boolean tags_mssql = doc.getDocumentElement().getAttribute("Tags").contains("mssql");
                        boolean tags_ms_sql_server = doc.getDocumentElement().getAttribute("Tags").contains("ms-sql-server");
                        boolean tags_sql_srever = doc.getDocumentElement().getAttribute("Tags").contains("sql-srever");
                        boolean tags_tsql = doc.getDocumentElement().getAttribute("Tags").contains("tsql");

                        boolean tags_csharp = doc.getDocumentElement().getAttribute("Tags").contains("c%23");
                        boolean tags_csharp2 = doc.getDocumentElement().getAttribute("Tags").contains("c#");
                        boolean tags_php = doc.getDocumentElement().getAttribute("Tags").contains("php");
                        boolean tags_xml = doc.getDocumentElement().getAttribute("Tags").contains("xml");
                        boolean tags_java = doc.getDocumentElement().getAttribute("Tags").contains("java");
                        boolean tags_python = doc.getDocumentElement().getAttribute("Tags").contains("python");

                        boolean parentHasExpectedFlags = false;

                        String q_a_id = doc.getDocumentElement().getAttribute("Id");

                        String question_answer_ID = doc.getDocumentElement().getAttribute("PostTypeId");

                        if(question_answer_ID.equals("2")){
                            String parent_id = doc.getDocumentElement().getAttribute("ParentId");
                            if(questions_ID.contains(parent_id)){
                                parentHasExpectedFlags = true;
                            }
                        }

                        String body = doc.getDocumentElement().getAttribute("Body");
                        String[] text = StringUtils.substringsBetween(body, "<code>", "</code>");
                        line_count++;
                        if(parentHasExpectedFlags || tags_sql_server || tags_mssql || tags_ms_sql_server || tags_sql_srever || tags_tsql && (!tags_csharp2 && !tags_csharp && !tags_php && !tags_xml && !tags_java && !tags_python)){
                            if(text != null && text.length > 0) {
                                for (String txt : text) {
                                    if(txt.contains("SELECT") && !txt.contains("new SqlClient.SqlConnection")) {
                                        txt = txt.replaceAll("&gt;", ">").replaceAll("&lt;", "<");

                                        txt = txt.replaceAll("\n","");

                                        if(question_answer_ID.equals("1")){
                                            if(!questions_ID.contains(q_a_id)){
                                                questions_ID.add(q_a_id);
                                            }
                                        }
                                        xml_handler.addToXML(txt,"preparsed_statements_withid.xml",q_a_id);
                                    }
                                }
                            }
                        }
                        previous_line = "";
                        //line = reader.readLine();

                    } else {
                        previous_line = line;
                        //line = line + reader.readLine();
                    }
                }

                long endTime = System.currentTimeMillis();

                System.out.println(
                        "Time taken to concatenate 100000 Strings using StringBuffer append : "
                                + (endTime - startTime) + " ms");
                leftover_line = previous_line;
                reader.close();

                bytes = new byte[100000000];
            }
            System.out.println("Number of records: "+line_count);

            sevenZFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
