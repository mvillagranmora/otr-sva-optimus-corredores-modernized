package cl.bcs.reports.equiv;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The documented masks (CRYSTAL_SPIKE.md section 4, README of this package). They hide ONLY what the engine takes from
 * the clock or from a temp file name:
 * <ul>
 *   <li>PDF: /Title (&lt;uuid&gt;.rpt); inside the inflated streams the print date (dd-MM-yyyy)Tj and the print time (h:mmAM)Tj;</li>
 *   <li>Excel: the 8-byte doubles of the print date/time cells, at the offsets recorded with the golden (XlsWindows);</li>
 *   <li>RTF/Word: \creatim, \revtim, \printim, \buptim groups;</li>
 *   <li>every text format: a time like 12:32PM and the LITERAL dates of the recording day and of today (several
 *       formats). The fake data never contains those days (checked by the test), so no data can be hidden by them.</li>
 * </ul>
 */
public final class Masks {

    private static final Pattern RTF_TIME = Pattern.compile("\\\\(creatim|revtim|printim|buptim)[^}]*");
    private static final Pattern TIME = Pattern.compile("\\d{1,2}\\\\?:\\d{2}\\s?[AaPp][Mm]");   // 12:32PM and POI's 12\:32 PM
    private static final String[] DATE_PATTERNS = {"dd-MM-yyyy", "dd/MM/yyyy", "yyyy-MM-dd", "MM/dd/yyyy", "d/M/yyyy",
        "M/d/yyyy", "dd.MM.yyyy", "d-M-yyyy", "dd/MM/yy", "yyyyMMdd", "MMMM d, yyyy", "MMM d, yyyy", "d MMMM yyyy",
        "EEEE, MMMM d, yyyy", "dd 'de' MMMM 'de' yyyy", "d 'de' MMMM 'de' yyyy", "EEEE d 'de' MMMM 'de' yyyy"};

    private Masks() {
    }

    public static Set<String> dateLiterals(Collection<LocalDate> days) {
        Set<String> out = new LinkedHashSet<>();
        for (LocalDate d : days) {
            for (String p : DATE_PATTERNS) {
                out.add(d.format(DateTimeFormatter.ofPattern(p, Locale.ENGLISH)));
                out.add(d.format(DateTimeFormatter.ofPattern(p, new Locale("es", "CL"))));
            }
        }
        return out;
    }

    public static String maskText(String s, Collection<LocalDate> days) {
        List<String> lits = new ArrayList<>(dateLiterals(days));
        lits.sort((x, y) -> y.length() - x.length());   // longest first (dd/MM/yyyy before dd/MM/yy)
        for (String l : lits) {
            s = s.replace(l, "<DATE>");
        }
        return TIME.matcher(s).replaceAll("<TIME>");
    }

    /** @param windows xls only: {offset, length} pairs (print date/time doubles) zeroed in the copy. */
    public static byte[] apply(String format, byte[] data, List<long[]> windows, Collection<LocalDate> days) {
        switch (format) {
            case "pdf":
                return maskText(PdfStructure.normalize(data), days).getBytes(StandardCharsets.ISO_8859_1);
            case "excel":
                byte[] c = data.clone();
                for (long[] w : windows) {
                    for (int i = 0; i < w[1]; i++) {
                        c[(int) (w[0] + i)] = 0;
                    }
                }
                return c;
            case "word":
            case "rtf":
                String r = new String(data, StandardCharsets.ISO_8859_1);
                r = RTF_TIME.matcher(r).replaceAll("\\\\$1<MASK>");
                return maskText(r, days).getBytes(StandardCharsets.ISO_8859_1);
            case "txt":
                return maskText(new String(data, StandardCharsets.UTF_8), days).getBytes(StandardCharsets.UTF_8);
            default:
                throw new IllegalArgumentException("format " + format);
        }
    }
}
