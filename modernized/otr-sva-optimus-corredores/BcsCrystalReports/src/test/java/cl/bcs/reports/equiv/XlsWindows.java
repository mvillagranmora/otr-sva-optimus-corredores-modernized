package cl.bcs.reports.equiv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Locates the print date / print time cells of a BIFF (.xls) export so they can be masked (8-byte doubles of NUMBER
 * records, id 0x0203, length 14: row, col, xf, value). Used by the recorder (from two legacy passes a minute apart plus
 * a search for the serial of the recording day) and by the test (which only checks that each recorded window still holds
 * a temporal value: a time of day in [0,1) or a date serial).
 */
public final class XlsWindows {

    private static final byte[] NUMBER_HEAD = {0x03, 0x02, 0x0E, 0x00};

    private XlsWindows() {
    }

    public static double serial(LocalDate d) {
        return ChronoUnit.DAYS.between(LocalDate.of(1899, 12, 30), d);
    }

    public static double doubleAt(byte[] b, long off) {
        return ByteBuffer.wrap(b, (int) off, 8).order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    public static boolean temporal(double v) {
        return (v >= 0 && v < 1) || (v >= 40000 && v < 80000);
    }

    /** Windows = value bytes of the NUMBER records that differ between a and b, or that hold the serial of one of days. */
    public static List<long[]> find(byte[] a, byte[] b, List<LocalDate> days) {
        List<long[]> out = new ArrayList<>();
        if (a.length != b.length) {
            throw new IllegalStateException("xls lengths differ between passes: " + a.length + " vs " + b.length);
        }
        for (int i = 0; i + 18 <= a.length; i++) {
            if (!isNumberHead(a, i)) {
                continue;
            }
            int v = i + 10;
            boolean differs = false;
            for (int k = 0; k < 8; k++) {
                differs |= a[v + k] != b[v + k];
            }
            double d = doubleAt(a, v);
            boolean isDay = false;
            for (LocalDate day : days) {
                isDay |= d == serial(day);
            }
            if (differs || isDay) {
                out.add(new long[] {v, 8});
            }
        }
        return out;
    }

    private static boolean isNumberHead(byte[] a, int i) {
        for (int k = 0; k < 4; k++) {
            if (a[i + k] != NUMBER_HEAD[k]) {
                return false;
            }
        }
        return true;
    }

    /** Bytes that differ between a and b outside the windows (empty = equal after masking). */
    public static List<Integer> unexplained(byte[] a, byte[] b, List<long[]> windows) {
        List<Integer> bad = new ArrayList<>();
        if (a.length != b.length) {
            bad.add(-1);
            return bad;
        }
        for (int i = 0; i < a.length && bad.size() < 20; i++) {
            if (a[i] != b[i] && !inside(windows, i)) {
                bad.add(i);
            }
        }
        return bad;
    }

    private static boolean inside(List<long[]> windows, int i) {
        for (long[] w : windows) {
            if (i >= w[0] && i < w[0] + w[1]) {
                return true;
            }
        }
        return false;
    }
}
