import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static java.lang.System.exit;

public class ChooseYourOperation {
    private final String ANSI_RED_BACKGROUND = "\033[41m";
    private final String ANSI_RESET = "\033[0m";

    private int executeNumber=0;


    public void chooseAndSpecifyOperations() throws Exception {
        Scanner sc= new Scanner(System.in);
        System.out.println("B.S. thesis - BIJ0013 - Error Correction of SQL Commands from StackOverflow");
        System.out.println("---------------------------------------------------------------------------");


        boolean preloadRan = preParseOperation(sc);
        parseOperations(sc,preloadRan);

    }
    private boolean preParseOperation(Scanner sc) throws Exception {
        boolean preloadRan = false;
        String path = null;
        String fileToGenerateOutputTo = null;

        System.out.println("Do you want to preload some data from StackOverflow?");
        System.out.println("NOTE: Only statements containing SELECT and required filters will be processed.");
        String answerOne = "";
        answerOne = whileAnswerIsYesOrNo(sc,answerOne);
        if(answerOne.equals("yes")){
            boolean canContinue = false;
            while(!canContinue){
                System.out.println("Please specify path to file that you want to be processed: ");
                System.out.println("NOTE: Specified file must have .7z suffix");
                path = sc.nextLine();
                File checkFile = new File(path);
                String suffix = FileNameUtils.getExtension(path);
                if(!checkFile.exists() || checkFile.isDirectory()){
                    System.out.println(ANSI_RED_BACKGROUND+"Specified path to file "+path+" does not exists."+ANSI_RESET);
                }
                else if(!suffix.equals("7z")){
                    System.out.println(ANSI_RED_BACKGROUND+"Specified path to file "+path+" does not have .7z suffix."+ANSI_RESET);
                }
                else{
                    System.out.println("---------------------------------------------------------------------------");
                    while(!canContinue){
                        System.out.println("Please specify name of the file that will be created in working directory: ");
                        System.out.println("NOTE: Do not specify the suffix");
                        fileToGenerateOutputTo = sc.nextLine();
                        canContinue = checkIfFileExistsOperations(sc,fileToGenerateOutputTo,".xml");
                    }
                    System.out.println("Generating output FROM: "+path+" TO file name: "+fileToGenerateOutputTo);
                    System.out.println("Are these information correct?");
                    String preloadFinalAnswer = "";
                    preloadFinalAnswer = whileAnswerIsYesOrNo(sc,preloadFinalAnswer);

                    if(preloadFinalAnswer.equals("yes")) {
                        //This class is used for preloading data, first argument is INPUT, second is OUTPUT file
                        PreloadClass preload = new PreloadClass(path, fileToGenerateOutputTo);
                        preload.run();
                        preloadRan = true;
                    }
                    else{
                        System.out.println("Do you want to enter new information?");
                        String preloadRetryAnswer = "";
                        preloadRetryAnswer = whileAnswerIsYesOrNo(sc,preloadRetryAnswer);
                        if(preloadRetryAnswer.equals("yes")){
                            canContinue = false;
                        }
                        else{
                            break;
                        }
                    }
                }
            }
        }
        return preloadRan;
    }
    private void parseOperations(Scanner sc, boolean preloadRan) throws Exception {
        String answerRepair = "";
        String fileToGenerateOutputTo = "";

        System.out.println("Do you want to repair statements?");

        answerRepair = whileAnswerIsYesOrNo(sc,answerRepair);
        if(answerRepair.equals("yes")) {
            boolean canContinue = false;
            while (!canContinue) {
                if (preloadRan) {
                    System.out.println("Do you want to execute repairs on file: " + fileToGenerateOutputTo + " that was generated from preParseOperations ?");
                    String answerKeepSameName = "";
                    answerKeepSameName = whileAnswerIsYesOrNo(sc, answerKeepSameName);
                    if (answerKeepSameName.equals("yes")) {
                        canContinue = true;
                    }
                }
                while (!canContinue) {
                    System.out.println("Please specify name of the file on which you want to execute repairs: (file must have .xml suffix)");
                    System.out.println("NOTE: Do not specify the suffix");
                    System.out.println("NOTE2: If the file is not present in the working directory, specify full path to this file");
                    System.out.println("NOTE3: Type quit if you want to exit program");
                    fileToGenerateOutputTo = sc.nextLine().toLowerCase();
                    canContinue = specifyFileNameForRepairs(sc,fileToGenerateOutputTo);
                }
                System.out.println("Are these information correct?");
                String repairFinalAnswer = "";
                repairFinalAnswer = whileAnswerIsYesOrNo(sc, repairFinalAnswer);
                if(repairFinalAnswer.equals("yes")) {
                    String summaryFile = "";
                    canContinue = false;
                    while(!canContinue) {
                        System.out.println("Please specify name of summary results file that will be created in working directory: ");
                        System.out.println("NOTE: Do not specify the suffix");
                        summaryFile = sc.nextLine();
                        canContinue = checkIfFileExistsOperations(sc,summaryFile,".txt");
                    }
                    //executeNumber 0 is clean pass through Parser without any modification
                    //file _repaired.txt is created when test 1 began
                    ParserClass parserClass = new ParserClass(executeNumber, fileToGenerateOutputTo);
                    parserClass.run();
                    parserClass.calculateFinalResults(summaryFile);
                    System.out.println("Your summary results file is located at: "+ Paths.get(summaryFile+".txt").toAbsolutePath());
                }
                else{
                    System.out.println("Do you want to enter new information?");
                    String parseRetryAnswer= "";
                    parseRetryAnswer = whileAnswerIsYesOrNo(sc,parseRetryAnswer);
                    if(parseRetryAnswer.equals("yes")){
                        canContinue = false;
                    }
                    else{
                        break;
                    }
                }
            }
        }
    }
    private String whileAnswerIsYesOrNo(Scanner sc, String varName){
        while(!varName.equals("yes") && !varName.equals("no")){
            System.out.println("NOTE: Type quit if you want to exit program");
            System.out.print("Type yes or no: ");
            varName = sc.nextLine().toLowerCase();
            if(varName.equals("quit")){
                exit(1);
            }
        }
        System.out.println("---------------------------------------------------------------------------");
        return varName;
    }
    private boolean checkIfFileExistsOperations(Scanner sc, String file, String suffix){
        boolean canContinue = false;
        File checkFile = new File(FileSystems.getDefault().getPath(file + suffix).toUri());
        if (checkFile.exists()) {
            System.out.println(ANSI_RED_BACKGROUND + "Specified file name " + file + " already exists, do you want to overwrite it?" + ANSI_RESET);
            String answerTwo = "";
            answerTwo = whileAnswerIsYesOrNo(sc, answerTwo);
            if (answerTwo.equals("yes")) {
                canContinue = true;
            }
        } else {
            System.out.println("---------------------------------------------------------------------------");
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
    private boolean specifyFileNameForRepairs(Scanner sc, String fileToGenerateOutputTo) throws IOException {
        boolean canContinue = false;
        if(fileToGenerateOutputTo.equals("quit")){
            exit(1);
        }
        File checkFile = new File(FileSystems.getDefault().getPath(fileToGenerateOutputTo + ".xml").toUri());
        if (!checkFile.exists()) {
            System.out.println(ANSI_RED_BACKGROUND + "Specified file name " + fileToGenerateOutputTo + " does not exists." + ANSI_RESET);
        } else {
            if(correctFileStructureChecker(fileToGenerateOutputTo)) {
                Path pathToAFile = Paths.get(fileToGenerateOutputTo);
                fileToGenerateOutputTo = pathToAFile.getFileName().toString();
                System.out.println("---------------------------------------------------------------------------");
                System.out.println("Choose from two options:");
                System.out.println("Type yes, if file " + fileToGenerateOutputTo + "_wrong0 is already present in the working directory.");
                System.out.println("Type no, if this file is missing.");
                String alreadyFilteredOriginalCorrect = "";
                alreadyFilteredOriginalCorrect = whileAnswerIsYesOrNo(sc, alreadyFilteredOriginalCorrect);
                if (alreadyFilteredOriginalCorrect.equals("yes")) {
                    checkFile = new File(FileSystems.getDefault().getPath(fileToGenerateOutputTo + "_wrong0" + ".xml").toUri());
                    if (!checkFile.exists()) {
                        System.out.println(ANSI_RED_BACKGROUND + "Specified file name " + fileToGenerateOutputTo + "_wrong0" + " does not exists." + ANSI_RESET);
                    } else {
                        executeNumber = 1;
                        System.out.println("Executing repairs on file: " + fileToGenerateOutputTo + ", " + fileToGenerateOutputTo + "_wrong0 is already processed.");
                        canContinue = true;
                    }
                } else {
                    System.out.println("Executing repairs on file: " + fileToGenerateOutputTo + ", generating all files.");
                    canContinue = true;
                }
            }
            else{
                System.out.println(ANSI_RED_BACKGROUND +"Provided file "+fileToGenerateOutputTo+" has invalid structure, please generate this file from preParseOperations or use different file." + ANSI_RESET);
            }
        }
        return canContinue;
    }
}
