package cl.bcs.reports.equiv;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * Object-level normalisation of a JRC PDF, so that two renders compare equal when (and only when) they differ just in the
 * clock-driven or file-name-driven parts. Finding (record.sh, 2 passes a minute apart): the PDF embeds a SUBSET of the
 * TrueType fonts, and the print time (h:mmAM) changes which glyphs are used, so the font program, its /Widths array and
 * its ToUnicode CMap change with the minute, which shifts every /Length and every xref offset after it. Masks:
 * <ol>
 *   <li>/Title (&lt;uuid&gt;.rpt);</li>
 *   <li>(dd-MM-yyyy)Tj and (h:mmAM)Tj inside the inflated content streams;</li>
 *   <li>font program streams (/Length1), ToUnicode CMaps, standalone numeric arrays (the /Widths), every /Length value;</li>
 *   <li>everything outside the "N 0 obj .. endobj" bodies (xref table, trailer, startxref offsets).</li>
 * </ol>
 * Everything else (object order, dictionaries incl. /BaseFont names, inflated page content incl. positions and text,
 * images) is compared exactly.
 */
public final class PdfStructure {

    private static final Pattern OBJ = Pattern.compile("(?m)^(\\d+) 0 obj");
    private static final Pattern TITLE = Pattern.compile("/Title \\([0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.rpt\\)");
    private static final Pattern LEN = Pattern.compile("/Length(\\d?) \\d+");
    private static final Pattern PRINT_DATE = Pattern.compile("\\(\\d{2}-\\d{2}-\\d{4}\\)Tj");
    private static final Pattern PRINT_TIME = Pattern.compile("\\(\\s*\\d{1,2}:\\d{2}\\s*[AP]M\\)Tj");
    private static final Pattern NUMARRAY = Pattern.compile("\\s*\\[[-\\d.\\s]{20,}\\]\\s*");

    private PdfStructure() {
    }

    public static String normalize(byte[] pdf) {
        String raw = new String(pdf, StandardCharsets.ISO_8859_1);
        StringBuilder out = new StringBuilder();
        Matcher m = OBJ.matcher(raw);
        while (m.find()) {
            int end = raw.indexOf("endobj", m.end());
            String body = raw.substring(m.end(), end < 0 ? raw.length() : end);
            out.append("obj ").append(m.group(1)).append('\n').append(object(body)).append('\n');
        }
        return out.toString();
    }

    private static String object(String body) {
        int s = body.indexOf("stream");
        if (s < 0) {
            return NUMARRAY.matcher(body).matches() ? "<NUMARRAY>" : TITLE.matcher(body).replaceAll("/Title (<MASK>.rpt)");
        }
        String head = LEN.matcher(TITLE.matcher(body.substring(0, s)).replaceAll("/Title (<MASK>.rpt)")).replaceAll("/Length$1 <N>");
        int start = s + 6;
        if (body.startsWith("\r\n", start)) {
            start += 2;
        } else if (body.startsWith("\n", start)) {
            start += 1;
        }
        int e = body.lastIndexOf("endstream");
        String data = body.substring(start, e < 0 ? body.length() : e);
        if (head.contains("/Length1")) {
            return head + "<FONTPROGRAM>";
        }
        String text = inflate(data.getBytes(StandardCharsets.ISO_8859_1));
        if (text.contains("begincmap")) {
            return head + "<TOUNICODE>";
        }
        text = PRINT_DATE.matcher(text).replaceAll("(<DATE>)Tj");
        return head + PRINT_TIME.matcher(text).replaceAll("(<TIME>)Tj");
    }

    private static String inflate(byte[] data) {
        Inflater inf = new Inflater();
        inf.setInput(data);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        try {
            while (!inf.finished()) {
                int n = inf.inflate(buf);
                if (n == 0 && (inf.needsInput() || inf.needsDictionary())) {
                    break;
                }
                bos.write(buf, 0, n);
            }
        } catch (Exception ex) {
            return new String(data, StandardCharsets.ISO_8859_1);   // not Flate: keep raw
        } finally {
            inf.end();
        }
        return new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);
    }
}
