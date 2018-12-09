package nz.wex.icefoxman.framework.csv.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by max user on 11.11.2017.
 */
public class CsvWriter {
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String COLUMN_SEPARATOR = ";";
    private static final String[] FILE_HEADER = {"Time", "Type", "Pair", "Amount", "Price", "Sum", "Profit"};

    private File file;

    public CsvWriter(String pair) {
        file = new File("log/deal/" + pair + ".csv");
        if (!file.exists()) {
            try {
                file.createNewFile();
                append(FILE_HEADER);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void append(Object[] values) {
        StringBuilder sb = new StringBuilder();
        for (Object value : values) {
            sb.append(value).append(COLUMN_SEPARATOR);
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(LINE_SEPARATOR);

        try {
            FileWriter writer = new FileWriter(file, true);
            writer.append(sb.toString());
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
