package cl.bcs.utils;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * UTIL-17: stream helpers (legacy {@code StreamUtils.java:10-15}).
 */
public final class StreamUtils {

    private StreamUtils() {
    }

    /**
     * Cartesian product of {@code tcol} x {@code scol} merged with {@code merger}, as a parallel stream whose encounter
     * order is t-major ({@code (t1,s1), (t1,s2), ..., (t2,s1), ...}).
     */
    public static <T, S, U> Stream<U> parallelFlatmap(Collection<T> tcol, Collection<S> scol, BiFunction<T, S, U> merger) {
        return tcol.parallelStream()
                .map(t -> scol.parallelStream().map(s -> merger.apply(t, s)))
                .reduce(Stream::concat)
                .orElseGet(Stream::empty);
    }
}
