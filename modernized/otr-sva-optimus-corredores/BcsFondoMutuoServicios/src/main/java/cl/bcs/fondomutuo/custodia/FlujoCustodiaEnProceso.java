package cl.bcs.fondomutuo.custodia;

import java.sql.SQLException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionAcotada;
import cl.bcs.fondomutuo.adapter.out.jdbc.ConexionUnidadDeTrabajo;
import cl.bcs.fondomutuo.kit.AccesoDatosException;
import cl.bcs.fondomutuo.kit.ColaboradorErrorException;
import cl.bcs.fondomutuo.kit.ServicioNoServidoException;
import cl.bcs.fondomutuo.port.out.http.CustodiaPort;

/**
 * {@link CustodiaPort} IN PROCESS (F3, 2026-10-28): the custody flow of {@code cl.bcs.custodia.flujo.ConsultarFlujoCustodiaFlujo} (library of
 * BcsCustodiaServicios, PROVEN against 85 goldens of the legacy {@code cl.bcs.custodia.ConsultarFlujoCustodia}) called inside the unit of work of the
 * request instead of over HTTP. This is what lets G-CUS ({@code fondomutuo.custodia.habilitada}) open without breaking atomicity: the library receives
 * the connection of the CURRENT Spring transaction ({@link ConexionUnidadDeTrabajo#usar}), never opens another one and never commits or rolls back
 * (its only write, the work rows of {@code CUS.FlujoCustodia_MatrizUso}, is deleted in its own {@code finally} and goes with the transaction of the
 * request; a rollback of the host leaves nothing). Pinned by {@code FlujoCustodiaEnProcesoTest}.
 *
 * <p>Same JSON contract as the HTTP adapter ({@code ColaboradoresHttp}, kept as the fall-back): request and answer are the legacy JSON of
 * {@code ConsultarFlujoCustodia}. Failures: a {@link SQLException} becomes the module's {@code AccesoDatosException} (SQLState and error code only, as every
 * port adapter); a runtime exception of the library (the legacy dereferences nulls on purpose, D-CUS-02) is rethrown as is; a checked
 * {@code java.lang.Exception} of the library becomes {@link ColaboradorErrorException} (generic 500, rollback; never a 503: the request already read and
 * wrote in this transaction).
 *
 * <p>VALUATION: the library delegates {@code p_ValorizarFlujo = "S"} to {@code ValorizarInstrumento} (Plataforma), which is NOT a collaborator of this
 * module (neither {@code ColaboradoresHttp} nor any in-process class of FondoMutuo calls it; {@code CalcularMontoNumeroCuota} is a different class). Every
 * caller of FondoMutuo asks for {@code "N"} ({@code ConsultarFlujoCustodiaFondoMutuoService}), so the valuation collaborator is CLOSED: a request that asks
 * for {@code "S"} is rejected BEFORE any read or write with {@link ServicioNoServidoException} (503: the router repeats it in TomEE), and the
 * collaborator handed to the library throws the same exception as a second net.
 *
 * <p>Review of 2026-10-28 (FFMM and custody in process): (H2) the library gets not the raw connection but {@link ConexionAcotada}: every statement it creates carries the
 * remaining time of the host transaction, and {@code commit}/{@code rollback}/{@code close}/{@code setAutoCommit}... raise {@link IllegalStateException}; (M1) with the guard on
 * ({@code fondomutuo.custodia.exigir-transaccion}, default true under Spring) the call needs an active read-write Spring transaction, because its work rows would otherwise
 * autocommit; (M4) one INFO line per call: duration, number of portfolios and instruments asked, key of the flow and outcome (counts and keys only, never values or database text).
 */
public class FlujoCustodiaEnProceso implements CustodiaPort {

    private static final Logger log = LoggerFactory.getLogger(FlujoCustodiaEnProceso.class);

    private final ConexionUnidadDeTrabajo conexion;
    private final ConsultarFlujoCustodiaFlujo flujo;
    private final boolean exigirTransaccion;

    /** Without the guard of {@link #exigirTransaccion}: the equivalence harnesses run on a raw connection with no Spring transaction. */
    public FlujoCustodiaEnProceso(ConexionUnidadDeTrabajo conexion, ConsultarFlujoCustodiaFlujo flujo) {
        this(conexion, flujo, false);
    }

    /** @param exigirTransaccion M1: the call requires an active Spring transaction that is NOT read only (its work rows must ride the transaction of the request) */
    public FlujoCustodiaEnProceso(ConexionUnidadDeTrabajo conexion, ConsultarFlujoCustodiaFlujo flujo, boolean exigirTransaccion) {
        this.conexion = conexion;
        this.flujo = flujo;
        this.exigirTransaccion = exigirTransaccion;
    }

    /** The {@code valorizador} collaborator of the library while ValorizarInstrumento is not available in process: fails closed, no effect to undo. */
    public static Function<JsonObject, JsonObject> valorizadorCerrado() {
        return solicitud -> {
            throw new ServicioNoServidoException("G-CUS", "ValorizarInstrumento is not available in process: the custody flow cannot be valued here");
        };
    }

    @Override
    public JsonObject consultarFlujo(JsonObject entrada) {
        JsonElement valorizar = entrada.get("p_ValorizarFlujo");
        if (valorizar != null && !valorizar.isJsonNull() && "S".equals(valorizar.getAsString())) {
            throw new ServicioNoServidoException("G-CUS", "valued custody flow (p_ValorizarFlujo = S) is not served in process");
        }
        if (exigirTransaccion && !(TransactionSynchronizationManager.isActualTransactionActive() && !TransactionSynchronizationManager.isCurrentTransactionReadOnly())) {
            throw new IllegalStateException("the custody flow in process needs an active read-write transaction (its work rows ride the transaction of the host)");
        }
        long inicio = System.nanoTime();
        String resultado = "ok";
        Object clave = null;
        try {
            JsonObject salida = conexion.usar(con -> {
                try {
                    return flujo.call(ConexionAcotada.de(con), entrada);
                } catch (SQLException | RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new ColaboradorErrorException("custody flow in process failed: " + e.getClass().getSimpleName(), e);
                }
            });
            clave = clave(salida);
            return salida;
        } catch (AccesoDatosException e) {
            resultado = "sql";
            throw e;
        } catch (ColaboradorErrorException e) {
            resultado = "checked";
            throw e;
        } catch (RuntimeException e) {
            resultado = "runtime";
            throw e;
        } finally {
            log.info("custody flow in process: result={} ms={} carteras={} instrumentos={} keyFlujoCustodia={}", resultado, (System.nanoTime() - inicio) / 1_000_000L,
                    cuantos(entrada, "p_Uso"), cuantos(entrada, "p_FiltrosBusqueda"), clave == null ? "-" : clave);
        }
    }

    private static int cuantos(JsonObject entrada, String campo) {
        JsonElement e = entrada.get(campo);
        return e != null && e.isJsonArray() ? e.getAsJsonArray().size() : 0;
    }

    private static Object clave(JsonObject salida) {
        JsonElement k = salida == null ? null : salida.get("p_out_KeyFlujoCustodia");
        return k != null && k.isJsonPrimitive() ? k.getAsString() : null;
    }
}
