package cl.bcs.reports.support;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.Inflater;

/**
 * Dependency-free PDF inspection for the render tests, including the documented masks for the only non-deterministic
 * parts of the JRC output (see CRYSTAL_SPIKE.md section 4):
 * <ol>
 *   <li>/Title (&lt;uuid&gt;.rpt) in the Info dictionary (UUID of the temp file);</li>
 *   <li>the report's PrintDate / PrintTime special fields, plain text inside the Flate content streams:
 *       (dd-MM-yyyy)Tj and (h:mmAM)Tj.</li>
 * </ol>
 */
public final class PdfProbe {

    private static final Pattern TITLE = Pattern.compile(
            "/Title \\([0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}\\.rpt\\)");
    private static final Pattern PRINT_DATE = Pattern.compile("\\(\\d{2}-\\d{2}-\\d{4}\\)Tj");
    private static final Pattern PRINT_TIME = Pattern.compile("\\(\\d{1,2}:\\d{2}[AP]M\\)Tj");
    private static final Pattern STREAM = Pattern.compile("stream\r?\n(.*?)endstream", Pattern.DOTALL);
    private static final Pattern PAGE = Pattern.compile("/Type\\s*/Page(?![A-Za-z])");
    private static final Pattern TJ = Pattern.compile("\\(([^)\\\\]*+(?:\\\\.[^)\\\\]*+)*+)\\)\\s*Tj");

    private PdfProbe() {
    }

    public static String latin1(byte[] pdf) {
        return new String(pdf, StandardCharsets.ISO_8859_1);
    }

    public static int pageCount(byte[] pdf) {
        Matcher m = PAGE.matcher(latin1(pdf));
        int n = 0;
        while (m.find()) {
            n++;
        }
        return n;
    }

    /** All inflated content streams concatenated. */
    public static String inflatedStreams(byte[] pdf) {
        return mapStreams(latin1(pdf), false);
    }

    /** Replaces every stream body (manual scan: a lazy DOTALL regex overflows the stack on big PDFs). */
    private static String mapStreams(String raw, boolean keepOutside) {
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        while (true) {
            int s = raw.indexOf("stream", pos);
            if (s < 0) {
                break;
            }
            if (s >= 3 && raw.startsWith("end", s - 3)) { // "endstream" token, not a start
                if (keepOutside) {
                    sb.append(raw, pos, s + 6);
                }
                pos = s + 6;
                continue;
            }
            int bodyStart = s + 6;
            if (raw.startsWith("\r\n", bodyStart)) {
                bodyStart += 2;
            } else if (raw.startsWith("\n", bodyStart)) {
                bodyStart += 1;
            }
            int e = raw.indexOf("endstream", bodyStart);
            if (e < 0) {
                break;
            }
            if (keepOutside) {
                sb.append(raw, pos, bodyStart);
            }
            String body = inflate(raw.substring(bodyStart, e).getBytes(StandardCharsets.ISO_8859_1));
            if (keepOutside) {
                body = PRINT_DATE.matcher(body).replaceAll("(<DATE>)Tj");
                body = PRINT_TIME.matcher(body).replaceAll("(<TIME>)Tj");
            }
            sb.append(body);
            pos = e;
        }
        if (keepOutside) {
            sb.append(raw, pos, raw.length());
        }
        return sb.toString();
    }

    /** Text of the (..)Tj operators joined with a single space. */
    public static String text(byte[] pdf) {
        StringBuilder sb = new StringBuilder();
        Matcher m = TJ.matcher(inflatedStreams(pdf));
        while (m.find()) {
            sb.append(m.group(1)).append(' ');
        }
        return sb.toString();
    }

    /** Raw text with the Title masked and every stream replaced by its inflated, date/time-masked content. */
    public static String normalized(byte[] pdf) {
        return mapStreams(TITLE.matcher(latin1(pdf)).replaceAll("/Title (<MASKED>.rpt)"), true);
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
        } catch (Exception e) {
            return latin1(data); // not Flate (image/raw): keep as is
        } finally {
            inf.end();
        }
        return new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);
    }
}
