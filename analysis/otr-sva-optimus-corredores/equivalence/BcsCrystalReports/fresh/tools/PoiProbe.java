import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;

/** Prints, for every numeric cell of an xls, the Excel format string and what POI's DataFormatter(en_US) makes of it
 *  (the same call the legacy and the sidecar TXT export use). Run once with POI 3.17 and once with POI 5.2.5 classpath. */
public class PoiProbe {
    public static void main(String[] a) throws Exception {
        HSSFWorkbook wb = new HSSFWorkbook(new POIFSFileSystem(new ByteArrayInputStream(Files.readAllBytes(Paths.get(a[0])))));
        DataFormatter f = new DataFormatter(new Locale("en", "US"));
        for (Row r : wb.getSheetAt(0)) {
            for (Cell c : r) {
                double v;
                try {
                    v = c.getNumericCellValue();
                } catch (RuntimeException e) {
                    continue;   // string cell
                }
                System.out.println(c.getRowIndex() + "," + c.getColumnIndex() + " v=" + v
                        + " fmt=[" + c.getCellStyle().getDataFormatString() + "] -> [" + f.formatCellValue(c) + "]");
            }
        }
    }
}
