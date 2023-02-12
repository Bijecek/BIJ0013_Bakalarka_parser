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


public class ParserClass implements ANTLRErrorListener {
    int testId;
    XMLHandler xmlHandlerWrong;

    XMLHandler xmlHandlerCorrect;

    ArrayList<ArrayList<String>> repairOptions = new ArrayList<>();
    ArrayList<String> repairRowAndCharpos = new ArrayList<>();
    Mssql parser;
    ArrayList<String> allowedRepairs = new ArrayList<>();

    String statementText = null;


    CharStream codePointCharStream = null;
    Mssql_lexer lexer = null;
    CommonTokenStream tokens = null;

    ANTLRErrorListener errorListener = null;
    ParseTree tree = null;
    ParserATNSimulator sim = null;

    DFA[] decisionToDFA = null;
    PredictionContextCache _sharedContextCache = new PredictionContextCache();


    ArrayList<String> repairPermutations = new ArrayList<>();

    ArrayList<Integer> positionsForRepair = new ArrayList<>();


    String path;

    String originalStatement;
    String tempText;
    String tempId;
    ArrayList<String> wrongStatements = new ArrayList<>();
    ArrayList<Integer> wrongIds = new ArrayList<>();
    ArrayList<String> correctStatements = new ArrayList<>();
    ArrayList<Integer> correctIds = new ArrayList<>();

    String statementForParser;

    boolean repairHelped = true;

    boolean isRepaired = false;

    boolean readyForParse = false;
    boolean isVisited = false;

    RepairsAndModifications repair = new RepairsAndModifications();


