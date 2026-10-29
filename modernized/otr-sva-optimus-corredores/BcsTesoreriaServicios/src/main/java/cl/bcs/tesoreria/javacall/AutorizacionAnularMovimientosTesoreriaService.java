package cl.bcs.tesoreria.javacall;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.JsonObject;

import cl.bcs.tesoreria.javacall.ConfirmarAutorizarAnularMovimientosTesoreriaService.EntradaConfirmarAutorizarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.javacall.ConfirmarAutorizarAnularMovimientosTesoreriaService.MovimientosPorAutorizar;
import cl.bcs.tesoreria.javacall.ConfirmarAutorizarAnularMovimientosTesoreriaService.SalidaConfirmarAutorizarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.javacall.ConfirmarRechazarAnularMovimientosTesoreriaService.EntradaConfirmarRechazarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.javacall.ConfirmarRechazarAnularMovimientosTesoreriaService.MovimientosPorRechazar;
import cl.bcs.tesoreria.javacall.ConfirmarRechazarAnularMovimientosTesoreriaService.SalidaConfirmarRechazarAnularMovimientosTesoreria;
import cl.bcs.tesoreria.javacall.comun.JsonLegacy;
import cl.bcs.tesoreria.javacall.comun.MensajeSalida;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.EntradaAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.ResultadoAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.BcsAutorizaciones.SalidaAutorizacion;
import cl.bcs.tesoreria.javacall.contrato.Evento;
import cl.bcs.tesoreria.port.out.DiccionarioMensajes;

/**
 * Legacy {@code cl.bcs.tesoreria.AutorizacionAnularMovimientosTesoreria} (hook {@code BcsAutorizaciones} of the authorization engine for the
 * event "annul movimiento"), behaviours TES-A-380..387: once the engine has authorized or rejected the events, the movimientos are annulled
 * ({@link ConfirmarAutorizarAnularMovimientosTesoreriaService}) or released ({@link ConfirmarRechazarAnularMovimientosTesoreriaService}).
 *
 * <p>Two entry points with the legacy meaning: {@code autorizarEvento} and {@code rechazarEvento}; each exists typed and as JSON (the
 * {@code EntradaAutorizacion} / {@code SalidaAutorizacion} contract). A failure is reported as {@code P_OUT_ERRORESCONTADOR = 1}, never thrown,
 * except for an event number that is not numeric, which fails before the protected block.
 */
@Service
public class AutorizacionAnularMovimientosTesoreriaService {

    private static final Logger log = LoggerFactory.getLogger(AutorizacionAnularMovimientosTesoreriaService.class);

    private final DiccionarioMensajes diccionario;
    private final ConfirmarAutorizarAnularMovimientosTesoreriaService confirmarAutorizar;
    private final ConfirmarRechazarAnularMovimientosTesoreriaService confirmarRechazar;

    public AutorizacionAnularMovimientosTesoreriaService(DiccionarioMensajes diccionario,
            ConfirmarAutorizarAnularMovimientosTesoreriaService confirmarAutorizar, ConfirmarRechazarAnularMovimientosTesoreriaService confirmarRechazar) {
        this.diccionario = diccionario;
        this.confirmarAutorizar = confirmarAutorizar;
        this.confirmarRechazar = confirmarRechazar;
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject autorizarEvento(JsonObject entrada) {
        return JsonLegacy.PLATAFORMA.toJsonTree(autorizar(JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaAutorizacion.class))).getAsJsonObject();
    }

    @Transactional(rollbackFor = Exception.class)
    public JsonObject rechazarEvento(JsonObject entrada) {
        return JsonLegacy.PLATAFORMA.toJsonTree(rechazar(JsonLegacy.PLATAFORMA.fromJson(entrada, EntradaAutorizacion.class))).getAsJsonObject();
    }

