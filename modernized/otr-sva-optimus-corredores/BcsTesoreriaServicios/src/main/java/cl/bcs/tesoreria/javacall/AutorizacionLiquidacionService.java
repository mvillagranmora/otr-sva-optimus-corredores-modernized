package cl.bcs.tesoreria.javacall;

import static cl.bcs.tesoreria.javacall.SqlSinComentarios.cargar;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.ComprobanteLiquidar_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Entrada;
import cl.bcs.tesoreria.javacall.LiquidarComprobanteService.LiquidarComprobante_Salida;
import cl.bcs.tesoreria.javacall.comun.Consultas;
import cl.bcs.tesoreria.javacall.comun.FechaSistema;
import cl.bcs.tesoreria.javacall.comun.Filas;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.ResultadoAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.Evento;
import cl.bcs.tesoreria.javacall.contrato.ModificarEstadoEventoAutorizacionMasivo.EtapaEventoPrc;
import cl.bcs.tesoreria.port.out.ConfiguracionSistema;
import cl.bcs.tesoreria.port.out.Reloj;
import cl.bcs.utils.Fecha;

/**
 * Legacy {@code cl.bcs.tesoreria.AutorizacionLiquidacion} (hook {@code BcsAutorizaciones} of the authorization engine for the events
 * "liquidar comprobante"), behaviours TES-A-360..375.
 * <ul>
 *   <li>{@code autorizarEvento}: when the comprobante has two events (one per authorizer stage) it waits for the other one; otherwise it liquidates
 *       the comprobante through {@link LiquidarComprobanteService}. TES-A-360..368.</li>
 *   <li>{@code rechazarEvento}: rejects the stages of the other open events and returns the comprobante from PDA to ING. TES-A-370..375.</li>
 * </ul>
 * Each entry point exists typed ({@link #autorizar}, {@link #rechazar}) and as JSON ({@code EntradaAutorizacion} / {@code SalidaAutorizacion}).
 * Failures are reported inside the output, not thrown (except a non numeric event number on reject, TES-A-373).
 */
@Service
public class AutorizacionLiquidacionService {

    private static final Logger log = LoggerFactory.getLogger(AutorizacionLiquidacionService.class);

    private static final String SQL_RECHAZO_COMPROBANTE = cargar("sql/AutorizacionLiquidacion_UpdateRechazo.sql");
    private static final String SQL_DETALLE_COMPROBANTE = cargar("sql/AutorizacionLiquidacion_SelectDetalleComprobante.sql");
    private static final String SQL_EVENTOS = cargar("sql/AutorizacionLiquidacion_ConsultaEvento.sql");
    private static final String SQL_ETAPAS_EVENTO = cargar("sql/AutorizacionLiquidacion_ConsultaEtapaEvento.sql");
    private static final String SQL_INSERTA_AUTORIZADO_ETAPA = cargar("sql/AutorizacionLiquidacion_InsertaAutorizadoEtapaEvento.sql");
    private static final String SQL_ACTUALIZA_ETAPA = cargar("sql/AutorizacionLiquidacion_ActualizaEtapaEvento.sql");
    private static final String SQL_ACTUALIZA_EVENTO = cargar("sql/AutorizacionLiquidacion_ActualizaEventoPorEtapa.sql");
    private static final String SQL_ESTADO_COMPROBANTE = cargar("sql/AutorizacionLiquidacion_EstadoComprobante.sql");

    public static class EstadoComprobanteAutorizar {
        public Long IdComprobante;
        public String IdEstado;
    }

    public static class EventoParaAutorizar {
        public Long IdEvento;
        public String IdTipoEvento;
        public String NumeroOperacionConcepto;
        public String IdEstado;
    }

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistema configuracion;
    private final Reloj reloj;
    private final LiquidarComprobanteService liquidarComprobante;

    public AutorizacionLiquidacionService(NamedParameterJdbcTemplate jdbc, ConfiguracionSistema configuracion, Reloj reloj,
            LiquidarComprobanteService liquidarComprobante) {
        this.jdbc = jdbc;
        this.configuracion = configuracion;
        this.reloj = reloj;
        this.liquidarComprobante = liquidarComprobante;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject autorizarEvento(JsonObject entrada) {
        return JsonLegacy.PLATAFORMA.toJsonTree(autorizar(JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaAutorizacion.class))).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject rechazarEvento(JsonObject entrada) {
        return JsonLegacy.PLATAFORMA.toJsonTree(rechazar(JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaAutorizacion.class))).getAsJsonObject();
    }

    // ------------------------------------------------------------------ authorize

