import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class RepairsAndModificationsTest {

    private void setAndTestStatement(RepairsAndModifications repair, ParserClass parse){
        parse.setStatementText(repair.getStatementText());
        parse.setStatementId("99999");
        parse.setAllRepairs(" ");
        parse.setStatementForParser("");
        parse.executeParseForUnitTestPurpose();
    }

    @Test
    public void test1FromModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT a, FROM B");

        ParserClass parse = new ParserClass(1);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test1FromModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test2WhereModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT a FROM B, WHERE a=0");

        ParserClass parse = new ParserClass(2);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test2WhereModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test3GroupModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT a FROM b GROUP a,b");

        ParserClass parse = new ParserClass(3);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test3GroupModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test4OrderModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT a FROM b ORDER a,b");

        ParserClass parse = new ParserClass(4);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test4OrderModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test5TopModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT TOP 1 FROM b");

        ParserClass parse = new ParserClass(5);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test5TopModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test6Brackets1stModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT (SELECT a FROM b )) FROM c");

        ParserClass parse = new ParserClass(6);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test6Brackets1stModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test7Brackets2ndModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT (SELECT a FROM b () FROM c");

        ParserClass parse = new ParserClass(7);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test7Brackets2ndModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test8AsModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("CREATE PROCEDURE GetProductDesc\n" +
                "@var_temp varchar(10), \n"+
                "AS\n" +
                "BEGIN\n" +
                "SELECT a FROM B \n" +
                "END");

        ParserClass parse = new ParserClass(8);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test8AsModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test9Brackets3rdModification() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("SELECT (SELECT a FROM a,) FROM c");

        ParserClass parse = new ParserClass(9);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test9Brackets3rdModification();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

    @Test
    public void test11RemoveDots() {
        RepairsAndModifications repair = new RepairsAndModifications();
        repair.setStatementText("...\n" +
                "SELECT a FROM b\n" +
                "WHERE b = 0\n" +
                "...");

        ParserClass parse = new ParserClass(11);
        setAndTestStatement(repair,parse);
        assertEquals(0, parse.getCorrectStatements().size());

        repair.test11RemoveDots();

        setAndTestStatement(repair,parse);
        assertEquals(1, parse.getCorrectStatements().size());
    }

}