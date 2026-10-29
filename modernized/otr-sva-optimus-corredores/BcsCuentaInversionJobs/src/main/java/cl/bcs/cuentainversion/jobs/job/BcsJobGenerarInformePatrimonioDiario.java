package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.cuentainversion.jobs.job.port.ConfiguracionJobs;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarInformePatrimonioDiarioService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.BcsJobGenerarInformePatrimonioDiario} (91 lines): daily equity report; updates BAT.Programa with the file names.
 */
public final class BcsJobGenerarInformePatrimonioDiario implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.BcsJobGenerarInformePatrimonioDiario";

    private final GenerarInformePatrimonioDiarioService generarInformePatrimonioDiarioService;
    private final ConfiguracionJobs configuracion;
    private final JdbcTemplate jdbc;

    public BcsJobGenerarInformePatrimonioDiario(GenerarInformePatrimonioDiarioService generarInformePatrimonioDiarioService, ConfiguracionJobs configuracion, JdbcTemplate jdbc) {
        this.generarInformePatrimonioDiarioService = Objects.requireNonNull(generarInformePatrimonioDiarioService, "generarInformePatrimonioDiarioService");
        this.configuracion = Objects.requireNonNull(configuracion, "configuracion");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // BcsJobGenerarInformePatrimonioDiario.java:45-79. The parameters are read OUTSIDE the try, in this order (CIJ-DEF-06).
        var entrada = new GenerarInformePatrimonioDiarioService.Entrada();
        entrada.p_IdEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        // :51 Utils.getConfiguracion(.., "PLA.RutaBaseArchivos") = module PLATAFORMA
        entrada.p_Ruta = Llamada.parametro(configuracion, entrada.p_IdEmpresa, "PLATAFORMA", "PLA.RutaBaseArchivos");
        entrada.p_IdModulo = "CUS";
        entrada.p_IdUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        entrada.p_IdCliente = Long.parseLong(ctx.getParam("IdCliente"));
        entrada.p_IdPortafolio = Long.parseLong(ctx.getParam("IdPortafolio"));
        entrada.p_IdEstado = ctx.getParam("IdEstado");
        entrada.p_IdProductoAgrupador = Long.parseLong(ctx.getParam("IdProductoAgrupador"));
        entrada.p_FechaDesde = ctx.getParam("FechaDesde").substring(0, 10);
        entrada.p_FechaHasta = ctx.getParam("FechaHasta").substring(0, 10);
        entrada.p_Periodo = Long.parseLong(ctx.getParam("Periodo"));
        try {
            var out = Llamada.de(generarInformePatrimonioDiarioService::execute, entrada, GenerarInformePatrimonioDiarioService.Salida.class);
            if (out.p_out_NombreArchivo != null) { // :65-70 (CIJ-DEF-03: the error counter is not looked at)
                Llamada.actualizarPrograma(jdbc, out.p_out_NombreArchivo, out.p_out_NombreArchivoLog, ctx.idPrograma(), out.p_out_Comentario);
                ctx.commit();
                return true;
            }
            return false; // :71-73 (CIJ-DEF-01)
        } catch (Exception e) { // :74-78
            ctx.rollback();
            throw e;
        }
    }
}
