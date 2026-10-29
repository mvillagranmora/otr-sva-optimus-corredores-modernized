package cl.bcs.fondomutuo.manejador;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.MensajeSalida;

/**
 * What the Manejador calls for each of the SEVEN destinations that are not served in F1 (legacy {@code CrearSolicitudAporteRescateProgramado},
 * {@code ...Overnight}, {@code ...Reinversion}, {@code CrearSolicitudCambioSerie}, {@code CrearSolicitudRescateReinversion},
 * {@code ...RescateTransferencia}, {@code ...RescateTraspaso}): legacy {@code new X().logica(con, entrada)}. The connection is not a parameter (the
 * unit of work of the call is the ambient transaction). The request carries the common legacy fields and the lists as raw JSON ({@code aportes} =
 * {@code p_SolicitudesAporte}, {@code rescates} = {@code p_SolicitudesRescate}; each destination reads the one it has). F1 implementation:
 * {@link TipoNoServido} (503). F2 adds one implementation per wave and lifts the gate of its list (PLAN_FONDOMUTUO_F2 6).
 */
public interface TipoSolicitudPort {

    Respuesta logica(Pedido pedido) throws Exception;

    /** Legacy {@code EntradaCrearSolicitud*}: a single mutable object, the same one is used in the simulation pass and in the real pass. */
    class Pedido {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_SimularOperacion;
        public String p_UsaReglasNegocio;
        public String p_IdTipoCanalIngreso;
        public List<JsonObject> p_SolicitudesAporte;
        public List<JsonObject> p_SolicitudesRescate;
        /**
         * F2: opaque state of the destination shared by the simulation and the real pass of ONE call (the legacy kept one typed input object for both,
         * so what the simulation mutates reaches the real pass). Never serialized, never kept after the call.
         */
        public transient Object estadoDestino;
    }

    /** Legacy {@code SalidaCrearSolicitud*}. */
    class Respuesta {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<MensajeSalida> P_OUT_MENSAJES = new ArrayList<>();
        public List<ResultadoSolicitud> P_OUT_RESULTADO = new ArrayList<>();
    }
}
