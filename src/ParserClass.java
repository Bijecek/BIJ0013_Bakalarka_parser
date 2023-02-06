import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ParseTree;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.*;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;


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

    ArrayList<ArrayList<String>> repair_Options = new ArrayList<>();
    ArrayList<String> repairRowAndCharpos = new ArrayList<>();
    //int repair_charpos;
    Mssql parser;
    ArrayList<String> allowed_Repairs = new ArrayList<>();

    String statement_text = null;
    boolean statement_Modified=false;

    ArrayList<String> modified_statement = new ArrayList<>();

    int from_mistake = 0;

    int where_mistake = 0;

    int groupby_mistake = 0;

    int orderby_mistake = 0;

    int top_check = 0;

    int left_right_brackets = 0;

    int comma_infront_as = 0;

    int comma_infront_bracket = 0;

    CharStream codePointCharStream = null;
    Hello lexer = null;
    CommonTokenStream tokens = null;

    ANTLRErrorListener errorListener = null;
    ParseTree tree = null;
    ParserATNSimulator sim = null;

    DFA[] decisionToDFA = null;
    PredictionContextCache  _sharedContextCache = new PredictionContextCache();

    long number_of_total_statements = 0;

    ArrayList<String> repairPermutations = new ArrayList<>();

    ArrayList<Integer> positionsForRepair = new ArrayList<>();





    public ParserClass(int test_id) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.number_help = test_id+1;
        this.xml_handler = new XML_handler("preparsed_statements_wrongaa_delete_fourth"+number_help+".xml");
        this.xml_handler_correct = new XML_handler("preparsed_statements_correct_delete_fourth"+number_help+".xml");
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
    public void getIdAndText(String line){
        save.temp_id = StringUtils.substringBetween(line, "<statement id=\"", "\">");

        statement_text =  StringUtils.substringBetween(line, "<statement id=\""+save.temp_id+"\">", "</statement>");
        statement_text = statement_text.replaceAll("&gt;",">").replaceAll("&lt;","<");
        statement_text = statement_text.replaceAll("&amp;amp","&");
    }
    public void test0NoModification(){
        if(Objects.equals(save.temp_id, "266372")){
            System.out.println("A");
        }
        statement_Modified = false;
        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
        statement_text = statement_text.replaceAll("</br>", " ");
        statement_text = statement_text.replaceAll("</pre>", " ");
        statement_Modified = true;
    }
    public void test1FromModification(){
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
            from_mistake++;
        }
    }
    public void test2WhereModification(){
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
    public void test3GroupModification(){
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
    public void test4OrderModification(){
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
    public void test5TopModification(){
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
    public void test6Brackets1stModification(){
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
    public void test7Brackets2ndModification(){
        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
        save.original_statement = statement_text;
        save.original_statement = save.original_statement.replaceAll("\n","&#10;");
        statement_Modified = false;
        if(statement_text.contains("(") || statement_text.contains(")") ){
            int left = StringUtils.countMatches(statement_text, "(");
            int right = StringUtils.countMatches(statement_text, ")");
            if(left != right){
                if (left > right && right != 0) {
                    int index_r = statement_text.indexOf(")")+1;
                    if(Objects.equals(nearestSubstring(index_r, statement_text).toLowerCase(), "from") || Objects.equals(nearestSubstring(index_r, statement_text), ",")){
                        statement_text = statement_text.substring(0, index_r) + ") " + statement_text.substring(index_r + 1);
                        statement_Modified = true;
                        left_right_brackets++;

                    }
                }
            }
        }
    }
    public void test8AsModification(){
        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
        statement_Modified = false;
        if(statement_text.toLowerCase().contains("procedure") && statement_text.toLowerCase().contains("as")){

            modified_statement = comma_or_something_infront("as", statement_text, false);
            if (Objects.equals(modified_statement.get(1), "true")) {
                statement_text = modified_statement.get(0);
                statement_Modified = true;
                comma_infront_as++;
            }

        }
    }
    public void test9Brackets3rdModification(){
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
    public void test10AdvancedModification(){
        statement_text = statement_text.replaceAll("&amp;#10;", "\n");
        save.statement_For_Parser = statement_text;
        statement_Modified = true;
    }
    public void executeTestModification(){
        switch (this.test_id){
            case 0:
                test0NoModification();
                break;
            case 1:
                test1FromModification();
                break;
            case 2:
                test2WhereModification();
                break;
            case 3:
                test3GroupModification();
                break;
            case 4:
                test4OrderModification();
                break;
            case 5:
                test5TopModification();
                break;
            case 6:
                test6Brackets1stModification();
                break;
            case 7:
                test7Brackets2ndModification();
                break;
            case 8:
                test8AsModification();
                break;
            case 9:
                test9Brackets3rdModification();
                break;
            case 10:
                test10AdvancedModification();
                break;
        }
    }
    public void setupLexerAndParserSSLAndBail(){
        codePointCharStream = CharStreams.fromString(statement_text);
        lexer = new Hello(codePointCharStream);

        //remove cmd output for lexer
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        tokens = new CommonTokenStream(lexer);
        parser = new Mssql(tokens);

        //remove cmd output for parser
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        //added
        parser.setBuildParseTree(false);

        //decisionToDFA=  parser.getInterpreter().decisionToDFA;
        //_sharedContextCache = new PredictionContextCache();
        //sim = new ParserATNSimulator(
        //        parser, parser.getATN(), decisionToDFA, _sharedContextCache);

        //parser.setInterpreter(sim);

        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
    }
    public void prepareStatementForXml(){
        save.temp_text = statement_text.replaceAll("&","&amp").replaceAll("\n","&#10;");
    }
    public void setupParserLLAndDefault(){
        tokens.seek(0);
        parser.reset();
        errorListener = this;
        parser.addErrorListener(errorListener);
        parser.setBuildParseTree(false);
        parser.setErrorHandler(new DefaultErrorStrategy());

        // full now with full LL(*)
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
    }
    public void setupLexerAndParserLLAndDefault(){
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
    }
    public int convertRowColToPosition(int row, int col){
        int row_count = 1;
        for(int i=0;i<save.statement_For_Parser.length();i++){
            if(save.statement_For_Parser.charAt(i) == '\n'){
                row_count++;
            }
            if(row_count == row){
                if(row_count != 1 && col != 0) {
                    i++;
                }
                int char_index=0;
                for(int j=i;j<save.statement_For_Parser.length()+1;j++){
                    if(char_index == col){
                        return j;
                    }
                    char_index++;
                }
                break;
            }
        }
        System.out.println("CHYBA");
        return 0;
    }
    public void saveCorrectAndWrongXml() throws IOException, TransformerException, SAXException {
        this.xml_handler.addToXML(save.wrong_statements,"preparsed_statements_wrongaa_delete_fourth"+this.number_help+".xml", save.wrong_Ids);
        this.xml_handler_correct.addToXML(save.correct_statements,"preparsed_statements_correct_delete_fourth"+this.number_help+".xml", save.correct_Ids);
    }
    public void resetArrays(){
        number_of_total_statements += save.wrong_statements.size();
        save.wrong_statements = new ArrayList<>();
        save.wrong_Ids = new ArrayList<>();
        save.correct_statements = new ArrayList<>();
        save.correct_Ids = new ArrayList<>();
    }
    public void generateRepairPermutations(ArrayList<ArrayList<String>> lists, ArrayList<String> result, int depth, String current) {
        if (depth == lists.size()) {
            current = current.substring(0,current.length() - 1);
            result.add(current);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); i++) {
            generateRepairPermutations(lists, result, depth + 1, current + lists.get(depth).get(i)+";");
        }
    }
    public void run() throws Exception {
        try {
            BufferedReader read;
            if(this.test_id == 0) {
                read = new BufferedReader(new FileReader("preparsed_statements_withid_final1_updated_8.xml"));
            }
            else{
                read = new BufferedReader(new FileReader("preparsed_statements_wrongaa"+this.test_id+".xml"));
            }

            String line="";
            StringBuilder leftOver_line= new StringBuilder();


            long startTime = System.currentTimeMillis();

            int is_corrected = 0;
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
                if(line.contains("<statement") && !line.contains("<statements>") && line.contains("</statement>")){
                    if(!leftOver_line.isEmpty()){
                        line=leftOver_line+line;
                    }
                    getIdAndText(line);

                    executeTestModification();

                    if(statement_text == null){
                        break;
                    }

                    setupLexerAndParserSSLAndBail();

                    isAlreadyWritten = false;


                    prepareStatementForXml();



                    if(Integer.parseInt(save.temp_id) > 36576920 && Integer.parseInt(save.temp_id) != 3454268 && Integer.parseInt(save.temp_id) != 11636147 && Integer.parseInt(save.temp_id) != 11670234 && Integer.parseInt(save.temp_id) != 28905401) {
                        if (statement_Modified) {
                            try {
                                if(save.temp_id.equals("1262941")){
                                    System.out.println("A");
                                }
                                tree = parser.tsql_file();
                            } catch (ParseCancellationException p) {
                                if (!save.isVisited) {
                                   setupParserLLAndDefault();
                                    tree = parser.tsql_file();
                                    if(this.test_id == 10 && this.repair_Options.size() != 0){
                                        save.ready_For_Parse = true;
                                        repairPermutations = new ArrayList<>();
                                        int size=1;
                                        for(ArrayList<String> i : repair_Options){
                                            size *= i.size();
                                            if(size > 500){
                                                break;
                                            }
                                        }
                                        if(size <= 500) {
                                            generateRepairPermutations(this.repair_Options, repairPermutations, 0, "");
                                        }
                                        positionsForRepair = new ArrayList<>();
                                        for(String pos : this.repairRowAndCharpos){
                                            String[] tempPos = pos.split(";");
                                            positionsForRepair.add(convertRowColToPosition(Integer.parseInt(tempPos[0]),Integer.parseInt(tempPos[1])));
                                        }
                                        for(String repair : repairPermutations){
                                            ArrayList<Integer> updatedPositionsForRepair = new ArrayList<>(positionsForRepair);
                                            int shiftIndex=0;
                                            String[] temp_repair = repair.split(";");
                                            StringBuilder repaired_statement = new StringBuilder(save.statement_For_Parser);
                                            for(int i=0;i<temp_repair.length;i++){
                                                repaired_statement.insert(updatedPositionsForRepair.get(i)," "+temp_repair[i]+" ");
                                                for(int x=shiftIndex+1;x<updatedPositionsForRepair.size();x++){
                                                    updatedPositionsForRepair.set(x,updatedPositionsForRepair.get(x)+(new String(" "+temp_repair[i]+" ")).length());
                                                }
                                                shiftIndex++;
                                            }
                                            statement_text = repaired_statement.toString();

                                            prepareStatementForXml();

                                            if(statement_text.replaceAll("&#10;","\n").equals(save.statement_For_Parser)){
                                                break;
                                            }
                                            setupLexerAndParserLLAndDefault();
                                            save.repair_Helped = true;
                                            tree = parser.tsql_file();

                                            if(statement_text.length() > 15000 || Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 1500000000 /*Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory() < 100000000*/){
                                                parser.getInterpreter().clearDFA();
                                                lexer.getInterpreter().clearDFA();
                                            }

                                            if(save.repair_Helped){
                                                System.out.print("Repaired ");
                                                save.isRepaired = true;
                                                save.repair_Helped = false;
                                                if(temp_repair.length > 1){
                                                    System.out.print("Multiple repairs " + Arrays.toString(temp_repair));
                                                }
                                                if(Arrays.asList(temp_repair).contains(")") || Arrays.asList(temp_repair).contains("(")){
                                                    auto_brackets++;
                                                    System.out.print(" ) | ( ");
                                                }
                                                if(Arrays.asList(temp_repair).contains("BEGIN") || Arrays.asList(temp_repair).contains("END")){
                                                    auto_beginend++;
                                                    System.out.print(" BEGIN | END ");
                                                }
                                                if(Arrays.asList(temp_repair).contains(",") ){
                                                    auto_comma++;
                                                    System.out.print(" CARKA ");
                                                }
                                                if(Arrays.asList(temp_repair).contains("SET")){
                                                    auto_brackets2++;
                                                    System.out.print(" SET ");
                                                }
                                                System.out.println();
                                                break;
                                            }
                                        }
                                        this.repair_Options = new ArrayList<>();
                                        this.repairRowAndCharpos = new ArrayList<>();
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
                                if(this.test_id != 6 && this.test_id != 7 && this.test_id != 10) {
                                    save.wrong_statements.add(save.temp_text);
                                }
                                else if (this.test_id == 10){
                                    save.wrong_statements.add(save.statement_For_Parser.replaceAll("&","&amp").replaceAll("\n","&#10;"));
                                }
                                save.wrong_Ids.add(Integer.parseInt(save.temp_id));
                            }
                        } else {
                            save.wrong_statements.add(save.temp_text);
                            save.wrong_Ids.add(Integer.parseInt(save.temp_id));
                        }

                    }
                    if(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 1500000000 /*Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory() < 100000000*/){
                        parser.getInterpreter().clearDFA();
                        lexer.getInterpreter().clearDFA();
                    }

                    save.isVisited = false;

                    leftOver_line = new StringBuilder();
                }
                else if(!line.contains("<statements>") && !line.contains("</statements>")){
                    System.out.println("SOMETHING WRONG");
                    leftOver_line.append(line);
                }

                if(save.wrong_statements.size()  > 1000){
                    saveCorrectAndWrongXml();

                    resetArrays();
                    long endTime = System.currentTimeMillis();


                    System.out.println(
                            "Time : "
                                    + (endTime - startTime) + " ms");

                    startTime = System.currentTimeMillis();


                }
            }
            if(save.wrong_statements.size() > 0){
                saveCorrectAndWrongXml();

                resetArrays();
            }
            read.close();


            if(this.test_id == 1) {
                System.out.println("FROM & FORM");

                System.out.println(from_mistake);
            }
            if(this.test_id == 2) {
                System.out.println("WHERE");

                System.out.println(where_mistake);

            }
            if(this.test_id == 3) {
                System.out.println("GROUP BY");

                System.out.println(groupby_mistake);

            }
            if(this.test_id == 4) {
                System.out.println("ORDER BY");

                System.out.println(orderby_mistake);
            }

            if(this.test_id == 5) {
                System.out.println("TOP FOR");

                System.out.println(top_check);

            }
            if(this.test_id == 6) {
                System.out.println("BRACKETS");

                System.out.println(left_right_brackets);
                left_right_brackets = 0;

            }
            if(this.test_id == 7) {
                System.out.println("BRACKETS");
                System.out.println(left_right_brackets);

            }
            if(this.test_id == 8){
                System.out.println("COMMA INFRONT AS");
                System.out.println(comma_infront_as);
            }
            if(this.test_id == 9){
                System.out.println("COMMA INFRONT BRACKET");
                System.out.println(comma_infront_bracket);
            }
            if(this.test_id == 10){
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
            if(test_id == 11){
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
        if (!save.isVisited || (this.test_id == 10 && !save.isRepaired && !save.ready_For_Parse)) {
            save.repair_Helped = false;
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
                    //pridat repairs pokud jich je méně než 10
                    for(String repair : this.allowed_Repairs){
                        if(options.contains("'"+repair+"'")){
                            final_List.add(repair);
                        }
                    }

                    if (final_List.size() != 0) {
                        final_List.add("");
                        this.repair_Options.add(final_List);
                        this.repairRowAndCharpos.add(i+";"+i1);
                    }
                }

                if(this.repair_Options == null ){
                    //System.out.println("WRONG");
                }
                //this.repair_row = i;
                //this.repair_charpos = i1;

            }
            if(s.toString().contains("extraneous")){

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
                    //pridat repairs pokud jich je méně než 10
                    for(String repair : this.allowed_Repairs){
                        if(options.contains("'"+repair+"'")){
                            final_List.add(repair);
                        }
                    }

                    if (final_List.size() != 0) {
                        final_List.add("");
                        this.repair_Options.add(final_List);
                        this.repairRowAndCharpos.add(i+";"+i1);
                    }
                }

                if(this.repair_Options == null ){
                    //System.out.println("WRONG");
                }
                //this.repair_row = i;
                //this.repair_charpos = i1;



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
                        final_List.add("");
                        this.repair_Options.add(final_List);
                        //this.repair_row = i;
                        //this.repair_charpos = i1;
                        this.repairRowAndCharpos.add(i+";"+i1);
                    }
                }
            }

            save.isVisited = true;

            if(this.test_id == 6 || this.test_id == 7){
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
