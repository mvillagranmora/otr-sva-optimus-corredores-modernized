package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarArchivoCargoAbonoService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarArchivoCargoAbonoJob} (183 lines): debit / credit movements file; updates BAT.Programa.
 */
public final class GenerarArchivoCargoAbonoJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarArchivoCargoAbonoJob";

    private final GenerarArchivoCargoAbonoService generarArchivoCargoAbonoService;
    private final JdbcTemplate jdbc;

    public GenerarArchivoCargoAbonoJob(GenerarArchivoCargoAbonoService generarArchivoCargoAbonoService, JdbcTemplate jdbc) {
        this.generarArchivoCargoAbonoService = Objects.requireNonNull(generarArchivoCargoAbonoService, "generarArchivoCargoAbonoService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivoCargoAbonoJob.java:48-139 (logica, debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String idModulo = ctx.getParam("IdModulo");
        Long idEjecutivo = Long.parseLong(ctx.getParam("IdEjecutivo"));
        var entrada = new GenerarArchivoCargoAbonoService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = idModulo;
        entrada.p_FechaDesde = ctx.getParam("FechaDesde");
        entrada.p_FechaHasta = ctx.getParam("FechaHasta");
        entrada.p_IdEjecutivo = idEjecutivo;
        entrada.p_IdMoneda = ctx.getParam("IdMoneda");
        entrada.p_IdTipoMovimiento = ctx.getParam("IdTipoMovimiento");
        entrada.p_IdConcepto = ctx.getParam("IdConcepto");
        entrada.p_IdEstado = ctx.getParam("IdEstado");
        try {
            var salida = Llamada.de(generarArchivoCargoAbonoService::execute, entrada, GenerarArchivoCargoAbonoService.Salida.class);
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) { // :110 (CIJ-DEF-12: a null counter is a NullPointerException)
                String comentario = salida.P_OUT_CANTIDADREGISTROS.compareTo(0L) == 0 ? "No existen movimientos para generar archivo" : "";
                Llamada.actualizar(jdbc, SQL_UPDATE_SALIDA_PROGRAMA_BATCH, "IdPrograma", ctx.idPrograma(),
                        "RutaArchivo", salida.P_OUT_RUTAARCHIVO, "Comentario", comentario);
                ctx.commit();
                return true;
            }
            return false; // :129-132 (CIJ-DEF-01)
        } catch (Exception e) { // :133-137
            ctx.rollback();
            throw e;
        }
    }

    private static final String SQL_UPDATE_SALIDA_PROGRAMA_BATCH = "UPDATE \"BSCL.Plataforma.BD::BAT.Programa\" SET \"RutaSalida\" = :RutaArchivo, "
            + "\"Comentario\" = :Comentario  WHERE \"IdPrograma\" = :IdPrograma ";
}
