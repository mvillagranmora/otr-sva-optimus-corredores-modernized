package cl.bcs.cuentainversion.jobs.job;

import cl.bcs.utils.Utils;
import java.util.Arrays;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.JobContext;
import cl.bcs.cuentainversion.service.GenerarArchivoSaldoCuentasService;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Legacy {@code cl.bcs.cuentaInversion.jobs.GenerarArchivoSaldoCuentasJob} (188 lines): account balances file (CSV zipped); updates BAT.Programa.
 */
public final class GenerarArchivoSaldoCuentasJob implements CuentaInversionJob {

    public static final String CLASE = "cl.bcs.cuentaInversion.jobs.GenerarArchivoSaldoCuentasJob";

    private final GenerarArchivoSaldoCuentasService generarArchivoSaldoCuentasService;
    private final JdbcTemplate jdbc;

    public GenerarArchivoSaldoCuentasJob(GenerarArchivoSaldoCuentasService generarArchivoSaldoCuentasService, JdbcTemplate jdbc) {
        this.generarArchivoSaldoCuentasService = Objects.requireNonNull(generarArchivoSaldoCuentasService, "generarArchivoSaldoCuentasService");
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
    }

    @Override
    public String clase() {
        return CLASE;
    }

    @Override
    public boolean transaction(JobContext ctx) throws Exception {
        // GenerarArchivoSaldoCuentasJob.java:26-122 (logica, debug=false)
        Long idEmpresa = Long.parseLong(ctx.getParam("IdEmpresa"));
        Long idUsuario = Long.parseLong(ctx.getParam("IdUsuario"));
        String idModulo = ctx.getParam("IdModulo");
        // :67 the legacy test (getParam("IdEjecutivo") != "0") compares references and is always true (CIJ-DEF-04): always parsed
        Long idEjecutivo = Long.parseLong(ctx.getParam("IdEjecutivo"));
        // :68 the same with (getParam("IdMoneda") != ""): an empty IdMoneda is never turned into null
        String idMoneda = ctx.getParam("IdMoneda");
        String soloClientesSobregirados = ctx.getParam("SoloClientesSobregirados");
        String soloClientesConSaldoDisponible = ctx.getParam("SoloClientesConSaldoDisponible");
        String soloClientesConSaldoDisponibleClientesSobregirados = ctx.getParam("SoloClientesConSaldoDisponibleClientesSobregirados");
        String incluyeProductoApv = ctx.getParam("IncluyeProductoApv");
        String incluyeOtrosSaldos = ctx.getParam("IncluyeOtrosSaldos");
        String fechaDesde = ctx.getParam("FechaDesde");
        String fechaHasta = ctx.getParam("FechaHasta");
        String encabezados = ctx.getParam("Encabezados");
        var entrada = new GenerarArchivoSaldoCuentasService.Entrada();
        entrada.p_IdEmpresa = idEmpresa;
        entrada.p_IdUsuario = idUsuario;
        entrada.p_IdModulo = idModulo;
        entrada.p_FechaDesde = fechaDesde;
        entrada.p_FechaHasta = fechaHasta;
        entrada.p_SoloClientesSobregirados = soloClientesSobregirados;
        entrada.p_SoloClientesConSaldoDisponible = soloClientesConSaldoDisponible;
        entrada.p_SoloClientesConSaldoDisponibleClientesSobregirados = soloClientesConSaldoDisponibleClientesSobregirados;
        entrada.p_IncluyeProductoApv = incluyeProductoApv;
        entrada.p_IncluyeOtrosSaldos = incluyeOtrosSaldos;
        entrada.p_IdEjecutivo = idEjecutivo;
        entrada.p_IdMoneda = idMoneda;
        String[] encabezadosLista = Utils.getGsonBuilder().fromJson(encabezados, String[].class); // :95-100
        if (encabezadosLista != null) {
            entrada.p_Encabezados = Arrays.asList(encabezadosLista);
        }
        try {
            var salida = Llamada.de(generarArchivoSaldoCuentasService::execute, entrada, GenerarArchivoSaldoCuentasService.Salida.class);
            salida.P_OUT_ERRORESCONTADOR = 0L; // :104 CIJ-DEF-03: the service counter is overwritten, the error branch is unreachable
            if (salida.P_OUT_ERRORESCONTADOR.compareTo(0L) == 0) {
                // :108 CIJ-DEF-04: every ".csv" is replaced, case-sensitive; a null path is a NullPointerException
                Llamada.actualizar(jdbc, Llamada.SQL_RUTA_SALIDA, "IdPrograma", ctx.idPrograma(),
                        "RutaArchivo", salida.P_OUT_RUTAARCHIVO.replace(".csv", ".zip"));
                ctx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            ctx.rollback();
            throw e;
        }
    }
}
