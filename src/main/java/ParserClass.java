import main.java.Mssql;
import main.java.Mssql_lexer;

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
import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.SAXException;


public class ParserClass implements ANTLRErrorListener {
    private int testId;
    private FileHandler fileHandlerWrong;

    private FileHandler fileHandlerCorrect;

    private ArrayList<ArrayList<String>> repairOptions = new ArrayList<>();
    private ArrayList<String> repairRowAndCharpos = new ArrayList<>();
    private Mssql parser;
    private ArrayList<String> allowedRepairs = new ArrayList<>();

    private String statementText = null;


    private CharStream codePointCharStream = null;
    private Mssql_lexer lexer = null;
    private CommonTokenStream tokens = null;

    private ANTLRErrorListener errorListener = null;
    private ParseTree tree = null;
    private ParserATNSimulator sim = null;

    private DFA[] decisionToDFA = null;
    private PredictionContextCache _sharedContextCache = new PredictionContextCache();


    private ArrayList<String> repairPermutations = new ArrayList<>();

    private ArrayList<Integer> positionsForRepair = new ArrayList<>();


    private String path;

    private String statementId;
    private ArrayList<String> allRepairs = new ArrayList<>();
    private ArrayList<String> allWrongRepairs = new ArrayList<>();
    private ArrayList<String> allCorrectRepairs = new ArrayList<>();

    private ArrayList<String> wrongStatements = new ArrayList<>();
    private ArrayList<Integer> wrongIds = new ArrayList<>();

    private ArrayList<Boolean> wrongRepairsDone = new ArrayList<>();
    private ArrayList<String> correctStatements = new ArrayList<>();
    private ArrayList<Integer> correctIds = new ArrayList<>();

    private ArrayList<Boolean> correctRepairsDone = new ArrayList<>();


    private String statementForParser;

    private boolean repairHelped = true;
    private boolean isVisited = false;

    private RepairsAndModifications repair = new RepairsAndModifications();



    public ParserClass(int testId, String path) throws IOException, ParserConfigurationException, TransformerException, SAXException {
        this.path = path;
        //create or modify file, that will be later written to
        this.fileHandlerWrong = new FileHandler(path + "_wrong" + (testId) + ".xml");
        if(testId == 0){
            this.fileHandlerCorrect = new FileHandler(path + "_correct.txt", false);
        }
        else if (testId == 1) {
            this.fileHandlerCorrect = new FileHandler(path + "_repaired.txt", false);
        }
        else{
            this.fileHandlerCorrect = new FileHandler(path + "_repaired.txt", true);
        }
        this.testId = testId;
        setupRepairsToLookFor();
    }
    public ParserClass(int testId){
        this.testId = testId;
    }

    //START Methods only used in JUnit testCases
    public void setStatementText(String statementText){
        this.statementText = statementText;
    }
    public void setStatementId(String statementId){
        this.statementId = statementId;
    }
    public void setAllRepairs(String rep){
        this.allRepairs = new ArrayList<>();
        this.allRepairs.add(rep);
    }
    public ArrayList<String> getCorrectStatements(){
        return this.correctStatements;
    }
    public void setStatementForParser(String statementForParser){
        this.statementForParser = statementForParser;
    }
    //END Methods only used in JUnit testCases

    private void getIdAndText(String line) {
        //get ID from statement
        this.statementId = StringUtils.substringBetween(line, "<statement id=\"", "\"");


        //get text from statement
        statementText = StringUtils.substringBetween(line, ">", "</statement>");


        //get all the repairs that the statement has already been modified by
        String repairsDone = StringUtils.substringBetween(line, "repairs=\"", "\"");
        if(repairsDone == null){
            repairsDone = "";
        }
        allRepairs.add(repairsDone);

    }

    private void executeTestModification() {
        //set statement to out class which handles Repairs
        repair.setStatementText(this.statementText);
        //choose correct test case that will be executed
        switch (this.testId) {
            case 0 -> repair.test0NoModification();
            case 1 -> repair.test1FromModification();
            case 2 -> repair.test2WhereModification();
            case 3 -> repair.test3GroupModification();
            case 4 -> repair.test4OrderModification();
            case 5 -> repair.test5TopModification();
            case 6 -> {
                this.statementForParser = statementText;
                repair.test6Brackets1stModification();
            }
            case 7 -> {
                this.statementForParser = statementText;
                repair.test7Brackets2ndModification();
            }
            case 8 -> repair.test8AsModification();
            case 9 -> repair.test9Brackets3rdModification();
            case 10 -> {
                this.statementForParser = statementText;
                repair.test10AdvancedModification();
            }
            case 11 -> repair.test11RemoveDots();
        }
        //get statement that has been modified by one of the test cases
        this.statementText = repair.getStatementText();
    }

