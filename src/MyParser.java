import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class MyParser {
    public static void main(String[] args) throws Exception {
        //Temporary_test preload = new Temporary_test();
        //preload.run();

        //for(int i=0;i<2;i++) {
            ParserClass parserClass = new ParserClass(11);
            parserClass.run();
        //}

    }
}
