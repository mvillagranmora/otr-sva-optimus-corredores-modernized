package cl.bcs.plataforma.lim.pojo;

import java.util.Objects;

@FunctionalInterface
public interface ChainableMapper<T, R extends Mergeable<R>> {
	R apply(T o);
	
	default ChainableMapper<T, R> andThen(ChainableMapper<T, R> nextChainableMapper) {
		return (T o) -> {
			R partial = apply(o);
			R nextPartial = nextChainableMapper.apply(o);
			return Objects.nonNull(partial) ? partial.merge(nextPartial) : nextPartial;
		};
	}
}
