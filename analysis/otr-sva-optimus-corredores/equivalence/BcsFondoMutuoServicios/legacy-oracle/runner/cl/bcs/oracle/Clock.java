package cl.bcs.oracle;

/** Pinned clock for the oracle: the generated copy of FechaDB uses it instead of System.currentTimeMillis() (that class caches the DB clock and adds the REAL elapsed milliseconds, which is not reproducible). */
public final class Clock {
    private Clock() { }
    public static long millis() { return 1_000_000_000_000L; }
}
