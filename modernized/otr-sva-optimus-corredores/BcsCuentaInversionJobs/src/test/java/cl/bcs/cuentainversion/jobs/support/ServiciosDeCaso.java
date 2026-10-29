package cl.bcs.cuentainversion.jobs.support;

import static org.mockito.Mockito.mock;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cl.bcs.cuentainversion.service.ActualizarSaldoCargoAbonoService;
import cl.bcs.cuentainversion.service.ActualizarSaldosReporteAntiguedadService;
import cl.bcs.cuentainversion.service.CargaInicialSaldosReporteAntiguedadService;
import cl.bcs.cuentainversion.service.ConsultaComparacionSaldoService;
import cl.bcs.cuentainversion.service.GenerarArchivoCargoAbonoService;
import cl.bcs.cuentainversion.service.GenerarArchivoClientesPorPagarYCobrarService;
import cl.bcs.cuentainversion.service.GenerarArchivoCuentasPorCobrarAnexo16Service;
import cl.bcs.cuentainversion.service.GenerarArchivoSaldoCuentasService;
import cl.bcs.cuentainversion.service.GenerarInformePatrimonioDiarioService;
import cl.bcs.cuentainversion.service.GenerarInformeResumenCobroService;
import cl.bcs.cuentainversion.service.GenerarReporteAcreedoresService;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaBolsaCTAService;
import cl.bcs.cuentainversion.service.GenerarReporteAntiguedadDeudaCTAService;
import cl.bcs.cuentainversion.service.GuardarPatrimonioCajaService;
import cl.bcs.cuentainversion.service.GuardarSaldosReporteAntiguedadService;

/**
 * Mocks of the BcsCuentaInversionServicios services the jobs call directly (JSON contract: {@code execute(JsonObject)}, the form every
 * service of the module exposes). Each mock records its input in the {@link Traza} under the LEGACY service name and answers what the oracle
 * fake of the same legacy class answers ({@code Hooks.servicio}): the scripted side effects first (SQL on the transaction of the run:
 * {@code colaboradores.<Servicio>.sql}), then the scripted failure ({@code colaboradores."<Servicio>.lanza"}), then the scripted output
 * ({@code colaboradores.<Servicio>.salida}; absent: an empty object, so every field takes the default of the Servicios output class).
 * A script must give the output fields the legacy class does NOT default (for example {@code P_OUT_ERRORESCONTADOR} of
 * GenerarArchivoCargoAbono): a missing one is a {@code null} in both worlds.
 */
public final class ServiciosDeCaso {

    private ServiciosDeCaso() {
    }

    /** One mock whose {@code execute(JsonObject)} behaves as described in the class comment. */
    public static <S> S servicio(Class<S> clase, String nombre, Traza traza, BdCaso bd) {
        return mock(clase, inv -> {
            if (!inv.getMethod().getName().equals("execute")) {
                return null;
            }
            traza.registrar(nombre, inv.<JsonObject>getArgument(0));
            JsonObject script = traza.objeto(nombre);
            if (script.has("sql")) {
                for (JsonElement s : script.getAsJsonArray("sql")) {
                    bd.jdbc().execute(s.getAsString());
                }
            }
            traza.lanzaSiCorresponde(nombre + ".lanza");
            JsonElement salida = script.get("salida");
            return salida == null || salida.isJsonNull() ? new JsonObject() : JsonParser.parseString(salida.toString()).getAsJsonObject();
        });
    }

    public static ActualizarSaldoCargoAbonoService actualizarSaldoCargoAbono(Traza t, BdCaso b) {
        return servicio(ActualizarSaldoCargoAbonoService.class, "ActualizarSaldoCargoAbono", t, b);
    }

    public static ActualizarSaldosReporteAntiguedadService actualizarSaldosReporteAntiguedad(Traza t, BdCaso b) {
        return servicio(ActualizarSaldosReporteAntiguedadService.class, "ActualizarSaldosReporteAntiguedad", t, b);
    }

    public static CargaInicialSaldosReporteAntiguedadService cargaInicialSaldosReporteAntiguedad(Traza t, BdCaso b) {
        return servicio(CargaInicialSaldosReporteAntiguedadService.class, "CargaInicialSaldosReporteAntiguedad", t, b);
    }

    public static GuardarSaldosReporteAntiguedadService guardarSaldosReporteAntiguedad(Traza t, BdCaso b) {
        return servicio(GuardarSaldosReporteAntiguedadService.class, "GuardarSaldosReporteAntiguedad", t, b);
    }

    public static GuardarPatrimonioCajaService guardarPatrimonioCaja(Traza t, BdCaso b) {
        return servicio(GuardarPatrimonioCajaService.class, "GuardarPatrimonioCaja", t, b);
    }

    public static GenerarInformePatrimonioDiarioService informePatrimonioDiario(Traza t, BdCaso b) {
        return servicio(GenerarInformePatrimonioDiarioService.class, "GenerarInformePatrimonioDiario", t, b);
    }

    public static GenerarInformeResumenCobroService informeResumenCobro(Traza t, BdCaso b) {
        return servicio(GenerarInformeResumenCobroService.class, "GenerarInformeResumenCobro", t, b);
    }

    public static GenerarArchivoCargoAbonoService archivoCargoAbono(Traza t, BdCaso b) {
        return servicio(GenerarArchivoCargoAbonoService.class, "GenerarArchivoCargoAbono", t, b);
    }

    public static GenerarArchivoClientesPorPagarYCobrarService archivoClientesPorPagarYCobrar(Traza t, BdCaso b) {
        return servicio(GenerarArchivoClientesPorPagarYCobrarService.class, "GenerarArchivoClientesPorPagarYCobrar", t, b);
    }

    public static GenerarArchivoCuentasPorCobrarAnexo16Service archivoAnexo16(Traza t, BdCaso b) {
        return servicio(GenerarArchivoCuentasPorCobrarAnexo16Service.class, "GenerarArchivoCuentasPorCobrarAnexo16", t, b);
    }

    public static GenerarArchivoSaldoCuentasService archivoSaldoCuentas(Traza t, BdCaso b) {
        return servicio(GenerarArchivoSaldoCuentasService.class, "GenerarArchivoSaldoCuentas", t, b);
    }

    public static ConsultaComparacionSaldoService comparacionSaldo(Traza t, BdCaso b) {
        return servicio(ConsultaComparacionSaldoService.class, "ConsultaComparacionSaldo", t, b);
    }

    public static GenerarReporteAcreedoresService reporteAcreedores(Traza t, BdCaso b) {
        return servicio(GenerarReporteAcreedoresService.class, "GenerarReporteAcreedores", t, b);
    }

    public static GenerarReporteAntiguedadDeudaBolsaCTAService reporteAntiguedadBolsa(Traza t, BdCaso b) {
        return servicio(GenerarReporteAntiguedadDeudaBolsaCTAService.class, "GenerarReporteAntiguedadDeudaBolsaCTA", t, b);
    }

    public static GenerarReporteAntiguedadDeudaCTAService reporteAntiguedad(Traza t, BdCaso b) {
        return servicio(GenerarReporteAntiguedadDeudaCTAService.class, "GenerarReporteAntiguedadDeudaCTA", t, b);
    }
}
