import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.TransformerException;
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
import org.xml.sax.SAXException;

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

//TODO
//commit do orig repository
// vzit i id otazky/odpovedi z orig souboru a natsavit jim priznak
public class ParserClass implements ANTLRErrorListener{
    static Global_savetext save = new Global_savetext();
    static boolean isAlreadyWritten=false;

    public int test_id;

    public int number_help;
    XML_handler xml_handler;
    public ParserClass(int test_id) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.number_help = test_id+1;
        this.xml_handler = new XML_handler("preparsed_statements_wrong"+number_help+".xml");
        System.out.println("A");
        this.test_id = test_id;
    }
    public void run() throws Exception {
        try {
            //xml_handler = new XML_handler("preparsed_statements_wrong.xml");
            BufferedReader read;
            if(this.test_id == 0) {
                read = new BufferedReader(new FileReader("preparsed_statements_2.xml"));
            }
            else{
                read = new BufferedReader(new FileReader("preparsed_statements_wrong"+this.test_id+".xml"));
            }
            int is_correct = 1;
            int is_wrong = 1;
            //read first line (xml declaration)
            read.readLine();
            String line="";
            StringBuilder leftOver_line= new StringBuilder();
            while((line=read.readLine())!=null){
                if(line.contains("</") && !line.contains("<statements>")){
                    if(!leftOver_line.isEmpty()){
                        line+=leftOver_line;
                    }


                        String statement_text =  StringUtils.substringBetween(line, "<statement>", "</statement>");//doc.getDocumentElement().getAttribute("statement");//.replaceAll("<.*?>|&quot", "");
                        if(statement_text == null){
                            break;
                        }
                        statement_text = statement_text.replaceAll("&gt;",">").replaceAll("&lt;","<");

                                    if(this.test_id == 1){
                                        if(statement_text.charAt(statement_text.length()-1)!= ';'){
                                            statement_text+=";";
                                        }
                                    }
                                    save.temp_text = statement_text;
                                    CharStream codePointCharStream = CharStreams.fromString(statement_text);
                                    Hello lexer = new Hello(codePointCharStream);

                                    //remove cmd output for lexer
                                    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

                                    CommonTokenStream tokens = new CommonTokenStream(lexer);
                                    Mssql parser = new Mssql(tokens);
                                    //parser.addParseListener(new MssqlBaseListener());
                                    ANTLRErrorListener errorListener = this; //new ParserClass()
                                    parser.addErrorListener(errorListener);

                                    //remove cmd output for parser
                                    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

                                    //added
                                    parser.setBuildParseTree(false);



                                    isAlreadyWritten = false;

                                    ParseTree tree = parser.tsql_file();
                                    //optimize heap usage
                                    //L for long
                                    //System.out.println(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory());
                                    if(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory() < 50000000){
                                        System.out.println("HEAP CLEARED");
                                        ParserATNSimulator sim = new ParserATNSimulator(parser, parser.getATN(),parser.getInterpreter().decisionToDFA,new PredictionContextCache());
                                        sim.clearDFA();
                                        LexerATNSimulator simLex = new LexerATNSimulator(lexer, lexer.getATN(), lexer.getInterpreter().decisionToDFA, new PredictionContextCache());
                                        simLex.clearDFA();

                                        //parser.reset();
                                        //lexer.reset();

                                    }



                                    if(!isAlreadyWritten){
                                        /*
                                        FileWriter fw = new FileWriter("good_SQL_changes.txt", true);
                                        BufferedWriter bw = new BufferedWriter(fw);
                                        bw.write(save.temp_text);
                                        bw.write("----------------------------------------------\n");
                                        bw.newLine();
                                        bw.close();
                                        fw.close();
                                        */


                                    }

                                    if(save.isVisited){
                                        is_wrong+=1;
                                        //System.out.println(txt);

                                    }
                                    else{
                                        is_correct+=1;
                                    }
                                    save.isVisited = false;

                    leftOver_line = new StringBuilder();
                }
                else if(!line.contains("<statements>")){
                    leftOver_line.append(line);
                }
            }
            read.close();
            System.out.println(((double)is_wrong*100)/(is_correct+is_wrong) +" %");
            System.out.println("RECURSION!");


            //recursion
            this.test_id+=1;
            if(test_id == 3){
                return;
            }
            ParserClass p = new ParserClass(this.test_id);
            p.run();

            }



        catch (Exception e){
            e.printStackTrace();
        }




    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        if (save.isVisited == false) {
            save.isVisited = true;

            try {
                //odkomentovat pozdeji
                this.xml_handler.addToXML(save.temp_text,"preparsed_statements_wrong"+this.number_help+".xml", "a");


                /*
                FileWriter fw = new FileWriter("bad_SQL_changes.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(save.temp_text);
                bw.write("nextCode;\n");
                bw.newLine();
                bw.close();
                fw.close();

                 */
                isAlreadyWritten = true;


                //}
            }
            catch (RuntimeException | IOException xe){
            } catch (TransformerException ex) {
                throw new RuntimeException(ex);
            } catch (SAXException ex) {
                throw new RuntimeException(ex);
            }
        }
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