    public ParserClass(int testId, String path) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        this.path = path;
        this.xmlHandlerWrong = new XMLHandler(path + "_wrong" + (testId) + ".xml");
        this.xmlHandlerCorrect = new XMLHandler(path + "_correct" + (testId) + ".xml");
        this.testId = testId;
        setupRepairsToLookFor();
    }

    public void getIdAndText(String line) {
        this.tempId = StringUtils.substringBetween(line, "<statement id=\"", "\">");

        statementText = StringUtils.substringBetween(line, "<statement id=\"" + this.tempId + "\">", "</statement>");
        statementText = statementText.replaceAll("&gt;", ">").replaceAll("&lt;", "<");
        statementText = statementText.replaceAll("&amp;amp;", "&");
        statementText = statementText.replaceAll("&amp;amp", "&");
        statementText = statementText.replaceAll("&amp;#10;", "\n");
    }

    public void executeTestModification() {
        repair.setStatementText(this.statementText);
        switch (this.testId) {
            case 0:
                repair.test0NoModification();
                break;
            case 1:
                repair.test1FromModification();
                break;
            case 2:
                repair.test2WhereModification();
                break;
            case 3:
                repair.test3GroupModification();
                break;
            case 4:
                repair.test4OrderModification();
                break;
            case 5:
                repair.test5TopModification();
                break;
            case 6:
                this.originalStatement = statementText;
                this.originalStatement = this.originalStatement.replaceAll("\n", "&#10;");
                repair.test6Brackets1stModification();
                break;
            case 7:
                this.originalStatement = statementText;
                this.originalStatement = this.originalStatement.replaceAll("\n", "&#10;");
                repair.test7Brackets2ndModification();
                break;
            case 8:
                repair.test8AsModification();
                break;
            case 9:
                repair.test9Brackets3rdModification();
                break;
            case 10:
                this.statementForParser = statementText;
                repair.test10AdvancedModification();
                break;
            case 11:
                repair.test11RemoveDots();
                break;
        }
        this.statementText = repair.getStatementText();
    }

    public void setupLexerAndParserSSLAndBail() {
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

        decisionToDFA = parser.getInterpreter().decisionToDFA;
        _sharedContextCache = new PredictionContextCache();
        sim = new ParserATNSimulator(
                parser, parser.getATN(), decisionToDFA, _sharedContextCache);

        parser.setInterpreter(sim);

        parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
        parser.removeErrorListeners();
        parser.setErrorHandler(new BailErrorStrategy());
    }

    public void prepareStatementForXml() {
        this.tempText = statementText.replaceAll("&", "&amp").replaceAll("\n", "&#10;");
    }

    public void setupParserLLAndDefault() {
        tokens.seek(0);
        parser.reset();
        errorListener = this;
        parser.addErrorListener(errorListener);
        parser.setBuildParseTree(false);
        parser.setErrorHandler(new DefaultErrorStrategy());

        //delete
        parser.setInterpreter(sim);
        // full now with full LL(*)
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
    }

    public void setupLexerAndParserLLAndDefault() {
        parser.reset();
        codePointCharStream = CharStreams.fromString(statementText);
        lexer = new Mssql_lexer(codePointCharStream);

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

    public int convertRowColToPosition(int row, int col) {
        int row_count = 1;
        for (int i = 0; i < this.statementForParser.length(); i++) {
            if (this.statementForParser.charAt(i) == '\n') {
                row_count++;
            }
            if (row_count == row) {
                if (row_count != 1 && col != 0) {
                    i++;
                }
                int char_index = 0;
                for (int j = i; j < this.statementForParser.length() + 1; j++) {
                    if (char_index == col) {
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
        this.xmlHandlerWrong.addToXML(this.wrongStatements, path + "_wrong" + (this.testId) + ".xml", this.wrongIds);
        this.xmlHandlerCorrect.addToXML(this.correctStatements, path + "_correct" + (this.testId) + ".xml", this.correctIds);
    }

    public void resetArrays() {
        repair.numberOfTotalStatements += this.wrongStatements.size();
        this.wrongStatements = new ArrayList<>();
        this.wrongIds = new ArrayList<>();
        this.correctStatements = new ArrayList<>();
        this.correctIds = new ArrayList<>();
    }

    public void generateRepairPermutations(ArrayList<ArrayList<String>> lists, ArrayList<String> result, int depth, String current) {
        if (depth == lists.size()) {
            current = current.substring(0, current.length() - 1);
            result.add(current);
            return;
        }

        for (int i = 0; i < lists.get(depth).size(); i++) {
            generateRepairPermutations(lists, result, depth + 1, current + lists.get(depth).get(i) + ";");
        }
    }

    public void handleAdvancedRepairs() {
        if (this.testId == 10 && this.repairOptions.size() != 0) {
            this.readyForParse = true;
            repairPermutations = new ArrayList<>();
            int size = 1;
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
                if (this.isRepaired) {
                    break;
                }
            }
            this.repairOptions = new ArrayList<>();
            this.repairRowAndCharpos = new ArrayList<>();
            this.readyForParse = false;
        }
    }

    public void clearHighDFA() {
        if (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() > 3800000000L /*Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory() + Runtime.getRuntime().freeMemory() < 100000000*/) {
            parser.getInterpreter().clearDFA();
            lexer.getInterpreter().clearDFA();
        }
    }

    public void tryToRepair(String possibleRepairs) {

        statementText = repair.modifyStatement(this.positionsForRepair, possibleRepairs, this.statementForParser);

        prepareStatementForXml();

        if (statementText.replaceAll("&#10;", "\n").equals(this.statementForParser)) {
            System.out.println("STEJNE");
            return;
        }
        setupLexerAndParserLLAndDefault();
        this.repairHelped = true;
        tree = parser.tsql_file();

        clearHighDFA();

        if (this.repairHelped) {
            System.out.print("Repaired ");
            this.isRepaired = true;
            this.repairHelped = false;
            String[] possibleRepairsArray = possibleRepairs.split(";");
            if (possibleRepairsArray.length > 1) {
                System.out.print("Multiple repairs " + Arrays.toString(possibleRepairsArray));
            }
            if (Arrays.asList(possibleRepairsArray).contains(")") || Arrays.asList(possibleRepairsArray).contains("(")) {
                repair.autoBrackets++;
                System.out.print(" ) | ( ");
            }
            if (Arrays.asList(possibleRepairsArray).contains("BEGIN") || Arrays.asList(possibleRepairsArray).contains("END")) {
                repair.autoBeginend++;
                System.out.print(" BEGIN | END ");
            }
            if (Arrays.asList(possibleRepairsArray).contains(",")) {
                repair.autoComma++;
                System.out.print(" CARKA ");
            }
            if (Arrays.asList(possibleRepairsArray).contains("SET")) {
                repair.autoBrackets2++;
                System.out.print(" SET ");
            }
            System.out.println();
        }
    }

    public void setupRepairsToLookFor() {
        allowedRepairs.add("(");
        allowedRepairs.add(")");
        allowedRepairs.add("BEGIN");
        allowedRepairs.add("END");
        allowedRepairs.add(",");
        allowedRepairs.add("SET");
    }

    public void parseAndSaveCorrectOrWrongStatement() {
        try {
            setupLexerAndParserSSLAndBail();
            tree = parser.tsql_file();
        } catch (ParseCancellationException p) {
            if (!this.isVisited) {
                setupParserLLAndDefault();
                tree = parser.tsql_file();
                //try to repair statement
                handleAdvancedRepairs();
            }
        }

        if (!this.isVisited || this.isRepaired) {
            repair.isCorrected++;
            this.correctStatements.add(this.tempText);
            this.correctIds.add(Integer.parseInt(this.tempId));
            this.isRepaired = false;

        } else {
            if (this.testId != 6 && this.testId != 7 && this.testId != 10) {
                this.wrongStatements.add(this.tempText);
            } else if (this.testId == 10) {
                this.wrongStatements.add(this.statementForParser.replaceAll("&", "&amp").replaceAll("\n", "&#10;"));
            }
            this.wrongIds.add(Integer.parseInt(this.tempId));
        }
        clearHighDFA();

        this.isVisited = false;

    }

    public void run() throws Exception {
        try {
            BufferedReader read;
            if (this.testId == 0) {
                read = new BufferedReader(new FileReader(path + ".xml"));
            } else {
                read = new BufferedReader(new FileReader(path + "_wrong" + (this.testId - 1) + ".xml"));
            }
            String line = "";
            StringBuilder leftOver_line = new StringBuilder();
            long startTime = System.currentTimeMillis();

            while ((line = read.readLine()) != null) {
                if (line.contains("<statement") && !line.contains("<statements>") && line.contains("</statement>")) {
                    if (!leftOver_line.isEmpty()) {
                        line = leftOver_line + line;
                    }
                    getIdAndText(line);
                    if (this.tempId.equals("6044505")) {
                        System.out.println("AA");
                    }
                    executeTestModification();
                    prepareStatementForXml();
                    if (Integer.parseInt(this.tempId) > 0) {
                        if (repair.getStatementModified()) {
                            parseAndSaveCorrectOrWrongStatement();
                        } else {
                            this.wrongStatements.add(this.tempText);
                            this.wrongIds.add(Integer.parseInt(this.tempId));
                        }
                    }
                    leftOver_line = new StringBuilder();
                } else if (!line.contains("<statements>") && !line.contains("</statements>")) {
                    leftOver_line.append(line);
                }

                if (this.wrongStatements.size() > 1000) {
                    saveCorrectAndWrongXml();
                    resetArrays();

                    long endTime = System.currentTimeMillis();
                    System.out.println(
                            "Time : "
                                    + (endTime - startTime) + " ms");
                    startTime = System.currentTimeMillis();
                }
            }
            if (this.wrongStatements.size() > 0 || this.correctStatements.size() > 0) {
                saveCorrectAndWrongXml();
                resetArrays();
            }
            read.close();
            repair.printResults(this.testId);

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

    public void findAndSaveAllowedRepairs(String s, int i, int i1) {
        String options;
        if (s.contains("{") && s.contains("}")) {
            options = StringUtils.substringBetween(s, "{", "}");
        } else {
            int index = s.indexOf("expecting");
            String temp_string = s.substring(index);
            options = StringUtils.substringBetween(temp_string, "'", "'");
            options = "'" + options + "'";
        }
        if (options != null) {
            checkAndSaveRepairs(options, i, i1);
        }
    }

    public void checkAndSaveRepairs(String options, int i, int i1) {
        ArrayList<String> final_List = new ArrayList<>();
        for (String repair : this.allowedRepairs) {
            if (options.contains("'" + repair + "'")) {
                final_List.add(repair);
            }
        }
        if (final_List.size() != 0) {
            //add "nothing" to repair options
            final_List.add("");
            this.repairOptions.add(final_List);
            this.repairRowAndCharpos.add(i + ";" + i1);
        }
    }

    public void findAndSaveAllowedRepairMissing(String s, int i, int i1) {
        String options;
        int index = s.indexOf("missing");
        if (index != -1) {
            int index_end = s.indexOf("at");
            String temp_string = s.substring(index, index_end);
            if (s.contains("{") && s.contains("}")) {
                options = StringUtils.substringBetween(s, "{", "}");
            } else {
                options = StringUtils.substringBetween(temp_string, "'", "'");
                options = "'" + options + "'";
            }

            if (options != null) {
                checkAndSaveRepairs(options, i, i1);
            }
        }
    }


    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object o, int i, int i1, String s, RecognitionException e) {
        if (!this.isVisited || (this.testId == 10 && !this.isRepaired && !this.readyForParse)) {
            this.repairHelped = false;
            if (s.contains("mismatched")) {
                findAndSaveAllowedRepairs(s, i, i1);
            }
            if (s.contains("extraneous")) {
                findAndSaveAllowedRepairs(s, i, i1);
            }
            if (s.contains("missing") && !s.contains("no viable alternative")) {
                findAndSaveAllowedRepairMissing(s, i, i1);
            }
            this.isVisited = true;
            if (this.testId == 6 || this.testId == 7) {
                this.wrongStatements.add(this.originalStatement);
            }
        } else {
            this.repairHelped = false;
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
