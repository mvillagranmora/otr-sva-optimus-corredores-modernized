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
import java.util.*;

@Service
public class AgregarGlosaTipoInstrumentoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(AgregarGlosaTipoInstrumentoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_INSERT;

    public AgregarGlosaTipoInstrumentoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/AgregarGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
        this.SQL_INSERT = loadSql("cl/bcs/plataforma/pla/sql/AgregarGlosaTipoInstrumento_InsertUsoGlosaTipoInstrumento.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.AgregarGlosaTipoInstrumento"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::AgregarGlosaTipoInstrumento");
        Entrada in = GSON.fromJson(params, Entrada.class);
        Salida out = new Salida();

        try {
            List<String> tipos = in.p_InGlosasTipoInstrumento.stream()
                    .map(g -> g.IdTipoInstrumento).distinct().toList();

            List<GlosaTipoInstrumento> existentes = jdbc.query(SQL_SELECT,
                    new MapSqlParameterSource("IdTipoInstrumento", tipos),
                    (rs, i) -> {
                        GlosaTipoInstrumento g = new GlosaTipoInstrumento();
                        g.IdUsoGlosaIdTipoInstrumento = rs.getString("IdUsoGlosaIdTipoInstrumento");
                        g.IdTipoInstrumento = rs.getString("IdTipoInstrumento");
                        g.IdGlosa = rs.getString("IdGlosa");
                        g.Prioridad = rs.getLong("Prioridad");
                        return g;
                    });

            Map<String, GlosaTipoInstrumento> byKey = new HashMap<>();
            for (GlosaTipoInstrumento g : existentes) {
                byKey.put(g.IdGlosa + "_" + g.IdTipoInstrumento, g);
            }
            long ultimaPrioridad = existentes.isEmpty() ? 0L
                    : existentes.get(existentes.size() - 1).Prioridad;

            List<GlosaTipoInstrumento> insertar = new ArrayList<>();
            Set<String> tiposVistos = new HashSet<>();

            for (InGlosaTipoInstrumento glosa : in.p_InGlosasTipoInstrumento) {
                if (tiposVistos.contains(glosa.IdTipoInstrumento)) {
                    out.P_OUT_ERRORESCONTADOR++;
                    Msg err = new Msg("E", "UI_GLOSA_ENTRADA_REPETIDO");
                    out.P_OUT_MENSAJES.add(err);
                } else {
                    tiposVistos.add(glosa.IdTipoInstrumento);
                    String key = glosa.IdGlosa + "_" + glosa.IdTipoInstrumento;
                    if (byKey.containsKey(key)) {
                        out.P_OUT_ERRORESCONTADOR++;
                        out.P_OUT_MENSAJES.add(new Msg("E", "UI_GLOSA_YA_EXISTE"));
                    } else {
                        ultimaPrioridad++;
                        GlosaTipoInstrumento nuevo = new GlosaTipoInstrumento();
                        nuevo.IdTipoInstrumento = glosa.IdTipoInstrumento;
                        nuevo.IdGlosa = glosa.IdGlosa;
                        nuevo.Prioridad = ultimaPrioridad;
                        insertar.add(nuevo);
                    }
                }
            }

            if (out.P_OUT_ERRORESCONTADOR == 0L && !insertar.isEmpty()) {
                SqlParameterSource[] batch = insertar.stream()
                        .map(BeanPropertySqlParameterSource::new)
                        .toArray(SqlParameterSource[]::new);
                jdbc.batchUpdate(SQL_INSERT, batch);
                out.P_OUT_MENSAJES.add(new Msg("I", "UI_GLOSA_AGREGADO_EXITOSAMENTE"));
            }

        } catch (Exception e) {
            log.error("Error en AgregarGlosaTipoInstrumento: {}", e.getMessage(), e);
            out.P_OUT_ERRORESCONTADOR++;
        }

        return JsonParser.parseString(GSON.toJson(out)).getAsJsonObject();
    }

    public static class Entrada {
        public Long p_IdEmpresa;
        public Long p_IdUsuario;
        public String p_IdModulo;
        public List<InGlosaTipoInstrumento> p_InGlosasTipoInstrumento = new ArrayList<>();
    }

    public static class InGlosaTipoInstrumento {
        public String IdTipoInstrumento;
        public String IdGlosa;
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
        try (InputStream is = AgregarGlosaTipoInstrumentoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
