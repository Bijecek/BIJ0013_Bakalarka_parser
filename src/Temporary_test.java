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

class Global_savetext{
    String temp_text;
    boolean isVisited=false;

    int duplicates=0;

    public Global_savetext() {

    }
    public String getText(){
        return temp_text;
    }

}
public class Temporary_test implements ANTLRErrorListener{
    static Global_savetext save = new Global_savetext();
    static boolean isAlreadyWritten=false;
    public static void main(String[] args) throws Exception {
        try {
            //SevenZFile sevenZFile = new SevenZFile(new File("C:\\Users\\sisin\\IdeaProjects\\B_tmp\\src\\randomtext3.7z"));
            SevenZFile sevenZFile = new SevenZFile(new File("C:\\Users\\sisin\\Downloads\\Posts.7z"));

            SevenZArchiveEntry entry = null;
            entry = sevenZFile.getNextEntry();
            System.out.println("Name: " + entry.getName());
            System.out.println("Size : " + entry.getSize());
            System.out.println("--------------------------------");

            int is_correct = 1;
            int is_wrong = 1;


            byte[] bytes = new byte[1000000];
            String bytes_str = new String(bytes);

            String leftover_line = "";
            double line_count=0;
            int temp_line=0;
            String line = "";
            double size=0;
            while(sevenZFile.read(bytes,0,1000000) != -1) {
                //size+=0.002;
                size+=0.001;
//                System.out.printf("%.5f", size);
//                System.out.println(" GB");
                //System.out.println("Number of records: "+line_count);

                if(leftover_line.contains("/>")){
//                           System.out.print("test: ");
//                            System.out.println(leftover_line);
                    String bytes_temp = new String(bytes);

                    bytes_str ="";
                    bytes_str = bytes_str.concat(leftover_line + "\r");
                    bytes_str = bytes_str.concat(bytes_temp);

//                            System.out.print("test orig: ");
//                            System.out.println(new String(bytes));

//                            System.out.print("after merge: ");
//                            System.out.println(bytes_str);
                }
                else{
//                            System.out.print("Leftover: ");
//                            System.out.println(leftover_line);
                    bytes_str ="";
                    bytes_str = bytes_str.concat(leftover_line + "\r");
                    bytes_str = bytes_str.concat(new String(bytes));
                }
                //bytes_str = bytes_str.replace("\r\n", "").replace("\n", "");
//                        System.out.print("Co jde co scan: ");
//                       System.out.println(bytes_str);
//                       System.out.println("Konec scan");
                //Scanner scanner = new Scanner(bytes_str);
                Reader inputString = new StringReader(bytes_str);
                BufferedReader reader = new BufferedReader(inputString);


                while(temp_line <= 1){
                    line = reader.readLine();
                    temp_line++;
                }

                String previous_line = "";

                while ((line = reader.readLine()) != null) {

                    if (line.length() > 2 && line.charAt(2) == '<' && line.charAt(line.length() - 1) == '>' && temp_line == 2) {

//                                    System.out.println(line);

                                /*
                                XMLInputFactory xmlInputFactory =
                                        XMLInputFactory.newInstance();
                                XMLStreamReader xmlStreamReader =
                                        xmlInputFactory.createXMLStreamReader(fileReader);
                                */

                        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

                        InputSource is = new InputSource();
                        is.setCharacterStream(new StringReader(line));

                        //InputStream is = new ByteArrayInputStream(line.getBytes());
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

                        String body = doc.getDocumentElement().getAttribute("Body");//.replaceAll("<.*?>|&quot", "");
                        //NodeList node = doc.getDocumentElement().getElementsByTagName("code");
                        String[] text = StringUtils.substringsBetween(body, "<code>", "</code>");
                        line_count++;
//                                    System.out.print("body: ");
                        if(tags_sql_server || tags_mssql || tags_ms_sql_server || tags_sql_srever || tags_tsql && (!tags_csharp2 && !tags_csharp && !tags_php && !tags_xml && !tags_java && !tags_python)){
                            //tady vybrat vsechny korektni data a vlozit do souboru, ktery nasledne budeme parsrovat, tim padem už tam tahle část s filtry nebude
                            // idealne to udelat jako mezikrok

                            //push request myssql.g4

                            //vytvorit rekurzivni cyklus pro spatne parsry



                            //System.out.println("Body");
                            //System.out.println(body);
                            //System.out.println("Line");
                            //System.out.println(line);
                            if(text != null && text.length > 0) {
                                for (String txt : text) {
                                    //if(txt.contains("SELECT * FROM") && !txt.contains("declare")) {
                                    if(txt.contains("SELECT * FROM") && !txt.contains("new SqlClient.SqlConnection")) {
                                        txt = txt.replaceAll("&gt;",">").replaceAll("&lt;","<");
                                        //System.out.println(txt);

                                            if(txt.charAt(txt.length()-1)!= ';'){
                                                txt+=";";
                                            }
                                            save.temp_text = txt;
                                        CharStream codePointCharStream = CharStreams.fromString(txt);
                                        Hello lexer = new Hello(codePointCharStream);
                                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                                        Mssql parser = new Mssql(tokens);
                                        //parser.addParseListener(new MssqlBaseListener());
                                        ANTLRErrorListener errorListener = new Temporary_test();
                                        parser.addErrorListener(errorListener);

                                            isAlreadyWritten = false;
                                            ParseTree tree = parser.tsql_file();
                                            if(!isAlreadyWritten){
                                                FileWriter fw = new FileWriter("good_SQL.txt", true);
                                                BufferedWriter bw = new BufferedWriter(fw);
                                                bw.write(save.temp_text);
                                                bw.write("----------------------------------------------\n");
                                                bw.newLine();
                                                bw.close();
                                                fw.close();
                                            }

                                                if(save.isVisited){
                                                    is_wrong+=1;
                                                    //System.out.println(txt);

                                                }
                                                else{
                                                    is_correct+=1;
                                                }
                                                save.isVisited = false;
                                                System.out.println(is_wrong);
                                                System.out.println(is_correct);
                                                System.out.println((double)100-(((double)is_wrong *100)/(double)is_correct) + " %");
                                                if(is_correct == 10000){
                                                    return;
                                                }
                                                //System.out.println(tree.toStringTree(parser));
                                                //System.out.println(txt);
                                                //System.out.println("--------------------");



                                    }
                                }
                            }
                        }
                        //System.out.println(body);
                        //System.out.println(line);
                        //tags = false;
                        //tags1 = false;

                        /*
                        CharStream codePointCharStream = CharStreams.fromString(body);
                        Hello lexer = new Hello(codePointCharStream);
                        CommonTokenStream tokens = new CommonTokenStream(lexer);
                        Mssql parser = new Mssql(tokens);
                        parser.addParseListener(new MssqlBaseListener());


                        ParseTree tree = parser.tsql_file();

                        if(body.contains("SELECT * FROM")){
                            System.out.println(body);

                        }

                        //System.out.println(tree.toStringTree(parser));

                        //System.out.println(body);
                        */
                        previous_line = line;
                        line = reader.readLine();

                    } else {
                        previous_line = line;
                        line = line + reader.readLine();
                    }
                }


                leftover_line = previous_line;
                reader.close();


            }
            System.out.println("Number of records: "+line_count);



        /*
                    bytes = new byte[20000];
                    CharStream codePointCharStream = CharStreams.fromString(bytes_str);
                    Hello lexer = new Hello(codePointCharStream);
                    CommonTokenStream tokens = new CommonTokenStream(lexer);
                    Mssql parser = new Mssql(tokens);
                    parser.addParseListener(new MssqlBaseListener());
                    ParseTree tree = parser.tsql_file();
                    System.out.println(tree.toStringTree(parser));
                    return;

         */

            /*CharStream codePointCharStream = CharStreams.fromString(bytes_str);

            //ANTLRInputStream input = new ANTLRInputStream(System.in);

// create a lexer that feeds off of input CharStream
            Hello lexer = new Hello(codePointCharStream);
// create a buffer of tokens pulled from the lexer
            CommonTokenStream tokens = new CommonTokenStream(lexer);

// create a parser that feeds off the tokens buffer
            Mssql parser = new Mssql(tokens);

            parser.addParseListener(new MssqlBaseListener());


            //ParseTree tree = parser.tsql_file(); // begin parsing at init rule

            //System.out.println(tree.toStringTree(parser)); // print LISP-style tree

*/

            sevenZFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }




    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
            if (save.isVisited == false) {
                save.isVisited = true;
                //try {

                    boolean found = false;  // flag for target txt being present
                    /*
                    try (BufferedReader br = new BufferedReader(new FileReader("bad_SQL_select.txt"))) {
                        String line;
                        String line_tmp = "";
                        while ((line = br.readLine()) != null) {  // classic way of reading a file line-by-line
                            line_tmp += line + "\n";
                            if (line_tmp.contains(save.temp_text)) {
                                found = true;
                                br.close();
                                FileWriter fw = new FileWriter("duplicates.txt", false);
                                BufferedWriter bw = new BufferedWriter(fw);
                                save.duplicates++;
                                bw.write(Integer.toString(save.duplicates));
                                bw.newLine();
                                bw.close();
                                fw.close();
                                break;
                            }
                        }
                        save.isVisited = true;
                    } catch (FileNotFoundException fnfe) { */
                //    }

                try {
                    //if (!found) {  // if the text is not found, it has to be written
                    FileWriter fw = new FileWriter("bad_SQL.txt", true);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(save.temp_text);
                    bw.write("nextCode;\n");
                    bw.newLine();
                    bw.close();
                    fw.close();
                    isAlreadyWritten = true;


                    //}
                }
                catch (IOException fnfe){

                }
               // } catch (Exception ee) {
               //     return;
               // }
            }
        //else{
        //    System.out.println("Error message: "+ s);
        //}
    }

    @Override
    public void reportAmbiguity(Parser parser, DFA dfa, int i, int i1, boolean b, BitSet bitSet, ATNConfigSet atnConfigSet) {
    }

    @Override
    public void reportAttemptingFullContext(Parser parser, DFA dfa, int i, int i1, BitSet bitSet, ATNConfigSet atnConfigSet) {

    }

    @Override
    public void reportContextSensitivity(Parser parser, DFA dfa, int i, int i1, int i2, ATNConfigSet atnConfigSet) {

    }
}
