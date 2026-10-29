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

/**
 * Spring replacement for legacy cl.bcs.sura.socionegocio.ModificarSocioNegocioModificarPerfilesPortafolio.
 *
 * Differences from the Crear variant:
 *  - Uses p_ActualizarIndicadorFormaCobro flag: when "N", APV portfolios are excluded
 *    from the COBROCUST indicator update.
 *  - Output adds to p_Out_EntidadModificada instead of p_Out_EntidadCreada.
 */
@Service
public class ModificarSocioNegocioModificarPerfilesPortafolioService {

    private static final Logger log = LoggerFactory.getLogger(
            ModificarSocioNegocioModificarPerfilesPortafolioService.class);

    private final PerfilesPortafolioUtilsService utils;

    public ModificarSocioNegocioModificarPerfilesPortafolioService(
            PerfilesPortafolioUtilsService utils) {
        this.utils = utils;
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ModificarSocioNegocioModificarPerfilesPortafolio");

        boolean actualizarFormaCobro = !"N".equals(strOf(params, "p_ActualizarIndicadorFormaCobro"));
        JsonArray sociosNegocioArr   = arrOf(params, "p_SocioNegocio");

        List<String>     documentosIdentidad = new ArrayList<>();
        List<JsonObject> sociosNegocio       = new ArrayList<>();
        for (JsonElement el : sociosNegocioArr) {
            JsonObject soc = el.getAsJsonObject();
            sociosNegocio.add(soc);
            documentosIdentidad.add(strOf(soc, "DocumentoIdentidad"));
        }

        List<IdsEntidadYPerfiles> ids = utils.consultarIdsPortafolioIdClientePorDocumentoIdentidad(
                documentosIdentidad);
        ids = utils.obtenerEntidadesParaInsertarYModificarEnWSSociosNegocio(sociosNegocio, ids);

        long   erroresContador     = longOf(params, "P_OUT_ERRORESCONTADOR", 0L);
        JsonArray entidadesModificadas = arrOf(params, "p_Out_EntidadModificada");

        if (!ids.isEmpty()) {
            for (IdsEntidadYPerfiles id : ids) {
                if (id.idPerfilComercial.isEmpty() || id.idPerfilProductos.isEmpty()
                        || id.idPerfilCobros.isEmpty()) {
                    entidadesModificadas.add(buildErrorEntry(
                            "PORTAFOLIO", id.idPortafolio,
                            "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS",
                            id.idFormaCobro));
                    erroresContador++;
                }
            }

            if (erroresContador == 0) {
                // Ingresar indicators per socio (same as Crear)
                List<IdsEntidadYPerfiles> idsConFormaCobro = new ArrayList<>();
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
                    idsConFormaCobro.addAll(perfiles);
                }

                // When flag is "N", exclude APV portfolios from indicator update
                if (!actualizarFormaCobro) {
                    utils.validarIndicadorFormaCobroPortafolioApv(idsConFormaCobro);
                }

                utils.modificarIndicadoresCobroCustodia(idsConFormaCobro);
                utils.modificarPerfilesPortafolio(idsConFormaCobro);
            }
        }

        JsonObject out = new JsonObject();
        copyAllFields(params, out);
        out.addProperty("P_OUT_ERRORESCONTADOR", erroresContador);
        out.add("p_Out_EntidadModificada", entidadesModificadas);
        log.info("FIN::ModificarSocioNegocioModificarPerfilesPortafolio errores={}", erroresContador);
        return out;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static JsonObject buildErrorEntry(String tipoEntidad, long idEntidad,
                                              String codigo, String idFormaCobro) {
        JsonObject entry = new JsonObject();
        entry.addProperty("TipoEntidad", tipoEntidad);
        entry.addProperty("IdEntidad", idEntidad);
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
