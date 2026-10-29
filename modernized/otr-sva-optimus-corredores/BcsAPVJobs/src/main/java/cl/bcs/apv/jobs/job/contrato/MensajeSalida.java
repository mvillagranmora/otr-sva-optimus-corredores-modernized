package cl.bcs.apv.jobs.job.contrato;

import cl.bcs.apv.jobs.job.JsonLegacy;

/**
 * Legacy platform message {@code MensajeSalida} as it travels in the collaborator JSON (Plataforma utils, :598-612; the constants are the ones the jobs compare
 * {@code tipo} with).
 */
public class MensajeSalida {
    public Long id;
    public String tipo; // ERROR,AUTORIZACION,ADVERTENCIA,INFORMACION
    public String codigo;
    public String descripcion;

    public static final String MENSAJE_OK = "OK";
    public static final String MENSAJE_ERROR = "ERROR";
    public static final String MENSAJE_ADVERTENCIA = "ADVERTENCIA";
    public static final String MENSAJE_AUTORIZACION = "AUTORIZACION";
    public static final String MENSAJE_INFORMACION = "INFORMACION";

    @Override
    public String toString() {
        return JsonLegacy.texto(this);
    }
}
