import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Objects;

public class RepairsAndModifications {
    private String statementText;
    private boolean statementModified;

    private ArrayList<String> modifiedStatement;

    int fromMistake = 0;

    int whereMistake = 0;

    int groupbyMistake = 0;

    int orderbyMistake = 0;

    int topCheck = 0;

    int leftRightBrackets = 0;

    int commaInfrontAs = 0;

    int commaInfrontBracket = 0;

    int autoBrackets = 0;
    int autoBeginend = 0;
    int autoComma = 0;
    int autoBrackets2 = 0;

    int isCorrected = 0;

    int numberOfTotalStatements = 0;

    public void setStatementText(String statementText) {
        this.statementText = statementText;
        this.statementModified = false;
    }

    public String getStatementText() {
        return this.statementText;
    }

    public boolean getStatementModified() {
        return this.statementModified;
    }

    public ArrayList<String> removeCommaOrSomethingInfront(String search_string, String statement_text, boolean add_space_infront) {
        int index = 0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        while ((index = statement_text.toLowerCase().indexOf(search_string, searchIndex)) != -1) {

            for (int i = index - 1; i >= 0; i--) {
                if (statement_text.charAt(i) == ',') {
                    modified = true;
                    modified_statement.setCharAt(i, ' ');
                    break;
                } else if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n') {
                    break;
                }
            }
            //test na napr. SELECT ASCFROM
            if (add_space_infront) {
                if (index > 0 && statement_text.charAt(index - 1) != ' ' && statement_text.charAt(index - 1) != '\n') {
                    modified_statement.insert(index - 1, ' ');
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

    public ArrayList<String> insertByAfter(String search_string, String statement_text) {
        int index = 0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        statement_text = statement_text.toLowerCase();
        while ((index = statement_text.indexOf(search_string, searchIndex)) != -1) {

            if ((search_string.equals("group ") || (search_string.equals("\ngroup "))) && statement_text.substring(searchIndex, index).contains("within ")) {
                searchIndex = index + search_string.length() - 1;
            } else {
                for (int i = (index + search_string.length() - 1); i < statement_text.length(); i++) {
                    if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n') {
                        if (i + 1 != statement_text.length() && statement_text.charAt(i) != 'b' && statement_text.charAt(i + 1) != 'y') {
                            modified_statement.insert(i - 1, " BY");
                            modified = true;
                            break;
                        } else if (statement_text.charAt(i) == 'b' && statement_text.charAt(i + 1) == 'y') {
                            break;
                        } else {
                            modified_statement.insert(i - 1, " BY");
                            modified = true;
                            break;
                        }
                    }
                }
                searchIndex = index + search_string.length() - 1;
            }
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }

    public ArrayList<String> checkForValidTop(String search_string, String statement_text) {
        int index = 0;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        while ((index = statement_text.toLowerCase().indexOf(search_string, searchIndex)) != -1) {

            for (int i = (index + search_string.length() - 1); i < statement_text.length(); i++) {
                if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n' && !Character.isDigit(statement_text.charAt(i))) {
                    if (i + 2 != statement_text.length() && statement_text.charAt(i) != '*' && (Objects.equals(nearestSubstring(i, statement_text), "FROM") || Objects.equals(nearestSubstring(i, statement_text), ","))) {
                        modified_statement.insert(i, "* ");
                        modified = true;
                        break;
                    } else {
                        break;
                    }
                }
            }
            searchIndex = index + search_string.length() - 1;
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }

    public String nearestSubstring(int start_Index, String text) {
        String nearest = new String();
        boolean found = false;
        for (int i = start_Index; i < text.length(); i++) {
            if (text.charAt(i) != '\n' && text.charAt(i) != ' ') {
                nearest += text.charAt(i);
                found = true;
            } else {
                if (found) {
                    break;
                }
            }
        }
        return nearest;
    }

    public void test0NoModification() {
        statementText = statementText.replaceAll("&amp;quot;", "\"");
        statementText = statementText.replaceAll("<br>", " ");
        statementText = statementText.replaceAll("</br>", " ");
        statementText = statementText.replaceAll("<pre>", " ");
        statementText = statementText.replaceAll("</pre>", " ");
        statementText = statementText.replaceAll("<p>", " ");
        statementText = statementText.replaceAll("</p>", " ");
        //nebo &lt;br&gt
        statementModified = true;
    }

    public void test1FromModification() {
        if (statementText.toLowerCase().contains("from")) {
            modifiedStatement = removeCommaOrSomethingInfront("from", statementText, true);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                fromMistake++;
            }
        }

        if (statementText.toLowerCase().contains(" form ")) {
            statementText = statementText.replaceAll(" form ", " FROM ");
            statementModified = true;
            fromMistake++;
        }
    }

    public void test2WhereModification() {
        if (statementText.toLowerCase().contains("where")) {
            modifiedStatement = removeCommaOrSomethingInfront("where", statementText, true);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                whereMistake++;
            }
        }
    }

    public void test3GroupModification() {

        if (statementText.toLowerCase().contains("group ")) {
            modifiedStatement = insertByAfter("group ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                groupbyMistake++;
            }

        }
    }

    public void test4OrderModification() {
        if (statementText.toLowerCase().contains("order ")) {
            modifiedStatement = insertByAfter("order ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                orderbyMistake++;
            }
        }
    }

    public void test5TopModification() {
        if (statementText.toLowerCase().contains(" top ")) {
            modifiedStatement = checkForValidTop(" top ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                topCheck++;
            }
        }
    }

    public void test6Brackets1stModification() {
        if (statementText.contains("(") || statementText.contains(")")) {
            int left = StringUtils.countMatches(statementText, "(");
            int right = StringUtils.countMatches(statementText, ")");
            if (left != right) {
                if (left > right) {
                    int index = statementText.lastIndexOf("(");
                    statementText = statementText.substring(0, index) + ' ' + statementText.substring(index + 1);
                    statementModified = true;
                } else {
                    int index = statementText.indexOf(")");
                    statementText = statementText.substring(0, index) + ' ' + statementText.substring(index + 1);
                    statementModified = true;
                }
                leftRightBrackets++;
            }
        }
    }

    public void test7Brackets2ndModification() {
        if (statementText.contains("(") || statementText.contains(")")) {
            int left = StringUtils.countMatches(statementText, "(");
            int right = StringUtils.countMatches(statementText, ")");
            if (left != right) {
                if (left > right && right != 0) {
                    int index_r = statementText.indexOf(")") + 1;
                    if (Objects.equals(nearestSubstring(index_r, statementText).toLowerCase(), "from") || Objects.equals(nearestSubstring(index_r, statementText), ",")) {
                        statementText = statementText.substring(0, index_r) + ") " + statementText.substring(index_r + 1);
                        statementModified = true;
                        leftRightBrackets++;

                    }
                }
            }
        }
    }

    public void test8AsModification() {
        if (statementText.toLowerCase().contains("procedure") && statementText.toLowerCase().contains("as")) {

            modifiedStatement = removeCommaOrSomethingInfront("as", statementText, false);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                commaInfrontAs++;
            }

        }
    }

