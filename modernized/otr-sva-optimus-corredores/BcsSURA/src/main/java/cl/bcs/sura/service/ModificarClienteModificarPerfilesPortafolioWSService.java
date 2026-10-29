package cl.bcs.sura.service;

import cl.bcs.sura.domain.IdsEntidadYPerfiles;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Spring replacement for legacy cl.bcs.sura.socionegocio.ModificarClienteModificarPerfilesPortafolioWS.
 *
 * Key differences from the SOC variants:
 *  - Input p_SocioNegocio has DatosPortafolio[].Indicadores (WS structure, not IndicadorPortafolio)
 *  - Performs its own INSERT/UPDATE for indicators using WS-specific SQL files
 *  - Uses obtenerEntidadesYPerfilesCompletas() (direct query) instead of the VALUES-based approach
 *  - Output uses P_OUT_ENTIDADES_MANIPULADAS / P_OUT_ADVERTENCIASCONTADOR (WS output schema)
 */
@Service
public class ModificarClienteModificarPerfilesPortafolioWSService {

    private static final Logger log = LoggerFactory.getLogger(
            ModificarClienteModificarPerfilesPortafolioWSService.class);

    private static final String BASE = "cl/bcs/sura/socionegocio/sql/";

    private final PerfilesPortafolioUtilsService utils;
    private final NamedParameterJdbcTemplate jdbc;

    private final String SQL_INSERT_INDICADOR_WS;
    private final String SQL_UPDATE_INDICADOR_WS;

    public ModificarClienteModificarPerfilesPortafolioWSService(
            PerfilesPortafolioUtilsService utils,
            NamedParameterJdbcTemplate jdbc) {
        this.utils = utils;
        this.jdbc  = jdbc;
        SQL_INSERT_INDICADOR_WS = load("ModificarClienteModificarPerfilesPortafolioWS_InsertIndicadorCobroCustodia.sql");
        SQL_UPDATE_INDICADOR_WS = load("ModificarClienteModificarPerfilesPortafolioWS_UpdateIndicadorCobroCustodia.sql");
    }

    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::ModificarClienteModificarPerfilesPortafolioWS");

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

        // WS-specific: ingresar indicators using DatosPortafolio structure
        ids = ingresarIndicadoresCobroCustodia(sociosNegocio, ids);

        if (!actualizarFormaCobro) {
            utils.validarIndicadorFormaCobroPortafolioApv(ids);
        }

        // WS-specific: update indicators using own SQL files
        modificarIndicadoresCobroCustodiaWS(ids);

        // Build output from jout fields already in params
        long      erroresContador       = longOf(params, "P_OUT_ERRORESCONTADOR", 0L);
        long      advertenciasContador  = longOf(params, "P_OUT_ADVERTENCIASCONTADOR", 0L);
        JsonArray entidadesManipuladas  = arrOf(params, "P_OUT_ENTIDADES_MANIPULADAS");

        if (!ids.isEmpty()) {
            ids = utils.obtenerEntidadesYPerfilesCompletas(ids);

            for (IdsEntidadYPerfiles id : ids) {
                if (id.idPerfilComercial.isEmpty() || id.idPerfilProductos.isEmpty()
                        || id.idPerfilCobros.isEmpty()) {
                    entidadesManipuladas.add(buildErrorEntry(
                            "PORTAFOLIO", id.idPortafolio,
                            "UI_ERROR_CONFIGURACION_PERFILES_PORTAFOLIO_NO_ENCONTRADOS",
                            id.idFormaCobro));
                    erroresContador++;
                }
            }

            if (erroresContador == 0) {
                utils.modificarPerfilesPortafolio(ids);
            }
        }