    /** Legacy typed {@code autorizarEvento}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaAutorizacion autorizar(EntradaAutorizacion entrada) {
        log.info("INICIO:AUTORIZACION");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        LiquidarComprobante_Entrada liquidar = new LiquidarComprobante_Entrada();
        liquidar.p_IdEmpresa = entrada.p_IdEmpresa;
        liquidar.p_IdUsuario = entrada.p_IdUsuario;
        liquidar.p_IdModulo = "TES";
        liquidar.p_UsaReglasNegocio = "S";
        liquidar.p_SimularOperacion = "N";
        for (Evento e : entrada.p_Lista) {
            try {
                liquidar.p_ComprobanteLiquidar.addAll(jdbc.query(SQL_DETALLE_COMPROBANTE, Map.of("IdComprobante", Long.parseLong(e.NumeroOperacionConcepto)),
                        Filas.de(ComprobanteLiquidar_Entrada.class)));
            } catch (Exception er) {
                log.error("ERROR::autorizarEvento", er);
            }
        }
        try {
            liquidar.p_FechaMovimiento = Fecha.toStringDate(FechaSistema.fechaHoraSistemaTs(configuracion, reloj, entrada.p_IdEmpresa));
        } catch (Exception er) {
            log.error("ERROR::autorizarEvento", er);
        }

        try {
            // the last event of the list drives the query (legacy)
            Object idEvento = null;
            Map<String, Object> consulta = new HashMap<>();
            consulta.put("IdComprobante", null); // no events: the query still runs, with a NULL parameter
            for (Evento e : entrada.p_Lista) {
                consulta.put("IdComprobante", e.NumeroOperacionConcepto);
                idEvento = e.IdEvento;
            }
            List<EventoParaAutorizar> eventos = jdbc.query(SQL_EVENTOS, consulta, Filas.de(EventoParaAutorizar.class));
            String estadoComprobante = "";
            for (EstadoComprobanteAutorizar e : jdbc.query(SQL_ESTADO_COMPROBANTE, consulta, Filas.de(EstadoComprobanteAutorizar.class))) {
                estadoComprobante = e.IdEstado;
            }
            Map<Long, String> tipoPorId = new HashMap<>();
            if (eventos.size() == 2) {
                for (EventoParaAutorizar ev : eventos) {
                    if (!idEvento.equals(ev.IdEvento) && ev.IdEstado.equalsIgnoreCase("VIG")) {
                        // the other stage is still pending: this authorization is recorded and the user is told to authorize the other event
                        salida.P_OUT_ERRORESCONTADOR = 0L;
                        salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ADVERTENCIA, "OPERACION_AUTORIZADA",
                                "Operacion autorizada con exito, para continuar favor autorizar evento:" + ev.IdEvento));
                        ResultadoAutorizacion res = new ResultadoAutorizacion();
                        res.evento = idEvento;
                        res.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                        salida.P_OUT_RESULTADO.add(res);
                    } else if (!idEvento.equals(ev.IdEvento) && ev.IdEstado.equalsIgnoreCase("AUT") && !estadoComprobante.equals("LIQ")) {
                        liquidar(entrada, salida, liquidar, tipoPorId);
                    }
                }
            } else {
                liquidar(entrada, salida, liquidar, tipoPorId);
            }
        } catch (Exception e1) {
            salida.P_OUT_ERRORESCONTADOR = 1L; // LEGACY DEFECT kept (see README TES-A-367): any failure is reported only as a counter
        }
        log.info("TERMINO:AUTORIZACION");
        return salida;
    }

    /**
     * Liquidates the comprobantes and builds the result per event. LEGACY DEFECT kept (see README TES-A-360): the success test looks the event
     * NUMBER (a String) up in a map keyed by Long, so it never matches and the output always carries the liquidation messages instead of success.
     */
    @SuppressWarnings("SuspiciousMethodCalls")
    private void liquidar(EntradaAutorizacion entrada, SalidaAutorizacion salida, LiquidarComprobante_Entrada liquidar, Map<Long, String> tipoPorId) {
        LiquidarComprobante_Salida liquidacion;
        try {
            liquidacion = liquidarComprobante.logica(liquidar);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        for (MensajeSalida m : liquidacion.P_OUT_MENSAJES) {
            tipoPorId.put(m.id(), m.tipo());
        }
        for (Evento evento : entrada.p_Lista) {
            if (tipoPorId.containsKey(evento.NumeroOperacionConcepto) && !tipoPorId.get(evento.NumeroOperacionConcepto).equals("ERROR")) {
                salida.P_OUT_ERRORESCONTADOR = 0L;
                salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, "OPERACION_AUTORIZADA", "Operacion autorizada con exito"));
                ResultadoAutorizacion res = new ResultadoAutorizacion();
                res.evento = evento.IdEvento;
                res.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(res);
            } else {
                salida.P_OUT_ERRORESCONTADOR = liquidacion.P_OUT_ERRORESCONTADOR;
                salida.P_OUT_MENSAJES = liquidacion.P_OUT_MENSAJES;
            }
        }
    }

    // ------------------------------------------------------------------ reject

    /** Legacy typed {@code rechazarEvento}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaAutorizacion rechazar(EntradaAutorizacion entrada) {
        log.info("INICIO:RECHAZO");
        SalidaAutorizacion salida = new SalidaAutorizacion();

        List<Map<String, Object>> liquidaciones = new ArrayList<>();
        for (Evento e : entrada.p_Lista) {
            liquidaciones.add(Map.of("IdComprobante", Long.parseLong(e.NumeroOperacionConcepto)));
        }
        try {
            // the last event / comprobante of the list drive the query (legacy)
            Object idEvento = null;
            for (Evento e : entrada.p_Lista) {
                idEvento = e.IdEvento;
            }
            Map<String, Object> consulta = new HashMap<>();
            consulta.put("IdComprobante", null); // no comprobantes: the query still runs, with a NULL parameter
            for (Map<String, Object> liquidacion : liquidaciones) {
                consulta.put("IdComprobante", liquidacion.get("IdComprobante"));
            }
            List<EventoParaAutorizar> eventos = jdbc.query(SQL_EVENTOS, consulta, Filas.de(EventoParaAutorizar.class));
            for (EventoParaAutorizar ev : eventos) {
                if (!idEvento.equals(ev.IdEvento)) {
                    rechazarEtapas(entrada, ev);
                }
            }
            Consultas.lote(jdbc, SQL_RECHAZO_COMPROBANTE, liquidaciones);
        } catch (Exception e1) {
            salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_ERROR, "OPERACION_NO_SE_PUEDE_ANULAR", "Operacion no se puede Anular"));
            log.error("ERROR::rechazarEvento", e1);
        }
        log.info("TERMINO:RECHAZO");
        return salida;
    }

    /** Marks as rejected (RZD) the stages of an open event, the authorizations given on them and the event itself. */
    private void rechazarEtapas(EntradaAutorizacion entrada, EventoParaAutorizar evento) throws Exception {
        Timestamp ahora = FechaSistema.fechaHoraSistemaTs(configuracion, reloj, entrada.p_IdEmpresa);
        List<EtapaEventoPrc> etapas = jdbc.query(SQL_ETAPAS_EVENTO, Map.of("ListaIdEventos", evento.IdEvento), Filas.de(EtapaEventoPrc.class));

        List<Map<String, Object>> autorizados = new ArrayList<>();
        List<Map<String, Object>> etapasActualizar = new ArrayList<>();
        List<Map<String, Object>> eventosActualizar = new ArrayList<>();
        for (EtapaEventoPrc etapa : etapas) {
            Map<String, Object> autorizado = new HashMap<>();
            autorizado.put("IdAutorizadoEtapaEvento", 0L);
            autorizado.put("IdEtapaEvento", etapa.IdEtapaEvento);
            autorizado.put("IdUsuario", entrada.p_IdUsuario);
            autorizado.put("IdRol", null);
            autorizado.put("Observaciones", "");
            autorizado.put("FechaHoraCambioEstado", ahora);
            autorizado.put("IdEstado", "RZD");
            autorizados.add(autorizado);

            Map<String, Object> etapaActualizar = new HashMap<>();
            etapaActualizar.put("IdEtapaEvento", etapa.IdEtapaEvento);
            etapaActualizar.put("IdEstado", "RZD");
            etapaActualizar.put("FechaHoraCambioEstado", ahora);
            etapaActualizar.put("IdUsuarioCambioEstado", entrada.p_IdUsuario);
            etapasActualizar.add(etapaActualizar);

            Map<String, Object> eventoActualizar = new HashMap<>();
            eventoActualizar.put("IdEvento", etapa.IdEvento);
            eventoActualizar.put("IdUsuarioCambioEstado", entrada.p_IdUsuario);
            eventoActualizar.put("FechaHoraCambioEstado", ahora);
            eventosActualizar.add(eventoActualizar);
        }
        Consultas.lote(jdbc, SQL_INSERTA_AUTORIZADO_ETAPA, autorizados);
        Consultas.lote(jdbc, SQL_ACTUALIZA_ETAPA, etapasActualizar);
        Consultas.lote(jdbc, SQL_ACTUALIZA_EVENTO, eventosActualizar);
    }
}
