package cl.bcs.tesoreria.javacall;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.ban.ActualizarSaldosCuentaBancoService;
import cl.bcs.tesoreria.javacall.ban.ConsultarLibretaBancoService;
import cl.bcs.tesoreria.javacall.ban.ConsultarSaldoCuentaBancoService;
import cl.bcs.tesoreria.javacall.car.ProcesarCargaMasivaService;
import cl.bcs.tesoreria.javacall.cloper.ActualizarTransaccionService;
import cl.bcs.tesoreria.javacall.dep.GestionDeDepositosService;
import cl.bcs.tesoreria.javacall.liq.ConsultarClearingFinancieroService;
import cl.bcs.tesoreria.javacall.liq.ProcesarClearingFinancieroService;

/**
 * Routes a JavaCall class name (the legacy {@code cl.bcs.tesoreria.*} names the platform router still sends) to the in-process service that
 * replaced it. Used by the internal endpoint and by {@code ColaboradoresJavaCall} for collaborators that live in this module.
 *
 * <p>The two authorization hooks of the legacy {@code BcsAutorizaciones} interface are not {@code JavaCall} classes; they are registered with the
 * method name as suffix ({@code ...AutorizacionLiquidacion.autorizarEvento}).
 *
 * <p>Services are looked up lazily by type, so a service that depends on {@code Colaboradores} does not create a cycle with it.
 */
@Component
public class JavaCallDispatcher {

    private static final String P = "cl.bcs.tesoreria.";

    private final ApplicationContext contexto;
    private final Map<String, Llamada> tabla = new TreeMap<>();
    private final Map<String, Class<?>> tipos = new TreeMap<>();

