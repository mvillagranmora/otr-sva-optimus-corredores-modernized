package cl.bcs.fondomutuo.rescatereinversion;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.kit.JsonLegacy;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.Entrada;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesAporte;
import cl.bcs.fondomutuo.rescatereinversion.RescateReinversionContrato.InSolicitudesRescate;

/**
 * The Manejador destination {@code RESCATE_REINVERSION} (legacy {@code ManejadorSolicitudAporteRescate:236-260}): {@code new CrearSolicitudRescateReinversion()
 * .logica(con, in)} with ONE typed input object for the simulation and the real pass. The legacy Manejador read the lists with its plain {@code new Gson()},
 * so each JSON row is decoded once with {@link JsonLegacy#plano()} and the same typed row is reused in the real pass (identity map kept in the
 * {@link TipoSolicitudPort.Pedido} of the call): what the simulation mutates ({@code TipoReinversion}, {@code IdSerieFondoOrigen}, {@code IdBloqueoRescate},
 * {@code SolicitudBatch}) is seen by the real pass, as in the legacy. {@code p_IdOrigenProceso} keeps its constructor default "" (not propagated, D-MAN-6).
 */
public final class RescateReinversionDestino implements TipoSolicitudPort {

    private final RescateReinversionPort servicio;

    public RescateReinversionDestino(RescateReinversionPort servicio) {
        this.servicio = servicio;
    }

    private static final class Estado {
        final Entrada entrada = new Entrada();
        final IdentityHashMap<JsonObject, InSolicitudesRescate> rescates = new IdentityHashMap<>();
        final IdentityHashMap<JsonObject, InSolicitudesAporte> aportes = new IdentityHashMap<>();
    }

    @Override
    public Respuesta logica(Pedido pedido) throws Exception {
        Estado e = pedido.estadoDestino instanceof Estado ? (Estado) pedido.estadoDestino : new Estado();
        pedido.estadoDestino = e;
        Entrada in = e.entrada;
        in.p_IdEmpresa = pedido.p_IdEmpresa;
        in.p_IdUsuario = pedido.p_IdUsuario;
        in.p_IdModulo = pedido.p_IdModulo;
        in.p_SimularOperacion = pedido.p_SimularOperacion;
        in.p_UsaReglasNegocio = pedido.p_UsaReglasNegocio;
        in.p_IdTipoCanalIngreso = pedido.p_IdTipoCanalIngreso;
        in.p_SolicitudesRescate = tipar(pedido.p_SolicitudesRescate, e.rescates, InSolicitudesRescate.class);
        in.p_SolicitudesAporte = tipar(pedido.p_SolicitudesAporte, e.aportes, InSolicitudesAporte.class);
        RescateReinversionContrato.Salida salida = servicio.logica(in);
        Respuesta r = new Respuesta();
        r.P_OUT_ERRORESCONTADOR = salida.P_OUT_ERRORESCONTADOR;
        r.P_OUT_MENSAJES = salida.P_OUT_MENSAJES;
        r.P_OUT_RESULTADO = salida.P_OUT_RESULTADO;
        return r;
    }

    private static <T> List<T> tipar(List<JsonObject> filas, IdentityHashMap<JsonObject, T> vistos, Class<T> tipo) {
        if (filas == null) return null;
        List<T> salida = new ArrayList<>();
        for (JsonObject f : filas) {
            salida.add(vistos.computeIfAbsent(f, x -> JsonLegacy.plano().fromJson(x, tipo)));
        }
        return salida;
    }
}
