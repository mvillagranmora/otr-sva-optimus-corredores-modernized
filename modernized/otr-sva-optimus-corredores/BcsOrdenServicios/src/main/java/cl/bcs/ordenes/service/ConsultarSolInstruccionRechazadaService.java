package cl.bcs.ordenes.service;

import cl.bcs.ordenes.util.ConfiguracionSistemaUtil;
import cl.bcs.ordenes.util.ObtenerDiaAnteriorUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Migrated from legacy cl.bcs.ordenes.ConsultarSolInstruccionRechazada (JavaCall).
 * Finds rejected instructions from the last 3 business days back for a given client.
 */
@Service
public class ConsultarSolInstruccionRechazadaService {

    private static final Logger log = LoggerFactory.getLogger(ConsultarSolInstruccionRechazadaService.class);
    private static final Gson GSON = new GsonBuilder().serializeSpecialFloatingPointValues()
        .setDateFormat("yyyy-MM-dd").serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final ConfiguracionSistemaUtil configUtil;
    private final ObtenerDiaAnteriorUtil diaAnteriorUtil;
    private final String SQL_SELECT_INSTRUCCIONES;
    private static final String SQL_SELECT_PAIS =
        "SELECT \"IdPais\" FROM \"BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa\" WHERE \"IdEmpresa\" = :IdEmpresa";

    public ConsultarSolInstruccionRechazadaService(NamedParameterJdbcTemplate jdbc,
                                                    ConfiguracionSistemaUtil configUtil,
                                                    ObtenerDiaAnteriorUtil diaAnteriorUtil) {
        this.jdbc = jdbc;
        this.configUtil = configUtil;
        this.diaAnteriorUtil = diaAnteriorUtil;
        this.SQL_SELECT_INSTRUCCIONES = loadSql("cl/bcs/ordenes/sql/ConsultarSolInstruccionRechazada_SelectInstruccionesRechazadas.sql");
    }

    public JsonObject execute(JsonObject params) {
        log.debug("ConsultarSolInstruccionRechazadaService.execute()");
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : null;
            Long idCliente = params.has("p_IdCliente") ? params.get("p_IdCliente").getAsLong() : null;

            // Get FechaSistema
            String fechaSistema = configUtil.getParametroString(idEmpresa, "PLA.FechaSistema");

            // Get IdPais from empresa
            Long idPais = null;
            try {
                List<Map<String, Object>> empresaRows = jdbc.queryForList(SQL_SELECT_PAIS,
                    new MapSqlParameterSource("IdEmpresa", idEmpresa));
                if (!empresaRows.isEmpty()) {
                    Object pais = empresaRows.get(0).get("IdPais");
                    if (pais instanceof Number) idPais = ((Number) pais).longValue();
                }
            } catch (Exception e) {
                log.warn("Could not resolve IdPais for empresa {}: {}", idEmpresa, e.getMessage());
            }

            // Calculate 3 previous business days iteratively (diaHabil="N", soloCaeDiaHabil="S")
            String fechaDesde = fechaSistema;
            for (int i = 0; i < 3; i++) {
                ObtenerDiaAnteriorUtil.FechaIn fi = new ObtenerDiaAnteriorUtil.FechaIn(fechaDesde, 1L, idPais);
                List<ObtenerDiaAnteriorUtil.FechaIn> lista = new ArrayList<>();
                lista.add(fi);
                ObtenerDiaAnteriorUtil.Salida salida = diaAnteriorUtil.calcular(idPais, "N", lista);
                if (!salida.p_out_fechas.isEmpty()) {
                    fechaDesde = salida.p_out_fechas.get(0).fechaCalculada;
                }
            }

            // Query rejected instructions since that date
            List<Map<String, Object>> instrucciones = jdbc.queryForList(SQL_SELECT_INSTRUCCIONES,
                new MapSqlParameterSource()
                    .addValue("p_IdCliente", idCliente)
                    .addValue("p_FechaDesde", fechaDesde));

            JsonObject out = new JsonObject();
            out.addProperty("P_OUT_ERRORESCONTADOR", 0L);
            out.add("P_OUT_MENSAJES", JsonParser.parseString("[]"));
            out.add("P_OUT_INSTRUCCION_HISTORICA", JsonParser.parseString(GSON.toJson(instrucciones)));
            return out;
        } catch (Exception e) {
            log.error("ConsultarSolInstruccionRechazadaService error: {}", e.getMessage(), e);
            JsonObject err = new JsonObject();
            err.addProperty("P_OUT_ERRORESCONTADOR", 1L);
            err.addProperty("error", e.getMessage());
            return err;
        }
    }

    private static String loadSql(String path) {
        try (var is = ConsultarSolInstruccionRechazadaService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
