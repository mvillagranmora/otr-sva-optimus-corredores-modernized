package cl.bcs.plataforma.internal;

import cl.bcs.apv.adapter.javacall.ApvJavaCallPipelineEnProceso;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro;
import cl.bcs.apv.adapter.javacall.ApvJavaCallRegistro.OperacionRegistrada;
import cl.bcs.apv.adapter.javacall.MapeoOperacion;
import cl.bcs.apv.api.CasoUso;
import org.springframework.core.ResolvableType;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * APV: legacy class -> typed REST route of apv-service through the registry of {@code bcs-apv-javacall-adapter}.
 * The legacy params are mapped to the typed Entrada with the adapter's own mapper (same defaults as the in-process path),
 * sent as JSON (POST/PUT) or as query string of flat scalars (GET), and the typed Resultado of a 2xx answer is mapped back to
 * the legacy JSON. A non-2xx answer goes through untouched. Classes without a typed route (the registry marks them not
 * invocable, or they are missing) are {@link NoRuteableAun}; {@code ValidarMandatosPAC.logica} is rejected before this point.
 */
public class ApvRestEstrategia implements EstrategiaDueno {

    private static final ApvJavaCallRegistro REGISTRO = new ApvJavaCallRegistro();
    private final JsonMapper json = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES).build();

    /** The registry entry that serves {@code clase}, or null when there is no typed route (documented marker). */
    public static OperacionRegistrada operacion(String clase) {
        return REGISTRO.buscar(clase).filter(OperacionRegistrada::invocable).orElse(null);
    }

    /** Operations that exist only in process and never over HTTP ({@code cl.bcs.APV.ValidarMandatosPAC.logica}). */
    public static boolean soloEnProceso(String clase) {
        return ApvJavaCallPipelineEnProceso.esOperacionTipada(clase);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Respuesta invocar(ClienteInterno http, String urlBase, SolicitudJavaCall s) {
        OperacionRegistrada op = operacion(s.clase());
        if (op == null) {
            throw new NoRuteableAun("APV class without typed REST route: " + s.clase());
        }
        MapeoOperacion mapeo = op.mapeo();
        Object entrada = mapeo.entradaDesdeJson(s.params());
        Respuesta r = switch (op.verbo()) {
            case "GET" -> http.get(consulta(urlBase + op.ruta(), entrada));
            case "PUT" -> http.put(urlBase + op.ruta(), json.writeValueAsString(entrada));
            default -> http.post(urlBase + op.ruta(), json.writeValueAsString(entrada), true, null);
        };
        if (r.status() < 200 || r.status() >= 300 || r.cuerpo() == null || r.cuerpo().isBlank()) {
            return r;
        }
        Class<?> tipoResultado = ResolvableType.forClass(op.interfaz()).as(CasoUso.class).getGeneric(1).resolve();
        Object resultado = json.readValue(r.cuerpo(), tipoResultado);
        return new Respuesta(r.status(), "application/json", mapeo.resultadoAJson(resultado).toString());
    }

    private java.net.URI consulta(String ruta, Object entrada) {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(ruta);
        for (Map.Entry<String, JsonNode> e : json.valueToTree(entrada).properties()) {
            JsonNode v = e.getValue();
            if (v.isNull()) continue;
            if (v.isContainer()) throw new NoRuteableAun("APV GET route needs flat scalars: " + e.getKey());
            b.queryParam(e.getKey(), v.asString());
        }
        return b.build().encode().toUri();
    }
}
