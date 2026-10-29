package cl.bcs.facturacion.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.facturacion.AgregarQuitarConfiguracionAFP (JavaCall).
 *
 * Adds or removes AFP (pension fund) configuration for clients/portfolios.
 * Input JSON: { "tipo": "ADD"|"DELETE",
 *               "p_EnvioAdd":    [{IdEmpresa, IdPortafolio, IdCliente, IdAFP, IdMultifondo, FechaIngreso}],
 *               "p_EnvioDelete": [{IdConfiguracionAFP}] }
 * Output JSON: { "P_OUT_MENSAJES": [...], "P_OUT_ERRORESCONTADOR": N }
 */
@Service
public class AgregarQuitarConfiguracionAfpService {

    private static final Logger log = LoggerFactory.getLogger(AgregarQuitarConfiguracionAfpService.class);

    private static final String MENSAJE_ERROR = "ERROR";

    private final NamedParameterJdbcTemplate jdbc;

    private final String sqlInsert;
    private final String sqlDelete;
    private final String sqlValidarAFP;
    private final String sqlValidarCliente;
    private final String sqlValidarClienteAFP;
    private final String sqlValidarClienteAFPMultifondo;
    private final String sqlValidarClienteAFPMultifondoPortafolio;

    public AgregarQuitarConfiguracionAfpService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.sqlInsert = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_AgregarConfiguracion.sql");
        this.sqlDelete = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_QuitarConfiguracion.sql");
        this.sqlValidarAFP = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_ValidarAFP.sql");
        this.sqlValidarCliente = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_ValidarCliente.sql");
        this.sqlValidarClienteAFP = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFP.sql");
        this.sqlValidarClienteAFPMultifondo = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFPMultifondo.sql");
        this.sqlValidarClienteAFPMultifondoPortafolio = loadSql("cl/bcs/facturacion/sql/AgregarQuitar_ValidarClienteAFPMultifondoPortafolio.sql");
    }

    /**
     * Dispatches to add or remove AFP configuration based on the "tipo" field.
     */
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("== INICIO AGREGAR QUITAR CONFIGURACION AFP ==");

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").serializeNulls().create();
        List<JsonObject> mensajes = new ArrayList<>();

        String tipo = params.has("tipo") ? params.get("tipo").getAsString() : "";

        if ("ADD".equalsIgnoreCase(tipo)) {
            procesarAgregado(params, mensajes, gson);
        } else {
            procesarEliminado(params, mensajes, gson);
        }

        long errores = mensajes.stream()
                .filter(m -> MENSAJE_ERROR.equalsIgnoreCase(m.get("tipo").getAsString()))
                .count();

        JsonObject out = new JsonObject();
        out.add("P_OUT_MENSAJES", gson.toJsonTree(mensajes));
        out.addProperty("P_OUT_ERRORESCONTADOR", errores);

        log.info("== TERMINO AGREGAR QUITAR CONFIGURACION AFP ==");
        return out;
    }

    // ------------------------------------------------------------------
    // ADD path
    // ------------------------------------------------------------------

    private void procesarAgregado(JsonObject params, List<JsonObject> mensajes, Gson gson) {
        if (!params.has("p_EnvioAdd") || params.getAsJsonArray("p_EnvioAdd").isEmpty()) return;
        JsonObject add = params.getAsJsonArray("p_EnvioAdd").get(0).getAsJsonObject();

        long idCliente   = add.get("IdCliente").getAsLong();
        long idAFP       = add.get("IdAFP").getAsLong();
        long idMultifondo= add.get("IdMultifondo").getAsLong();
        long idPortafolio= add.get("IdPortafolio").getAsLong();

        // Validar cliente
        long validacionCliente = queryValidacion(sqlValidarCliente,
                new MapSqlParameterSource("IdCliente", idCliente));

        if (validacionCliente == 0) {
            // Cliente nuevo: validar que la AFP no exista globalmente
            long validacionAFP = queryValidacion(sqlValidarAFP,
                    new MapSqlParameterSource("IdAFP", idAFP).addValue("IdMultiFondo", idMultifondo));
            if (validacionAFP == 0) {
                batchInsert(sqlInsert, params.getAsJsonArray("p_EnvioAdd"));
            } else {
                mensajes.add(buildMensaje(idCliente, MENSAJE_ERROR,
                        "Configuracion AFP", "La AFP ya esta Configurada"));
            }
        } else {
            // Cliente existe: validar combinación cliente+AFP
            long validacionClienteAFP = queryValidacion(sqlValidarClienteAFP,
                    new MapSqlParameterSource("IdCliente", idCliente).addValue("IdAFP", idAFP));
            if (validacionClienteAFP != 0) {
                // Cliente ya tiene AFP: validar multifondo
                long validacionMultifondo = queryValidacion(sqlValidarClienteAFPMultifondo,
                        new MapSqlParameterSource("IdCliente", idCliente)
                                .addValue("IdAFP", idAFP)
                                .addValue("IdMultifondo", idMultifondo));
                if (validacionMultifondo == 0) {
                    // Validar portafolio
                    long validacionPortafolio = queryValidacion(sqlValidarClienteAFPMultifondoPortafolio,
                            new MapSqlParameterSource("IdCliente", idCliente)
                                    .addValue("IdAFP", idAFP)
                                    .addValue("IdPortafolio", idPortafolio)
                                    .addValue("IdMultiFondo", idMultifondo));
                    if (validacionPortafolio == 0) {
                        batchInsert(sqlInsert, params.getAsJsonArray("p_EnvioAdd"));
                    } else {
                        mensajes.add(buildMensaje(idCliente, MENSAJE_ERROR,
                                "Configuracion AFP",
                                "Cliente Numero " + idCliente + " ya tiene el Portafolio Configurado"));
                    }
                } else {
                    mensajes.add(buildMensaje(idCliente, MENSAJE_ERROR,
                            "Configuracion AFP",
                            "Cliente Numero " + idCliente + " ya tiene el Multifondo Configurado"));
                }
            } else {
                mensajes.add(buildMensaje(idCliente, MENSAJE_ERROR,
                        "Configuracion AFP",
                        "Cliente Numero " + idCliente + " ya tiene AFP Configurada"));
            }
        }
    }

    // ------------------------------------------------------------------
    // DELETE path
    // ------------------------------------------------------------------

    private void procesarEliminado(JsonObject params, List<JsonObject> mensajes, Gson gson) {
        if (!params.has("p_EnvioDelete") || params.getAsJsonArray("p_EnvioDelete").isEmpty()) return;
        batchInsert(sqlDelete, params.getAsJsonArray("p_EnvioDelete"));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private long queryValidacion(String sql, MapSqlParameterSource sqlParams) {
        List<Map<String, Object>> rows = jdbc.queryForList(sql, sqlParams);
        if (rows.isEmpty()) return 0L;
        Object val = rows.get(0).get("Validacion");
        if (val == null) return 0L;
        return ((Number) val).longValue();
    }

    private void batchInsert(String sql, JsonArray items) {
        SqlParameterSource[] batch = new SqlParameterSource[items.size()];
        for (int i = 0; i < items.size(); i++) {
            JsonObject item = items.get(i).getAsJsonObject();
            MapSqlParameterSource p = new MapSqlParameterSource();
            for (Map.Entry<String, JsonElement> entry : item.entrySet()) {
                JsonElement v = entry.getValue();
                if (v.isJsonNull()) {
                    p.addValue(entry.getKey(), null);
                } else {
                    p.addValue(entry.getKey(), v.getAsString());
                }
            }
            batch[i] = p;
        }
        jdbc.batchUpdate(sql, batch);
    }

    private static JsonObject buildMensaje(long id, String tipo, String codigo, String descripcion) {
        JsonObject m = new JsonObject();
        m.addProperty("id", id);
        m.addProperty("tipo", tipo);
        m.addProperty("codigo", codigo);
        m.addProperty("descripcion", descripcion);
        return m;
    }

    private static String loadSql(String resourcePath) {
        try (InputStream is = AgregarQuitarConfiguracionAfpService.class
                .getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) throw new IllegalStateException("SQL resource not found: " + resourcePath);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load SQL: " + resourcePath, e);
        }
    }
}
