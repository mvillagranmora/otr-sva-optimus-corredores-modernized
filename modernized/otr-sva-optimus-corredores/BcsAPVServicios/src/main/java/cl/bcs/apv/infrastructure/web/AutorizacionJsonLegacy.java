package cl.bcs.apv.infrastructure.web;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import cl.bcs.apv.adapter.javacall.MapeoJson;
import cl.bcs.apv.adapter.javacall.MensajeMapeo;
import cl.bcs.apv.adapter.javacall.Sabor;
import cl.bcs.apv.application.autorizaciones.EntradaAutorizacion;
import cl.bcs.apv.application.autorizaciones.EventoAutorizacion;
import cl.bcs.apv.application.autorizaciones.ResultadoAutorizacion;
import cl.bcs.apv.application.autorizaciones.SalidaAutorizacion;

/**
 * Legacy JSON of the authorization hook ({@code BcsAutorizaciones}): {@code EntradaAutorizacion} ({@code p_IdEmpresa}, {@code p_IdUsuario}, {@code p_IdModulo},
 * {@code p_UsaReglasNegocio}, {@code p_Lista} of platform {@code Evento} with its capitalized field names) in, {@code SalidaAutorizacion}
 * ({@code P_OUT_MENSAJES}, {@code P_OUT_ERRORESCONTADOR}, {@code P_OUT_RESULTADO}) out. Same Gson flavour ({@link Sabor#PLANO}, {@code new Gson()}: nulls omitted)
 * and same {@link MensajeMapeo} as the JavaCall operations, so the messages serialize exactly as their {@code P_OUT_MENSAJES}. The platform fields of the
 * Evento that the hook does not read are ignored.
 */
public final class AutorizacionJsonLegacy {

    private static final Sabor S = Sabor.PLANO;

    private AutorizacionJsonLegacy() {
    }

    /** Parses the request text; anything that is not a JSON object is a {@link JsonSyntaxException} (HTTP 400). */
    public static EntradaAutorizacion entradaDesdeTexto(String texto) {
        JsonElement e = JsonParser.parseString(texto == null ? "" : texto);
        if (!e.isJsonObject()) {
            throw new JsonSyntaxException("Expected a JSON object");
        }
        return entradaDesdeJson(e.getAsJsonObject());
    }

    public static EntradaAutorizacion entradaDesdeJson(JsonObject o) {
        // legacy Gson: a missing p_Lista keeps the constructor default (empty list), a JSON null is null
        return new EntradaAutorizacion(
                MapeoJson.escalar(S, o, "p_IdEmpresa", Long.class, null),
                MapeoJson.escalar(S, o, "p_IdUsuario", Long.class, null),
                MapeoJson.escalar(S, o, "p_IdModulo", String.class, null),
                MapeoJson.escalar(S, o, "p_UsaReglasNegocio", String.class, null),
                MapeoJson.listaObjeto(S, o, "p_Lista", AutorizacionJsonLegacy::leerEvento, new ArrayList<>()));
    }

    private static EventoAutorizacion leerEvento(JsonObject o) {
        return new EventoAutorizacion(
                MapeoJson.escalar(S, o, "IdEvento", Long.class, null),
                MapeoJson.escalar(S, o, "IdEmpresa", Long.class, null),
                MapeoJson.escalar(S, o, "IdModulo", String.class, null),
                MapeoJson.escalar(S, o, "IdUsuario", Long.class, null),
                MapeoJson.escalar(S, o, "IdCliente", Long.class, null));
    }

    public static JsonObject salidaAJson(SalidaAutorizacion s) {
        JsonObject j = new JsonObject();
        MapeoJson.ponerListaObjeto(S, j, "P_OUT_MENSAJES", s.mensajes(), m -> MensajeMapeo.aJson(S, m));
        MapeoJson.poner(S, j, "P_OUT_ERRORESCONTADOR", s.contadorErrores());
        MapeoJson.ponerListaObjeto(S, j, "P_OUT_RESULTADO", s.resultado(), AutorizacionJsonLegacy::escribirResultado);
        return j;
    }

    public static String salidaATexto(SalidaAutorizacion s) {
        return S.gson().toJson(salidaAJson(s));
    }

    private static JsonObject escribirResultado(ResultadoAutorizacion r) {
        JsonObject j = new JsonObject();
        MapeoJson.poner(S, j, "evento", r.evento());
        MapeoJson.poner(S, j, "resultado", r.resultado());
        return j;
    }
}