    /** Legacy typed {@code autorizarEvento}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaAutorizacion autorizar(EntradaAutorizacion entrada) {
        log.info("INICIO:AUTORIZACION");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        EntradaConfirmarAutorizarAnularMovimientosTesoreria confirmar = new EntradaConfirmarAutorizarAnularMovimientosTesoreria();
        for (Evento evento : entrada.p_Lista) {
            confirmar.p_IdEmpresa = evento.IdEmpresa; // the last event gives company, module and user (legacy)
            confirmar.p_IdModulo = evento.IdModulo;
            confirmar.p_IdUsuario = evento.IdUsuario;
            confirmar.p_Movimientos.add(new MovimientosPorAutorizar(Long.parseLong(evento.NumeroOperacionConcepto)));
        }
        try {
            SalidaConfirmarAutorizarAnularMovimientosTesoreria confirmacion = confirmarAutorizar.logica(confirmar);
            resultado(entrada, salida, confirmacion.P_OUT_MENSAJES, confirmacion.P_OUT_ERRORESCONTADOR, "OPERACION_AUTORIZADA");
        } catch (Exception e) {
            salida.P_OUT_ERRORESCONTADOR = 1L; // LEGACY DEFECT kept (see README, AutorizacionAnularMovimientosTesoreria): the failure is only a counter
        }
        log.info("TERMINO:AUTORIZACION");
        return salida;
    }

    /** Legacy typed {@code rechazarEvento}. */
    @Transactional(rollbackFor = Exception.class)
    public SalidaAutorizacion rechazar(EntradaAutorizacion entrada) {
        log.info("INICIO:RECHAZO");
        SalidaAutorizacion salida = new SalidaAutorizacion();
        EntradaConfirmarRechazarAnularMovimientosTesoreria confirmar = new EntradaConfirmarRechazarAnularMovimientosTesoreria();
        for (Evento evento : entrada.p_Lista) {
            confirmar.p_IdEmpresa = evento.IdEmpresa;
            confirmar.p_IdModulo = evento.IdModulo;
            confirmar.p_IdUsuario = evento.IdUsuario;
            confirmar.p_Movimientos.add(new MovimientosPorRechazar(Long.parseLong(evento.NumeroOperacionConcepto)));
        }
        try {
            SalidaConfirmarRechazarAnularMovimientosTesoreria confirmacion = confirmarRechazar.logica(confirmar);
            resultado(entrada, salida, confirmacion.P_OUT_MENSAJES, confirmacion.P_OUT_ERRORESCONTADOR, "AUTORIZACION_RECHAZADA");
        } catch (Exception e) {
            salida.P_OUT_ERRORESCONTADOR = 1L; // LEGACY DEFECT kept (see README, AutorizacionAnularMovimientosTesoreria): the failure is only a counter
        }
        log.info("TERMINO:RECHAZO");
        return salida;
    }

    /**
     * An event succeeded when its movimiento answered with an OK message; otherwise the confirmation's messages and counter are the output
     * (the last failing event wins, as in the legacy).
     */
    private void resultado(EntradaAutorizacion entrada, SalidaAutorizacion salida, java.util.List<MensajeSalida> mensajes, Long errores, String codigo) {
        Map<Long, String> tipoPorMovimiento = new HashMap<>();
        for (MensajeSalida m : mensajes) {
            tipoPorMovimiento.put(m.id(), m.tipo());
        }
        for (Evento evento : entrada.p_Lista) {
            long id = 0L;
            if (evento.NumeroOperacionConcepto != null && !evento.NumeroOperacionConcepto.equals("")) {
                id = Long.parseLong(evento.NumeroOperacionConcepto);
            }
            if (tipoPorMovimiento.containsKey(id) && tipoPorMovimiento.get(id).equals(MensajeSalida.MENSAJE_OK)) {
                String descripcion = diccionario.mensaje(entrada.p_IdEmpresa, codigo, new HashMap<>());
                salida.P_OUT_ERRORESCONTADOR = 0L;
                salida.P_OUT_MENSAJES.add(new MensajeSalida(0L, MensajeSalida.MENSAJE_INFORMACION, codigo, descripcion));
                ResultadoAutorizacion res = new ResultadoAutorizacion();
                res.evento = evento.IdEvento;
                res.resultado = BcsAutorizaciones.EXITO_AUTORIZACION;
                salida.P_OUT_RESULTADO.add(res);
            } else {
                salida.P_OUT_ERRORESCONTADOR = errores;
                salida.P_OUT_MENSAJES = mensajes;
            }
        }
    }
}
