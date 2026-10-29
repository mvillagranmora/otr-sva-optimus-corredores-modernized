package cl.bcs.fondomutuo.manejador;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.fondomutuo.cierre.ListaHash;
import cl.bcs.fondomutuo.comisionnn.ResultadoSolicitud;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Entrada;
import cl.bcs.fondomutuo.manejador.ManejadorContrato.Salida;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort.Pedido;
import cl.bcs.fondomutuo.manejador.TipoSolicitudPort.Respuesta;
import cl.bcs.fondomutuo.normal.NormalContrato;
import cl.bcs.fondomutuo.normal.NormalPort;

/**
 * Legacy {@code ManejadorSolicitudAporteRescate.logica} (id 601190; 337 LOC): a facade without business rules of its own. For every list that has rows,
 * in the fixed order Normal, Programado, Overnight, Reinversion (aporte), CambioSerie, RescateReinversion, RescateTransferencia, RescateTraspaso, it
 * (1) calls the destination with {@code p_SimularOperacion="S"}, (2) adds the messages of the simulation to the output, (3) keeps only the requests
 * whose {@code IdInterno} is in the result of the simulation, (4) if any is left calls it again with "N" and adds ITS counter and results (the messages
 * of the real pass are dropped: D-MAN-5; a simulation that rejects everything leaves the counter at 0: D-MAN-1). CambioSerie and RescateReinversion
 * need good contributions AND good rescues to run the real pass (D-MAN-7). One request object is shared by both passes, as in the legacy.
 *
 * <p>Only the NORMAL list is served in F1: gate G0 ({@code g0}, the property {@code fondomutuo.solicitud.listas-servidas}) is evaluated FIRST over every
 * list with rows, before the first call, so a mixed request answers 503 with no side effect and no sequence consumed (DEV-F4). The dispatch of the other
 * seven is written in full over {@link TipoSolicitudPort}. No state between calls; no logging (DEV-4B-3); sequential streams where the legacy used
 * {@code parallelStream} (same order, no side effects).
 */
public class ManejadorSolicitudAporteRescateService {

    /** Lists as named by the gate (properties of {@code Compuertas}). */
    public enum Lista { NORMAL, PROGRAMADO, OVERNIGHT, REINVERSION_APORTE, CAMBIO_SERIE, RESCATE_REINVERSION, TRANSFERENCIA, TRASPASO }

    private final NormalPort normal;
    private final Map<Lista, TipoSolicitudPort> destinos;
    private final Consumer<Set<String>> g0;

    public ManejadorSolicitudAporteRescateService(NormalPort normal, Map<Lista, TipoSolicitudPort> destinos, Consumer<Set<String>> g0) {
        this.normal = normal;
        this.destinos = new EnumMap<>(destinos);
        this.g0 = g0;
    }

    private static boolean filas(List<?> l) {
        return l != null && l.size() > 0;   // a null list is NOT a gate matter: the legacy NPE happens at its turn
    }

    /** Lists with rows, by gate name (G0 input). */
    static Set<String> conFilas(Entrada e) {
        Set<Lista> s = EnumSet.noneOf(Lista.class);
        if (filas(e.p_SolicitudesAporteRescateNormal)) s.add(Lista.NORMAL);
        if (filas(e.p_SolicitudesAporteRescateProgramado)) s.add(Lista.PROGRAMADO);
        if (filas(e.p_SolicitudAporteOvernight)) s.add(Lista.OVERNIGHT);
        if (filas(e.p_SolicitudAporteReinversion)) s.add(Lista.REINVERSION_APORTE);
        if (filas(e.p_SolicitudCambioSerieRescate) || filas(e.p_SolicitudCambioSerieAporte)) s.add(Lista.CAMBIO_SERIE);
        if (filas(e.p_SolicitudRescateReinversionRescate) || filas(e.p_SolicitudRescateReinversionAporte)) s.add(Lista.RESCATE_REINVERSION);
        if (filas(e.p_SolicitudRescateTransferencia)) s.add(Lista.TRANSFERENCIA);
        if (filas(e.p_SolicitudRescateTraspaso)) s.add(Lista.TRASPASO);
        return s.stream().map(Enum::name).collect(Collectors.toSet());
    }

    public Salida logica(Entrada entrada) throws Exception {
        g0.accept(conFilas(entrada));
        Salida out = new Salida();
        try {
            if (entrada.p_SolicitudesAporteRescateNormal.size() > 0) pasoNormal(entrada, out);
            if (entrada.p_SolicitudesAporteRescateProgramado.size() > 0) {
                unaLista(Lista.PROGRAMADO, entrada, entrada.p_SolicitudesAporteRescateProgramado, false, out);
            }
            if (entrada.p_SolicitudAporteOvernight.size() > 0) unaLista(Lista.OVERNIGHT, entrada, entrada.p_SolicitudAporteOvernight, false, out);
            if (entrada.p_SolicitudAporteReinversion.size() > 0) {
                unaLista(Lista.REINVERSION_APORTE, entrada, entrada.p_SolicitudAporteReinversion, false, out);
            }
            if (entrada.p_SolicitudCambioSerieRescate.size() > 0 || entrada.p_SolicitudCambioSerieAporte.size() > 0) {
                dosListas(Lista.CAMBIO_SERIE, entrada, entrada.p_SolicitudCambioSerieAporte, entrada.p_SolicitudCambioSerieRescate, out);
            }
            if (entrada.p_SolicitudRescateReinversionRescate.size() > 0 || entrada.p_SolicitudRescateReinversionAporte.size() > 0) {
                dosListas(Lista.RESCATE_REINVERSION, entrada, entrada.p_SolicitudRescateReinversionAporte,
                        entrada.p_SolicitudRescateReinversionRescate, out);
            }
            if (entrada.p_SolicitudRescateTransferencia.size() > 0) {
                unaLista(Lista.TRANSFERENCIA, entrada, entrada.p_SolicitudRescateTransferencia, true, out);
            }
            if (entrada.p_SolicitudRescateTraspaso.size() > 0) unaLista(Lista.TRASPASO, entrada, entrada.p_SolicitudRescateTraspaso, true, out);
            return out;
        } catch (Exception error) {
            out.P_OUT_ERRORESCONTADOR = 1L;   // the legacy sets it and rethrows: the body with the counter is never sent (D-MAN-4)
            throw error;
        }
    }

