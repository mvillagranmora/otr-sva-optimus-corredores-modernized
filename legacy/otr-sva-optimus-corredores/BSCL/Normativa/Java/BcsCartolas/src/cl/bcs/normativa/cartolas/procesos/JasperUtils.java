package cl.bcs.normativa.cartolas.procesos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;

import cl.bcs.utils.FileUtils;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintElement;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.fill.JRTemplatePrintText;
import net.sf.jasperreports.engine.util.JRLoader;

public final class JasperUtils {

    public static String compileAndSerializeReport(
            final InputStream inputStream)
                    throws IOException,
                    JRException {
        final File tempfile = File.createTempFile(RandomStringUtils.randomAlphanumeric(8), ".tmp");
        compileReport(inputStream, tempfile);
        return FileUtils.encodeBase64String(tempfile);
    }

    private static void compileReport(
            final InputStream inputStream,
            final File file)
                    throws JRException,
                    IOException,
                    FileNotFoundException {
        try (final OutputStream os = new FileOutputStream(file)) {
            JasperCompileManager.compileReportToStream(inputStream, os);
        }
    }

    public static int countPages(
            final List<JasperPrint> jpList) {
        int count = 0;
        for (final JasperPrint jasperPrint : jpList) {
            count += jasperPrint.getPages().size();
        }

        return count;
    }

    public static JasperReport loadJasperReport(
            final String base64String)
                    throws IOException,
                    JRException {
        final byte[] bytes = Base64.decodeBase64(base64String);
        // FileOutputStream output = new FileOutputStream(new
        // File("abc.jasper"));
        // IOUtils.write(bytes, output);
        try (final InputStream is = new ByteArrayInputStream(bytes)) {
            return (JasperReport) JRLoader.loadObject(is);
        }
    }

    public static void setPageNumbers(
            final List<JasperPrint> jpList,
            final String key,
            final String format) {
        final int totalPages = countPages(jpList);
        int pageCount = 0;
        for (final JasperPrint jasperPrint : jpList) {
            for (final JRPrintPage page : jasperPrint.getPages()) {
                pageCount++;
                for (final JRPrintElement element : page.getElements()) {
                    if (element instanceof JRTemplatePrintText) {
                        final JRTemplatePrintText printText = (JRTemplatePrintText) element;
                        if (printText.getKey().equalsIgnoreCase(key)) {
                            final String text = String.format(format, pageCount, totalPages);
                            printText.setText(text);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static void trim(
            final JasperPrint jasperPrint) {
        for (int i = jasperPrint.getPages().size() - 1; i > 0; i--) {
            if (jasperPrint.getPages().get(i).getElements().size() <= 3) {
                jasperPrint.getPages().remove(i);
            }
        }
    }

    private JasperUtils() {
    }

}
