package cl.bcs.utils;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.stream.Stream;

public final class StreamUtils {
	
	// En java 8 Stream::flatMap transforma todos los streams a secuenciales :c
	public static <T, S, U> Stream<U> 	parallelFlatmap(Collection<T> tcol, Collection<S> scol, BiFunction<T, S, U> merger) {
		return tcol.parallelStream()
				.map(t -> scol.parallelStream().map(s -> merger.apply(t, s)))
				.reduce(Stream::concat)
				.orElse(Stream.empty());
	}
}
