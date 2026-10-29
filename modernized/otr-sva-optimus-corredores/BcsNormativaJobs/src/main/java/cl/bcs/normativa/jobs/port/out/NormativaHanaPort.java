package cl.bcs.normativa.jobs.port.out;

import cl.bcs.normativa.jobs.domain.ParametrosJob;
import cl.bcs.normativa.jobs.domain.ResultadoJob;

/**
 * Port for invoking HANA stored procedures specific to the Normativa domain.
 *
 * All methods in this port require a live SAP HANA connection.
 * Integration tests must use @Disabled("requires HANA").
 */
public interface NormativaHanaPort {

    /**
     * Invokes NOR.ProcesarTarea stored procedure.
     * Legacy: "BSCL.Normativa.BD.Servicios::NOR.ProcesarTarea"
     */
    ResultadoJob procesarTarea(Long idEmpresa, String idTipoTarea, Long idSolicitudCircular, Long jobId);

    /**
     * Invokes the generación process for a given document.
     * Legacy: ProcesoGeneracion.procesar()
     */
    ResultadoJob ejecutarGeneracion(ParametrosJob parametros);

    /**
     * Invokes the validación process for a given document.
     * Legacy: ProcesoValidacion.procesar()
     */
    ResultadoJob ejecutarValidacion(ParametrosJob parametros);

    /**
     * Invokes the carga process.
     * Legacy: ProcesoCarga.procesar()
     */
    ResultadoJob ejecutarCarga(ParametrosJob parametros);

    /**
     * Invokes the cuadratura process.
     * Legacy: ProcesoCuadratura.procesar()
     */
    ResultadoJob ejecutarCuadratura(ParametrosJob parametros);

    /**
     * Invokes the exportación process.
     * Legacy: ProcesoExportacion.procesar()
     */
    ResultadoJob ejecutarExportacion(ParametrosJob parametros);

    /**
     * Invokes the exportación NCG380/serv process.
     * Legacy: ProcesoExportacionServ.procesar()
     */
    ResultadoJob ejecutarExportacionServ(ParametrosJob parametros);
}