    public void test9Brackets3rdModification() {

        if (statementText.contains(")")) {
            modifiedStatement = removeCommaOrSomethingInfront(")", statementText, false);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
                commaInfrontBracket++;
            }
        }
    }

    public void test10AdvancedModification() {
        statementModified = true;
    }

    public void test11RemoveDots() {
        String statementUpdated = statementText.replaceAll("\\.\\.\\.", "").replaceAll("\\. \\. \\.", "").replaceAll("==", "=").replaceAll("//", "--");
        if (!statementText.equals(statementUpdated)) {
            statementText = statementUpdated;
            statementModified = true;
        }
    }

    public void printResults(int testId) {
        if (testId == 1) {
            System.out.println("FROM & FORM");

            System.out.println("Nalezeno " + fromMistake);
        }
        if (testId == 2) {
            System.out.println("WHERE");

            System.out.println("Nalezeno " + whereMistake);

        }
        if (testId == 3) {
            System.out.println("GROUP BY");

            System.out.println("Nalezeno " + groupbyMistake);

        }
        if (testId == 4) {
            System.out.println("ORDER BY");

            System.out.println("Nalezeno " + orderbyMistake);
        }

        if (testId == 5) {
            System.out.println("TOP FOR");

            System.out.println("Nalezeno " + topCheck);

        }
        if (testId == 6) {
            System.out.println("BRACKETS");

            System.out.println("Nalezeno " + leftRightBrackets);
            leftRightBrackets = 0;

        }
        if (testId == 7) {
            System.out.println("BRACKETS");
            System.out.println("Nalezeno " + leftRightBrackets);

        }
        if (testId == 8) {
            System.out.println("COMMA INFRONT AS");
            System.out.println("Nalezeno " + commaInfrontAs);
        }
        if (testId == 9) {
            System.out.println("COMMA INFRONT BRACKET");
            System.out.println("Nalezeno " + commaInfrontBracket);
        }
        if (testId == 10) {
            System.out.println("Zavorky " + autoBrackets);
            System.out.println("BEGIN|END " + autoBeginend);
            System.out.println("CARKA " + autoComma);
            System.out.println("SET " + autoBrackets2);
        }
        System.out.println("Spravnych " + isCorrected);
        isCorrected = 0;

        System.out.println("CELKEM prikazu: " + numberOfTotalStatements);
        numberOfTotalStatements = 0;
    }

    public String modifyStatement(ArrayList<Integer> positionsForRepair, String possibleRepairs, String statementForParser) {
        ArrayList<Integer> updatedPositionsForRepair = new ArrayList<>(positionsForRepair);
        int shiftIndex = 0;
        String[] possibleRepairsArray = possibleRepairs.split(";");
        StringBuilder repaired_statement = new StringBuilder(statementForParser);
        for (int i = 0; i < possibleRepairsArray.length; i++) {
            repaired_statement.insert(updatedPositionsForRepair.get(i), " " + possibleRepairsArray[i] + " ");
            for (int x = shiftIndex + 1; x < updatedPositionsForRepair.size(); x++) {
                updatedPositionsForRepair.set(x, updatedPositionsForRepair.get(x) + (new String(" " + possibleRepairsArray[i] + " ")).length());
            }
            shiftIndex++;
        }
        return repaired_statement.toString();
    }
}
