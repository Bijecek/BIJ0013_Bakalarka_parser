
public class MyParser {
    public static void main(String[] args) throws Exception {
        //This class is used for preloading data, first argument is INPUT, second is OUTPUT file
        //PreloadClass preload = new PreloadClass("C:\\Users\\sisin\\Downloads\\Posts.7z","final_statements");
        //preload.run();

        //This class is used for statement correction, first argument is testId (0-10), second argument is input file for those corrections
        //testID 0 is clean pass through Parser without any modification
        //file _repaired.txt is created when test 1 began
        ParserClass parserClass = new ParserClass(0, "statementy_pro_ukazku");
        parserClass.run();
        parserClass.calculateFinalResults();

        //VYSLEDKY
        /*
        Test  1 Pocet uspesnych oprav: 1755
        Test  2 Pocet uspesnych oprav: 36
        Test  1 2 Pocet uspesnych oprav: 1
        Test  3 Pocet uspesnych oprav: 27
        Test  4 Pocet uspesnych oprav: 27
        Test  5 Pocet uspesnych oprav: 94
        Test  1 5 Pocet uspesnych oprav: 1
        Test  6 Pocet uspesnych oprav: 583
        Test  1 6 Pocet uspesnych oprav: 18
        Test  4 6 Pocet uspesnych oprav: 1
        Test  2 6 Pocet uspesnych oprav: 1
        Test  7 Pocet uspesnych oprav: 25
        Test  1 7 Pocet uspesnych oprav: 1
        Test  8 Pocet uspesnych oprav: 39
        Test  1 8 Pocet uspesnych oprav: 3
        Test  9 Pocet uspesnych oprav: 142
        Test  1 9 Pocet uspesnych oprav: 23
        Test  8 9 Pocet uspesnych oprav: 2
        Test  2 9 Pocet uspesnych oprav: 1
        Test  10 Pocet uspesnych oprav: 983
        Test  1 10 Pocet uspesnych oprav: 32
        Test  4 10 Pocet uspesnych oprav: 4
        Test  2 10 Pocet uspesnych oprav: 7
        Test  8 10 Pocet uspesnych oprav: 1
        Test  3 10 Pocet uspesnych oprav: 7
        Test  5 10 Pocet uspesnych oprav: 3
        Test  1 2 4 10 Pocet uspesnych oprav: 1
        Test  1 3 10 Pocet uspesnych oprav: 1
        Test  1 9 10 Pocet uspesnych oprav: 1
        Test  3 4 10 Pocet uspesnych oprav: 1
        Test  9 10 Pocet uspesnych oprav: 2
        Test  4 8 10 Pocet uspesnych oprav: 1
        Test  11 Pocet uspesnych oprav: 1686
        Test  1 11 Pocet uspesnych oprav: 43
        Test  2 11 Pocet uspesnych oprav: 9
        Test  1 9 11 Pocet uspesnych oprav: 2
        Test  5 11 Pocet uspesnych oprav: 4
        Test  4 11 Pocet uspesnych oprav: 13
        Test  1 2 11 Pocet uspesnych oprav: 2
        Test  1 2 4 11 Pocet uspesnych oprav: 1
        Test  3 11 Pocet uspesnych oprav: 7
        Test  9 11 Pocet uspesnych oprav: 3
        Test  8 11 Pocet uspesnych oprav: 1
        ------------------------------------
        Celkem opraveno 5595 prikazu z celkovych 87219
        Coz je 6.4148865 % uspesnost
        ------------------------------------
         */

    }
}
