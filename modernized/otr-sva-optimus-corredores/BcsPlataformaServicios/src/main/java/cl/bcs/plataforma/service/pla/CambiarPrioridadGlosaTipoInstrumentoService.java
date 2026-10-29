package cl.bcs.plataforma.service.pla;

import cl.bcs.plataforma.service.JavaCallService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CambiarPrioridadGlosaTipoInstrumentoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(CambiarPrioridadGlosaTipoInstrumentoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;

    public CambiarPrioridadGlosaTipoInstrumentoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/CambiarPrioridadGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/CambiarPrioridadGlosaTipoInstrumento_UpdateUsoGlosaTipoInstrumento.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.CambiarPrioridadGlosaTipoInstrumento"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::CambiarPrioridadGlosaTipoInstrumento");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<GlosaTipoInstrumento> glosas = jdbc.query(SQL_SELECT,
                    new MapSqlParameterSource("IdTipoInstrumento", in.p_IdTipoInstrumento),
                    (rs, i) -> {
                        GlosaTipoInstrumento g = new GlosaTipoInstrumento();
                        g.IdUsoGlosaIdTipoInstrumento = rs.getString("IdUsoGlosaIdTipoInstrumento");
                        g.IdTipoInstrumento = rs.getString("IdTipoInstrumento");
                        g.IdGlosa = rs.getString("IdGlosa");
                        g.Prioridad = rs.getLong("Prioridad");
                        return g;
                    });

            GlosaTipoInstrumento glosaNueva = null;
            GlosaTipoInstrumento glosaAntigua = null;

            for (GlosaTipoInstrumento g : glosas) {
                if (g.IdGlosa.equals(in.p_IdGlosa) && g.IdTipoInstrumento.equals(in.p_IdTipoInstrumento)) {
                    glosaNueva = g;
                } else if (g.Prioridad.equals(in.p_PrioridadNueva)) {
                    glosaAntigua = g;
                }
            }

            if (glosaNueva == null || glosaAntigua == null) {
                out.P_OUT_ERRORESCONTADOR = 1L;
                out.P_OUT_MENSAJES.add(new Msg("E", "UI_PRIORIDAD_GLOSA_TIPO_INSTRUMENTO_INVALIDA"));
            } else {
                glosaNueva.Prioridad = in.p_PrioridadNueva;
                glosaAntigua.Prioridad = in.p_PrioridadAnterior;

                SqlParameterSource[] batch = new SqlParameterSource[]{
                    new BeanPropertySqlParameterSource(glosaNueva),
                    new BeanPropertySqlParameterSource(glosaAntigua)
                };
                jdbc.batchUpdate(SQL_UPDATE, batch);
                out.P_OUT_MENSAJES.add(new Msg("I", "UI_CAMBIO_PRIORIDAD_GLOSA_TIPO_INSTRUMENTO_EXITOSO"));
            }

        } catch (Exception e) {
            log.error("Error en CambiarPrioridadGlosaTipoInstrumento: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR = 1L;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public String p_IdGlosa;
        public String p_IdTipoInstrumento;
        public Long p_PrioridadNueva;
        public Long p_PrioridadAnterior;
    }

    public static class GlosaTipoInstrumento {
        public String IdUsoGlosaIdTipoInstrumento;
        public String IdTipoInstrumento;
        public String IdGlosa;
        public Long Prioridad;
    }

    public static class Salida {
        public Long P_OUT_ERRORESCONTADOR = 0L;
        public List<Msg> P_OUT_MENSAJES = new ArrayList<>();
    }

    public static class Msg {
        public String tipo;
        public String codigo;
        public Msg() {}
        public Msg(String tipo, String codigo) { this.tipo = tipo; this.codigo = codigo; }
    }

    private static String loadSql(String path) {
        try (InputStream is = CambiarPrioridadGlosaTipoInstrumentoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
