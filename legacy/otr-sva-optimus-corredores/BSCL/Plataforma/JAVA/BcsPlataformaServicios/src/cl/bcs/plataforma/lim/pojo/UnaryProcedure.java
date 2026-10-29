package cl.bcs.plataforma.lim.pojo;

@FunctionalInterface
public interface UnaryProcedure<T> {
	void apply(T t);
}
