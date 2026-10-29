package cl.bcs.reports.equiv;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The masks must hide the clock and nothing else: a changed data byte has to survive them. No Crystal needed. */
class MasksSelfTest {

    private static final List<LocalDate> DAYS = Collections.singletonList(LocalDate.of(2026, 10, 21));

    private static byte[] pdf(String uuid, String stream, String fontBytes) throws Exception {
        Deflater d = new Deflater();
        d.setInput(stream.getBytes(StandardCharsets.ISO_8859_1));
        d.finish();
        byte[] buf = new byte[4096];
        int n = d.deflate(buf);
        String body = "%PDF-1.7\n1 0 obj\n<< /Title (" + uuid + ".rpt) >>\nendobj\n2 0 obj\n<< /Length " + n
                + " >>\nstream\n" + new String(buf, 0, n, StandardCharsets.ISO_8859_1) + "\nendstream\nendobj\n"
                + "3 0 obj\n<< /Length1 " + fontBytes.length() + " /Length " + fontBytes.length() + " >>\nstream\n"
                + fontBytes + "\nendstream\nendobj\nxref\n0 1\n" + stream.length() + "\n%%EOF\n";
        return body.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static final String U1 = "0a1b2c3d-1111-2222-3333-444455556666";
    private static final String U2 = "9f8e7d6c-aaaa-bbbb-cccc-ddddeeeeffff";

    @Test
    void pdfClockUuidAndFontSubsetAreMaskedButDataIsNot() throws Exception {
        byte[] a = Masks.apply("pdf", pdf(U1, "(21-10-2026)Tj ( 1:47PM)Tj (JUAN PEREZ)Tj", "FONT-A"), null, DAYS);
        byte[] b = Masks.apply("pdf", pdf(U2, "(22-10-2026)Tj ( 1:48PM)Tj (JUAN PEREZ)Tj", "FONT-BB-subset"), null, DAYS);
        assertArrayEquals(a, b);
        byte[] c = Masks.apply("pdf", pdf(U1, "(21-10-2026)Tj ( 1:47PM)Tj (JUAN PEREZ)Tj", "FONT-A"), null, DAYS);
        byte[] d = Masks.apply("pdf", pdf(U1, "(21-10-2026)Tj ( 1:47PM)Tj (JUAN PEREX)Tj", "FONT-A"), null, DAYS);
        assertFalse(Arrays.equals(c, d), "a changed data character must not be masked");
    }

    @Test
    void rtfAndTxtMasksOnlyHideClock() {
        byte[] a = "{\\rtf1{\\creatim\\yr2026\\mo10\\dy21\\hr12\\min33}Monto 100 21-10-2026 1:47 PM}".getBytes(StandardCharsets.ISO_8859_1);
        byte[] b = "{\\rtf1{\\creatim\\yr2026\\mo10\\dy21\\hr12\\min34}Monto 100 21-10-2026 1:48 PM}".getBytes(StandardCharsets.ISO_8859_1);
        byte[] c = "{\\rtf1{\\creatim\\yr2026\\mo10\\dy21\\hr12\\min34}Monto 101 21-10-2026 1:48 PM}".getBytes(StandardCharsets.ISO_8859_1);
        assertArrayEquals(Masks.apply("rtf", a, null, DAYS), Masks.apply("rtf", b, null, DAYS));
        assertFalse(Arrays.equals(Masks.apply("rtf", a, null, DAYS), Masks.apply("rtf", c, null, DAYS)));
        byte[] t1 = "Fecha 21-10-2026 Hora 1\\:47 PM Monto 5".getBytes(StandardCharsets.UTF_8);
        byte[] t2 = "Fecha 21-10-2026 Hora 1\\:48 PM Monto 6".getBytes(StandardCharsets.UTF_8);
        assertFalse(Arrays.equals(Masks.apply("txt", t1, null, DAYS), Masks.apply("txt", t2, null, DAYS)));
    }

    @Test
    void xlsWindowsMaskOnlyTheRecordedDoubles() {
        byte[] x = new byte[40];
        byte[] y = new byte[40];
        y[12] = 7;
        List<long[]> w = Collections.singletonList(new long[] {10, 8});
        assertArrayEquals(Masks.apply("excel", x, w, DAYS), Masks.apply("excel", y, w, DAYS));
        y[30] = 1;
        assertFalse(Arrays.equals(Masks.apply("excel", x, w, DAYS), Masks.apply("excel", y, w, DAYS)));
        assertTrue(XlsWindows.temporal(0.5) && XlsWindows.temporal(46316.5) && !XlsWindows.temporal(1500000.5));
    }
}
