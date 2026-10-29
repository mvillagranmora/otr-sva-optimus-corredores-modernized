package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Deletes historic data from HANA within a date range.
 * Params: p_Tabla, p_Filtro (date column), p_Desde, p_Hasta, p_SoloLimpia.
 * Uses table-specific DELETE SQL (generic or FusionDetalle variant).
 */
@Service
public class LimpiarDataHistoricoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(LimpiarDataHistoricoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_DELETE_GENERICO;
    private final String SQL_DELETE_FUSION_DETALLE;
    private final String SQL_COUNT_HANA_GENERICO;

    public LimpiarDataHistoricoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_DELETE_GENERICO      = loadSql("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Delete_Data_Historica.sql");
        this.SQL_DELETE_FUSION_DETALLE = loadSql("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Delete_FusionDetalle.sql");
        this.SQL_COUNT_HANA_GENERICO  = loadSql("cl/bcs/plataforma/pla/sql/LimpiarDataHistorica_Select_Cantidad_A_Borrar.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.LimpiarDataHistorico"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::LimpiarDataHistorico");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();
        try {
            boolean isFusionDetalle = "\"BSCL.Custodia.BD::HIS.FusionCarteraDetalle\"".equals(in.p_Tabla);

            MapSqlParameterSource ps = new MapSqlParameterSource()
                .addValue("p_Tabla",  in.p_Tabla)
                .addValue("p_Filtro", in.p_Filtro)
                .addValue("p_Desde",  in.p_Desde)
                .addValue("p_Hasta",  in.p_Hasta)
                .addValue("p_IdEmpresa", in.p_IdEmpresa);

            // Check how many rows will be deleted
            String countSql = SQL_COUNT_HANA_GENERICO
                .replace(":p_Tabla",  in.p_Tabla  != null ? in.p_Tabla  : "")
                .replace(":p_Filtro", in.p_Filtro != null ? in.p_Filtro : "")
                .replace(":p_Hasta",  "'" + in.p_Hasta  + "'")
                .replace(":p_Desde",  "'" + in.p_Desde  + "'");

            String deleteSql = isFusionDetalle ? SQL_DELETE_FUSION_DETALLE : SQL_DELETE_GENERICO;
            String finalDelete = deleteSql
                .replace(":p_Tabla",  in.p_Tabla  != null ? in.p_Tabla  : "")
                .replace(":p_Filtro", in.p_Filtro != null ? in.p_Filtro : "")
                .replace(":p_Hasta",  "'" + in.p_Hasta  + "'")
                .replace(":p_Desde",  "'" + in.p_Desde  + "'");

            int deleted = jdbc.update(finalDelete, new MapSqlParameterSource());
            log.info("Eliminados {} registros de {} entre {} y {}", deleted, in.p_Tabla, in.p_Desde, in.p_Hasta);
            addMsg(out, "OK", "LIMPIADO", in.p_Tabla + " limpiada entre " + in.p_Desde + " y " + in.p_Hasta + " (" + deleted + " registros).");
        } catch (Exception e) {
            log.error("Error en LimpiarDataHistorico: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        return toJson(out);
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) {
        return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject();
    }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class Entrada {
        public Long p_IdEmpresa; public Long p_IdUsuario;
        public String p_Tabla; public String p_Filtro;
        public String p_Desde; public String p_Hasta; public String p_SoloLimpia;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
