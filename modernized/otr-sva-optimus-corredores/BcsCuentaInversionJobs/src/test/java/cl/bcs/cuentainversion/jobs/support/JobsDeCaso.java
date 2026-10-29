package cl.bcs.cuentainversion.jobs.support;

import java.time.Clock;

import cl.bcs.cuentainversion.jobs.job.ActualizarSaldoCargoAbonoJob;
import cl.bcs.cuentainversion.jobs.job.ActualizarSaldosReporteAntiguedadJob;
import cl.bcs.cuentainversion.jobs.job.BcsJobGenerarInformePatrimonioDiario;
import cl.bcs.cuentainversion.jobs.job.BcsJobGenerarInformeResumenCobro;
import cl.bcs.cuentainversion.jobs.job.CargaInicialSaldosReporteAntiguedadJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivo;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoCargoAbonoJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoClientesPorPagarYCobrarJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoCuentasPorCobrarAnexo16Job;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoDiferenciaSaldoJob;
import cl.bcs.cuentainversion.jobs.job.GenerarArchivoSaldoCuentasJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAcreedoresJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAntiguedadDeudaBolsaCTAJob;
import cl.bcs.cuentainversion.jobs.job.GenerarReporteAntiguedadDeudaCTAJob;
import cl.bcs.cuentainversion.jobs.job.GuardarPatrimonioCajaJob;
import cl.bcs.cuentainversion.jobs.job.LibroInventarioJob;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaActualizarSaldoCargoAbono;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGenerarReporteAcreedores;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGenerarReporteAntiguedadDeudaBolsaCTA;
import cl.bcs.cuentainversion.jobs.job.pauta.PautaGuardarSaldosReporteAntiguedad;
import cl.bcs.cuentainversion.jobs.runtime.CuentaInversionJob;
import cl.bcs.cuentainversion.jobs.runtime.ProcesoPauta;

/**
 * Builds the job (or PAUTA step) named by the case field {@code "job"} with the scripted collaborators of the case: the constructors are
 * the contract the Step 2 implementation must keep (see the test README, "Target API defined by these tests").
 */
public final class JobsDeCaso {

    private JobsDeCaso() {
    }

    public static boolean esPauta(String job) {
        return job.startsWith("Pauta");
    }

    public static CuentaInversionJob job(String job, Caso caso, Traza t) {
        BdCaso b = caso.bd();
        var jdbc = b.jdbc();
        return switch (job) {
            case "ActualizarSaldoCargoAbonoJob" -> new ActualizarSaldoCargoAbonoJob(ServiciosDeCaso.actualizarSaldoCargoAbono(t, b), jdbc);
            case "ActualizarSaldosReporteAntiguedadJob" ->
                    new ActualizarSaldosReporteAntiguedadJob(ServiciosDeCaso.actualizarSaldosReporteAntiguedad(t, b));
            case "CargaInicialSaldosReporteAntiguedadJob" ->
                    new CargaInicialSaldosReporteAntiguedadJob(ServiciosDeCaso.cargaInicialSaldosReporteAntiguedad(t, b));
            case "BcsJobGenerarInformePatrimonioDiario" ->
                    new BcsJobGenerarInformePatrimonioDiario(ServiciosDeCaso.informePatrimonioDiario(t, b), Puertos.configuracion(caso), jdbc);
            case "BcsJobGenerarInformeResumenCobro" ->
                    new BcsJobGenerarInformeResumenCobro(ServiciosDeCaso.informeResumenCobro(t, b), Puertos.configuracion(caso), jdbc);
            case "GenerarArchivoCargoAbonoJob" -> new GenerarArchivoCargoAbonoJob(ServiciosDeCaso.archivoCargoAbono(t, b), jdbc);
            case "GenerarArchivoClientesPorPagarYCobrarJob" ->
                    new GenerarArchivoClientesPorPagarYCobrarJob(ServiciosDeCaso.archivoClientesPorPagarYCobrar(t, b), jdbc);
            case "GenerarArchivoCuentasPorCobrarAnexo16Job" ->
                    new GenerarArchivoCuentasPorCobrarAnexo16Job(ServiciosDeCaso.archivoAnexo16(t, b), jdbc);
            case "GenerarArchivoDiferenciaSaldoJob" -> new GenerarArchivoDiferenciaSaldoJob(ServiciosDeCaso.comparacionSaldo(t, b), jdbc);
            case "GenerarArchivoSaldoCuentasJob" -> new GenerarArchivoSaldoCuentasJob(ServiciosDeCaso.archivoSaldoCuentas(t, b), jdbc);
            case "GenerarReporteAcreedoresJob" -> new GenerarReporteAcreedoresJob(ServiciosDeCaso.reporteAcreedores(t, b), jdbc);
            case "GenerarReporteAntiguedadDeudaBolsaCTAJob" ->
                    new GenerarReporteAntiguedadDeudaBolsaCTAJob(ServiciosDeCaso.reporteAntiguedadBolsa(t, b), jdbc);
            case "GenerarReporteAntiguedadDeudaCTAJob" ->
                    new GenerarReporteAntiguedadDeudaCTAJob(ServiciosDeCaso.reporteAntiguedad(t, b), jdbc);
            case "GuardarPatrimonioCajaJob" -> new GuardarPatrimonioCajaJob(ServiciosDeCaso.guardarPatrimonioCaja(t, b), Puertos.configuracion(caso));
            case "LibroInventarioJob" -> new LibroInventarioJob(Puertos.libroInventario(t));
            case "GenerarArchivo" -> new GenerarArchivo(Puertos.procedimiento(t), jdbc, Clock.systemDefaultZone(),
                    !"GA-29".equals(caso.nombre())); // GA-29 pins a Servicio with a double quote (review M-3 would reject it)
            default -> throw new AssertionError("unknown job in the case: " + job);
        };
    }

    public static ProcesoPauta pauta(String job, Caso caso, Traza t) {
        BdCaso b = caso.bd();
        return switch (job) {
            case "PautaActualizarSaldoCargoAbono" ->
                    new PautaActualizarSaldoCargoAbono(Puertos.configuracion(caso), ServiciosDeCaso.actualizarSaldoCargoAbono(t, b));
            case "PautaGuardarSaldosReporteAntiguedad" ->
                    new PautaGuardarSaldosReporteAntiguedad(Puertos.configuracion(caso), ServiciosDeCaso.guardarSaldosReporteAntiguedad(t, b));
            case "PautaGenerarReporteAcreedores" -> new PautaGenerarReporteAcreedores(Puertos.configuracion(caso), new ColaboradoresDeCaso(t));
            case "PautaGenerarReporteAntiguedadDeudaBolsaCTA" ->
                    new PautaGenerarReporteAntiguedadDeudaBolsaCTA(Puertos.configuracion(caso), new ColaboradoresDeCaso(t));
            default -> throw new AssertionError("unknown PAUTA step in the case: " + job);
        };
    }

    /** Runs the case through the production runner (jobs) or directly (PAUTA steps) and observes it in the shape of expected.json. */
    public static com.google.gson.JsonObject observar(Caso caso) {
        Traza traza = new Traza(caso.colaboradores());
        String job = caso.job();
        return esPauta(job) ? Observacion.ejecutarPauta(caso, pauta(job, caso, traza), traza)
                : Observacion.ejecutar(caso, job(job, caso, traza), traza);
    }
}
