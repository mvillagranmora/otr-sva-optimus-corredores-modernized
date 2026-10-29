package cl.bcs.reports.equiv;

import cl.bcs.reports.support.PdfProbe;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Canonical text facet of one rendered output (the unit compare.py judges): masked length, masked SHA-256 and, per
 * format, the extracted text (PDF Tj strings + page count, RTF/Word text without control words, TXT, XLS sheet shape and
 * string cells). Identical facets for legacy and sidecar are a consequence of identical masked bytes; the text is there
 * so that a human reading a "differs" sees WHAT differs.
 */
public final class Facet {

    private static final Pattern RTF_CTRL = Pattern.compile("\\\\[a-zA-Z]+-?\\d* ?|[{}]|\\\\\\*|\\\\\\\\");
    private static final Pattern RTF_HEX = Pattern.compile("\\\\'([0-9a-fA-F]{2})");

    private Facet() {
    }

    public static String of(String caseName, String format, byte[] raw, List<long[]> windows, List<LocalDate> days)
            throws Exception {
        byte[] masked = Masks.apply(format, raw, windows, days);
        StringBuilder sb = new StringBuilder();
        sb.append("case: ").append(caseName).append('\n');
        sb.append("format: ").append(format).append('\n');
        sb.append("masked-length: ").append(masked.length).append('\n');
        sb.append("masked-sha256: ").append(sha256(masked)).append('\n');
        switch (format) {
            case "pdf":
                sb.append("pages: ").append(PdfProbe.pageCount(raw)).append('\n');
                sb.append("text:\n").append(printable(Masks.maskText(PdfProbe.text(raw), days))).append('\n');
                break;
            case "excel":
                sb.append(xls(raw));
                break;
            case "word":
            case "rtf":
                sb.append("text:\n").append(rtfText(Masks.apply(format, raw, windows, days))).append('\n');
                break;
            default:
                sb.append("text:\n").append(new String(masked, StandardCharsets.UTF_8)).append('\n');
        }
        return sb.toString();
    }

    /** PDF Tj strings are font codes: keep the facet printable (non-ASCII bytes as \xNN). */
    static String printable(String s) {
        StringBuilder sb = new StringBuilder();
        for (char ch : s.toCharArray()) {
            if (ch >= 32 && ch < 127) {
                sb.append(ch);
            } else {
                sb.append(String.format("\\x%02x", (int) ch & 0xFF));
            }
        }
        return sb.toString();
    }

    static String rtfText(byte[] masked) {
        String s = new String(masked, StandardCharsets.ISO_8859_1);
        s = RTF_HEX.matcher(s).replaceAll(m -> String.valueOf((char) Integer.parseInt(m.group(1), 16)));
        s = RTF_CTRL.matcher(s).replaceAll(" ");
        return s.replaceAll("[ \\t]+", " ").replaceAll("(\\r?\\n)+", "\n").trim();
    }

    private static String xls(byte[] raw) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (HSSFWorkbook wb = new HSSFWorkbook(new ByteArrayInputStream(raw))) {
            sb.append("sheets: ").append(wb.getNumberOfSheets()).append('\n');
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                Sheet sh = wb.getSheetAt(i);
                int cells = 0;
                StringBuilder txt = new StringBuilder();
                for (Row r : sh) {
                    for (Cell c : r) {
                        cells++;
                        if (c.getCellType() == Cell.CELL_TYPE_STRING) {
                            txt.append(c.getStringCellValue()).append(" | ");
                        }
                    }
                }
                sb.append("sheet ").append(i).append(": rows=").append(sh.getPhysicalNumberOfRows())
                        .append(" cells=").append(cells).append('\n').append("strings: ").append(txt).append('\n');
            }
        }
        return sb.toString();
    }

    public static String sha256(byte[] b) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (byte x : MessageDigest.getInstance("SHA-256").digest(b)) {
            sb.append(String.format("%02x", x));
        }
        return sb.toString();
    }
}
