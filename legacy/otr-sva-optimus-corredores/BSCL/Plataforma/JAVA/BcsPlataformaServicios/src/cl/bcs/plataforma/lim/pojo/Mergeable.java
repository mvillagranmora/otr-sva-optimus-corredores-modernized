package cl.bcs.plataforma.lim.pojo;

public interface Mergeable<T> {
	T merge(T rhs);
}