    public JavaCallDispatcher(ApplicationContext contexto) {
        this.contexto = contexto;
        // family A: ingreso/egreso, comprobantes, liquidacion, anulaciones, autorizaciones
        registrar(P + "ValidarTransferenciaDVP", ValidarTransferenciaDVPService.class, ValidarTransferenciaDVPService::execute);
        registrar(P + "CrearIngresoEgreso", CrearIngresoEgresoService.class, CrearIngresoEgresoService::execute);
        registrar(P + "LiquidarComprobante", LiquidarComprobanteService.class, LiquidarComprobanteService::execute);
        registrar(P + "CrearComprobanteJava", CrearComprobanteJavaService.class, CrearComprobanteJavaService::execute);
        registrar(P + "CrearComprobante", CrearComprobanteService.class, CrearComprobanteService::execute);
        registrar(P + "CrearComprobantePartidaInEg", CrearComprobantePartidaInEgService.class, CrearComprobantePartidaInEgService::execute);
        registrar(P + "LiquidacionMasivaComprobante", LiquidacionMasivaComprobanteService.class, LiquidacionMasivaComprobanteService::execute);
        registrar(P + "PreLiquidarComprobante", PreLiquidarComprobanteService.class, PreLiquidarComprobanteService::execute);
        registrar(P + "LiquidarMovimientoAutomatico", LiquidarMovimientoAutomaticoService.class, LiquidarMovimientoAutomaticoService::execute);
        registrar(P + "LiquidarComprobanteTodos", LiquidarComprobanteTodosService.class, LiquidarComprobanteTodosService::execute);
        registrar(P + "ModificarComprobanteTesoreria", ModificarComprobanteTesoreriaService.class, ModificarComprobanteTesoreriaService::execute);
        registrar(P + "GirarComprobanteIngreso", GirarComprobanteIngresoService.class, GirarComprobanteIngresoService::execute);
        registrar(P + "GirarComprobantesEgreso", GirarComprobantesEgresoService.class, GirarComprobantesEgresoService::execute);
        registrar(P + "RequisitosGirarComprobanteIngreso", RequisitosGirarComprobanteIngresoService.class, RequisitosGirarComprobanteIngresoService::execute);
        registrar(P + "RequisitosGirarComprobanteEgreso", RequisitosGirarComprobanteEgresoService.class, RequisitosGirarComprobanteEgresoService::execute);
        registrar(P + "ConsultarIngresoEgreso", ConsultarIngresoEgresoService.class, ConsultarIngresoEgresoService::execute);
        registrar(P + "ConsultarComprobantes", ConsultarComprobantesService.class, ConsultarComprobantesService::execute);
        registrar(P + "ConsultarComprobantesWS", ConsultarComprobantesWSService.class, ConsultarComprobantesWSService::execute);
        registrar(P + "AnularRegistros", AnularRegistrosService.class, AnularRegistrosService::execute);
        registrar(P + "EjecutarAnulacionIngresoEgreso", EjecutarAnulacionIngresoEgresoService.class, EjecutarAnulacionIngresoEgresoService::execute);
        registrar(P + "EjecutarAnulacionComprobantes", EjecutarAnulacionComprobantesService.class, EjecutarAnulacionComprobantesService::execute);
        registrar(P + "AutorizacionLiquidacion.autorizarEvento", AutorizacionLiquidacionService.class, AutorizacionLiquidacionService::autorizarEvento);
        registrar(P + "AutorizacionLiquidacion.rechazarEvento", AutorizacionLiquidacionService.class, AutorizacionLiquidacionService::rechazarEvento);
        registrar(P + "AutorizacionAnularMovimientosTesoreria.autorizarEvento", AutorizacionAnularMovimientosTesoreriaService.class,
                AutorizacionAnularMovimientosTesoreriaService::autorizarEvento);
        registrar(P + "AutorizacionAnularMovimientosTesoreria.rechazarEvento", AutorizacionAnularMovimientosTesoreriaService.class,
                AutorizacionAnularMovimientosTesoreriaService::rechazarEvento);
        registrar(P + "ConfirmarRechazarAnularMovimientosTesoreria", ConfirmarRechazarAnularMovimientosTesoreriaService.class,
                ConfirmarRechazarAnularMovimientosTesoreriaService::execute);
        registrar(P + "ConfirmarAutorizarAnularMovimientosTesoreria", ConfirmarAutorizarAnularMovimientosTesoreriaService.class,
                ConfirmarAutorizarAnularMovimientosTesoreriaService::execute);
        registrar(P + "EdicionMultiple", EdicionMultipleService.class, EdicionMultipleService::execute);
        registrar(P + "SaldoPendienteDeLiquidar", SaldoPendienteDeLiquidarService.class, SaldoPendienteDeLiquidarService::execute);
        registrar(P + "CrearTraspaso", CrearTraspasoService.class, CrearTraspasoService::execute);
        registrar(P + "cloper.ActualizarTransaccion", ActualizarTransaccionService.class, ActualizarTransaccionService::execute);
        registrar(P + "CuadraturaCierreDiario", CuadraturaCierreDiarioService.class, CuadraturaCierreDiarioService::execute);

        // family B (owned by the other implementer; entry point execute(JsonObject))
        registrar(P + "AnulacionMandatoPAC", AnulacionMandatoPACService.class, AnulacionMandatoPACService::execute);
        registrar(P + "RevocacionMandatoPAC", RevocacionMandatoPACService.class, RevocacionMandatoPACService::execute);
        registrar(P + "IngresarMandatoPAC", IngresarMandatoPACService.class, IngresarMandatoPACService::execute);
        registrar(P + "ActivarMandatosArchivoCargo", ActivarMandatosArchivoCargaService.class, ActivarMandatosArchivoCargaService::execute);
        registrar(P + "ActualizarSaldosCuentaBancoCorredora", ActualizarSaldosCuentaBancoCorredoraService.class,
                ActualizarSaldosCuentaBancoCorredoraService::execute);
        registrar(P + "nor.AnularNominasTransferencias", AnularNominasTransferenciasService.class, AnularNominasTransferenciasService::execute);
        registrar(P + "nor.QuitarDetalleNominasTransferencias", QuitarDetalleNominasTransferenciasService.class,
                QuitarDetalleNominasTransferenciasService::execute);
        registrar(P + "CargaMasivaIngresoEgresoDAPFFMM", CargaMasivaIngresoEgresoDAPFFMMService.class, CargaMasivaIngresoEgresoDAPFFMMService::execute);
        registrar(P + "ConsultaBCP", ConsultaBCPService.class, ConsultaBCPService::execute);
        registrar(P + "ConsultarFormatoArchivo", ConsultarFormatoArchivoService.class, ConsultarFormatoArchivoService::execute);
        registrar(P + "CrearCargaMasivaPac", CrearCargaMasivaPacService.class, CrearCargaMasivaPacService::execute);
        registrar(P + "CrearCargaMasivaPacRen", CrearCargaMasivaPacRenService.class, CrearCargaMasivaPacRenService::execute);
        registrar(P + "IngresarMandatoPACManejador", IngresarMandatoPACManejadorService.class, IngresarMandatoPACManejadorService::execute);
        registrar(P + "ModificarPAC", ModificarPACService.class, ModificarPACService::execute);
        registrar(P + "ProcesarDistribucion", ProcesarDistribucionService.class, ProcesarDistribucionService::execute);
        registrar(P + "RecaudacionBCP", RecaudacionBCPService.class, RecaudacionBCPService::execute);
        registrar(P + "ban.ActualizarSaldosCuentaBanco", ActualizarSaldosCuentaBancoService.class, ActualizarSaldosCuentaBancoService::execute);
        registrar(P + "ban.ConsultarLibretaBanco", ConsultarLibretaBancoService.class, ConsultarLibretaBancoService::execute);
        registrar(P + "ban.ConsultarSaldoCuentaBanco", ConsultarSaldoCuentaBancoService.class, ConsultarSaldoCuentaBancoService::execute);
        registrar(P + "car.ProcesarCargaMasiva", ProcesarCargaMasivaService.class, ProcesarCargaMasivaService::execute);
        registrar(P + "dep.GestionDeDepositos", GestionDeDepositosService.class, GestionDeDepositosService::execute);
        registrar(P + "liq.ConsultarClearingFinanciero", ConsultarClearingFinancieroService.class, ConsultarClearingFinancieroService::execute);
        registrar(P + "liq.ProcesarClearingFinanciero", ProcesarClearingFinancieroService.class, ProcesarClearingFinancieroService::execute);
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
        tipos.put(clase, tipo);
    }

    /** The legacy class names this module serves, with the type of the bean that serves each (for the wiring test). */
    public Map<String, Class<?>> servicios() {
        return Map.copyOf(tipos);
    }

    public boolean maneja(String clase) {
        return tabla.containsKey(clase);
    }

    /** Runs the service; the result is {@code null} when the legacy entry point returns nothing. */
    public JsonObject despachar(String clase, JsonObject params) throws Exception {
        Llamada servicio = tabla.get(clase);
        if (servicio == null) {
            throw new IllegalArgumentException("JavaCall class not migrated: " + clase);
        }
        return servicio.invocar(params);
    }
}
