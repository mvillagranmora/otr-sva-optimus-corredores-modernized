package cl.bcs.apv.api;

/**
 * A typed use case of the APV service: one request record in, one outcome record out.
 * Runtime exceptions propagate unchanged (the legacy behaviour "NPE as behavior" is part of the contract the goldens pin).
 *
 * @param <E> request record
 * @param <R> outcome record
 */
public interface CasoUso<E, R> {

    R ejecutar(E entrada);
}
