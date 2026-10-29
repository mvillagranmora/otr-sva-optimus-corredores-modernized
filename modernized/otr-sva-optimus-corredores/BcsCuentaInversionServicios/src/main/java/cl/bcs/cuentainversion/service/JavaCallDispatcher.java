package cl.bcs.cuentainversion.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

/**
 * Routes a JavaCall class name (the legacy {@code cl.bcs.cuentaInversion.*} names the platform still sends) to the in-process
 * service that replaced it. Used by the internal endpoint and by {@code ColaboradoresJavaCall} for collaborators that live in this module.
 *
 * <p>Services are looked up lazily by type, so a service that depends on {@code Colaboradores} does not create a cycle with it.
 */
@Component
public class JavaCallDispatcher {

    private final ApplicationContext contexto;
    private final Map<String, Llamada> tabla = new HashMap<>();

    public JavaCallDispatcher(ApplicationContext contexto) {
        this.contexto = contexto;
        registrar("cl.bcs.cuentaInversion.ActualizarSaldoCargoAbono", ActualizarSaldoCargoAbonoService.class, ActualizarSaldoCargoAbonoService::call);
        registrar("cl.bcs.cuentaInversion.ActualizarSaldosReporteAntiguedad", ActualizarSaldosReporteAntiguedadService.class, ActualizarSaldosReporteAntiguedadService::execute);
        registrar("cl.bcs.cuentaInversion.AnularCargoAbono", AnularCargoAbonoService.class, AnularCargoAbonoService::execute);
        registrar("cl.bcs.cuentaInversion.CargaInicialSaldosReporteAntiguedad", CargaInicialSaldosReporteAntiguedadService.class, CargaInicialSaldosReporteAntiguedadService::execute);
        registrar("cl.bcs.cuentaInversion.CertificarCargoAbono", CertificarCargoAbonoService.class, CertificarCargoAbonoService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultaAntiguedadDeudasCuentaInversion", ConsultaAntiguedadDeudasCuentaInversionService.class, ConsultaAntiguedadDeudasCuentaInversionService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultaComparacionSaldo", ConsultaComparacionSaldoService.class, ConsultaComparacionSaldoService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultarCartolaDeMovimiento", ConsultarCartolaDeMovimientoService.class, ConsultarCartolaDeMovimientoService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultarCobroComisionRegistros", ConsultarCobroComisionRegistrosService.class, ConsultarCobroComisionRegistrosService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultaReporteAcreedores", ConsultaReporteAcreedoresService.class, ConsultaReporteAcreedoresService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultaReporteAntiguedadBolsa", ConsultaReporteAntiguedadBolsaService.class, ConsultaReporteAntiguedadBolsaService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultarMovimientoCuentaInversion", ConsultarMovimientoCuentaInversionService.class, ConsultarMovimientoCuentaInversionService::execute);
        registrar("cl.bcs.cuentaInversion.ConsultarSaldoCuentaInversion", ConsultarSaldoCuentaInversionService.class, ConsultarSaldoCuentaInversionService::execute);
        registrar("cl.bcs.cuentaInversion.CrearCargaMasiva", CrearCargaMasivaService.class, CrearCargaMasivaService::execute);
        registrar("cl.bcs.cuentaInversion.CrearCargoAbono", CrearCargoAbonoService.class, CrearCargoAbonoService::execute);
        registrar("cl.bcs.cuentaInversion.CrearCobroComisionExclusionConcepto", CrearCobroComisionExclusionConceptoService.class, CrearCobroComisionExclusionConceptoService::execute);
        registrar("cl.bcs.cuentaInversion.CrearCobroComisionOpcionesCobro", CrearCobroComisionOpcionesCobroService.class, CrearCobroComisionOpcionesCobroService::execute);
        registrar("cl.bcs.cuentaInversion.CuadraturaCierreDiario", CuadraturaCierreDiarioService.class, CuadraturaCierreDiarioService::execute);
        registrar("cl.bcs.cuentaInversion.EditarCobroComisionOpcionesCobro", EditarCobroComisionOpcionesCobroService.class, EditarCobroComisionOpcionesCobroService::execute);
        registrar("cl.bcs.cuentaInversion.EditarCobroComisionRegistros", EditarCobroComisionRegistrosService.class, EditarCobroComisionRegistrosService::execute);
        registrar("cl.bcs.cuentaInversion.EliminaCargaSeleccionada", EliminaCargaSeleccionadaService.class, EliminaCargaSeleccionadaService::execute);
        registrar("cl.bcs.cuentaInversion.EliminarCobroComisionExclusionConcepto", EliminarCobroComisionExclusionConceptoService.class, EliminarCobroComisionExclusionConceptoService::execute);
        registrar("cl.bcs.cuentaInversion.EliminarCobroComisionOpcionesCobro", EliminarCobroComisionOpcionesCobroService.class, EliminarCobroComisionOpcionesCobroService::execute);
        registrar("cl.bcs.cuentaInversion.EliminarCobroComisionRegistros", EliminarCobroComisionRegistrosService.class, EliminarCobroComisionRegistrosService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarArchivoCargoAbono", GenerarArchivoCargoAbonoService.class, GenerarArchivoCargoAbonoService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarArchivoClientesPorPagarYCobrar", GenerarArchivoClientesPorPagarYCobrarService.class, GenerarArchivoClientesPorPagarYCobrarService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarArchivoCuentasPorCobrarAnexo16", GenerarArchivoCuentasPorCobrarAnexo16Service.class, GenerarArchivoCuentasPorCobrarAnexo16Service::execute);
        registrar("cl.bcs.cuentaInversion.GenerarArchivoSaldoCuentas", GenerarArchivoSaldoCuentasService.class, GenerarArchivoSaldoCuentasService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarInformePatrimonioDiario", GenerarInformePatrimonioDiarioService.class, GenerarInformePatrimonioDiarioService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarInformeResumenCobro", GenerarInformeResumenCobroService.class, GenerarInformeResumenCobroService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarReporteAcreedores", GenerarReporteAcreedoresService.class, GenerarReporteAcreedoresService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaBolsaCTA", GenerarReporteAntiguedadDeudaBolsaCTAService.class, GenerarReporteAntiguedadDeudaBolsaCTAService::execute);
        registrar("cl.bcs.cuentaInversion.GenerarReporteAntiguedadDeudaCTA", GenerarReporteAntiguedadDeudaCTAService.class, GenerarReporteAntiguedadDeudaCTAService::execute);
        registrar("cl.bcs.cuentaInversion.GuardarPatrimonioCajaCalculoComision", GuardarPatrimonioCajaCalculoComisionService.class, GuardarPatrimonioCajaCalculoComisionService::execute);
        registrar("cl.bcs.cuentaInversion.GuardarPatrimonioCaja", GuardarPatrimonioCajaService.class, GuardarPatrimonioCajaService::execute);
        registrar("cl.bcs.cuentaInversion.GuardarSaldosReporteAntiguedad", GuardarSaldosReporteAntiguedadService.class, GuardarSaldosReporteAntiguedadService::execute);
        registrar("cl.bcs.cuentaInversion.IndiceRentabilidadAtributtionAnalysis", IndiceRentabilidadAttributionAnalysisService.class, IndiceRentabilidadAttributionAnalysisService::execute);
        registrar("cl.bcs.cuentaInversion.InformeAtributtionAnalisys", InformeAttributionAnalysisService.class, InformeAttributionAnalysisService::execute);
        registrar("cl.bcs.cuentaInversion.ProcesaCargaSeleccionada", ProcesaCargaSeleccionadaService.class, ProcesaCargaSeleccionadaService::execute);
        registrar("cl.bcs.cuentaInversion.ProcesaIndicesAtributtionAnalisys", ProcesaIndicesAtributtionAnalisysService.class, ProcesaIndicesAtributtionAnalisysService::execute);
        registrar("cl.bcs.cuentaInversion.ProcesarCobroComisionPatrimonio", ProcesarCobroComisionPatrimonioService.class, ProcesarCobroComisionPatrimonioService::execute);
        registrar("cl.bcs.cuentaInversion.SaldoCuentaInversion", SaldoCuentaInversionService.class, SaldoCuentaInversionService::execute);
        registrar("cl.bcs.cuentaInversion.webservices.ConsultarMovimientos", ConsultarMovimientosWSService.class, ConsultarMovimientosWSService::execute);
        registrar("cl.bcs.cuentaInversion.webservices.CrearCargoAbonoWS", CrearCargoAbonoWSService.class, CrearCargoAbonoWSService::execute);
    }

    /** Entry point of a service; the legacy ones declare {@code throws Exception}. */
    @FunctionalInterface
    private interface Llamada {
        JsonObject invocar(JsonObject params) throws Exception;
    }

    @FunctionalInterface
    private interface EntradaServicio<T> {
        JsonObject invocar(T servicio, JsonObject params) throws Exception;
    }

    private <T> void registrar(String clase, Class<T> tipo, EntradaServicio<T> entrada) {
        tabla.put(clase, params -> entrada.invocar(contexto.getBean(tipo), params));
    }

    public boolean maneja(String clase) {
        return tabla.containsKey(clase);
    }

    /** Runs the service; the result is {@code null} when the legacy entry point returns nothing (ActualizarSaldoCargoAbono). */
    public JsonObject despachar(String clase, JsonObject params) throws Exception {
        Llamada servicio = tabla.get(clase);
        if (servicio == null) {
            throw new IllegalArgumentException("JavaCall class not migrated: " + clase);
        }
        return servicio.invocar(params);
    }
}
