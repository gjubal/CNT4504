package version1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class CSVParser {
    public void export(ArrayList<String> dataList, String fileName) throws FileNotFoundException {
            PrintWriter out = new PrintWriter(new File(fileName + ".csv"));
            StringBuilder sb = new StringBuilder();

            for (int row = 0; row < dataList.size(); row++) {
                String escapedValue = String.format("\"%s\"%n", dataList.get(row));
                sb.append(escapedValue);
            }

            out.write(sb.toString());
    }
}