    private void setupLexerAndParser(){
        codePointCharStream = CharStreams.fromString(statementText);
        lexer = new Mssql_lexer(codePointCharStream);

        //remove cmd output for lexer
        lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

        tokens = new CommonTokenStream(lexer);
        parser = new Mssql(tokens);

        //remove cmd output for parser
        parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

        //added
        parser.setBuildParseTree(false);
        parser.removeErrorListeners();

        decisionToDFA = parser.getInterpreter().decisionToDFA;
        _sharedContextCache = new PredictionContextCache();

        //we are setting up ParserATNSimulator with 1 min timeout of parse time
        sim = new ParserATNSimulatorWithTimeOut(
                parser, parser.getATN(), decisionToDFA, _sharedContextCache, 1);

        parser.setInterpreter(sim);

        parser.setErrorHandler(new BailErrorStrategy());
    }
    private void setupLexerAndParserSSLAndBail() {
        //this method sets up Lexer and Parser. The Parser for fast parsing and no-error recovery,
        //this method does not guarantee 100% accuracy
        setupLexerAndParser();
        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
    }
    private void setupParser(){
        tokens.seek(0);
        parser.reset();
        parser.setBuildParseTree(false);

        decisionToDFA = parser.getInterpreter().decisionToDFA;
        _sharedContextCache = new PredictionContextCache();

        //we are setting up ParserATNSimulator with 1 min timeout of parse time
        sim = new ParserATNSimulatorWithTimeOut(
                parser, parser.getATN(), decisionToDFA, _sharedContextCache, 1);

        parser.setInterpreter(sim);

        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
    }
    private void setupParserLLAndBail() {
        //this method sets up the Parser for slow parsing and no error recovery,
        //this method does guarantee 100% accuracy
        setupParser();
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
    }
    private void setupParserLLAndDefault() {
        //this method sets up the Parser for slow parsing and error recovery,
        //this method does guarantee 100% accuracy
        setupParser();
        errorListener = this;
        parser.addErrorListener(errorListener);
        parser.setErrorHandler(new DefaultErrorStrategy());
    }

    private void setupLexerAndParserLLAndBail() {
        //this method sets up Lexer and Parser. The Parser for slow parsing and error recovery,
        //this method does guarantee 100% accuracy
        parser.reset();
        setupLexerAndParser();
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
    }

    private int convertRowColToPosition(int row, int col) {
        //the ANTLR provides row-col error message, we need to convert this row-col into position
        int rowCount = 1;
        for (int i = 0; i < this.statementForParser.length(); i++) {
            if (this.statementForParser.charAt(i) == '\n') {
                rowCount++;
            }
            if (rowCount == row) {
                if (rowCount != 1 && col != 0) {
                    i++;
                }
                int charIndex = 0;
                for (int j = i; j < this.statementForParser.length() + 1; j++) {
                    if (charIndex == col) {
                        return j;
                    }
                    charIndex++;
                }
                break;
            }
        }
        return 0;
    }

    private void saveCorrectAndWrongXml() throws TransformerException, IOException {
        //we are saving correct and wrong statements
        this.fileHandlerWrong.addToXML(this.wrongStatements, this.wrongIds,this.wrongRepairsDone, this.allWrongRepairs, this.testId);
        this.fileHandlerCorrect.addToTXTCorrect(this.correctStatements, this.correctIds,this.correctRepairsDone,this.allCorrectRepairs, this.testId);
    }

    private void resetArrays() {
        this.wrongStatements = new ArrayList<>();
        this.wrongIds = new ArrayList<>();
        this.wrongRepairsDone = new ArrayList<>();
        this.allWrongRepairs = new ArrayList<>();
        this.correctStatements = new ArrayList<>();
        this.correctIds = new ArrayList<>();
        this.correctRepairsDone = new ArrayList<>();
        this.allCorrectRepairs = new ArrayList<>();
    }

