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

import static java.lang.System.gc;
import static java.time.temporal.ChronoUnit.MINUTES;

public class Temporary_test{
    public void run() throws Exception {
        XML_handler xml_handler = new XML_handler("preparsed_statements_withid_final1_updated_8.xml");
        Set<Integer> questions_ID = new HashSet<>();
        try {

            SevenZFile sevenZFile = new SevenZFile(new File("C:\\Users\\sisin\\Downloads\\Posts.7z"));

            SevenZArchiveEntry entry = null;
            entry = sevenZFile.getNextEntry();
            System.out.println("Name: " + entry.getName());
            System.out.println("Size : " + entry.getSize());
            System.out.println("--------------------------------");

            //100000000
            byte[] bytes = new byte[50000000];
            String bytes_str = null;

            String leftover_line = "";
            int temp_line=0;
            String line = "";
            double size=0;
            //1000000
            //50000000
            int length_read = 50000000;

            String bytes_temp = null;
            Reader inputString = null;
            BufferedReader reader = null;
            String previous_line = null;
            DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource is = new InputSource();
            Document doc = null;
            String body = null;
            String[] text = null;
            String tags = null;
            boolean positive_Tags = false;
            boolean negative_Tags = false;
            int q_a_id;
            String question_answer_ID = null;
            boolean parentHasExpectedFlags;
            int parent_id;
            ArrayList<String> passed_Statements = new ArrayList<>();
            ArrayList<Integer> passed_IDs = new ArrayList<>();
            long startTime = System.currentTimeMillis();
            long endTime;
            boolean isthere = false;
            String[] text_without_comments = null;
            int lomitko_count=0;
            while(sevenZFile.read(bytes,0,length_read) != -1) {

/*
                if(leftover_line.contains("/>")){
                    bytes_temp = new String(bytes);
                    bytes_str ="";
                    bytes_str = bytes_str.concat(leftover_line + "\r");
                    bytes_str = bytes_str.concat(bytes_temp);
                }

 */
               // else{
                    bytes_temp = new String(bytes);
                    bytes = new byte[50000000];
                    bytes_str = new String();
                    bytes_str += leftover_line;
                    //bytes_str += bytes_temp;
                    bytes_str = bytes_str.concat(bytes_temp);

                //}

                //inputString = new StringReader(bytes_str);
                //inputString = new StringReader(bytes_str);
                //reader = new BufferedReader(inputString);
                //inputString = new StringReader(bytes_str);
                reader = new BufferedReader(new StringReader(bytes_str));

                while(temp_line <= 1){
                    line = reader.readLine();
                    temp_line++;
                }

                previous_line = "";
                while ((line = reader.readLine()) != null) {

                    if (line.length() > 2 && line.charAt(2) == '<' && line.charAt(line.length() - 1) == '>' && temp_line == 2) {

                        //db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                        is.setCharacterStream(new StringReader(line));

                        doc = db.parse(is);

                        tags = doc.getDocumentElement().getAttribute("Tags");
                        positive_Tags = tags.contains("sql-server") || tags.contains("mssql") || tags.contains("ms-sql-server") || tags.contains("sql-srever") || tags.contains("tsql");
                        negative_Tags = tags.contains("linq") || tags.contains("coldfusion") || tags.contains("db2") || tags.contains("oracle") ||tags.contains("mdx") ||tags.contains("postgresql") || tags.contains("mysql") || tags.contains("cfml") || tags.contains("linq-to-sql") || tags.contains("vbscript") || tags.contains(".net") || tags.contains("c++") || tags.contains("ado") || tags.contains("ms-access") || tags.contains("vba") || tags.contains("asp-classic") || tags.contains("c%23") || tags.contains("c#") || tags.contains("php") || tags.contains("xml") || tags.contains("java") || tags.contains("python");


                        parentHasExpectedFlags = false;

                        q_a_id = Integer.parseInt(doc.getDocumentElement().getAttribute("Id"));

                        //if(q_a_id.equals("43544150") || isthere) {
                            //isthere = true;
                            question_answer_ID = doc.getDocumentElement().getAttribute("PostTypeId");

                            if (question_answer_ID.equals("2")) {
                                parent_id = Integer.parseInt(doc.getDocumentElement().getAttribute("ParentId"));
                                if (questions_ID.contains(parent_id)) {
                                    parentHasExpectedFlags = true;
                                }
                            }

                            if (parentHasExpectedFlags || (positive_Tags && !negative_Tags)) {
                                body = doc.getDocumentElement().getAttribute("Body");
                                text = StringUtils.substringsBetween(body, "<code>", "</code>");
                                if (text != null && text.length > 0) {
                                    for (String txt : text) {
                                        if (txt.toLowerCase().contains("select") && !txt.toLowerCase().contains(" new ")) {
                                            if(txt.contains("// ")){
                                                lomitko_count++;
                                            }
                                            if(txt.contains("---")) {
                                                text_without_comments = StringUtils.substringsBetween(txt, "---", "\n");
                                                if(text_without_comments != null && text_without_comments.length > 0) {
                                                    Arrays.sort(text_without_comments, Comparator.comparingInt(String::length).reversed());
                                                    for (String tmp : text_without_comments) {
                                                        txt = txt.replace("---"+tmp+"\n","\n");
                                                    }
                                                }
                                            }
                                            if(txt.contains("--")) {
                                                text_without_comments = StringUtils.substringsBetween(txt, "--", "\n");
                                                if(text_without_comments != null && text_without_comments.length > 0) {
                                                    Arrays.sort(text_without_comments, Comparator.comparingInt(String::length).reversed());
                                                    for (String tmp : text_without_comments) {
                                                        txt = txt.replace("--"+tmp+"\n","\n");
                                                    }
                                                }
                                                text_without_comments = StringUtils.substringsBetween(txt, "--", "</statement>");
                                                if(text_without_comments != null && text_without_comments.length > 0) {
                                                    Arrays.sort(text_without_comments, Comparator.comparingInt(String::length).reversed());
                                                    for (String tmp : text_without_comments) {
                                                        txt = txt.replace("--"+tmp+"\n","\n");
                                                    }
                                                }
                                            }
                                            if(txt.contains("/*")){
                                                text_without_comments = StringUtils.substringsBetween(txt, "/*", "*/");
                                                if(text_without_comments != null && text_without_comments.length > 0) {
                                                    for (String tmp : text_without_comments) {
                                                        txt = txt.replace("/*"+tmp+"*/","");
                                                    }
                                                }
                                            }

                                            if(txt.toLowerCase().contains("select ") || txt.toLowerCase().contains("select\n")) {
                                                txt = txt.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
                                                txt = txt.replaceAll("&#60;", ">");
                                                txt = txt.replaceAll("‘", "'");
                                                txt = txt.replaceAll("’", "'");
                                                txt = txt.replaceAll("′", "'");
                                                txt = txt.replaceAll("…","...");


                                                if(txt.contains("&#")){
                                                    System.out.println("AA");
                                                }
                                                txt = txt.replaceAll("\n", "&#10;");
                                                txt = txt.replaceAll("\\P{Print}", "undef");

                                                if (question_answer_ID.equals("1")) {
                                                    questions_ID.add(q_a_id);
                                                }
                                                //if(Integer.parseInt(q_a_id) > 53328171) {
                                                passed_Statements.add(txt);
                                                passed_IDs.add(q_a_id);
                                                //}
                                                //xml_handler.addToXML(txt,"preparsed_statements_withid.xml",q_a_id);
                                            }
                                        }
                                    }
                                }
                            }
                            previous_line = "";
                        //}

                    } else {
                        previous_line = line;
                    }
                }



                leftover_line = previous_line;
                reader.close();
                //inputString.close();

                if(passed_Statements.size() > 10000 || length_read == 41935589){
                    endTime = System.currentTimeMillis();
                    xml_handler.addToXML(passed_Statements,"preparsed_statements_withid_final1_updated_8.xml",passed_IDs);
                    passed_Statements = new ArrayList<String>();
                    passed_IDs = new ArrayList<Integer>();
                    System.out.println(
                            "Time : "
                                    + (endTime - startTime) + " ms");
                    startTime = System.currentTimeMillis();
                    gc();
                }
                //bytes = new byte[100000000];
                if(sevenZFile.getStatisticsForCurrentEntry().getUncompressedCount() == Long.parseLong("98400000000")){
                    length_read = 41935589;
                    bytes = new byte[41935589];
                    System.out.println("aa");
                }
            }
            System.out.println("Finished");
            System.out.println("Lomitka:"+ lomitko_count);

            sevenZFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }
}
