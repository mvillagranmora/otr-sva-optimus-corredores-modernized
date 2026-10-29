package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.service.GenerarInformeClienteRelacionadoService;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Legacy {@code cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch} (CONT-J-80..89).
 *
 * <p>Legacy: GenerarInformeClienteRelacionadoBatch.java:27-64, {@code logica(conn, debug = false)}. Every parameter is
 * optional with a default (IdEmpresa 1, IdUsuario 97, IdModulo "CON", FechaInicio "2025-03-20", FechaFin
 * "2025-03-30", :37-48); an empty value is present, so it is not defaulted (CONT-J-82); the numeric ones go through
 * {@code Long.valueOf}. {@code P_OUT_ERRORESCONTADOR > 0} throws an error WITHOUT a message (legacy
 * {@code throw new Throwable()}, :55-58, CONT-J-84, kept), so the status message is null; a missing counter fails
 * like the legacy unboxing NPE (CONT-J-87). Otherwise RutaSalida = {@code RutaArchivo} (:61, null when absent).
 */
public final class GenerarInformeClienteRelacionadoBatch implements ContabilidadJob {

    public static final String CLASE = "cl.bcs.contabilidad.jobs.GenerarInformeClienteRelacionadoBatch";

    private final GenerarInformeClienteRelacionadoService servicio;

    public GenerarInformeClienteRelacionadoBatch(GenerarInformeClienteRelacionadoService servicio) {
        this.servicio = Objects.requireNonNull(servicio, "servicio");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws InformeConErroresException {
        JsonObject entrada = new JsonObject();
        entrada.addProperty("p_IdEmpresa", ctx.getOptionalParam("IdEmpresa").map(Long::valueOf).orElse(1L));
        entrada.addProperty("p_IdUsuario", ctx.getOptionalParam("IdUsuario").map(Long::valueOf).orElse(97L));
        entrada.addProperty("p_IdModulo", ctx.getOptionalParam("IdModulo").orElse("CON"));
        entrada.addProperty("p_FechaInicio", ctx.getOptionalParam("FechaInicio").orElse("2025-03-20"));
        entrada.addProperty("p_FechaFin", ctx.getOptionalParam("FechaFin").orElse("2025-03-30"));

        JsonObject salida = servicio.execute(entrada);
        JsonElement errores = salida.get("P_OUT_ERRORESCONTADOR");
        if (errores == null || errores.isJsonNull()) {
            throw new IllegalStateException("GenerarInformeClienteRelacionado returned no P_OUT_ERRORESCONTADOR");
        }
        if (errores.getAsLong() > 0) {
            throw new InformeConErroresException();
        }
        JsonElement ruta = salida.get("RutaArchivo");
        ctx.updateRutaSalida(ruta == null || ruta.isJsonNull() ? null : ruta.getAsString(), ctx.idPrograma());
        return true;
    }

    /** The report reported errors. Deliberately without a message: the legacy job threw a bare Throwable. */
    public static final class InformeConErroresException extends Exception {
        public InformeConErroresException() {
            super((String) null);
        }
    }
}
