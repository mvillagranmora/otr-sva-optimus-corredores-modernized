package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarArchivoClientesPorPagarYCobrarService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarArchivoClientesPorPagarYCobrarJob} (162 lines): receivables / payables file; a service error becomes a java.lang.Error.
 */
public final class GenerarArchivoClientesPorPagarYCobrarJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarArchivoClientesPorPagarYCobrarJob";

    private final GenerarArchivoClientesPorPagarYCobrarService generarArchivoClientesPorPagarYCobrarService;
    private final JdbcTemplate jdbc;

    public GenerarArchivoClientesPorPagarYCobrarJob(GenerarArchivoClientesPorPagarYCobrarService generarArchivoClientesPorPagarYCobrarService, JdbcTemplate jdbc) {
        this.generarArchivoClientesPorPagarYCobrarService = Objects.requireNonNull(generarArchivoClientesPorPagarYCobrarService, "generarArchivoClientesPorPagarYCobrarService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivoClientesPorPagarYCobrarJob.java:229-304 (logica, debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        var entrada = new GenerarArchivoClientesPorPagarYCobrarService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = ctx.getParam("IdModulo");
        entrada.FechaSistema = ctx.getParam("FechaSistema");
        entrada.EsBusqueda = ctx.getParam("EsBusqueda");
        entrada.IdMoneda = ctx.getParam("IdMoneda");
        entrada.TipoSaldo = ctx.getParam("TipoSaldo");
        try {
            var salida = Llamada.de(generarArchivoClientesPorPagarYCobrarService::execute, entrada,
                    GenerarArchivoClientesPorPagarYCobrarService.Salida.class);
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) { // :282
                Llamada.actualizar(jdbc, Llamada.SQL_RUTA_SALIDA, "IdPrograma", ctx.idPrograma(), "RutaArchivo", salida.P_OUT_RUTAARCHIVO);
                ctx.commit();
                return true;
            }
            // :292-296 CIJ-DEF-05: a java.lang.Error (not an Exception): the catch below does not roll back; the runner does
            StringBuilder mensajeError = new StringBuilder();
            for (var m : salida.P_OUT_MENSAJES) {
                mensajeError.append(m.tipo()).append(": ").append(m.descripcion()).append("  ");
            }
            throw new Error(mensajeError.toString());
        } catch (Exception e) { // :298-302
            ctx.rollback();
            throw e;
        }
    }
}
