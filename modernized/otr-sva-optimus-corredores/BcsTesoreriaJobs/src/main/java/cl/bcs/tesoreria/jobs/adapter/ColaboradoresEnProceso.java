package cl.bcs.tesoreria.jobs.adapter;

import cl.bcs.apv.adapter.javacall.ValidarMandatosPACLogicaMapeo;
import cl.bcs.tesoreria.jobs.job.port.ServiciosExternos;
import cl.bcs.tesoreria.port.out.Colaboradores;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.Objects;

/**
 * Step 5 (JOBS_PATTERN.md H-1, JOBS_H1_H4_INVENTORY.md section A group M): the {@link Colaboradores} port of the job application when
 * {@code tesoreria.jobs.colaboradores-en-proceso=true}.
 *
 * <p>The collaborators of group M, the ones whose owner is ALREADY migrated and packaged as a library that this application carries, run IN
 * PROCESS through the entry point of their owner ({@link DespachadorEnProceso}). They therefore join the transaction the {@code JobRunner}
 * opened for the run (same {@code DataSource}, same {@code PlatformTransactionManager}; the services are {@code @Transactional} REQUIRED and the APV
 * use cases take the connection of the caller): a rollback of the job undoes what they wrote, as the legacy did when it handed them its own
 * {@code java.sql.Connection} ({@code logica(con, ...)}).
 *
 * <p>Everything else (the group TW/TL collaborators that still live in TomEE: ValidaEjecucionProcesoRecPAC, ConsultarEstadoModulo,
 * ObtenerDiaAnterior, AgregarLog, CrearOrdenes, ...) and the Tesoreria services that {@code ColaboradoresJavaCall} already runs in process
 * is delegated, untouched, to the existing {@link ColaboradoresJobs} (HTTP to {@code {JAVACALL_URL}/internal/java-call}; that endpoint is step 5b).
 *
 * <p>Collaborators of group M that THIS module reaches over HTTP today (verified in the code):
 * <ul>
 *   <li>{@code ValidarMandatosPAC} (APV): the job BcsJobProcesarDistribucion;</li>
 *   <li>{@code CrearCargoAbono} (CuentaInversion): MovimientosDistribucion of ProcesarDistribucion, through {@code ColaboradoresJavaCall};</li>
 *   <li>{@code AnularCargoAbono} (CuentaInversion) and {@code CambiarEstadoContratosSAAPV} (APV): the Tesoreria services AnularNominasTransferencias
 *       and AnulacionMandatoPAC, which share this port.</li>
 * </ul>
 */
public class ColaboradoresEnProceso implements Colaboradores {

    /**
     * Collaborator simple name -> in-process operation, for the group M names that this module sends over HTTP without this class. For
     * {@code ValidarMandatosPAC} the operation is NOT the JavaCall class ({@code cl.bcs.APV.ValidarMandatosPAC}, whose {@code call()} answers null and writes
     * nothing, which is all the HTTP path can reach) but the typed legacy {@code logica(con, salida, idEmpresa, idUsuario)} that the job called
     * ({@link ValidarMandatosPACLogicaMapeo#OPERACION}): it updates {@code LogMandatoPAC} in the transaction of the job.
     */
    public static final Map<String, String> GRUPO_M = Map.of(
            ServiciosExternos.VALIDAR_MANDATOS_PAC, ValidarMandatosPACLogicaMapeo.OPERACION,
            "CambiarEstadoContratosSAAPV", "cl.bcs.APV.CambiarEstadoContratosSAAPV",
            "CrearCargoAbono", "cl.bcs.cuentaInversion.CrearCargoAbono",
            "AnularCargoAbono", "cl.bcs.cuentaInversion.AnularCargoAbono");

    private final Colaboradores http;
    private final DespachadorEnProceso despachador;

    /**
     * @param http        the existing HTTP port ({@link ColaboradoresJobs}): everything that is not a group M name goes to it
     * @param despachador the in-process entry points of the owner libraries
     */
    public ColaboradoresEnProceso(Colaboradores http, DespachadorEnProceso despachador) {
        this.http = Objects.requireNonNull(http, "http");
        this.despachador = Objects.requireNonNull(despachador, "despachador");
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        String clase = servicio == null ? null : GRUPO_M.get(servicio);
        if (clase != null && despachador.maneja(clase)) {
            return despachador.despachar(clase, entrada);
        }
        return http.invocar(servicio, entrada);
    }
}
