package cl.bcs.fondomutuo.manejador;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.kit.Compuertas;
import cl.bcs.fondomutuo.kit.MensajeSalida;
import cl.bcs.fondomutuo.manejador.ManejadorSolicitudAporteRescateService.Lista;
import cl.bcs.fondomutuo.normal.NormalContrato;
import cl.bcs.fondomutuo.normal.NormalPort;

/** Fixtures of the Manejador tests: scripted Normal and scripted destinations that share ONE call log (order across destinations is observable). */
final class Banco {

    static final Set<String> TODAS = Set.of("NORMAL", "PROGRAMADO", "OVERNIGHT", "REINVERSION_APORTE", "CAMBIO_SERIE", "RESCATE_REINVERSION",
            "TRANSFERENCIA", "TRASPASO");

    final List<String> log = new ArrayList<>();
    final List<NormalContrato.Entrada> entradasNormal = new ArrayList<>();
    final List<TipoSolicitudPort.Pedido> pedidos = new ArrayList<>();
    /** answer of Normal: (simulation?, request) -> output; default: every request accepted */
    BiFunction<Boolean, NormalContrato.Entrada, NormalContrato.Salida> normal = (sim, in) -> salidaNormal(0, in.p_SolicitudesAporteRescate.stream().map(s -> s.IdInterno).toList());
    final Map<Lista, BiFunction<Boolean, TipoSolicitudPort.Pedido, TipoSolicitudPort.Respuesta>> destinos = new EnumMap<>(Lista.class);

    static NormalContrato.Salida salidaNormal(long contador, List<Long> ids, MensajeSalida... mensajes) {
        NormalContrato.Salida s = new NormalContrato.Salida();
        s.P_OUT_ERRORESCONTADOR = contador;
        s.P_OUT_MENSAJES.addAll(List.of(mensajes));
        for (Long id : ids) s.P_OUT_RESULTADO.add(resultado(id));
        return s;
    }

    static TipoSolicitudPort.Respuesta respuesta(long contador, List<Long> ids, MensajeSalida... mensajes) {
        TipoSolicitudPort.Respuesta r = new TipoSolicitudPort.Respuesta();
        r.P_OUT_ERRORESCONTADOR = contador;
        r.P_OUT_MENSAJES.addAll(List.of(mensajes));
        for (Long id : ids) r.P_OUT_RESULTADO.add(resultado(id));
        return r;
    }

    static ResultadoSolicitud resultado(Long idInterno) {
        ResultadoSolicitud r = new ResultadoSolicitud();
        r.IdInterno = idInterno;
        return r;
    }

    static MensajeSalida error(long id, String codigo) {
        return new MensajeSalida(id, MensajeSalida.MENSAJE_ERROR, codigo, codigo + " texto");
    }

    static NormalContrato.InSolicitud sol(long id) {
        NormalContrato.InSolicitud s = new NormalContrato.InSolicitud();
        s.IdInterno = id;
        return s;
    }

    static NormalContrato.InFormaPagoSpot fp(long idOperacion) {
        NormalContrato.InFormaPagoSpot f = new NormalContrato.InFormaPagoSpot();
        f.IdOperacion = idOperacion;
        return f;
    }

    static JsonObject json(long id) {
        JsonObject o = new JsonObject();
        o.addProperty("IdInterno", id);
        return o;
    }

    static ManejadorContrato.Entrada entrada() {
        ManejadorContrato.Entrada e = new ManejadorContrato.Entrada();
        e.p_IdEmpresa = 1L;
        e.p_IdUsuario = 7L;
        e.p_IdModulo = "FMM";
        e.p_UsaReglasNegocio = "S";
        e.p_IdTipoCanalIngreso = "OPT";
        e.p_FormaPagoSpot = new ArrayList<>();
        return e;
    }

    static String ids(List<JsonObject> l) {
        return l == null ? "null" : l.stream().map(o -> o.get("IdInterno").getAsString()).collect(Collectors.joining(",", "[", "]"));
    }

    NormalPort puertoNormal() {
        return in -> {
            boolean sim = "S".equals(in.p_SimularOperacion);
            log.add("NORMAL:" + in.p_SimularOperacion + in.p_SolicitudesAporteRescate.stream().map(s -> String.valueOf(s.IdInterno)).collect(Collectors.joining(",", "[", "]")));
            entradasNormal.add(in);
            return normal.apply(sim, in);
        };
    }

    ManejadorSolicitudAporteRescateService servicio(Set<String> servidas) {
        Map<Lista, TipoSolicitudPort> puertos = new EnumMap<>(Lista.class);
        for (Lista l : Lista.values()) {
            if (l == Lista.NORMAL) continue;
            puertos.put(l, p -> {
                log.add(l + ":" + p.p_SimularOperacion + ":A" + ids(p.p_SolicitudesAporte) + ":R" + ids(p.p_SolicitudesRescate));
                pedidos.add(p);
                return destinos.getOrDefault(l, (sim, ped) -> respuesta(0, List.of())).apply("S".equals(p.p_SimularOperacion), p);
            });
        }
        Compuertas g = new Compuertas(String.join(",", servidas), false, false, false, false, false);
        return new ManejadorSolicitudAporteRescateService(puertoNormal(), puertos, g::g0ListasConFilas);
    }
}
