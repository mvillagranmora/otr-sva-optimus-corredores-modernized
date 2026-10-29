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
public class QuitarGlosaTipoInstrumentoService implements JavaCallService {

    private static final Logger log = LoggerFactory.getLogger(QuitarGlosaTipoInstrumentoService.class);
    private static final Gson GSON = new GsonBuilder().serializeNulls().create();

    private final NamedParameterJdbcTemplate jdbc;
    private final String SQL_SELECT;
    private final String SQL_UPDATE;
    private final String SQL_DELETE;

    public QuitarGlosaTipoInstrumentoService(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
        this.SQL_SELECT = loadSql("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_SelectUsoGlosaTipoInstrumento.sql");
        this.SQL_UPDATE = loadSql("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_UpdateUsoGlosaTipoInstrumento.sql");
        this.SQL_DELETE = loadSql("cl/bcs/plataforma/pla/sql/QuitarGlosaTipoInstrumento_DeleteUsoGlosaTipoInstrumento.sql");
    }

    @Override
    public String fqn() { return "cl.bcs.plataforma.pla.QuitarGlosaTipoInstrumento"; }

    @Override
    @Transactional
    public JsonObject execute(JsonObject params) {
        log.debug("INICIO::QuitarGlosaTipoInstrumento");
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

            // Validate all exist
            for (InGlosaTipoInstrumento inGlosa : in.p_InGlosasTipoInstrumento) {
                String key = inGlosa.IdGlosa + "_" + inGlosa.IdTipoInstrumento;
                if (!byKey.containsKey(key)) {
                    out.P_OUT_ERRORESCONTADOR++;
                    out.P_OUT_MENSAJES.add(new Msg("E", "UI_GLOSA_NO_EXISTE"));
                }
            }

            if (out.P_OUT_ERRORESCONTADOR == 0L) {
                // Build sets for delete and update
                Set<String> eliminarKeys = new HashSet<>();
                for (InGlosaTipoInstrumento inGlosa : in.p_InGlosasTipoInstrumento) {
                    eliminarKeys.add(inGlosa.IdGlosa + "_" + inGlosa.IdTipoInstrumento);
                }

                List<GlosaTipoInstrumento> aEliminar = new ArrayList<>();
                List<GlosaTipoInstrumento> aActualizar = new ArrayList<>();

                for (String tipo : tipos) {
                    long prioridad = 0L;
                    for (GlosaTipoInstrumento g : existentes) {
                        if (!g.IdTipoInstrumento.equals(tipo)) continue;
                        String key = g.IdGlosa + "_" + g.IdTipoInstrumento;
                        if (eliminarKeys.contains(key)) {
                            aEliminar.add(g);
                        } else {
                            prioridad++;
                            g.Prioridad = prioridad;
                            aActualizar.add(g);
                        }
                    }
                }

                if (!aEliminar.isEmpty()) {
                    SqlParameterSource[] delBatch = aEliminar.stream()
                            .map(BeanPropertySqlParameterSource::new)
                            .toArray(SqlParameterSource[]::new);
                    jdbc.batchUpdate(SQL_DELETE, delBatch);
                }
                if (!aActualizar.isEmpty()) {
                    SqlParameterSource[] updBatch = aActualizar.stream()
                            .map(BeanPropertySqlParameterSource::new)
                            .toArray(SqlParameterSource[]::new);
                    jdbc.batchUpdate(SQL_UPDATE, updBatch);
                }
                out.P_OUT_MENSAJES.add(new Msg("I", "UI_GLOSA_TIPO_INSTRUMENTO_QUITADO_EXITOSAMENTE"));
            }

        } catch (Exception e) {
            log.error("Error en QuitarGlosaTipoInstrumento: {}", e.getMessage(), e);
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
        try (InputStream is = QuitarGlosaTipoInstrumentoService.class.getClassLoader().getResourceAsStream(path)) {
            if (is == null) throw new IllegalStateException("SQL not found: " + path);
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SQL: " + path, e);
        }
    }
}
