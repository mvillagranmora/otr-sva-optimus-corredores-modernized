package cl.bcs.fondomutuo.tesoreria;

import java.util.Map;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.adapter.out.http.ColaboradoresHttp;
import cl.bcs.fondomutuo.adapter.out.http.RouterClient;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.tesoreria.infrastructure.adapter.ProcedimientoAlmacenado;
import cl.bcs.tesoreria.port.out.Colaboradores;

/**
 * C4 of PLAN_FONDOMUTUO_F2 4.1: Tesoreria's {@code Colaboradores} port inside this module. The legacy {@code CrearIngresoEgreso} called these classes in
 * process on the same connection; here:
 * <ul>
 *   <li>Plataforma, by HTTP through this module's {@link RouterClient} (legacy class names): {@code RevisarRequisitosBasicos} (read),
 *       {@code SaldoFlujoCaja} (read; gate G3 first, as in Normal: Plataforma does not serve it as a JavaCall yet), {@code AgregarLog} (append-only write,
 *       commits in its owner: G5);</li>
 *   <li>Tesoreria's own work, IN PROCESS on the connection of the unit of work (as BcsTesoreriaServicios runs it): {@code StoredProcedure} (the existing
 *       procedure {@code CrearComprobante}) and {@code ActualizarSaldosCuentaBanco};</li>
 *   <li>any other name (the liquidation path: {@code CrearCargoAbono}, {@code ConsultarFlujoCaja}, ...; the helper never asks for it) fails closed:
 *       {@code ServicioNoServidoException} (503, rollback).</li>
 * </ul>
 * DEV-F5 inside Tesoreria: {@code CrearIngresoEgresoService.logica} swallows every exception of its collaborators into the message
 * {@code UI_ERROR_INGRESO_EGRESO}, so a router 503 / timeout would reach Normal as a business error. {@link #vigilar} records the FIRST failure of a
 * collaborator during one call of the helper and the helper rethrows it after Tesoreria returns: rollback + 503 (repeatable in TomEE) or the generic 500,
 * exactly what DEV-F5 approved for the collaborators by HTTP. The record lives only for that call (thread-confined, cleared in {@code finally}).
 */
public class ColaboradoresTesoreria implements Colaboradores {

    /** Legacy simple name used by Tesoreria -> legacy fully qualified class sent to the router. */
    static final Map<String, String> HTTP = Map.of(
            "RevisarRequisitosBasicos", "cl.bcs.plataforma.pla.RevisarRequisitosBasicos",
            "SaldoFlujoCaja", ColaboradoresHttp.SALDO_FLUJO_CAJA,
            "AgregarLog", ColaboradoresHttp.AGREGAR_LOG);
    static final String PROCEDIMIENTO = "StoredProcedure";
    static final String SALDOS_CUENTA_BANCO = "ActualizarSaldosCuentaBanco";

    /** Tesoreria's in-process {@code ActualizarSaldosCuentaBancoService.execute}. */
    @FunctionalInterface
    public interface EnProceso {
        JsonObject ejecutar(JsonObject entrada) throws Exception;
    }

    /** One call of Tesoreria. */
    @FunctionalInterface
    public interface Llamada<T> {
        T hacer() throws Exception;
    }

    private static final ThreadLocal<RuntimeException[]> FALLO = new ThreadLocal<>();

    private final RouterClient router;
    private final Compuertas compuertas;
    private final ProcedimientoAlmacenado procedimientos;
    private final EnProceso saldosCuentaBanco;

    public ColaboradoresTesoreria(RouterClient router, Compuertas compuertas, ProcedimientoAlmacenado procedimientos, EnProceso saldosCuentaBanco) {
        this.router = router;
        this.compuertas = compuertas;
        this.procedimientos = procedimientos;
        this.saldosCuentaBanco = saldosCuentaBanco;
    }

    @Override
    public JsonObject invocar(String servicio, JsonObject entrada) {
        // in process: a failure here is what the legacy saw on its own connection and swallowed the same way (bug for bug): not recorded
        if (PROCEDIMIENTO.equals(servicio)) {
            return procedimientos.ejecutar(entrada);
        }
        if (SALDOS_CUENTA_BANCO.equals(servicio)) {
            try {
                return saldosCuentaBanco.ejecutar(entrada);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException("Tesoreria collaborator " + servicio + " failed", e);
            }
        }
        // by HTTP (or not wired): a failure of the transport is recorded for DEV-F5
        try {
            String clase = HTTP.get(servicio);
            if (clase == null) {
                throw new ServicioNoServidoException("G1", "Tesoreria collaborator " + servicio + " is not wired in process in this module");
            }
            if ("SaldoFlujoCaja".equals(servicio)) {
                compuertas.g3SaldoFlujoCaja();
            }
            return router.invocar(clase, entrada);
        } catch (RuntimeException e) {
            registrar(e);
            throw e;
        }
    }

    private static void registrar(RuntimeException e) {
        RuntimeException[] r = FALLO.get();
        if (r != null && r[0] == null) {
            r[0] = e;
        }
    }

    /**
     * Runs one call of Tesoreria and, when one of its collaborators failed (even if Tesoreria swallowed it), rethrows that failure instead of the
     * result (DEV-F5). A nested call keeps the outer record.
     */
    public static <T> T vigilar(Llamada<T> llamada) throws Exception {
        boolean propia = FALLO.get() == null;
        if (propia) {
            FALLO.set(new RuntimeException[1]);
        }
        try {
            T resultado = llamada.hacer();
            RuntimeException fallo = FALLO.get()[0];
            if (fallo != null) {
                throw fallo;
            }
            return resultado;
        } finally {
            if (propia) {
                FALLO.remove();
            }
        }
    }
}
