package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates data archiving: migrates then cleans tables listed in GestionDataHistorica SQL config.
 * Date range uses calendar days (not business days) as a safe approximation for the archive window.
 */
@Service
public class GestionDataHistoricaService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(GestionDataHistoricaService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final NamedParameterJdbcTemplate jdbc;
    private final MigrarDataHistoricoService migrar;
    private final LimpiarDataHistoricoService limpiar;
    private final String SQL_TABLAS_MIGRAR;
    private final String SQL_TABLAS_LIMPIAR;

    public GestionDataHistoricaService(NamedParameterJdbcTemplate jdbc,
                                       MigrarDataHistoricoService migrar,
                                       LimpiarDataHistoricoService limpiar) {
        this.jdbc = jdbc;
        this.migrar = migrar;
        this.limpiar = limpiar;
        this.SQL_TABLAS_MIGRAR = loadSql("cl/bcs/plataforma/pla/sql/GestionDataHistorica_Select_Tablas_Migrar.sql");
        this.SQL_TABLAS_LIMPIAR = loadSql("cl/bcs/plataforma/pla/sql/GestionDataHistorica_Select_Tablas_Limpiar.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.GestionDataHistorica"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.info("INICIO::GestionDataHistorica");
        Salida out = new Salida();
        try {
            Long idEmpresa = params.has("p_IdEmpresa") ? params.get("p_IdEmpresa").getAsLong() : 1L;
            Long idUsuario = params.has("p_IdUsuario") ? params.get("p_IdUsuario").getAsLong() : 0L;
            String fecha   = params.has("p_Fecha")     ? params.get("p_Fecha").getAsString()   : LocalDate.now().format(FMT);

            LocalDate refDate = LocalDate.parse(fecha, FMT);

            // Migration phase
            List<TablaDataHistorica> tablasM = jdbc.query(SQL_TABLAS_MIGRAR,
                new MapSqlParameterSource("p_IdEmpresa", idEmpresa),
                BeanPropertyRowMapper.newInstance(TablaDataHistorica.class));

            for (TablaDataHistorica t : tablasM) {
                LocalDate desde = refDate.minusDays(t.DiasEnLinea);
                LocalDate hasta = desde.plusDays(1);
                JsonObject migParams = buildParams(idEmpresa, idUsuario, t.CodigoTabla, t.CampoFiltro,
                    desde.format(FMT), hasta.format(FMT), "N");
                JsonObject migOut = migrar.execute(migParams);
                long err = migOut.has("P_OUT_ERRORESCONTADOR") ? migOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                if (err > 0) {
                    out.P_OUT_ERRORESCONTADOR += err;
                    addMsg(out, "ERROR", "ERROR_MIGRACION", "Error al migrar: " + t.CodigoTabla);
                    return toJson(out);
                }
                addMsg(out, "OK", "MIGRADO", t.CodigoTabla + " migrada entre " + desde + " y " + hasta);
            }

            // Cleanup phase
            List<TablaDataHistorica> tablasL = jdbc.query(SQL_TABLAS_LIMPIAR,
                new MapSqlParameterSource("p_IdEmpresa", idEmpresa),
                BeanPropertyRowMapper.newInstance(TablaDataHistorica.class));

            for (TablaDataHistorica t : tablasL) {
                long desfase = (t.Desfase != null ? t.Desfase : 0L) + (t.DiasEnLinea != null ? t.DiasEnLinea : 0L);
                LocalDate desde = refDate.minusDays(desfase);
                LocalDate hasta = desde.plusDays(1);
                JsonObject limParams = buildParams(idEmpresa, idUsuario, t.CodigoTabla, t.CampoFiltro,
                    desde.format(FMT), hasta.format(FMT), t.SoloLimpia);
                JsonObject limOut = limpiar.execute(limParams);
                long err = limOut.has("P_OUT_ERRORESCONTADOR") ? limOut.get("P_OUT_ERRORESCONTADOR").getAsLong() : 0L;
                if (err > 0) {
                    out.P_OUT_ERRORESCONTADOR += err;
                    addMsg(out, "ERROR", "ERROR_LIMPIEZA", "Error al limpiar: " + t.CodigoTabla);
                    return toJson(out);
                }
                addMsg(out, "OK", "LIMPIADO", t.CodigoTabla + " limpiada entre " + desde + " y " + hasta);
            }

            if (out.P_OUT_ERRORESCONTADOR == 0L)
                addMsg(out, "OK", "OK", "GestionDataHistorica ejecutado correctamente.");
        } catch (Exception e) {
            log.error("Error en GestionDataHistorica: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
            addMsg(out, "ERROR", "ERROR_INTERNO", e.getMessage());
        }
        log.info("FIN::GestionDataHistorica");
        return toJson(out);
    }

    private JsonObject buildParams(Long idEmpresa, Long idUsuario, String tabla, String filtro,
                                   String desde, String hasta, String soloLimpia) {
        JsonObject p = new JsonObject();
        p.addProperty("p_IdEmpresa", idEmpresa); p.addProperty("p_IdUsuario", idUsuario);
        p.addProperty("p_Tabla", tabla); p.addProperty("p_Filtro", filtro);
        p.addProperty("p_Desde", desde); p.addProperty("p_Hasta", hasta);
        if (soloLimpia != null) p.addProperty("p_SoloLimpia", soloLimpia);
        return p;
    }

    private void addMsg(Salida out, String tipo, String codigo, String desc) {
        Mensaje m = new Mensaje(); m.tipo = tipo; m.codigo = codigo; m.descripcion = desc;
        out.P_OUT_MENSAJES.add(m);
    }

    private JsonObject toJson(Object o) { return JsonParser.parseString(GSON.toJson(o)).getAsJsonObject(); }

    private String loadSql(String path) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(path)) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) { throw new IllegalStateException("SQL not found: " + path, e); }
    }

    public static class TablaDataHistorica {
        public String CodigoTabla; public String CampoFiltro;
        public Long DiasEnLinea; public Long Desfase; public String SoloLimpia;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Mensaje> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Mensaje { public String tipo; public String codigo; public String descripcion; }
}
