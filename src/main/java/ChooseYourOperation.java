import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

import static java.lang.System.exit;

public class ChooseYourOperation {
    private final String ANSI_RED_BACKGROUND = "\033[41m";
    private final String ANSI_RESET = "\033[0m";

    private int executeNumber=0;
    private String[] arguments;
    private int argumentIndex=0;
    String fileToGenerateOutputTo;
    String fileToGenerateOutputToPreParse;

    String summaryFile;
    String preParsePath;


    public ChooseYourOperation(String[] args) throws Exception {
        this.arguments = args;
        if(this.arguments.length == 0){
            System.out.println("Invalid number of arguments, use -help");
            exit(1);
        }
        else if(this.arguments.length == 1 && Objects.equals(this.arguments[0],"-help")){
            printHelp();
            exit(1);
        }
        int requiredArguments = checkNumberOfRequiredArguments(this.arguments);
        if(requiredArguments != this.arguments.length-1) {
            System.out.println("Invalid number of arguments, use -help");
            exit(1);
        }

        System.out.println("B.S. thesis - BIJ0013 - Error Correction of SQL Commands from StackOverflow");
        System.out.println("---------------------------------------------------------------------------");


        boolean preloadRan = preParseOperation();
        boolean parseRan = parseOperations(preloadRan);
        if(preloadRan){
            PreloadClass preload = new PreloadClass(preParsePath, fileToGenerateOutputToPreParse);
            preload.run();
        }
        if(parseRan){
            ParserClass parserClass = new ParserClass(executeNumber, fileToGenerateOutputTo);
            parserClass.run();
            parserClass.calculateFinalResults(summaryFile);
            System.out.println("Your summary results file is located at: "+ Paths.get(summaryFile+".txt").toAbsolutePath());
        }
    }
    private void printHelp(){
        //-PP C:\Users\sisin\Downloads\Posts.7z DeletemeLater -OW -PO -SF vysledky_delete -OW
        System.out.println("HELP menu:");
        System.out.println("The arguments you are looking for may be listed below:");
        System.out.println("java -Xmx4096M -jar target/B_tmp-1.0-SNAPSHOT.jar -PP pathTo7zFile nameOfFile -OW -PO -SF myResults -OW");
        System.out.println("java -Xmx4096M -jar target/B_tmp-1.0-SNAPSHOT.jar -noPP -PO nameOfFile -EA myResults -OW");
        System.out.println("java -Xmx4096M -jar target/B_tmp-1.0-SNAPSHOT.jar -PP pathTo7zFile nameOfFile -OW -noPO");
        System.out.println();
        System.out.println("[-PP|-noPP]\n" +
                "-PP -> [-PP pathTo7zFile nameOfFile [-OW|-noOW] [-PO|-noPO]]\n" +
                "-PO -> [-PP pathTo7zFile nameOfFile [-OW|-noOW] -PO [-SF|nameOfFile [-EA|-noEA]] nameOfResultsFile [-OW|-noOW]]\n" +
                "-noPP -> [-noPP -PO nameOfFile [-EA|-noEA] nameOfResultsFile [-OW|-noOW]]\n");
        System.out.println();
        System.out.println("-PP or -noPP   NOTE: -PP option enables to preload some data from StackOverflow" +
                                        "\n   NOTE: -noPP disables preloading data from Stackoverflow" );
        System.out.println("---");
        System.out.println("NOTE: Next arguments are only applicable to option -PP");
        System.out.println("----------------------------------------");
        System.out.println("    pathTo7zFile   NOTE: Provide full path to your .7z file that you want to process, for example C:\\Users\\MyPc\\Posts.7z" );
        System.out.println("---");
        System.out.println("    nameOfFile   NOTE: Choose name of the file that will be created in working directory" );
        System.out.println("---");
        System.out.println("    -OW or -noOW   NOTE: -OW option rewrites nameOfFile if its already present in the working directory" +
                "\n         NOTE: -noOW option does not overwrite file, using program with this option could mean that you need to provide different nameOfFile" );
        System.out.println("----------------------------------------");
        System.out.println("    -PO or -noPO   NOTE: -PO option enables to parse and repair data" +
                "\n         NOTE: -noPO disables parsing and repairing data" );
        System.out.println("---");
        System.out.println("NOTE: Next arguments are only applicable to option -PO");
        System.out.println("----------------------------------------");
        System.out.println("    -SF or nameOfFile   NOTE: -SF option is only applicable when option -PP is enabled" +
                "\n         NOTE: -SF option allows to automatically parse and repair data based on preloaded results from option -PP" +
                "\n         NOTE: nameOfFile is applicable on both -PP and -noPP options"+
                "\n         NOTE: nameOfFile option allows to specify name of file that will be processed" );
        System.out.println("---");
        System.out.println("    -EA or -noEA   NOTE: -EA or -noEA options are only applicable when option nameOfFile is enabled" +
                "\n         NOTE: -EA option enables to execute all repairs" +
                "\n         NOTE: -noEA options enables to execute all repairs except first parse"+
                "\n         NOTE: -noEA options requires that nameOfFile_wrong0 is present in the working directory");
        System.out.println("---");
        System.out.println("    nameOfResultsFile   NOTE: Choose name of the results file that will be created in working directory ");
        System.out.println("---");
        System.out.println("    -OW or -noOW   NOTE: -OW option rewrites nameOfFile if its already present in the working directory" +
                "\n         NOTE: -noOW option does not overwrite file, using program with this option could mean that you need to provide different nameOfFile" );

        System.out.println("----------------------------------------");


    }
    private int checkNumberOfRequiredArguments(String[] arguments){
        int currentNumber=0;
            if (arguments[currentNumber].equals("-PP")) {
                currentNumber += 3;
            }
            currentNumber++;
            checkForIndexOutOfBounds(arguments,currentNumber);
            if (arguments[currentNumber].equals("-PO")) {
                currentNumber++;
                checkForIndexOutOfBounds(arguments,currentNumber);
                if (!arguments[currentNumber].equals("-SF")) {
                    currentNumber++;
                }
                currentNumber += 2;
            }

        return currentNumber;
    }
    private void checkForIndexOutOfBounds(String []arguments, int currentNumber){
        if(arguments.length-1 < currentNumber){
            System.out.println("Invalid number of arguments, use -help");
            exit(1);
        }
    }
    private boolean preParseOperation() throws Exception {
        boolean preloadRan = false;
        String answerOne = arguments[argumentIndex];
        argumentIndex++;
        answerOne = whileAnswerIsYesOrNo(answerOne, "-PP","-noPP");
        if(answerOne.equals("-PP")){
            preParsePath = arguments[argumentIndex];
            argumentIndex++;
            File checkFile = new File(preParsePath);
            String suffix = FileNameUtils.getExtension(preParsePath);
            if(!checkFile.exists() || checkFile.isDirectory()){
                System.out.println("Specified path to file "+preParsePath+" does not exists.");
                exit(1);
            }
            else if(!suffix.equals("7z")){
                System.out.println("Specified path to file "+preParsePath+" does not have .7z suffix.");
                exit(1);
            }
            else{
                fileToGenerateOutputToPreParse = arguments[argumentIndex];
                argumentIndex++;
                boolean canContinue = checkIfFileExistsOperations(fileToGenerateOutputToPreParse,".xml", arguments[argumentIndex]);
                argumentIndex++;
                if(!canContinue){
                    System.out.println("Specified file name " + fileToGenerateOutputToPreParse + " already exists, choose -OW or specify another file name");
                    exit(1);
                }
                preloadRan = true;
            }
        }
        return preloadRan;
    }
    private boolean parseOperations(boolean preloadRan) throws Exception {
        boolean canContinue = false;
        String answerRepair = arguments[argumentIndex];
        argumentIndex++;
        answerRepair = whileAnswerIsYesOrNo(answerRepair,"-PO","-noPO");
        if(answerRepair.equals("-PO")) {
            if (preloadRan) {
                String answerKeepSameName = arguments[argumentIndex];
                argumentIndex++;
                if (answerKeepSameName.equals("-SF")) {
                    canContinue = true;
                    fileToGenerateOutputTo = fileToGenerateOutputToPreParse;
                }
                else if(answerKeepSameName.startsWith("-")){
                    System.out.println("Wrong argument");
                    exit(1);
                }
                else{
                    argumentIndex--;
                }
            }
            if(!canContinue){
                fileToGenerateOutputTo = arguments[argumentIndex];
                argumentIndex++;
                specifyFileNameForRepairs(fileToGenerateOutputTo);
            }
            summaryFile = arguments[argumentIndex];
            argumentIndex++;
            canContinue = checkIfFileExistsOperations(summaryFile,".txt",arguments[argumentIndex]);
            if(!canContinue){
                System.out.println("Specified file name " + summaryFile + " already exists, choose -OW or specify another file name");
                exit(1);
            }
            argumentIndex++;

            return true;
        }
        return false;
    }
    private String whileAnswerIsYesOrNo(String varName, String positive, String negative){
        if(varName.equals(positive) || varName.equals(negative)){
            return varName;
        }
        System.out.println(positive+" or "+negative+" were not chosen");
        exit(1);
        return varName;
    }
    private boolean checkIfFileExistsOperations(String file, String suffix, String argument){
        boolean canContinue = false;
        File checkFile = new File(FileSystems.getDefault().getPath(file + suffix).toUri());
        if (checkFile.exists()) {
            String answerTwo = argument;
            answerTwo = whileAnswerIsYesOrNo(answerTwo, "-OW","-noOW");
            if (answerTwo.equals("-OW")) {
                canContinue = true;
            }
        } else {
            canContinue = true;
        }
        return canContinue;
    }
    private boolean correctFileStructureChecker(String path) throws IOException {
        String line;
        String statementId = "", statementText = "";
        BufferedReader read = new BufferedReader(new FileReader(path + ".xml"));
        int declarationAndParentCount = 3;
        while ((line = read.readLine()) != null) {
            if (line.contains("<statement id")) {
                line = StringEscapeUtils.unescapeXml(line);
                line = StringEscapeUtils.unescapeXml(line);
                statementId = StringUtils.substringBetween(line, "<statement id=\"", "\"");
                statementText = StringUtils.substringBetween(line, ">", "</statement>");
            }
            else{
                declarationAndParentCount--;
            }
            if(declarationAndParentCount < 0 || statementId == null || statementText == null){
                return false;
            }
        }
        return true;
    }

