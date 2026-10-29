package cl.bcs.cuentainversion.reportes;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Small helpers shared by the two commission "informe" files (daily patrimony .xlsx, collection summary .xls): the thin-bordered
 * cell styles of the legacy workbooks and the one-line log file.
 */
public final class ExcelInformes {

    public static final String FORMATO_MONEDA = "_ $* #,##0_ ;_ $* -#,##0_ ;_ $* \"-\"??_ ;_ @_ ";

    private ExcelInformes() {
    }

    /** Thin border on the four sides, with an optional number format. */
    public static CellStyle conBorde(Workbook libro, String formato) {
        CellStyle e = libro.createCellStyle();
        if (formato != null) {
            DataFormat df = libro.createDataFormat();
            e.setDataFormat(df.getFormat(formato));
        }
        e.setBorderBottom(BorderStyle.THIN);
        e.setBorderLeft(BorderStyle.THIN);
        e.setBorderRight(BorderStyle.THIN);
        e.setBorderTop(BorderStyle.THIN);
        return e;
    }

    /** Bold, centered, wrapped header cell with border. */
    public static CellStyle encabezado(Workbook libro) {
        CellStyle e = conBorde(libro, null);
        Font f = libro.createFont();
        f.setBold(true);
        e.setFont(f);
        e.setAlignment(HorizontalAlignment.CENTER);
        e.setWrapText(true);
        return e;
    }

    /**
     * Writes {@code texto} and a line break to the log file. The legacy used the platform line separator; the recorded goldens come
     * from Windows, so CR LF is written explicitly (same bytes on every platform).
     */
    public static void escribirLog(String ruta, String texto) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(new File(ruta)))) {
            w.write(texto);
            w.write("\r\n");
        }
    }
}
