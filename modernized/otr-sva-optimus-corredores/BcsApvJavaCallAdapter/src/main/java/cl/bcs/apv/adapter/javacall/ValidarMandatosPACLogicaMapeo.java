package cl.bcs.apv.adapter.javacall;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.logica.RegistroMandatoPAC;
import cl.bcs.apv.api.logica.ValidarMandatosPACLogicaEntrada;

/**
 * JSON of the TYPED in-process operation {@link #OPERACION} (legacy {@code ValidarMandatosPAC.logica(Connection, SalidaRecaudacionTraspaso, Long IdEmpresa,
 * Long IdUsuario)}) to {@link ValidarMandatosPACLogicaEntrada}. HAND WRITTEN, not generated, and NOT part of {@link ApvJavaCallRegistro} (the registry
 * keeps its 64 invocable operations and the null adapter of the JavaCall {@code ValidarMandatosPAC.call}); no route.
 *
 * <p>JSON shape (legacy Gson conventions, {@link Sabor#PLANO}):
 * <pre>
 * { "entrada":   { "P_OUT_SOLICITUDES_A_PROCESAR":     [ { "IdMandatoPAC": 1, "IdentServicio": "S1", "EstadoRendicion": "..." }, ... ],
 *                  "P_OUT_SOLICITUDES_NO_PROCESADAS": [ { "IdMandatoPAC": 2, "IdentServicio": "S2", "EstadoRendicion": "..." }, ... ],
 *                  ...any other field of SalidaRecaudacionTraspaso (P_OUT_ERRORESCONTADOR, P_OUT_MENSAJES, P_OUT_SOLICITUDES_PROCESADAS,
 *                     P_OUT_RESULTADO, other fields of the records) is accepted and ignored },
 *   "idEmpresa": 1, "idUsuario": 21 }
 * </pre>
 * The typed object may also come under the key {@code "salida"} (what the Tesoreria job already sends: the complete output of the distribution) or be
 * flat (its fields at the top level next to idEmpresa / idUsuario). An absent list keeps the legacy constructor default (empty), an absent
 * {@code IdMandatoPAC} the record default 0, absent texts "", and an explicit JSON null stays null (the legacy then throws NullPointerException).
 * The other fields of the records are NOT validated here (the legacy Gson would fail on a malformed date of an unused field).
 */
public final class ValidarMandatosPACLogicaMapeo {

    /** Operation name for {@link ApvJavaCallPipelineEnProceso#invocar}: class-name keyed like the JavaCall operations, plus the method. */
    public static final String OPERACION = "cl.bcs.APV.ValidarMandatosPAC.logica";

    private static final Sabor S = Sabor.PLANO;

    private ValidarMandatosPACLogicaMapeo() {
    }

    public static ValidarMandatosPACLogicaEntrada entradaDesdeJson(JsonObject o) {
        Long idEmpresa = MapeoJson.escalar(S, o, "idEmpresa", Long.class, null);
        Long idUsuario = MapeoJson.escalar(S, o, "idUsuario", Long.class, null);
        JsonObject t = tipada(o);
        if (t == null) { // "entrada": null: the legacy object is null, so reading its lists throws NullPointerException
            return new ValidarMandatosPACLogicaEntrada(idEmpresa, idUsuario, null, null);
        }
        return new ValidarMandatosPACLogicaEntrada(idEmpresa, idUsuario,
                MapeoJson.listaObjeto(S, t, "P_OUT_SOLICITUDES_A_PROCESAR", ValidarMandatosPACLogicaMapeo::registro, new ArrayList<>()),
                MapeoJson.listaObjeto(S, t, "P_OUT_SOLICITUDES_NO_PROCESADAS", ValidarMandatosPACLogicaMapeo::registro, new ArrayList<>()));
    }

    private static JsonObject tipada(JsonObject o) {
        for (String clave : new String[] { "entrada", "salida" }) {
            JsonElement e = o.get(clave);
            if (e != null && e.isJsonObject()) {
                return e.getAsJsonObject();
            }
            if (e != null && e.isJsonNull()) {
                return null;
            }
        }
        return o;
    }

    private static RegistroMandatoPAC registro(JsonObject x) {
        RegistroMandatoPAC d = RegistroMandatoPAC.porDefecto();
        return new RegistroMandatoPAC(
                MapeoJson.escalar(S, x, "IdMandatoPAC", Long.class, d.idMandatoPAC()),
                MapeoJson.escalar(S, x, "IdentServicio", String.class, d.identServicio()),
                MapeoJson.escalar(S, x, "EstadoRendicion", String.class, d.estadoRendicion()));
    }
}
