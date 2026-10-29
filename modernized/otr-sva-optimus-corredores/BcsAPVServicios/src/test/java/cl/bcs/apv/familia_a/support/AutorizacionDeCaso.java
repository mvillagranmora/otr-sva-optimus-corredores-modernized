package cl.bcs.apv.familia_a.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import cl.bcs.apv.api.casouso.CambiarContratosAPlanActivo;
import cl.bcs.apv.api.comun.Mensaje;
import cl.bcs.apv.application.ApvCasosDeUso;
import cl.bcs.apv.application.autorizaciones.AutorizacionCambioPlanActivoServicio;
import cl.bcs.apv.application.autorizaciones.EntradaAutorizacion;
import cl.bcs.apv.application.autorizaciones.EventoAutorizacion;
import cl.bcs.apv.application.autorizaciones.ResultadoAutorizacion;
import cl.bcs.apv.application.autorizaciones.SalidaAutorizacion;
import cl.bcs.apv.infrastructure.persistence.contratos.EventoAutorizacionJdbc;

/**
 * Runs the authorization hook of the plan change (legacy {@code AutorizacionCambiarAPlanActivoContratoSAAPV}, interface {@code BcsAutorizaciones}) for a case.
 * The hook is not a JavaCall operation (no entry in the registry, so no {@code pipeline.invocar}): the legacy platform instantiated it and called
 * {@code autorizarEvento} / {@code rechazarEvento} in process. Here the typed service is built on the case's H2 with the same ports and the REAL
 * {@code CambiarContratosAPlanActivo} of the composition root; the legacy JSON of the case ({@code EntradaAutorizacion}: {@code p_IdEmpresa}, {@code p_IdUsuario},
 * {@code p_IdModulo}, {@code p_UsaReglasNegocio}, {@code p_Lista[Evento]}) is read into the typed input and the typed output is written as the legacy
 * {@code SalidaAutorizacion} ({@code P_OUT_MENSAJES}, {@code P_OUT_ERRORESCONTADOR}, {@code P_OUT_RESULTADO}) so it can be compared with the golden.
 */
public final class AutorizacionDeCaso {

    private AutorizacionDeCaso() {
    }

    public static AutorizacionCambioPlanActivoServicio servicio(Dobles d, Caso caso) {
        Map<Class<?>, Object> casos = ApvCasosDeUso.crear(caso.jdbc(), d.configuracion(), d.diccionario(), d.secuencias(), d.reloj(), d.archivos(),
                d.colaboradores());
        return new AutorizacionCambioPlanActivoServicio(d.diccionario(), (CambiarContratosAPlanActivo) casos.get(CambiarContratosAPlanActivo.class),
                new EventoAutorizacionJdbc(caso.jdbc()));
    }

    /** The legacy entry point of the case ({@code autorizarEvento} or {@code rechazarEvento}) on the typed service, answer as legacy JSON. */
    public static JsonElement invocar(Dobles d, Caso caso) {
        AutorizacionCambioPlanActivoServicio servicio = servicio(d, caso);
        EntradaAutorizacion entrada = entrada(caso.entrada());
        SalidaAutorizacion salida = switch (caso.metodo()) {
            case "autorizarEvento" -> servicio.autorizarEvento(entrada);
            case "rechazarEvento" -> servicio.rechazarEvento(entrada);
            default -> throw new IllegalStateException("not a BcsAutorizaciones method: " + caso.metodo());
        };
        return salidaAJson(salida);
    }

    private static Long largo(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsLong() : null;
    }

    private static String texto(JsonObject o, String k) {
        return o.has(k) && !o.get(k).isJsonNull() ? o.get(k).getAsString() : null;
    }

    static EntradaAutorizacion entrada(JsonObject j) {
        // legacy Gson: a missing p_Lista keeps the constructor default (empty list), a JSON null is null
        List<EventoAutorizacion> lista = new ArrayList<>();
        if (j.has("p_Lista")) {
            lista = null;
            if (!j.get("p_Lista").isJsonNull()) {
                lista = new ArrayList<>();
                for (JsonElement e : j.getAsJsonArray("p_Lista")) {
                    if (e.isJsonNull()) {
                        lista.add(null);
                        continue;
                    }
                    JsonObject o = e.getAsJsonObject();
                    lista.add(new EventoAutorizacion(largo(o, "IdEvento"), largo(o, "IdEmpresa"), texto(o, "IdModulo"), largo(o, "IdUsuario"),
                            largo(o, "IdCliente")));
                }
            }
        }
        return new EntradaAutorizacion(largo(j, "p_IdEmpresa"), largo(j, "p_IdUsuario"), texto(j, "p_IdModulo"), texto(j, "p_UsaReglasNegocio"), lista);
    }

    static JsonObject salidaAJson(SalidaAutorizacion s) {
        JsonObject o = new JsonObject();
        JsonArray mensajes = new JsonArray();
        for (Mensaje m : s.mensajes()) {
            JsonObject mj = new JsonObject();
            mj.addProperty("id", m.id());
            mj.addProperty("tipo", m.tipo());
            mj.addProperty("codigo", m.codigo());
            mj.addProperty("descripcion", m.descripcion());
            mensajes.add(mj);
        }
        o.add("P_OUT_MENSAJES", mensajes);
        o.addProperty("P_OUT_ERRORESCONTADOR", s.contadorErrores());
        JsonArray resultados = new JsonArray();
        for (ResultadoAutorizacion r : s.resultado()) {
            JsonObject rj = new JsonObject();
            rj.addProperty("evento", r.evento());
            rj.addProperty("resultado", r.resultado());
            resultados.add(rj);
        }
        o.add("P_OUT_RESULTADO", resultados);
        return o;
    }
}
