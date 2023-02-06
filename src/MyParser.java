import org.apache.commons.compress.archivers.sevenz.SevenZFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MyParser {
    public static void main(String[] args) throws Exception {
        //Temporary_test preload = new Temporary_test();
        //preload.run();

        //for(int i=0;i<2;i++) {
            ParserClass parserClass = new ParserClass(10);
            parserClass.run();
        //}

    }
}
