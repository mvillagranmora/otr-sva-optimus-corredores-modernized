package cl.bcs.apv.adapter.javacall;

import com.google.gson.JsonObject;

import cl.bcs.apv.api.CasoUso;

/**
 * Mapper of one operation: legacy JSON {@code <->} typed records. Rules (see BcsAPVApi/CONTRATO.md): key missing = legacy constructor default
 * ({@code porDefecto()} of the record); key present with null = null (primitives keep the default); unknown keys are ignored;
 * output follows the legacy Gson ({@link Sabor}).
 */
public interface MapeoOperacion<E, R> {

    E entradaDesdeJson(JsonObject json);

    JsonObject entradaAJson(E entrada);

    R resultadoDesdeJson(JsonObject json);

    JsonObject resultadoAJson(R resultado);

    /** legacy JSON in -> use case -> legacy JSON out. */
    @SuppressWarnings("unchecked")
    default JsonObject invocar(Object casoUso, JsonObject entrada) {
        E e = entradaDesdeJson(entrada);
        R r = ((CasoUso<E, R>) casoUso).ejecutar(e);
        return resultadoAJson(r);
    }
}