    private void pasoNormal(Entrada entrada, Salida out) throws Exception {
        NormalContrato.Entrada in = new NormalContrato.Entrada();
        in.p_IdEmpresa = entrada.p_IdEmpresa;
        in.p_IdUsuario = entrada.p_IdUsuario;
        in.p_IdModulo = entrada.p_IdModulo;
        in.p_SimularOperacion = "S";
        in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;   // null overwrites the "OPT" default (D-MAN-6)
        in.p_SolicitudesAporteRescate = entrada.p_SolicitudesAporteRescateNormal;
        in.p_FormaPagoSpot = entrada.p_FormaPagoSpot;
        NormalContrato.Salida salida = normal.logica(in);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        HashMap<Long, ResultadoSolicitud> resultado = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);
        List<NormalContrato.InSolicitud> buenos = in.p_SolicitudesAporteRescate.stream().filter(s -> resultado.get(s.IdInterno) != null)
                .collect(Collectors.toList());
        // NullPointerException when p_FormaPagoSpot did not come (D-MAN-2), AFTER the simulation pass, as in the legacy
        List<NormalContrato.InFormaPagoSpot> fp = in.p_FormaPagoSpot.stream().filter(f -> resultado.get(f.IdOperacion) != null)
                .collect(Collectors.toList());
        if (buenos.size() > 0) {
            in.p_SolicitudesAporteRescate = buenos;
            in.p_FormaPagoSpot = fp;
            in.p_SimularOperacion = "N";
            salida = normal.logica(in);
            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
            out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
        }
    }

    private Pedido pedido(Entrada entrada) {
        Pedido in = new Pedido();
        in.p_IdEmpresa = entrada.p_IdEmpresa;
        in.p_IdUsuario = entrada.p_IdUsuario;
        in.p_IdModulo = entrada.p_IdModulo;
        in.p_SimularOperacion = "S";
        in.p_UsaReglasNegocio = entrada.p_UsaReglasNegocio;
        in.p_IdTipoCanalIngreso = entrada.p_IdTipoCanalIngreso;
        return in;
    }

    private static Long idInterno(JsonObject o) {
        JsonElement x = o.get("IdInterno");
        return x == null || x.isJsonNull() ? null : x.getAsLong();
    }

    private static List<JsonObject> buenas(List<JsonObject> l, Map<Long, ResultadoSolicitud> resultado) {
        return l.stream().filter(s -> resultado.get(idInterno(s)) != null).collect(Collectors.toList());
    }

    /** One list: Programado / Overnight / Reinversion send it as {@code p_SolicitudesAporte}, Transferencia / Traspaso as {@code p_SolicitudesRescate}. */
    private void unaLista(Lista tipo, Entrada entrada, List<JsonObject> lista, boolean comoRescate, Salida out) throws Exception {
        TipoSolicitudPort servicio = destinos.get(tipo);
        Pedido in = pedido(entrada);
        if (comoRescate) in.p_SolicitudesRescate = lista; else in.p_SolicitudesAporte = lista;
        Respuesta salida = servicio.logica(in);
        HashMap<Long, ResultadoSolicitud> resultado = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);
        List<JsonObject> buenos = buenas(comoRescate ? in.p_SolicitudesRescate : in.p_SolicitudesAporte, resultado);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        if (buenos.size() > 0) {
            in.p_SimularOperacion = "N";
            if (comoRescate) in.p_SolicitudesRescate = buenos; else in.p_SolicitudesAporte = buenos;
            salida = servicio.logica(in);
            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
            out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
        }
    }

    /** CambioSerie / RescateReinversion: the real pass needs good contributions AND good rescues (D-MAN-7). */
    private void dosListas(Lista tipo, Entrada entrada, List<JsonObject> aportes, List<JsonObject> rescates, Salida out) throws Exception {
        TipoSolicitudPort servicio = destinos.get(tipo);
        Pedido in = pedido(entrada);
        in.p_SolicitudesRescate = rescates;
        in.p_SolicitudesAporte = aportes;
        Respuesta salida = servicio.logica(in);
        out.P_OUT_MENSAJES.addAll(salida.P_OUT_MENSAJES);
        HashMap<Long, ResultadoSolicitud> resultado = ListaHash.aHash(salida.P_OUT_RESULTADO, r -> r.IdInterno);
        List<JsonObject> aportesBuenos = buenas(in.p_SolicitudesAporte, resultado);
        List<JsonObject> rescatesBuenos = buenas(in.p_SolicitudesRescate, resultado);
        if (aportesBuenos.size() > 0 && rescatesBuenos.size() > 0) {
            in.p_SimularOperacion = "N";
            in.p_SolicitudesRescate = rescatesBuenos;
            in.p_SolicitudesAporte = aportesBuenos;
            salida = servicio.logica(in);
            out.P_OUT_ERRORESCONTADOR += salida.P_OUT_ERRORESCONTADOR;
            out.P_OUT_RESULTADO.addAll(salida.P_OUT_RESULTADO);
        }
    }
}
