import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
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

import static java.lang.System.exit;
import static java.time.temporal.ChronoUnit.MINUTES;

class Global_savetext{
    String original_statement;
    String temp_text;
    String temp_id;
    ArrayList<String> wrong_statements = new ArrayList<>();
    ArrayList<Integer> wrong_Ids = new ArrayList<>();
    ArrayList<String> correct_statements = new ArrayList<>();
    ArrayList<Integer> correct_Ids = new ArrayList<>();

    String statement_For_Parser;

    boolean repair_Helped = true;

    boolean isRepaired = false;

    boolean ready_For_Parse = false;



    boolean isVisited=false;


    public Global_savetext() {

    }
    public String getText(){
        return temp_text;
    }

}

public class ParserClass implements ANTLRErrorListener{
    static Global_savetext save = new Global_savetext();
    static boolean isAlreadyWritten=false;

    public int test_id;

    public int number_help;
    XML_handler xml_handler;

    XML_handler xml_handler_correct;

    String[] repair_Options;
    int repair_row;
    int repair_charpos;
    Mssql parser;
    ArrayList<String> allowed_Repairs = new ArrayList<>();



    public ParserClass(int test_id) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.number_help = test_id+1;
        this.xml_handler = new XML_handler("preparsed_statements_wrongaa"+number_help+".xml");
        this.xml_handler_correct = new XML_handler("preparsed_statements_correct"+number_help+".xml");
        this.test_id = test_id;
    }
    public ArrayList<String> comma_or_something_infront(String search_string, String statement_text, boolean add_space_infront){
        int index=0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        while((index = statement_text.toLowerCase().indexOf(search_string,searchIndex)) != -1) {

            for (int i = index - 1; i >= 0; i--) {
                if (statement_text.charAt(i) == ',') {
                    modified = true;
                    modified_statement.setCharAt(i,' ');
                    break;
                } else if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n') {
                    break;
                }
            }
            //test na napr. SELECT ASCFROM
            if(add_space_infront) {
                if (index > 0 && statement_text.charAt(index - 1) != ' ' && statement_text.charAt(index - 1) != '\n') {
                    modified_statement.insert(index-1,' ');
                    modified = true;

                }
            }
            searchIndex = index + search_string.length();
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }
    public ArrayList<String> by_after_order_group(String search_string, String statement_text){
        int index=0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        statement_text = statement_text.toLowerCase();
        while((index = statement_text.indexOf(search_string,searchIndex)) != -1) {

            if((search_string.equals("group ") || (search_string.equals("\ngroup "))) && statement_text.substring(searchIndex,index).contains("within ")){
                searchIndex = index + search_string.length()-1;
            }
            else{
                for(int i=(index+search_string.length()-1);i<statement_text.length();i++){
                    if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n') {
                        if(i+1 != statement_text.length() && statement_text.charAt(i) != 'b' && statement_text.charAt(i+1) != 'y'){
                            modified_statement.insert(i-1, " BY");
                            modified = true;
                            break;
                        }
                        else if(statement_text.charAt(i) == 'b' && statement_text.charAt(i+1) == 'y' ){
                            break;
                        }
                    }
                }
                searchIndex = index + search_string.length()-1;
            }
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }
    public ArrayList<String> top_check(String search_string, String statement_text){
        int index=0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        while((index = statement_text.indexOf(search_string,searchIndex)) != -1) {

            for(int i=(index+search_string.length()-1);i<statement_text.length();i++){
                if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n' && !Character.isDigit(statement_text.charAt(i))) {
                    if(i+2 != statement_text.length() && statement_text.charAt(i) != '*' && (Objects.equals(nearestSubstring(i, statement_text), "FROM") || Objects.equals(nearestSubstring(i, statement_text), ","))){
                        modified_statement.insert(i, "* ");
                        modified = true;
                        break;
                    }
                    else{
                        break;
                    }
                }
            }
            searchIndex = index + search_string.length()-1;
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }
    public String nearestSubstring(int start_Index, String text){
        String nearest = new String();
        boolean found = false;
        for(int i=start_Index;i<text.length();i++){
            if (text.charAt(i) != '\n' && text.charAt(i) != ' ') {
                nearest += text.charAt(i);
                found = true;
            }
            else{
                if(found){
                    break;
                }
            }
        }
        return nearest;
    }
    public void run() throws Exception {
        try {
            //xml_handler = new XML_handler("preparsed_statements_wrong.xml");
            BufferedReader read;
            if(this.test_id == 0) {
                read = new BufferedReader(new FileReader("preparsed_statements_withid_final1_updated_8.xml"));
            }
            else{
                read = new BufferedReader(new FileReader("preparsed_statements_wrongaa"+this.test_id+".xml"));
            }
            int is_correct = 1;
            int is_wrong = 1;
            //read first line (xml declaration)
            read.readLine();
            String line="";
            StringBuilder leftOver_line= new StringBuilder();
            String statement_text = null;
            CharStream codePointCharStream = null;
            Hello lexer = null;
            CommonTokenStream tokens = null;
            //Mssql parser = null;
            ANTLRErrorListener errorListener = null;
            ParseTree tree = null;
            ParserATNSimulator sim = null;

            LexerATNSimulator simLex = null;
            long statement_id = 0;
            int counter = 0;
            DFA[] decisionToDFA = null;
            //PredictionContextCache _sharedContextCache = null;
            PredictionContextCache  _sharedContextCache = new PredictionContextCache();


            int strednik_mistake = 0;
            int from_mistake = 0;
            int from_mistake_lower = 0;
            int where_mistake = 0;
            int where_mistake_lower = 0;
            int form_mistake=0;
            int groupby_mistake = 0;
            int groupby_mistake_lower = 0;
            int orderby_mistake = 0;
            int orderby_mistake_lower = 0;
            boolean statement_Modified=false;
            int left_right_brackets = 0;
            ArrayList<String> modified_statement = new ArrayList<>();
            long number_of_total_statements = 0;
            long startTime = System.currentTimeMillis();
            int cursor_for=0;
            int top_check = 0;
            int is_corrected = 0;
            int comma_infront_as = 0;
            int comma_infront_bracket = 0;
            int auto_brackets = 0;
            int auto_beginend = 0;
            int auto_comma = 0;
            int auto_brackets2 = 0;

            allowed_Repairs.add("(");
            allowed_Repairs.add(")");
            allowed_Repairs.add("BEGIN");
            allowed_Repairs.add("END");
            allowed_Repairs.add(",");
            allowed_Repairs.add("SET");


            while((line=read.readLine())!=null){
                //System.out.println(line);
                counter++;
                if(line.contains("<statement") && !line.contains("<statements>") && line.contains("</statement>")){
                    if(!leftOver_line.isEmpty()){
                        line=leftOver_line+line;
                    }

                    save.temp_id = StringUtils.substringBetween(line, "<statement id=\"", "\">");

                    statement_text =  StringUtils.substringBetween(line, "<statement id=\""+save.temp_id+"\">", "</statement>");//doc.getDocumentElement().getAttribute("statement");//.replaceAll("<.*?>|&quot", "");
                    if(statement_text == null){
                        break;
                    }
                    statement_text = statement_text.replaceAll("&gt;",">").replaceAll("&lt;","<");
                    statement_text = statement_text.replaceAll("&amp;amp","&");

                    if(this.test_id == 0) {
                        statement_Modified = false;
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_text = statement_text.replaceAll("</br>", " ");
                        statement_text = statement_text.replaceAll("</pre>", " ");
                        statement_Modified = true;

                    }

                    if(this.test_id == 1){

                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;

                    }

                    if(this.test_id == 2){

                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;

                        if(statement_text.toLowerCase().contains("from")){
                            modified_statement = comma_or_something_infront("from",statement_text,true);
                            if(Objects.equals(modified_statement.get(1), "true")){
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                from_mistake++;
                            }
                        }

                        if(statement_text.toLowerCase().contains(" form ")){
                            statement_text = statement_text.replaceAll(" form ", " FROM ");
                            statement_Modified = true;
                            form_mistake++;
                        }

                    }
                    if(this.test_id == 3){
                        //korektni WHERE
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;

                        if(statement_text.toLowerCase().contains("where")){
                            modified_statement = comma_or_something_infront("where",statement_text, true);
                            if(Objects.equals(modified_statement.get(1), "true")){
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                where_mistake++;
                            }
                        }
                    }
                    if(this.test_id == 4){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;


                        if(statement_text.toLowerCase().contains("group ") ){
                            modified_statement = by_after_order_group("group ",statement_text);
                            if(Objects.equals(modified_statement.get(1), "true")){
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                groupby_mistake++;
                            }

                        }

                    }
                    if(this.test_id == 5){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;

                        if(statement_text.toLowerCase().contains("order ")){
                            modified_statement = by_after_order_group("order ",statement_text);
                            if(Objects.equals(modified_statement.get(1), "true")){
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                orderby_mistake++;
                            }
                        }
                    }

                    if(this.test_id == 6){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;
                        if(statement_text.toLowerCase().contains(" top ")){
                            modified_statement = top_check(" top ",statement_text);
                            if(Objects.equals(modified_statement.get(1), "true")){
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                top_check++;
                            }
                        }
                    }
                    if(this.test_id == 7){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        save.original_statement = statement_text;
                        save.original_statement = save.original_statement.replaceAll("\n","&#10;");

                        statement_Modified = false;
                        if(statement_text.contains("(") || statement_text.contains(")") ){
                            int left = StringUtils.countMatches(statement_text, "(");
                            int right = StringUtils.countMatches(statement_text, ")");
                            if(left != right){
                                if (left > right) {
                                    int index = statement_text.lastIndexOf("(");
                                    statement_text = statement_text.substring(0, index) + ' ' + statement_text.substring(index + 1);
                                    statement_Modified = true;
                                } else {
                                    int index = statement_text.indexOf(")");
                                    statement_text = statement_text.substring(0, index) + ' ' + statement_text.substring(index + 1);
                                    statement_Modified = true;
                                }
                                left_right_brackets++;
                            }
                        }

                    }
                    if(this.test_id == 8){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        save.original_statement = statement_text;
                        save.original_statement = save.original_statement.replaceAll("\n","&#10;");
                        statement_Modified = false;
                        if(statement_text.contains("(") || statement_text.contains(")") ){
                            int left = StringUtils.countMatches(statement_text, "(");
                            int right = StringUtils.countMatches(statement_text, ")");
                            if(left != right){
                                if (left > right && right != 0) {
                                    int index = statement_text.lastIndexOf("(");
                                    int index_r = statement_text.indexOf(")")+1;
                                    if(Objects.equals(nearestSubstring(index_r, statement_text), "FROM") || Objects.equals(nearestSubstring(index_r, statement_text), "from") ||Objects.equals(nearestSubstring(index_r, statement_text), ",")){
                                        statement_text = statement_text.substring(0, index_r) + ") " + statement_text.substring(index_r + 1);
                                        statement_Modified = true;
                                        left_right_brackets++;

                                    }
                                }
                            }
                        }
                    }
                    if(this.test_id == 9){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;
                        if((statement_text.contains("PROCEDURE") || statement_text.contains("procedure")) && (statement_text.contains("AS") || statement_text.contains("as")) ){
                            if(statement_text.contains("AS")) {
                                modified_statement = comma_or_something_infront("AS", statement_text, false);
                                if (Objects.equals(modified_statement.get(1), "true")) {
                                    statement_text = modified_statement.get(0);
                                    statement_Modified = true;
                                    comma_infront_as++;
                                }
                            }
                            if(statement_text.contains("as")) {
                                modified_statement = comma_or_something_infront("as", statement_text, false);
                                if (Objects.equals(modified_statement.get(1), "true")) {
                                    statement_text = modified_statement.get(0);
                                    statement_Modified = true;
                                    comma_infront_as++;
                                }
                            }
                        }
                    }
                    if(this.test_id == 10){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = false;

                        if(statement_text.contains(")")){
                            modified_statement = comma_or_something_infront(")", statement_text, false);
                            if (Objects.equals(modified_statement.get(1), "true")) {
                                statement_text = modified_statement.get(0);
                                statement_Modified = true;
                                comma_infront_bracket++;
                            }
                        }


                    }
                    if(this.test_id == 11){
                        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
                        statement_Modified = true;

                    }


                    save.statement_For_Parser = statement_text;
                    codePointCharStream = CharStreams.fromString(statement_text);
                    lexer = new Hello(codePointCharStream);

                    //remove cmd output for lexer
                    lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

                    tokens = new CommonTokenStream(lexer);
                    parser = new Mssql(tokens);

                    //parser.addParseListener(new MssqlBaseListener());
                    errorListener = this; //new ParserClass()
                    //parser.addErrorListener(errorListener);

                    //remove cmd output for parser
                    parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

                    parser.addErrorListener(errorListener);
                    //added
                    parser.setBuildParseTree(false);

                    decisionToDFA=  parser.getInterpreter().decisionToDFA;
                    _sharedContextCache = new PredictionContextCache();
                    sim = new ParserATNSimulator(
                            parser, parser.getATN(), decisionToDFA, _sharedContextCache);

                    parser.setInterpreter(sim);

                    parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
                    parser.removeErrorListeners();
                    parser.setErrorHandler(new BailErrorStrategy());

                    isAlreadyWritten = false;

                    //optimize heap usage
                    //L for long
                    //System.out.println(Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory());
                    //700000000

                    statement_text = statement_text.replaceAll("&","&amp");
                    statement_text = statement_text.replaceAll("\n","&#10;");

                    save.temp_text = statement_text;


                    if(Integer.parseInt(save.temp_id) != 3454268 && Integer.parseInt(save.temp_id) != 11636147 && Integer.parseInt(save.temp_id) != 11670234 && Integer.parseInt(save.temp_id) != 28905401) {

                        if (statement_Modified) {
                            try {
                                tree = parser.tsql_file();
                            } catch (ParseCancellationException p) {
                                if (!save.isVisited) {

                                    tokens.seek(0);
                                    parser.reset();
                                    parser.addErrorListener(errorListener);
                                    parser.setBuildParseTree(false);
                                    parser.setErrorHandler(new DefaultErrorStrategy());

                                    // full now with full LL(*)
                                    parser.getInterpreter().setPredictionMode(PredictionMode.LL);
                                    tree = parser.tsql_file();
                                    if(this.test_id == 11 && this.repair_Options != null){
                                        save.ready_For_Parse = true;
                                        for(String repair : this.repair_Options){
                                            int row_count = 1;
                                            for(int i=0;i<save.statement_For_Parser.length();i++){
                                                if(save.statement_For_Parser.charAt(i) == '\n'){
                                                    row_count++;
                                                }
                                                if(row_count == this.repair_row){
                                                    int char_index=0;
                                                    for(int j=i;j<save.statement_For_Parser.length();j++){
                                                        if(char_index == this.repair_charpos){
                                                            StringBuilder repaired_statement = new StringBuilder(save.statement_For_Parser);
                                                            repaired_statement.insert(j," "+repair+" ");
                                                            statement_text = repaired_statement.toString();

                                                            save.temp_text = statement_text.replaceAll("&","&amp").replaceAll("\n","&#10;");;
                                                            break;
                                                        }
                                                        char_index++;
                                                    }
                                                    break;
                                                }
                                            }
                                            if(statement_text.replaceAll("&#10;","\n").equals(save.statement_For_Parser)){
                                                break;
                                            }
                                            parser.reset();
                                            codePointCharStream = CharStreams.fromString(statement_text);
                                            lexer = new Hello(codePointCharStream);

                                            //remove cmd output for lexer
                                            lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

                                            tokens = new CommonTokenStream(lexer);
                                            parser = new Mssql(tokens);

                                            //added
                                            parser.setBuildParseTree(false);
                                            parser.removeErrorListener(ConsoleErrorListener.INSTANCE);
                                            parser.addErrorListener(errorListener);
                                            parser.setErrorHandler(new DefaultErrorStrategy());
                                            save.repair_Helped = true;
                                            tree = parser.tsql_file();
                                            if(save.repair_Helped){
                                                //System.out.println("Repaired");
                                                save.isRepaired = true;
                                                save.repair_Helped = false;
                                                if(Objects.equals(repair, ")") || Objects.equals(repair, "(")){
                                                    auto_brackets++;
                                                }
                                                else if(Objects.equals(repair, "BEGIN") || Objects.equals(repair, "END")){
                                                    auto_beginend++;
                                                }
                                                else if(Objects.equals(repair, ",")){
                                                    auto_comma++;
                                                }
                                                else if(Objects.equals(repair, "SET")){
                                                    auto_brackets2++;
                                                }
                                                break;
                                            }
                                        }

                                        this.repair_Options = null;
                                        save.ready_For_Parse = false;
                                    }
                                }
                            }

                            if (!save.isVisited || save.isRepaired) {
                                is_corrected++;
                                save.correct_statements.add(save.temp_text);
                                save.correct_Ids.add(Integer.parseInt(save.temp_id));
                                save.isRepaired = false;

                            } else {
                                if(this.test_id != 7 && this.test_id != 8 && this.test_id != 11) {
                                    save.wrong_statements.add(save.temp_text);
                                }
                                else if (this.test_id == 11){
                                    save.wrong_statements.add(save.statement_For_Parser.replaceAll("&","&amp").replaceAll("\n","&#10;"));
                                }
                                save.wrong_Ids.add(Integer.parseInt(save.temp_id));
                            }
                        } else {
                            save.wrong_statements.add(save.temp_text);
                            save.wrong_Ids.add(Integer.parseInt(save.temp_id));
                        }

                    }
                    if(statement_text.length() > 15000 || Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 800000000 /*Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory() < 100000000*/){
                        parser.getInterpreter().clearDFA();

                        lexer.getInterpreter().clearDFA();
                    }

                    if(save.isVisited){
                        is_wrong+=1;
                    }
                    else{
                        is_correct+=1;
                    }
                    save.isVisited = false;

                    leftOver_line = new StringBuilder();
                }
                else if(!line.contains("<statements>") && !line.contains("</statements>")){
                    System.out.println("SOMETHING WRONG");
                    leftOver_line.append(line);
                }

                if(save.wrong_statements.size()  > 1000){
                    this.xml_handler.addToXML(save.wrong_statements,"preparsed_statements_wrongaa"+this.number_help+".xml", save.wrong_Ids);
                    this.xml_handler_correct.addToXML(save.correct_statements,"preparsed_statements_correct"+this.number_help+".xml", save.correct_Ids);

                    number_of_total_statements += save.wrong_statements.size();
                    save.wrong_statements = new ArrayList<>();
                    save.wrong_Ids = new ArrayList<>();
                    save.correct_statements = new ArrayList<>();
                    save.correct_Ids = new ArrayList<>();
                    counter = 0;
                    //long endTime = System.currentTimeMillis();

                    /*
                    System.out.println(
                            "Time : "
                                    + (endTime - startTime) + " ms");

                    startTime = System.currentTimeMillis();

                     */

                }
            }
            if(save.wrong_statements.size() > 0){
                this.xml_handler.addToXML(save.wrong_statements,"preparsed_statements_wrongaa"+this.number_help+".xml", save.wrong_Ids);
                this.xml_handler_correct.addToXML(save.correct_statements,"preparsed_statements_correct"+this.number_help+".xml", save.correct_Ids);

                number_of_total_statements += save.wrong_statements.size();
                save.wrong_statements = new ArrayList<>();
                save.wrong_Ids = new ArrayList<>();

                save.correct_statements = new ArrayList<>();
                save.correct_Ids = new ArrayList<>();
            }
            read.close();


            //recursion
            if(this.test_id == 1) {
                System.out.println("Strednik");
                System.out.println(strednik_mistake);
            }
            if(this.test_id == 2) {
                System.out.println("FROM & FORM");

                System.out.println(from_mistake);
                System.out.println(from_mistake_lower);

                System.out.println(form_mistake);
            }
            if(this.test_id == 3) {
                System.out.println("WHERE");

                System.out.println(where_mistake);
                System.out.println(where_mistake_lower);

            }
            if(this.test_id == 4) {
                System.out.println("GROUP BY");

                System.out.println(groupby_mistake);
                System.out.println(groupby_mistake_lower);

            }
            if(this.test_id == 5) {
                System.out.println("ORDER BY");

                System.out.println(orderby_mistake);
                System.out.println(orderby_mistake_lower);
            }

            if(this.test_id == 6) {
                System.out.println("TOP FOR");

                System.out.println(top_check);

            }
            if(this.test_id == 7) {
                System.out.println("BRACKETS");

                System.out.println(left_right_brackets);
                left_right_brackets = 0;

            }
            if(this.test_id == 8) {
                System.out.println("BRACKETS");

                System.out.println(left_right_brackets);

            }
            if(this.test_id == 9){
                System.out.println("COMMA INFRONT AS");
                System.out.println(comma_infront_as);
            }
            if(this.test_id == 10){
                System.out.println("COMMA INFRONT BRACKET");
                System.out.println(comma_infront_bracket);
            }
            if(this.test_id == 11){
                System.out.println(auto_brackets);
                System.out.println(auto_beginend);
                System.out.println(auto_comma);
                System.out.println(auto_brackets2);
            }
            System.out.println("Spravnych "+is_corrected);
            is_corrected = 0;

            System.out.println("CELKEM prikazu: "+number_of_total_statements);
            number_of_total_statements = 0;

            this.test_id+=1;
            if(test_id == 12){
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
        if (!save.isVisited || (this.test_id == 11 && !save.isRepaired && !save.ready_For_Parse)) {
            save.repair_Helped = false;

            if(Objects.equals(save.temp_id, "1255108")){
                System.out.println("A");
            }

            if(s.toString().contains("mismatched")){
                String options;
                if(s.contains("{") && s.contains("}")) {
                    options = StringUtils.substringBetween(s, "{", "}");
                }
                else{
                    int index = s.indexOf("expecting");
                    String temp_string = s.substring(index,s.length());
                    options = StringUtils.substringBetween(temp_string, "'", "'");
                }
                if(options != null) {
                    ArrayList<String> final_List = new ArrayList<>();

                    for(String repair : this.allowed_Repairs){
                        if(options.contains("'"+repair+"'")){
                            final_List.add(repair);
                        }
                    }

                    if (final_List.size() != 0) {
                        this.repair_Options = final_List.toArray(new String[0]);
                    }
                }

                if(this.repair_Options == null ){
                    //System.out.println("WRONG");
                }
                this.repair_row = i;
                this.repair_charpos = i1;

            }
            if(s.toString().contains("extraneous")){

            }
            if(s.toString().contains("missing") && !s.contains("no viable alternative")){
                String options;
                int index = s.indexOf("missing");
                if(index != -1) {
                    int index_end = s.indexOf("at");
                    String temp_string = s.substring(index, index_end);
                    options = StringUtils.substringBetween(temp_string, "'", "'");
                    if (Objects.equals(options, "(") || Objects.equals(options, ")") || Objects.equals(options, "BEGIN") || Objects.equals(options, "END") || Objects.equals(options, ",")) {
                        ArrayList<String> final_List = new ArrayList<>(List.of(options));
                        this.repair_Options = final_List.toArray(new String[0]);
                        this.repair_row = i;
                        this.repair_charpos = i1;
                    }
                }
            }

            save.isVisited = true;

            if(this.test_id == 7 || this.test_id == 8){
                save.wrong_statements.add(save.original_statement);
            }

            isAlreadyWritten = true;
        }
        else{
            save.repair_Helped = false;
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