        JsonObject out = new JsonObject();
        copyAllFields(params, out);
        out.addProperty("P_OUT_ERRORESCONTADOR",      erroresContador);
        out.addProperty("P_OUT_ADVERTENCIASCONTADOR",  advertenciasContador);
        out.add("P_OUT_ENTIDADES_MANIPULADAS", entidadesManipuladas);
        log.info("FIN::ModificarClienteModificarPerfilesPortafolioWS errores={}", erroresContador);
        return out;
    }

    // ─── WS-specific indicator handling ──────────────────────────────────────

    /**
     * WS variant: each socio has DatosPortafolio[{IdPortafolio, Indicadores[{CodigoTipoIndicador, ValorIndicador}]}].
     * Sets IdFormaCobro on the in-memory list and INSERTs new indicators.
     */
    private List<IdsEntidadYPerfiles> ingresarIndicadoresCobroCustodia(
            List<JsonObject> sociosNegocio, List<IdsEntidadYPerfiles> ids) {

        List<IdsEntidadYPerfiles> result = new ArrayList<>();

        for (JsonObject socio : sociosNegocio) {
            String docId = strOf(socio, "DocumentoIdentidad");
            List<IdsEntidadYPerfiles> perfiles = ids.stream()
                    .filter(id -> docId.equals(id.documentoIdentidad))
                    .collect(Collectors.toList());

            for (JsonElement datosEl : arrOf(socio, "DatosPortafolio")) {
                JsonObject datos = datosEl.getAsJsonObject();
                Optional<String> valorIndicador = cobrocustValorWS(datos);
                if (valorIndicador.isPresent() && !perfiles.isEmpty()) {
                    String valor = valorIndicador.get();
                    perfiles.forEach(p -> p.idFormaCobro = valor);
                    insertarNuevosIndicadores(perfiles, valor);
                }
                result.addAll(perfiles);
            }
        }
        return result;
    }

    private void insertarNuevosIndicadores(List<IdsEntidadYPerfiles> perfiles, String valorIndicador) {
        SqlParameterSource[] batch = perfiles.stream()
                .filter(p -> p.idIndicador == 0)
                .map(p -> new MapSqlParameterSource()
                        .addValue("IdEntidad",      p.idPortafolio)
                        .addValue("ValorIndicador", valorIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(SQL_INSERT_INDICADOR_WS, batch);
    }

    private void modificarIndicadoresCobroCustodiaWS(List<IdsEntidadYPerfiles> ids) {
        SqlParameterSource[] batch = ids.stream()
                .filter(id -> id.idIndicador != 0 && "S".equals(id.actualizarFormaCobro))
                .map(id -> new MapSqlParameterSource()
                        .addValue("ValorIndicador", id.idFormaCobro)
                        .addValue("IdEntidad",      id.idIndicador))
                .toArray(SqlParameterSource[]::new);
        if (batch.length == 0) return;
        jdbc.batchUpdate(SQL_UPDATE_INDICADOR_WS, batch);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static Optional<String> cobrocustValorWS(JsonObject datos) {
        for (JsonElement el : arrOf(datos, "Indicadores")) {
            JsonObject ind = el.getAsJsonObject();
            if ("COBROCUST".equals(strOf(ind, "CodigoTipoIndicador"))) {
                String v = strOf(ind, "ValorIndicador");
                if (!v.isEmpty()) return Optional.of(v);
            }
        }
        return Optional.empty();
    }

    private static JsonObject buildErrorEntry(String tipoEntidad, long idEntidad,
                                              String codigo, String idFormaCobro) {
        JsonObject entry = new JsonObject();
        entry.addProperty("TipoEntidad", tipoEntidad);
        entry.addProperty("IdEntidad", idEntidad);
        JsonArray mensajes = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("IdInterno", 0L);
        msg.addProperty("TipoMensaje", "ERROR");
        msg.addProperty("Codigo", codigo);
        msg.addProperty("Mensaje", codigo + " IdFormaCobro=" + idFormaCobro);
        mensajes.add(msg);
        entry.add("Mensajes", mensajes);
        return entry;
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

    private static String load(String name) {
        String path = BASE + name;
        try (InputStream is = ModificarClienteModificarPerfilesPortafolioWSService.class
                .getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + path, e);
        }
    }
}
