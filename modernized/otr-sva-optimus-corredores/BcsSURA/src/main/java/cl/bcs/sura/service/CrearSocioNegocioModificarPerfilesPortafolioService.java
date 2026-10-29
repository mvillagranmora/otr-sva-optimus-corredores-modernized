package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Spring replacement for legacy cl.bcs.sura.socionegocio.CrearSocioNegocioModificarPerfilesPortafolio.
 *
 * Receives the merged jin+jout params from InternalJavaCallController and:
 *  1. Queries portfolio IDs for each CLIENTE socio-negocio
 *  2. Resolves profile config from COBROCUST indicators
 *  3. Inserts/updates COBROCUST indicators in HANA
 *  4. Updates portfolio profiles in HANA
 *  5. Returns the updated output JsonObject (adds errors if profiles not found)
 */
@Service
public class CrearSocioNegocioModificarPerfilesPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(
            CrearSocioNegocioModificarPerfilesPortafolioService.class);

    private final PerfilesPortafolioUtilsService utils;

    public CrearSocioNegocioModificarPerfilesPortafolioService(PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::CrearSocioNegocioModificarPerfilesPortafolio");

        long idEmpresa  = longOf(params, "p_IdEmpresa", 1L);
        JsonArray sociosNegocioArr = arrOf(params, "p_SocioNegocio");

        // Collect documentosIdentidad for CLIENTE-type socios
        List<String> documentosIdentidad = new ArrayList<>();
        List<JsonObject> sociosNegocio   = new ArrayList<>();
        for (JsonElement el : sociosNegocioArr) {
            JsonObject soc = el.getAsJsonObject();
            sociosNegocio.add(soc);
            if ("CLIENTE".equals(strOf(soc, "TipoEntidad"))) {
                documentosIdentidad.add(strOf(soc, "DocumentoIdentidad"));
            }
        }

        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(
                documentosIdentidad);

        ids = utils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(sociosNegocio, ids);

        // Build output — start from jout already in params
        long   erroresContador = longOf(params, "P_OUT_ERRORESCONTADOR", 0L);
        JsonArray entidadesCreadas = arrOf(params, "p_Out_EntidadCreada");

        if (!ids.isEmpty()) {
            for (IdsEntidadYPerfiles id : ids) {
                if (id.idPerfilComercial.isEmpty() || id.idPerfilProductos.isEmpty()
                        || id.idPerfilCobros.isEmpty()) {
                    entidadesCreadas.add(buildErrorEntry(
                            "PORTAFOLIO", id.idPortafolio,
                            "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS",
                            id.idFormaCobro));
                    erroresContador++;
                }
            }

            if (erroresContador == 0) {
                // Ingresar indicators por cada socio
                for (JsonObject socio : sociosNegocio) {
                    String docId = strOf(socio, "DocumentoIdentidad");
                    List<IdsEntidadYPerfiles> perfiles = ids.stream()
                            .filter(id -> docId.equals(id.documentoIdentidad))
                            .collect(Collectors.toList());

                    Optional<String> valorIndicador = cobrocustValor(socio, "IndicadorPortafolio");
                    if (valorIndicador.isPresent() && !perfiles.isEmpty()) {
                        String valor = valorIndicador.get();
                        perfiles.forEach(p -> p.idFormaCobro = valor);
                        utils.ingresarIndicadoresCobroCustodia(perfiles, valor);
                    }
                }

                utils.modificarIndicadoresCobroCustodia(ids);
                utils.modificarPerfilesPortafolio(ids);
            }
        }

        // Merge updated fields back to output
        JsonObject out = new JsonObject();
        copyAllFields(params, out);
        out.addProperty("P_OUT_ERRORESCONTADOR", erroresContador);
        out.add("p_Out_EntidadCreada", entidadesCreadas);
        log.info("FIN::CrearSocioNegocioModificarPerfilesPortafolio errores={}", erroresContador);
        return out;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static JsonObject buildErrorEntry(String tipoEntidad, long idEntidad,
                                              String codigo, String idFormaCobro) {
        JsonObject entry = new JsonObject();
        entry.addProperty("TipoEntidad", tipoEntidad);
        entry.addProperty("IdEntidad", idEntidad);
        entry.addProperty("Estado", "");
        entry.addProperty("Nuevo", "");
        JsonArray mensajes = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("IdInterno", 0);
        msg.addProperty("TipoMensaje", "ERROR");
        msg.addProperty("Codigo", codigo);
        msg.addProperty("Mensaje", codigo + " IdFormaCobro=" + idFormaCobro);
        mensajes.add(msg);
        entry.add("Mensajes", mensajes);
        return entry;
    }

    private static Optional<String> cobrocustValor(JsonObject socio, String field) {
        if (!socio.has(field)) return Optional.empty();
        for (JsonElement el : socio.get(field).getAsJsonArray()) {
            JsonObject ind = el.getAsJsonObject();
            if ("COBROCUST".equals(strOf(ind, "CodigoTipoIndicador"))) {
                String v = strOf(ind, "ValorIndicador");
                if (!v.isEmpty()) return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    private static void copyAllFields(JsonObject src, JsonObject dst) {
        for (String key : src.keySet()) dst.add(key, src.get(key).deepCopy());
    }

    private static String strOf(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e == null || e.isJsonNull()) ? "" : e.getAsString();
    }

    private static long longOf(JsonObject o, String key, long def) {
        JsonElement e = o.get(key);
        if (e == null || e.isJsonNull()) return def;
        try { return e.getAsLong(); } catch (Exception ex) { return def; }
    }

    private static JsonArray arrOf(JsonObject o, String key) {
        JsonElement e = o.get(key);
        return (e != null && e.isJsonArray()) ? e.getAsJsonArray() : new JsonArray();
    }
}
