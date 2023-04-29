import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepairsAndModificationsTest {
    double start;
    double end;

    public static ArrayList<String> testCases = new ArrayList<>();
    @Test
    @Order(0)
    public void loadFromFile() throws IOException {
        BufferedReader br=new BufferedReader(new FileReader("predpripraveneSQL.txt"));
        String line="";
        StringBuilder sb = new StringBuilder();
        while((line = br.readLine()) != null){
            if(line.startsWith("--Oprava ") && !sb.isEmpty()){
                testCases.add(sb.toString());
                sb = new StringBuilder();
            }
            else if(!line.startsWith("--Oprava ")){
                sb.append(line);
                sb.append("\n");
            }
        }
        if(!sb.isEmpty()){
            testCases.add(sb.toString());
        }
    }

    private void setAndTestStatement(RepairsAndModifications repair, ParserClass parse){
        parse.setStatementText(repair.getStatementText());
        parse.setStatementId("99999");
        parse.setAllRepairs(" ");
        parse.setStatementForParser("");
        parse.executeParseForUnitTestPurpose();
    }
    private void start(){
        start = System.currentTimeMillis();
    }
    private void endAndResult(int testId){
        end = System.currentTimeMillis();
        System.out.println("Repair"+testId+" ="+ (((end - start)/60000)*60)+" seconds");
    }

    @Test
    @Order(1)
    public void test1FromModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(0));

        ParserClass parse = new ParserClass(1);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();
        repair.test1FromModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(1);
    }

    @Test
    @Order(2)
    public void test2WhereModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(1));

        ParserClass parse = new ParserClass(2);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test2WhereModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(2);
    }

    @Test
    @Order(3)
    public void test3GroupModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(2));

        ParserClass parse = new ParserClass(3);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test3GroupModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(3);
    }

    @Test
    @Order(4)
    public void test4OrderModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(3));

        ParserClass parse = new ParserClass(4);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test4OrderModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(4);
    }

    @Test
    @Order(5)
    public void test5TopModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(4));

        ParserClass parse = new ParserClass(5);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test5TopModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(5);
    }

    @Test
    @Order(6)
    public void test6Brackets1stModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(5));

        ParserClass parse = new ParserClass(6);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test6Brackets1stModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(6);
    }

    @Test
    @Order(7)
    public void test7Brackets2ndModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(6));

        ParserClass parse = new ParserClass(7);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test7Brackets2ndModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(7);
    }

    @Test
    @Order(8)
    public void test8AsModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(7));

        ParserClass parse = new ParserClass(8);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test8AsModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(8);
    }

    @Test
    @Order(9)
    public void test9Brackets3rdModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(8));

        ParserClass parse = new ParserClass(9);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test9Brackets3rdModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(9);
    }
    @Test
    @Order(10)
    public void test10AdvancedModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(9));

        //testId 9 is there because if we type testId 10, the tool will automatically repair it
        //we need to see that this SQL command is wrong
        ParserClass parse = new ParserClass(9);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test10AdvancedModification();
        ParserClass parse1 = new ParserClass(10);
        setAndTestStatement(repair,parse1);
        assertEquals(1, parse1.getCorrectStatements().size());
        endAndResult(10);
    }
    @Test
    @Order(11)
    public void test11RemoveDots() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText(testCases.get(10));


        ParserClass parse = new ParserClass(11);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        start();

        repair.test11RemoveDots();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
        endAndResult(11);
    }

}