    private void generateRepairPermutations(ArrayList<ArrayList<String>> lists, ArrayList<String> result, int depth, String current) {
        //we are generating all possible repair combinations gained from ANTLR error messages
        if (depth == lists.size()) {
            current = current.substring(0, current.length() - 1);
            result.add(current);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); i++) {
            generateRepairPermutations(lists, result, depth + 1, current + lists.get(depth).get(i) + ";");
        }
    }

    private void handleAdvancedRepairs() {
        //this method handles advanced repairs with help of ANTLR error messages
        if (this.testId == 10 && this.repairOptions.size() != 0) {
            repairPermutations = new ArrayList<>();
            int size = 1;
            //we are testing if those repair combinations are no more than 500
            for (ArrayList<String> i : repairOptions) {
                size *= i.size();
                if (size > 500) {
                    break;
                }
            }
            //we want no more than 500 possible combinations of repairs
            if (size <= 500) {
                generateRepairPermutations(this.repairOptions, repairPermutations, 0, "");
            }
            positionsForRepair = new ArrayList<>();
            for (String pos : this.repairRowAndCharpos) {
                String[] tempPos = pos.split(";");
                positionsForRepair.add(convertRowColToPosition(Integer.parseInt(tempPos[0]), Integer.parseInt(tempPos[1])));
            }
            for (String repair : repairPermutations) {
                //try to modify statement and see if it was successful, if not try another
                tryToRepair(repair);
                if (this.repairHelped) {
                    break;
                }
            }
            this.repairOptions = new ArrayList<>();
            this.repairRowAndCharpos = new ArrayList<>();
        }
    }

    private void clearHighDFA() {
        //as DFA is still rising over time we need to manage it, clearing DFA after 3.8 GB of HEAP space used
        if (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 3800000000L) {
            parser.getInterpreter().clearDFA();
            lexer.getInterpreter().clearDFA();
        }
    }

    private void tryToRepair(String possibleRepairs) {

        statementText = repair.modifyStatement(this.positionsForRepair, possibleRepairs, this.statementForParser);

        setupLexerAndParserLLAndBail();
        this.repairHelped = true;
        try {
            tree = parser.tsql_file();
        }
        catch (ParseCancellationException p) {
            this.repairHelped = false;
        }
        clearHighDFA();

    }

    private void setupRepairsToLookFor() {
        //those are the allowed repairs that we will work with in handleAdvancedRepairs()
        allowedRepairs.add("(");
        allowedRepairs.add(")");
        allowedRepairs.add("BEGIN");
        allowedRepairs.add("END");
        allowedRepairs.add(",");
        allowedRepairs.add("SET");
    }

    public void executeParseForUnitTestPurpose(){
        parseAndSaveCorrectOrWrongStatement();
    }
    private void parseAndSaveCorrectOrWrongStatement() {
        //we are parsing statement and controlling if it got caught by ANTLR syntax error
        try {
            setupLexerAndParserSSLAndBail();
            tree = parser.tsql_file();
        } catch (ParseCancellationException p) {
            if(p.getCause().getMessage() != null){
                return;
            }
            if(this.testId == 10){
                setupParserLLAndDefault();
            }
            else {
                setupParserLLAndBail();
            }
            try {
                tree = parser.tsql_file();
            }
            catch (ParseCancellationException p1){
                if(p1.getCause().getMessage() != null){
                    return;
                }
                this.isVisited = true;
            }
            //try to repair statement
            handleAdvancedRepairs();

        }
        //is statement was not in SyntaxError or is repaired by advanced repairs we may proceed
        if (!this.isVisited || (this.testId == 10 && this.repairHelped)) {
            this.correctStatements.add(this.statementText);
            this.correctIds.add(Integer.parseInt(this.statementId));
            this.correctRepairsDone.add(true);
            this.allCorrectRepairs.add(this.allRepairs.get(0));
            this.allRepairs.remove(0);
            this.repairHelped = false;

        } else {
            //this branch is entered when the syntax was unable to parse despite changes made
            //if test 6 or 7 was unable to parse, we save not the updated statement but the original (test 6 and 7 are based on "trial and error" method)
            if(this.testId == 6 || this.testId == 7 || this.testId == 10){
                this.wrongStatements.add(this.statementForParser.replaceAll("\n", "&#xA;"));
                this.wrongRepairsDone.add(false);
            }
            else{
                this.wrongStatements.add(this.statementText.replaceAll("\n","&#xA;"));
                this.wrongRepairsDone.add(true);
            }
            this.allWrongRepairs.add(this.allRepairs.get(0));
            this.allRepairs.remove(0);
            this.wrongIds.add(Integer.parseInt(this.statementId));
        }

        clearHighDFA();

        this.isVisited = false;

    }

    public void run() throws Exception {
        try {
            double start = System.currentTimeMillis();
            BufferedReader read;
            if (this.testId == 0) {
                read = new BufferedReader(new FileReader(path + ".xml"));
            } else {
                read = new BufferedReader(new FileReader(path + "_wrong" + (this.testId - 1) + ".xml"));
            }
            System.out.println("Test"+this.testId+" launched");
            String line;
            //reading line by line
            while ((line = read.readLine()) != null) {
                if (line.contains("<statement id")) {
                    line = StringEscapeUtils.unescapeXml(line);
                    line = StringEscapeUtils.unescapeXml(line);
                    getIdAndText(line);
                    executeTestModification();
                    if (Integer.parseInt(this.statementId) != 61299191 && Integer.parseInt(this.statementId) != 61300771 && Integer.parseInt(this.statementId) != 28905401 && Integer.parseInt(this.statementId) != 68073560) {
                        if (repair.getStatementModified()) {
                            parseAndSaveCorrectOrWrongStatement();
                        } else {
                            this.wrongStatements.add(this.statementText.replaceAll("\n","&#xA;"));
                            this.wrongIds.add(Integer.parseInt(this.statementId));
                            this.wrongRepairsDone.add(false);
                            this.allWrongRepairs.add(this.allRepairs.get(0));
                            this.allRepairs.remove(0);
                        }
                    }
                }
                //we are saving wrong statements by 1000 pieces, correct statements are saved as well
                if (this.wrongStatements.size() > 1000) {
                    saveCorrectAndWrongXml();
                    resetArrays();

                }
            }
            //save leftover wrong and correct statements
            if (this.wrongStatements.size() > 0 || this.correctStatements.size() > 0) {
                saveCorrectAndWrongXml();
                resetArrays();
            }
            //close BufferedReader and print results
            read.close();
            double end = System.currentTimeMillis();
            System.out.println("TEST"+this.testId+" ="+ (((end - start)/60000)*60)+" seconds");
            //increment testId and proceed to another test-case
            this.testId += 1;
            if (testId == 12) {
                return;
            }
            //run again to try another repair if possible
            ParserClass p = new ParserClass(this.testId, path);
            p.run();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAndSaveAllowedRepairs(String s, int i, int i1) {
        //this method works with ANTLR SyntaxError, it processes the error output and extract our allowed repairs if it contains some
        String options;
        if (s.contains("{") && s.contains("}")) {
            options = StringUtils.substringBetween(s, "{", "}");
        } else {
            int index = s.indexOf("expecting");
            String unformattedOptions = s.substring(index);
            options = StringUtils.substringBetween(unformattedOptions, "'", "'");
            options = "'" + options + "'";
        }
        if (options != null) {
            checkAndSaveRepairs(options, i, i1);
        }
    }

    private void checkAndSaveRepairs(String options, int i, int i1) {
        //extract our allowed repairs handling
        ArrayList<String> finalRepairsList = new ArrayList<>();
        for (String repair : this.allowedRepairs) {
            if (options.contains("'" + repair + "'")) {
                finalRepairsList.add(repair);
            }
        }
        if (finalRepairsList.size() != 0) {
            //add "nothing" to repair options
            finalRepairsList.add("");
            this.repairOptions.add(finalRepairsList);
            this.repairRowAndCharpos.add(i + ";" + i1);
        }
    }

    private void findAndSaveAllowedRepairMissing(String s, int i, int i1) {
        //this method also works with ANTLR SyntaxError (it's specially designed for "Missing xxx" error message as this message has slightly different format that those other two,
        // it processes the error output and extract our allowed repairs if it contains some
        String options;
        int index = s.indexOf("missing");
        if (index != -1) {
            int indexEnd = s.indexOf("at");
            String unformattedOptions = s.substring(index, indexEnd);
            if (s.contains("{") && s.contains("}")) {
                options = StringUtils.substringBetween(s, "{", "}");
            } else {
                options = StringUtils.substringBetween(unformattedOptions, "'", "'");
                options = "'" + options + "'";
            }

            if (options != null) {
                checkAndSaveRepairs(options, i, i1);
            }
        }
    }
    public void calculateFinalResults(String file) throws IOException {
        BufferedReader read;
        read = new BufferedReader(new FileReader(path + "_repaired.txt"));
        String line;
        ArrayList<String> testCombinations = new ArrayList<>();
        ArrayList<Integer> numberOfResults = new ArrayList<>();
        while ((line = read.readLine()) != null) {
            if(line.contains("repairs=\"")){
                String repairsPresent =StringUtils.substringBetween(line,"repairs=\"","\"");
                if(!testCombinations.contains(repairsPresent)){
                    testCombinations.add(repairsPresent);
                    numberOfResults.add(0);
                }
                int index = testCombinations.indexOf(repairsPresent);
                numberOfResults.set(index,numberOfResults.get(index)+1);
            }
        }
        read.close();
        FileHandler saveSummary = new FileHandler(file+".txt",false);
        saveSummary.saveSummaryResults(path,testCombinations,numberOfResults);
    }


    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        //This syntax error is used in advanced repairs, we need to extract every information that ANTLR syntaxError gives us
            if (s.contains("mismatched") || s.contains("extraneous")) {
                findAndSaveAllowedRepairs(s, i, i1);
            }
            if (s.contains("missing") && !s.contains("no viable alternative")) {
                findAndSaveAllowedRepairMissing(s, i, i1);
            }
            this.isVisited=true;

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
