package cl.bcs.contabilidad.jobs.job;

import cl.bcs.contabilidad.jobs.runtime.ContabilidadJob;
import cl.bcs.contabilidad.jobs.runtime.JobContext;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService.Entrada;
import cl.bcs.contabilidad.service.CentralizaMultiSeleccionService.TipoSeleccionado;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Legacy {@code cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob} (CONT-J-10..19).
 *
 * <p>Legacy: CentralizaMultiSeleccionJob.java:27-101, {@code logica(con, debug = false)}. Reads IdEmpresa, IdUsuario
 * and Ejercicio with {@code Long.parseLong}, IdModulo, FechaContable and {@code fechaConsulta} (lower-case f, :65)
 * as given; splits IdsTipoCentralizacion on ';' (:68, trailing empty tokens dropped, no trim); runs the
 * centralization with {@code p_SimulaOperacion = "N"} and IGNORES its output, error counter included, so the run
 * commits even when {@code P_OUT_ERRORESCONTADOR > 0} (:88-92, CONT-J-16, legacy defect kept). Any exception rolls
 * back and is rethrown (:95-98); the runner does both. The debug inputs of {@code logica(con, true)} are not ported.
 */
public final class CentralizaMultiSeleccionJob implements ContabilidadJob {

    public static final String CLASE = "cl.bcs.contabilidad.jobs.CentralizaMultiSeleccionJob";

    private static final Gson GSON = new Gson();

    private final CentralizaMultiSeleccionService servicio;

    public CentralizaMultiSeleccionJob(CentralizaMultiSeleccionService servicio) {
        this.servicio = Objects.requireNonNull(servicio, "servicio");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) {
        long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String idModulo = ctx.getParam("IdModulo");
        String fechaContable = ctx.getParam("FechaContable");
        long ejercicio = Long.parseLong(ctx.getParam("Ejercicio"));
        List<TipoSeleccionado> tipos = tipos(ctx.getParam("IdsTipoCentralizacion"));
        String fechaConsulta = ctx.getParam("fechaConsulta");

        Entrada entrada = new Entrada(idEmpresa, idUsuario, idModulo, fechaContable, fechaConsulta, ejercicio, "N",
                tipos);
        servicio.execute(GSON.toJsonTree(entrada).getAsJsonObject());
        return true;
    }

    /** CentralizaMultiSeleccionJob.java:68-78: a missing value fails like the legacy NPE, a bad id with NFE. */
    private static List<TipoSeleccionado> tipos(String ids) {
        Objects.requireNonNull(ids, "IdsTipoCentralizacion");
        return Arrays.stream(ids.split(";")).map(id -> new TipoSeleccionado(Long.parseLong(id))).toList();
    }
}