    private void specifyFileNameForRepairs(String fileToGenerateOutputTo) throws IOException {
        if(fileToGenerateOutputTo.startsWith("-")){
            System.out.println("Invalid number of arguments, use -help");
            exit(1);
        }
        File checkFile = new File(FileSystems.getDefault().getPath(fileToGenerateOutputTo + ".xml").toUri());
        if (!checkFile.exists()) {
            System.out.println("Specified file name " + fileToGenerateOutputTo + " does not exists.");
            exit(1);
        } else {
            if(correctFileStructureChecker(fileToGenerateOutputTo)) {
                Path pathToAFile = Paths.get(fileToGenerateOutputTo);
                fileToGenerateOutputTo = pathToAFile.getFileName().toString();
                String alreadyFilteredOriginalCorrect = arguments[argumentIndex];
                argumentIndex++;
                alreadyFilteredOriginalCorrect = whileAnswerIsYesOrNo(alreadyFilteredOriginalCorrect,"-EA","-noEA");
                if (alreadyFilteredOriginalCorrect.equals("-noEA")) {
                    checkFile = new File(FileSystems.getDefault().getPath(fileToGenerateOutputTo + "_wrong0" + ".xml").toUri());
                    if (!checkFile.exists()) {
                        System.out.println("Specified file name " + fileToGenerateOutputTo + "_wrong0 does not exists.");
                        exit(1);
                    } else {
                        executeNumber = 1;
                    }
                }
            }
            else{
                System.out.println("Provided file "+fileToGenerateOutputTo+" has invalid structure, please generate this file from preParseOperations or use different file.");
                exit(1);
            }
        }
    }
}
