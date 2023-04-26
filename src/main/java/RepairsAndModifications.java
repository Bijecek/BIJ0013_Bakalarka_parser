import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RepairsAndModifications {
    private String statementText;
    private boolean statementModified;

    private ArrayList<String> modifiedStatement;

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

    //this method handles comma before entered clause as well as adding space before this clause (FROM, WHERE etc...)
    private ArrayList<String> removeCommaOrSomethingInfront(String search_string, String statement_text, boolean add_space_infront) {
        int index;
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

    //this method handles BY insertion after entered clause (ORDER, GROUP)
    private ArrayList<String> insertByAfter(String search_string, String statement_text) {
        int index;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        statement_text = statement_text.toLowerCase();
        while ((index = statement_text.indexOf(search_string, searchIndex)) != -1) {
            if (/*(!search_string.equals("group ") && (!search_string.equals("\ngroup "))) || */!statement_text.substring(searchIndex, index).contains("within ") || search_string.equals("order ")) {
                for (int i = (index + search_string.length()); i < statement_text.length(); i++) {
                    if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n') {
                        if (!Objects.equals(nearestSubstring(i, statement_text).toLowerCase(), "by")) {
                            modified_statement.insert(i - 1, " BY");
                            modified = true;
                        }
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

    //method that checks for valid MSSQL TOP clause
    private ArrayList<String> checkForValidTop(String search_string, String statement_text) {
        int index;
        int searchIndex = 0;
        boolean modified = false;
        StringBuilder modified_statement = new StringBuilder(statement_text);
        while ((index = statement_text.toLowerCase().indexOf(search_string, searchIndex)) != -1) {

            for (int i = (index + search_string.length()); i < statement_text.length(); i++) {
                if (statement_text.charAt(i) != ' ' && statement_text.charAt(i) != '\n' && !Character.isDigit(statement_text.charAt(i))) {
                    if (i + 2 != statement_text.length() && statement_text.charAt(i) != '*' && (Objects.equals(nearestSubstring(i, statement_text).toLowerCase(), "from") || Objects.equals(nearestSubstring(i, statement_text), ","))) {
                        modified_statement.insert(i, "* ");
                        modified = true;
                    }
                    break;
                }
            }
            searchIndex = index + search_string.length() - 1;
        }
        ArrayList<String> array = new ArrayList<>();
        array.add(modified_statement.toString());
        array.add(Boolean.toString(modified));
        return array;
    }

    //method that finds the nearest substring to given index
    private String nearestSubstring(int start_Index, String text) {
        String nearest = "";
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

    //test 0 - plain parse without any modification
    public void test0NoModification() {
        statementModified = true;
    }

    //test 1 - from modification
    public void test1FromModification() {
        if (statementText.toLowerCase().contains("from")) {
            modifiedStatement = removeCommaOrSomethingInfront("from", statementText, true);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }
        }

        if (statementText.toLowerCase().contains(" form ")) {
            statementText = statementText.replaceAll(" form ", " FROM ");
            statementModified = true;
        }

    }

    //test 2 - where modification
    public void test2WhereModification() {
        if (statementText.toLowerCase().contains("where")) {
            modifiedStatement = removeCommaOrSomethingInfront("where", statementText, true);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }
        }
    }

    //test 3 - group modification
    public void test3GroupModification() {

        if (statementText.toLowerCase().contains("group ")) {
            modifiedStatement = insertByAfter("group ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }

        }
    }

    //test 4 - order modification
    public void test4OrderModification() {
        if (statementText.toLowerCase().contains("order ")) {
            modifiedStatement = insertByAfter("order ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }
        }
    }

    //test 5 - top modification
    public void test5TopModification() {
        if (statementText.toLowerCase().contains(" top ")) {
            modifiedStatement = checkForValidTop(" top ", statementText);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }
        }
    }

    //test 6 - Brackets 1st modification
    public void test6Brackets1stModification() {
        if (statementText.contains("(") || statementText.contains(")")) {
            int left = StringUtils.countMatches(statementText, "(");
            int right = StringUtils.countMatches(statementText, ")");
            if (left != right) {
                int index;
                if (left > right) {
                    index = statementText.lastIndexOf("(");
                } else {
                    index = statementText.indexOf(")");
                }
                statementText = statementText.substring(0, index) + ' ' + statementText.substring(index + 1);
                statementModified = true;
            }
        }
    }

    //test 7 - Brackets 2nd modification
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

                    }
                }
            }
        }
    }

    //test 8 - as modification
    public void test8AsModification() {
        if (statementText.toLowerCase().contains("procedure") && statementText.toLowerCase().contains("as")) {

            modifiedStatement = removeCommaOrSomethingInfront("as", statementText, false);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }

        }
    }

    //test 9 - Brackets 3rd modification
    public void test9Brackets3rdModification() {
        if (statementText.contains(")")) {
            modifiedStatement = removeCommaOrSomethingInfront(")", statementText, false);
            if (Objects.equals(modifiedStatement.get(1), "true")) {
                statementText = modifiedStatement.get(0);
                statementModified = true;
            }
        }
    }

    //test 10 - advanced modification
    public void test10AdvancedModification() {
        statementModified = true;
    }

    //test 11 - remove dots, replace comments and wrap all @xxx using [ and ] brackets
    public void test11RemoveDots() {
        String statementUpdated = statementText.replaceAll("\\.\\.\\.", "").replaceAll("\\. \\. \\.", "").replaceAll("==", "=").replaceAll("//", "--");
        if (!statementText.equals(statementUpdated)) {
            statementText = statementUpdated;
            statementModified = true;
        }

        if(statementText.contains("@")) {
            Pattern pattern = Pattern.compile("@+([a-zA-Z_]+)");
            Matcher matcher = pattern.matcher(statementText);

            Set<String> listMatches = new HashSet<>();

            while (matcher.find()) {
                listMatches.add(matcher.group(0));
            }
            String[] matchesArray = listMatches.toArray(new String[0]);
            Arrays.sort(matchesArray, Comparator.comparing(String::length).reversed());
            for (String s : matchesArray) {
                statementText = statementText.replace(s, "[" + s + "]");
            }
            statementModified = true;
        }

    }



    //modify statement with help of advanced repairs (ANTLR syntax error)
    public String modifyStatement(ArrayList<Integer> positionsForRepair, String possibleRepairs, String statementForParser) {
        ArrayList<Integer> updatedPositionsForRepair = new ArrayList<>(positionsForRepair);
        int shiftIndex = 0;
        String[] possibleRepairsArray = possibleRepairs.split(";");
        StringBuilder repaired_statement = new StringBuilder(statementForParser);
        for (int i = 0; i < possibleRepairsArray.length; i++) {
            repaired_statement.insert(updatedPositionsForRepair.get(i), " " + possibleRepairsArray[i] + " ");
            for (int x = shiftIndex + 1; x < updatedPositionsForRepair.size(); x++) {
                updatedPositionsForRepair.set(x, updatedPositionsForRepair.get(x) + (" " + possibleRepairsArray[i] + " ").length());
            }
            shiftIndex++;
        }
        return repaired_statement.toString();
    }
}
